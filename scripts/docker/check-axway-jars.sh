#!/bin/bash

# Script para verificar JARs disponíveis na imagem base do Axway
# Útil para identificar quais JARs podem ser copiados para ext/lib

set -e

echo "🔍 Verificando JARs disponíveis na imagem base do Axway"
echo "========================================================"

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

echo ""
echo "📦 JARs disponíveis em /opt/Axway/apigateway/lib/:"
echo "=================================================="
docker run --rm docker.repository.axway.com/apigateway-docker-prod/7.7/gateway:7.7.0.20240830-4-BN0145-ubi9 find /opt/Axway/apigateway/lib -name "*.jar" | head -20

echo ""
echo "🔍 JARs específicos do AWS:"
echo "============================"
docker run --rm docker.repository.axway.com/apigateway-docker-prod/7.7/gateway:7.7.0.20240830-4-BN0145-ubi9 find /opt/Axway/apigateway/lib -name "*aws*" -o -name "*lambda*" | head -10

echo ""
echo "🔍 JARs do Jackson:"
echo "==================="
docker run --rm docker.repository.axway.com/apigateway-docker-prod/7.7/gateway:7.7.0.20240830-4-BN0145-ubi9 find /opt/Axway/apigateway/lib -name "*jackson*" | head -10

echo ""
echo "📁 Estrutura de diretórios:"
echo "==========================="
docker run --rm docker.repository.axway.com/apigateway-docker-prod/7.7/gateway:7.7.0.20240830-4-BN0145-ubi9 ls -la /opt/Axway/apigateway/

echo ""
echo "📋 Diretório groups (se existir):"
echo "================================="
docker run --rm docker.repository.axway.com/apigateway-docker-prod/7.7/gateway:7.7.0.20240830-4-BN0145-ubi9 find /opt/Axway/apigateway -name "groups" -type d 2>/dev/null || echo "Diretório groups não encontrado"

echo ""
echo "✅ Verificação concluída!"
echo ""
echo "📋 Próximos passos:"
echo "   1. Identificar os JARs específicos necessários"
echo "   2. Atualizar o Dockerfile com os caminhos corretos"
echo "   3. Testar a cópia dos JARs" 