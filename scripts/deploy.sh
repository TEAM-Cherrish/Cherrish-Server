#!/bin/bash
set -euo pipefail

# Variables
AWS_REGION="${AWS_REGION:-ap-northeast-2}"
ECR_REGISTRY="${ECR_REGISTRY:?ERROR: ECR_REGISTRY must be set}"
IMAGE_TAG="${IMAGE_TAG:-latest}"
CONTAINER_NAME="cherrish-server"
IMAGE="${ECR_REGISTRY}:${IMAGE_TAG}"

echo "=== Cherrish Server Deployment ==="
echo "Image: ${IMAGE}"

# ECR Login
echo "Logging in to ECR..."
REGISTRY_HOST="${ECR_REGISTRY%%/*}"
aws ecr get-login-password --region "${AWS_REGION}" | \
  docker login --username AWS --password-stdin "${REGISTRY_HOST}"

# Pull latest image
echo "Pulling image..."
docker pull "${IMAGE}"

# Fetch environment variables from Parameter Store
echo "Fetching environment variables..."
DB_HOST=$(aws ssm get-parameter --name "/cherrish/DB_HOST" --region "${AWS_REGION}" --query "Parameter.Value" --output text)
DB_PORT=$(aws ssm get-parameter --name "/cherrish/DB_PORT" --region "${AWS_REGION}" --query "Parameter.Value" --output text)
DB_NAME=$(aws ssm get-parameter --name "/cherrish/DB_NAME" --region "${AWS_REGION}" --query "Parameter.Value" --output text)
DB_USERNAME=$(aws ssm get-parameter --name "/cherrish/DB_USERNAME" --region "${AWS_REGION}" --query "Parameter.Value" --output text)
DB_PASSWORD=$(aws ssm get-parameter --name "/cherrish/DB_PASSWORD" --region "${AWS_REGION}" --with-decryption --query "Parameter.Value" --output text)
OPENAI_API_KEY=$(aws ssm get-parameter --name "/cherrish/OPENAI_API_KEY" --region "${AWS_REGION}" --with-decryption --query "Parameter.Value" --output text)
OPENAI_MODEL=$(aws ssm get-parameter --name "/cherrish/OPENAI_MODEL" --region "${AWS_REGION}" --query "Parameter.Value" --output text)

# Stop and remove existing container
echo "Stopping existing container..."
docker rm -f "${CONTAINER_NAME}" 2>/dev/null || true

# Start new container
echo "Starting new container..."
docker run -d \
  --name "${CONTAINER_NAME}" \
  --restart unless-stopped \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST="${DB_HOST}" \
  -e DB_PORT="${DB_PORT}" \
  -e DB_NAME="${DB_NAME}" \
  -e DB_USERNAME="${DB_USERNAME}" \
  -e DB_PASSWORD="${DB_PASSWORD}" \
  -e OPENAI_API_KEY="${OPENAI_API_KEY}" \
  -e OPENAI_MODEL="${OPENAI_MODEL}" \
  "${IMAGE}"

# Cleanup old images
echo "Cleaning up old images..."
docker image prune -f

# Health check with retry
echo "Waiting for application to start..."
MAX_RETRIES=10
RETRY_INTERVAL=10

for i in $(seq 1 $MAX_RETRIES); do
  echo "Health check attempt ${i}/${MAX_RETRIES}..."

  if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "Deployment successful!"
    exit 0
  fi

  if [ $i -lt $MAX_RETRIES ]; then
    echo "Not ready yet, waiting ${RETRY_INTERVAL}s..."
    sleep $RETRY_INTERVAL
  fi
done

echo "Health check failed after ${MAX_RETRIES} attempts. Container logs:"
docker logs "${CONTAINER_NAME}" --tail 100
exit 1
