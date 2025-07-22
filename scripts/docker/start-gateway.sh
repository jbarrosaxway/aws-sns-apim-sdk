#!/bin/bash

# Script de inicialização para Axway API Gateway
set -e

echo "🚀 Iniciando Axway API Gateway..."

# Verificar se o Java está disponível
if ! command -v java &> /dev/null; then
    echo "❌ Java não encontrado!"
    exit 1
fi

echo "✅ Java encontrado: $(java -version 2>&1 | head -n 1)"

# Verificar se o diretório do Axway existe
if [ ! -d "$AXWAY_HOME" ]; then
    echo "❌ Diretório do Axway não encontrado: $AXWAY_HOME"
    echo "📋 Instruções:"
    echo "1. Copie os arquivos de instalação do Axway para o container"
    echo "2. Descomente as linhas de instalação no Dockerfile"
    echo "3. Rebuild a imagem"
    exit 1
fi

echo "✅ Diretório do Axway encontrado: $AXWAY_HOME"

# Verificar se o JAR do projeto existe
JAR_FILE="$APIGATEWAY_HOME/groups/group-2/instance-1/ext/lib/aws-lambda-apim-sdk-*.jar"
if ls $JAR_FILE 1> /dev/null 2>&1; then
    echo "✅ JAR do projeto encontrado: $(ls $JAR_FILE)"
else
    echo "⚠️  JAR do projeto não encontrado. Execute o build primeiro."
fi

# Configurar variáveis de ambiente
export AXWAY_HOME
export APIGATEWAY_HOME
export POLICYSTUDIO_HOME

# Iniciar o API Gateway (comando específico do Axway)
# Nota: Você precisará ajustar este comando baseado na instalação real do Axway
echo "🔧 Iniciando API Gateway..."

# Exemplo de comando (ajuste conforme necessário):
# $APIGATEWAY_HOME/posix/bin/startinstance -n group-2 -i instance-1

# Por enquanto, apenas manter o container rodando
echo "📋 Container iniciado com sucesso!"
echo "📁 Axway Home: $AXWAY_HOME"
echo "📁 API Gateway: $APIGATEWAY_HOME"
echo "📁 Policy Studio: $POLICYSTUDIO_HOME"
echo ""
echo "🔧 Para instalar o filtro AWS Lambda:"
echo "   ./gradlew -Daxway.base=$AXWAY_HOME installLinux"
echo ""
echo "⏳ Aguardando comandos..."

# Manter o container rodando
tail -f /dev/null 