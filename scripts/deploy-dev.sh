#!/bin/bash
set -euo pipefail

# Variables
AWS_REGION="${AWS_REGION:-ap-northeast-2}"
ECR_REGISTRY="${ECR_REGISTRY:?ERROR: ECR_REGISTRY must be set}"
IMAGE_TAG="${IMAGE_TAG:-latest}"
CONTAINER_NAME="cherrish-server"
ES_CONTAINER_NAME="cherrish-elasticsearch"
NETWORK_NAME="cherrish-network"
IMAGE="${ECR_REGISTRY}:${IMAGE_TAG}"
ES_IMAGE="${ECR_REGISTRY/cherrish-server/cherrish-elasticsearch}:latest"

echo "=== Cherrish Server Deployment ==="
echo "App Image: ${IMAGE}"
echo "ES Image: ${ES_IMAGE}"

# ECR Login
echo "Logging in to ECR..."
REGISTRY_HOST="${ECR_REGISTRY%%/*}"
aws ecr get-login-password --region "${AWS_REGION}" | \
  docker login --username AWS --password-stdin "${REGISTRY_HOST}"

# Pull images
echo "Pulling images..."
docker pull "${IMAGE}"
docker pull "${ES_IMAGE}"

# Create network if not exists
echo "Creating Docker network..."
docker network create "${NETWORK_NAME}" 2>/dev/null || true

# Fetch environment variables from Parameter Store
echo "Fetching environment variables..."
DB_HOST=$(aws ssm get-parameter --name "/cherrish/DB_HOST" --region "${AWS_REGION}" --query "Parameter.Value" --output text)
DB_PORT=$(aws ssm get-parameter --name "/cherrish/DB_PORT" --region "${AWS_REGION}" --query "Parameter.Value" --output text)
DB_NAME=$(aws ssm get-parameter --name "/cherrish/DB_NAME" --region "${AWS_REGION}" --query "Parameter.Value" --output text)
DB_USERNAME=$(aws ssm get-parameter --name "/cherrish/DB_USERNAME" --region "${AWS_REGION}" --query "Parameter.Value" --output text)
DB_PASSWORD=$(aws ssm get-parameter --name "/cherrish/DB_PASSWORD" --region "${AWS_REGION}" --with-decryption --query "Parameter.Value" --output text)
OPENAI_API_KEY=$(aws ssm get-parameter --name "/cherrish/OPENAI_API_KEY" --region "${AWS_REGION}" --with-decryption --query "Parameter.Value" --output text)
OPENAI_MODEL=$(aws ssm get-parameter --name "/cherrish/OPENAI_MODEL" --region "${AWS_REGION}" --query "Parameter.Value" --output text)
SERVER_URL=$(aws ssm get-parameter --name "/cherrish/SERVER_URL" --region "${AWS_REGION}" --query "Parameter.Value" --output text)
DISCORD_ERROR_WEBHOOK_URL=$(aws ssm get-parameter --name "/cherrish/DISCORD_ERROR_WEBHOOK_URL" --region "${AWS_REGION}" --with-decryption --query "Parameter.Value" --output text)

# ES environment variables (optional - use defaults if not set)
ELASTICSEARCH_ENABLED=$(aws ssm get-parameter --name "/cherrish/ELASTICSEARCH_ENABLED" --region "${AWS_REGION}" --query "Parameter.Value" --output text 2>/dev/null || echo "true")

# Start Elasticsearch container (if enabled)
ES_ENV=()
if [ "${ELASTICSEARCH_ENABLED}" = "true" ]; then
  echo "Starting Elasticsearch container..."
  docker rm -f "${ES_CONTAINER_NAME}" 2>/dev/null || true

  # Create data volume for persistence
  docker volume create cherrish-es-data 2>/dev/null || true

  docker run -d \
    --name "${ES_CONTAINER_NAME}" \
    --network "${NETWORK_NAME}" \
    --restart unless-stopped \
    -e "discovery.type=single-node" \
    -e "xpack.security.enabled=false" \
    -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
    -v cherrish-es-data:/usr/share/elasticsearch/data \
    "${ES_IMAGE}"

  # Wait for ES to be ready
  echo "Waiting for Elasticsearch to be ready..."
  for i in $(seq 1 30); do
    if docker exec "${ES_CONTAINER_NAME}" curl -sf http://localhost:9200/_cluster/health > /dev/null 2>&1; then
      echo "Elasticsearch is ready!"
      break
    fi
    echo "ES not ready yet, waiting... (${i}/30)"
    sleep 5
  done

  ELASTICSEARCH_URI="http://${ES_CONTAINER_NAME}:9200"
  ES_ENV=(-e ELASTICSEARCH_URI="${ELASTICSEARCH_URI}")
else
  echo "Elasticsearch is disabled, skipping..."
fi

# Stop and remove existing app container
echo "Stopping existing app container..."
docker rm -f "${CONTAINER_NAME}" 2>/dev/null || true

# Start new app container
echo "Starting new app container..."
docker run -d \
  --name "${CONTAINER_NAME}" \
  --network "${NETWORK_NAME}" \
  --restart unless-stopped \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e DB_HOST="${DB_HOST}" \
  -e DB_PORT="${DB_PORT}" \
  -e DB_NAME="${DB_NAME}" \
  -e DB_USERNAME="${DB_USERNAME}" \
  -e DB_PASSWORD="${DB_PASSWORD}" \
  -e OPENAI_API_KEY="${OPENAI_API_KEY}" \
  -e OPENAI_MODEL="${OPENAI_MODEL}" \
  -e SERVER_URL="${SERVER_URL}" \
  -e DISCORD_ERROR_WEBHOOK_URL="${DISCORD_ERROR_WEBHOOK_URL}" \
  -e ELASTICSEARCH_ENABLED="${ELASTICSEARCH_ENABLED}" \
  "${ES_ENV[@]}" \
  "${IMAGE}"

# Cleanup old images
echo "Cleaning up old images..."
docker image prune -f

# Health check with retry
echo "Waiting for application to start..."
MAX_RETRIES=15
RETRY_INTERVAL=10

for i in $(seq 1 "$MAX_RETRIES"); do
  echo "Health check attempt ${i}/${MAX_RETRIES}..."

  if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "Deployment successful!"
    exit 0
  fi

  if [ "$i" -lt "$MAX_RETRIES" ]; then
    echo "Not ready yet, waiting ${RETRY_INTERVAL}s..."
    sleep "$RETRY_INTERVAL"
  fi
done

echo "Health check failed after ${MAX_RETRIES} attempts. Container logs:"
docker logs "${CONTAINER_NAME}" --tail 100
exit 1
