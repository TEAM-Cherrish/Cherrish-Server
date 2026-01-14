#!/bin/bash
set -euo pipefail

# Variables (passed from SSM or environment)
AWS_REGION="${AWS_REGION:-ap-northeast-2}"
ECR_REGISTRY="${ECR_REGISTRY}"
IMAGE_TAG="${IMAGE_TAG:-latest}"
APP_DIR="/home/ec2-user/cherrish"
CONTAINER_NAME="cherrish-server"

echo "=== Cherrish Server Deployment ==="
echo "Region: ${AWS_REGION}"
echo "Registry: ${ECR_REGISTRY}"
echo "Tag: ${IMAGE_TAG}"

# Ensure app directory exists
mkdir -p "${APP_DIR}"
cd "${APP_DIR}"

# ECR Login
echo "Logging in to ECR..."
REGISTRY_HOST="${ECR_REGISTRY%%/*}"
aws ecr get-login-password --region "${AWS_REGION}" | \
  docker login --username AWS --password-stdin "${REGISTRY_HOST}"

# Fetch environment variables from Parameter Store
echo "Fetching environment variables from Parameter Store..."
export DB_HOST=$(aws ssm get-parameter --name "/cherrish/DB_HOST" --region "${AWS_REGION}" --query "Parameter.Value" --output text)
export DB_PORT=$(aws ssm get-parameter --name "/cherrish/DB_PORT" --region "${AWS_REGION}" --query "Parameter.Value" --output text)
export DB_NAME=$(aws ssm get-parameter --name "/cherrish/DB_NAME" --region "${AWS_REGION}" --query "Parameter.Value" --output text)
export DB_USERNAME=$(aws ssm get-parameter --name "/cherrish/DB_USERNAME" --region "${AWS_REGION}" --query "Parameter.Value" --output text)
export DB_PASSWORD=$(aws ssm get-parameter --name "/cherrish/DB_PASSWORD" --region "${AWS_REGION}" --with-decryption --query "Parameter.Value" --output text)
export OPENAI_API_KEY=$(aws ssm get-parameter --name "/cherrish/OPENAI_API_KEY" --region "${AWS_REGION}" --with-decryption --query "Parameter.Value" --output text)
export OPENAI_MODEL=$(aws ssm get-parameter --name "/cherrish/OPENAI_MODEL" --region "${AWS_REGION}" --query "Parameter.Value" --output text)

# Create .env file for docker-compose
cat > "${APP_DIR}/.env" << EOF
ECR_REGISTRY=${ECR_REGISTRY}
IMAGE_TAG=${IMAGE_TAG}
DB_HOST=${DB_HOST}
DB_PORT=${DB_PORT}
DB_NAME=${DB_NAME}
DB_USERNAME=${DB_USERNAME}
DB_PASSWORD=${DB_PASSWORD}
OPENAI_API_KEY=${OPENAI_API_KEY}
OPENAI_MODEL=${OPENAI_MODEL}
EOF

# Pull latest image
echo "Pulling latest image..."
docker compose -f docker-compose.prod.yml pull app

# Stop and remove existing container if exists
if docker ps -aq -f "name=^${CONTAINER_NAME}$" | grep -q .; then
  echo "Stopping existing container..."
  docker rm -f "${CONTAINER_NAME}" || true
fi

# Start new container
echo "Starting new container..."
docker compose -f docker-compose.prod.yml up -d app

# Cleanup old images
echo "Cleaning up old images..."
docker image prune -f

# Health check
echo "Waiting for health check..."
sleep 30
if curl -sf http://localhost:8080/actuator/health > /dev/null; then
  echo "Deployment successful!"
else
  echo "Health check failed!"
  docker logs "${CONTAINER_NAME}" --tail 50
  exit 1
fi
