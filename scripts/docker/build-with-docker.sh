#!/bin/bash

# Script para build completo usando Docker
# Faz o build do JAR dentro de um container com Axway

set -e

echo "🐳 Build Completo com Docker"
echo "============================"

# Verificar se o Docker está rodando
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker não está rodando. Inicie o Docker Desktop primeiro."
    exit 1
fi

# Login no registry do Axway (se necessário)
echo "🔐 Fazendo login no registry do Axway..."
if [ -z "$AXWAY_USERNAME" ] || [ -z "$AXWAY_PASSWORD" ]; then
    echo "⚠️  Variáveis AXWAY_USERNAME e AXWAY_PASSWORD não definidas."
    echo "   Para build local, você pode precisar fazer login manualmente:"
    echo "   docker login docker.repository.axway.com"
    echo ""
    read -p "Continuar sem login? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
else
    echo "$AXWAY_PASSWORD" | docker login docker.repository.axway.com -u "$AXWAY_USERNAME" --password-stdin
fi

# Build da imagem de build
echo "🔨 Buildando imagem de build..."
docker build -f Dockerfile.build -t aws-lambda-apim-sdk-build:latest .

# Executar build do JAR dentro do container
echo "📦 Executando build do JAR dentro do container..."
docker run --rm \
    -v "$(pwd)/build:/workspace/build" \
    aws-lambda-apim-sdk-build:latest \
    gradle buildJarLinux

# Verificar se o JAR foi criado
JAR_FILE=$(find build/libs -name "aws-lambda-apim-sdk-*.jar" | head -1)
if [ -z "$JAR_FILE" ]; then
    echo "❌ JAR não encontrado em build/libs/"
    exit 1
fi

echo "✅ JAR criado: $JAR_FILE"

# Copiar JAR para o contexto do Docker
echo "📋 Copiando JAR para contexto do Docker..."
cp "$JAR_FILE" ./

# Build da imagem de runtime
echo "🔨 Buildando imagem de runtime..."
docker build -t aws-lambda-apim-sdk:latest .

# Limpar JAR do contexto
rm aws-lambda-apim-sdk-*.jar

# Teste da imagem
echo "🧪 Testando imagem..."
docker run --rm aws-lambda-apim-sdk:latest java -version
docker run --rm aws-lambda-apim-sdk:latest ls -la /opt/aws-lambda-sdk/

echo ""
echo "✅ Build completo concluído com sucesso!"
echo ""
echo "📋 Fluxo executado:"
echo "   1. Build da imagem de build (com Axway + Gradle)"
echo "   2. Build do JAR dentro do container"
echo "   3. Build da imagem de runtime (com JAR integrado)"
echo ""
echo "📋 Comandos úteis:"
echo "   docker run --rm aws-lambda-apim-sdk:latest java -version"
echo "   docker run --rm aws-lambda-apim-sdk:latest ls -la /opt/aws-lambda-sdk/"
echo "   docker images | grep aws-lambda-apim-sdk" 