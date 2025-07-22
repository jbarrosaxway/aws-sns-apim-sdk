#!/bin/bash

# Script para testar a imagem publicada no Docker Hub
# axwayjbarros/aws-lambda-apim-sdk:1.0.0
#
# Esta imagem contém todas as bibliotecas do Axway API Gateway
# para compilar o projeto, não para execução.

set -e

echo "🚀 Testando imagem publicada: axwayjbarros/aws-lambda-apim-sdk:1.0.0"
echo "📋 Nota: Esta imagem é apenas para build, não para execução"
echo ""

# Verificar se Docker está rodando
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker não está rodando. Inicie o Docker e tente novamente."
    exit 1
fi

# Pull da imagem
echo "📥 Fazendo pull da imagem..."
docker pull axwayjbarros/aws-lambda-apim-sdk:1.0.0

# Testar se a imagem existe
echo ""
echo "🔍 Verificando imagem..."
docker images axwayjbarros/aws-lambda-apim-sdk:1.0.0

# Testar Java
echo ""
echo "☕ Testando Java..."
docker run --rm axwayjbarros/aws-lambda-apim-sdk:1.0.0 java -version

# Testar estrutura do Axway
echo ""
echo "🏗️  Testando estrutura do Axway..."
docker run --rm axwayjbarros/aws-lambda-apim-sdk:1.0.0 ls -la /opt/Axway/

# Testar se o SDK está presente
echo ""
echo "📦 Verificando se o SDK AWS Lambda está presente..."
docker run --rm axwayjbarros/aws-lambda-apim-sdk:1.0.0 find /opt -name "*aws-lambda*" -type f

# Testar se os JARs AWS estão presentes
echo ""
echo "🔍 Verificando JARs AWS SDK..."
docker run --rm axwayjbarros/aws-lambda-apim-sdk:1.0.0 find /opt -name "*aws-java-sdk*" -type f

# Testar se o ext/lib está configurado
echo ""
echo "📁 Verificando ext/lib..."
docker run --rm axwayjbarros/aws-lambda-apim-sdk:1.0.0 ls -la /opt/Axway/apigateway/groups/emt-group/emt-service/ext/lib/ 2>/dev/null || echo "Diretório ext/lib não encontrado"

# Testar variáveis de ambiente
echo ""
echo "🌍 Verificando variáveis de ambiente..."
docker run --rm axwayjbarros/aws-lambda-apim-sdk:1.0.0 env | grep -E "(AXWAY|JAVA)" || echo "Variáveis de ambiente não encontradas"

echo ""
echo "✅ Testes concluídos!"
echo ""
echo "📋 Para fazer build do JAR:"
echo "   ./scripts/build-with-docker-image.sh"
echo ""
echo "📋 Para instalar no Linux:"
echo "   ./gradlew installLinux"
echo ""
echo "📋 Para instalar no Windows:"
echo "   ./gradlew installWindows" 