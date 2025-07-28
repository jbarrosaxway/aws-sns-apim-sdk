#!/bin/bash

# Build usando imagem Docker customizada
DOCKER_IMAGE="axwayjbarros/aws-lambda-apim-sdk:7.7.0.20240830"

set -e

if ! docker image inspect "$DOCKER_IMAGE" > /dev/null 2>&1; then
  echo "📥 Pulling Docker image: $DOCKER_IMAGE"
  docker pull "$DOCKER_IMAGE"
fi

echo "🚀 Building JAR using Docker image: $DOCKER_IMAGE"
echo "📋 Note: This image contém as libs necessárias para build, não para runtime"

docker run --rm -v "$(pwd)":/workspace -w /workspace "$DOCKER_IMAGE" ./gradlew clean build -Daxway.base=/opt/Axway 