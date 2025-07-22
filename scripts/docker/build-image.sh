#!/bin/bash

# Script para build da imagem Docker com Axway API Gateway
# Esta imagem é usada para build e desenvolvimento, não para execução do gateway

set -e

echo "🐳 Build da imagem Docker AWS Lambda + Axway API Gateway"
echo "========================================================"

# Verificar se o Docker está rodando
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker não está rodando. Inicie o Docker Desktop primeiro."
    exit 1
fi

# Verificar se o build.gradle existe
if [ ! -f "build.gradle" ]; then
    echo "❌ Arquivo build.gradle não encontrado. Execute este script na raiz do projeto."
    exit 1
fi

# Build do JAR
echo "📦 Buildando JAR..."
./gradlew buildJarLinux

# Verificar se o JAR foi criado
JAR_FILE=$(find build/libs -name "aws-lambda-apim-sdk-*.jar" | head -1)
if [ -z "$JAR_FILE" ]; then
    echo "❌ JAR não encontrado em build/libs/"
    exit 1
fi

echo "✅ JAR criado: $JAR_FILE"

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

# Copiar JAR para o contexto do Docker
echo "📋 Copiando JAR para contexto do Docker..."
cp "$JAR_FILE" ./

# Build da imagem
echo "🔨 Buildando imagem Docker..."
docker build -t aws-lambda-apim-sdk:latest .

# Limpar JAR do contexto
rm aws-lambda-apim-sdk-*.jar

# Teste da imagem
echo "🧪 Testando imagem..."
docker run --rm aws-lambda-apim-sdk:latest java -version
docker run --rm aws-lambda-apim-sdk:latest ls -la /opt/aws-lambda-sdk/

# Verificar JARs em ext/lib
echo "🔍 Verificando JARs em ext/lib..."
docker run --rm aws-lambda-apim-sdk:latest ls -la /opt/Axway/apigateway/groups/emt-group/emt-service/ext/lib/ 2>/dev/null || echo "Diretório ext/lib não encontrado"

echo ""
echo "✅ Build concluído com sucesso!"
echo ""
echo "📋 Informações:"
echo "   Esta imagem contém o SDK AWS Lambda integrado ao Axway API Gateway"
echo "   Use para desenvolvimento e build de projetos que dependem do SDK"
echo ""
echo "📋 Comandos úteis:"
echo "   docker run --rm aws-lambda-apim-sdk:latest java -version"
echo "   docker run --rm aws-lambda-apim-sdk:latest ls -la /opt/aws-lambda-sdk/"
echo "   docker run --rm aws-lambda-apim-sdk:latest ls -la /opt/Axway/apigateway/groups/emt-group/emt-service/ext/lib/"
echo "   docker images | grep aws-lambda-apim-sdk" 