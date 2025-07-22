#!/bin/bash

# Script para debugar o conteúdo da imagem base do Axway
# Útil para entender o que está disponível na imagem oficial

set -e

echo "🔍 Debug da Imagem Base do Axway"
echo "================================="

# Verificar se o Docker está rodando
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker não está rodando."
    exit 1
fi

# Login no registry (se necessário)
echo "🔐 Fazendo login no registry do Axway..."
if [ -z "$AXWAY_USERNAME" ] || [ -z "$AXWAY_PASSWORD" ]; then
    echo "⚠️  Variáveis AXWAY_USERNAME e AXWAY_PASSWORD não definidas."
    echo "   Tentando login manual..."
    docker login docker.repository.axway.com
else
    echo "$AXWAY_PASSWORD" | docker login docker.repository.axway.com -u "$AXWAY_USERNAME" --password-stdin
fi

# Pull da imagem base
echo "📥 Fazendo pull da imagem base..."
docker pull docker.repository.axway.com/apigateway-docker-prod/7.7/gateway:7.7.0.20240830-4-BN0145-ubi9

# Analisar conteúdo da imagem base
echo ""
echo "📋 Conteúdo da Imagem Base:"
echo "============================"

echo ""
echo "🏗️  Estrutura de diretórios:"
docker run --rm docker.repository.axway.com/apigateway-docker-prod/7.7/gateway:7.7.0.20240830-4-BN0145-ubi9 find /opt -type d -name "*jar*" 2>/dev/null | head -10

echo ""
echo "📦 JARs do Axway:"
docker run --rm docker.repository.axway.com/apigateway-docker-prod/7.7/gateway:7.7.0.20240830-4-BN0145-ubi9 find /opt -name "*.jar" | head -20

echo ""
echo "☕ Java disponível:"
docker run --rm docker.repository.axway.com/apigateway-docker-prod/7.7/gateway:7.7.0.20240830-4-BN0145-ubi9 java -version

echo ""
echo "🔧 Variáveis de ambiente:"
docker run --rm docker.repository.axway.com/apigateway-docker-prod/7.7/gateway:7.7.0.20240830-4-BN0145-ubi9 env | grep -E "(JAVA|AXWAY|APIGATEWAY)" | sort

echo ""
echo "📁 Diretórios principais:"
docker run --rm docker.repository.axway.com/apigateway-docker-prod/7.7/gateway:7.7.0.20240830-4-BN0145-ubi9 ls -la /opt/

echo ""
echo "✅ Debug concluído!"
echo ""
echo "📋 Informações:"
echo "   - A imagem base contém o Axway API Gateway completo"
echo "   - Nossa imagem adiciona apenas o SDK AWS Lambda"
echo "   - O login é necessário apenas para pull da imagem base" 