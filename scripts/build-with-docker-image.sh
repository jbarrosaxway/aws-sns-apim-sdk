#!/bin/bash

# Script para fazer build do JAR usando a imagem publicada
# axwayjbarros/aws-lambda-apim-sdk:1.0.0
# 
# Esta imagem contém todas as bibliotecas do Axway API Gateway
# para compilar o projeto, não para execução.

set -e

echo "🚀 Build do JAR usando imagem Docker: axwayjbarros/aws-lambda-apim-sdk:1.0.0"
echo "📋 Nota: Esta imagem contém apenas as bibliotecas para build, não para execução"
echo ""

# Verificar se Docker está rodando
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker não está rodando. Inicie o Docker e tente novamente."
    exit 1
fi

# Verificar se estamos no diretório correto
if [ ! -f "build.gradle" ]; then
    echo "❌ Arquivo build.gradle não encontrado. Execute este script no diretório raiz do projeto."
    exit 1
fi

# Pull da imagem se necessário
echo "📥 Verificando imagem Docker..."
docker pull axwayjbarros/aws-lambda-apim-sdk:1.0.0

# Limpar build anterior
echo ""
echo "🧹 Limpando build anterior..."
rm -rf build/
rm -rf .gradle/

# Criar diretório para o build
mkdir -p build/libs

# Fazer build usando a imagem Docker
echo ""
echo "🔨 Iniciando build do JAR..."
echo "📁 Diretório atual: $(pwd)"
echo "📁 Build será salvo em: $(pwd)/build/libs/"

# Executar build dentro do container
docker run --rm \
  -v "$(pwd):/workspace" \
  -v "$(pwd)/build:/workspace/build" \
  -v "$(pwd)/.gradle:/workspace/.gradle" \
  -w /workspace \
  axwayjbarros/aws-lambda-apim-sdk:1.0.0 \
  bash -c "
    echo '🔧 Configurando ambiente...'
    export JAVA_HOME=/opt/java/openjdk-11
    export PATH=\$JAVA_HOME/bin:\$PATH
    
    echo '📦 Verificando Java...'
    java -version
    
    echo '📦 Verificando Gradle...'
    gradle --version || echo 'Gradle não encontrado, instalando...'
    
    echo '🔨 Executando build...'
    gradle clean build || echo 'Build falhou, tentando sem clean...'
    gradle build || echo 'Build falhou novamente'
    
    echo '📋 Verificando resultado...'
    ls -la build/libs/ || echo 'Diretório build/libs não encontrado'
  "

# Verificar se o JAR foi criado
echo ""
echo "🔍 Verificando resultado do build..."

if [ -f "build/libs/aws-lambda-apim-sdk-1.0.1.jar" ]; then
    echo "✅ JAR criado com sucesso!"
    echo "📁 Arquivo: build/libs/aws-lambda-apim-sdk-1.0.1.jar"
    echo "📏 Tamanho: $(du -h build/libs/aws-lambda-apim-sdk-1.0.1.jar | cut -f1)"
    
    echo ""
    echo "📋 Conteúdo do JAR:"
    jar -tf build/libs/aws-lambda-apim-sdk-1.0.1.jar | head -20
    
    echo ""
    echo "🎉 Build concluído com sucesso!"
    echo ""
    echo "📋 Próximos passos:"
    echo "1. Para Linux: ./gradlew installLinux"
    echo "2. Para Windows: Copie o JAR e execute ./gradlew installWindows"
    echo "3. Para Docker: docker-compose up -d"
    
else
    echo "❌ JAR não foi criado!"
    echo ""
    echo "🔍 Verificando diretório build:"
    ls -la build/ || echo "Diretório build não existe"
    
    echo ""
    echo "🔍 Verificando logs do Gradle:"
    if [ -f ".gradle/build.log" ]; then
        tail -20 .gradle/build.log
    else
        echo "Log do Gradle não encontrado"
    fi
    
    echo ""
    echo "💡 Tentativas de solução:"
    echo "1. Verifique se o Docker está rodando"
    echo "2. Verifique se a imagem existe: docker images axwayjbarros/aws-lambda-apim-sdk:1.0.0"
    echo "3. Tente fazer pull da imagem: docker pull axwayjbarros/aws-lambda-apim-sdk:1.0.0"
    echo "4. Verifique se há espaço em disco"
fi 