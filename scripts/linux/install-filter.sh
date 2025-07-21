#!/bin/bash

# Script de instalação do filtro AWS Lambda para Axway API Gateway (Linux)
# Autor: Assistente
# Data: $(date)
# Nota: Os arquivos YAML estão organizados em src/main/resources/yaml/
# Para Windows, use: install-filter-windows.ps1 ou install-filter-windows.cmd

AXWAY_DIR="/opt/axway/Axway-7.7.0.20240830"
JAR_FILE="build/libs/aws-lambda-apim-sdk-1.0.1.jar"
EXT_LIB_DIR="$AXWAY_DIR/apigateway/groups/group-2/instance-1/ext/lib"

echo "=== Instalação do Filtro AWS Lambda para Axway API Gateway ==="
echo "Diretório Axway: $AXWAY_DIR"
echo "JAR: $JAR_FILE"
echo ""

# Verificar se o JAR existe
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ Erro: JAR não encontrado: $JAR_FILE"
    echo "Execute './gradlew build' primeiro"
    exit 1
fi

# Verificar se o diretório Axway existe
if [ ! -d "$AXWAY_DIR" ]; then
    echo "❌ Erro: Diretório Axway não encontrado: $AXWAY_DIR"
    exit 1
fi

# Criar diretório ext/lib se não existir
if [ ! -d "$EXT_LIB_DIR" ]; then
    echo "📁 Criando diretório: $EXT_LIB_DIR"
    mkdir -p "$EXT_LIB_DIR"
fi

# Copiar JAR para o diretório ext/lib
echo "📦 Copiando JAR para: $EXT_LIB_DIR"
cp "$JAR_FILE" "$EXT_LIB_DIR/"

# Verificar se a cópia foi bem-sucedida
if [ $? -eq 0 ]; then
    echo "✅ JAR copiado com sucesso"
else
    echo "❌ Erro ao copiar JAR"
    exit 1
fi

# Listar JARs no diretório
echo ""
echo "📋 JARs no diretório ext/lib:"
ls -la "$EXT_LIB_DIR"/*.jar

echo ""
echo "=== Instalação Concluída ==="
echo ""
echo "📝 Próximos passos:"
echo "1. Reinicie o Axway API Gateway"
echo "2. No Policy Studio, vá em Window > Preferences > Runtime Dependencies"
echo "3. Adicione o JAR: $EXT_LIB_DIR/aws-lambda-apim-sdk-1.0.1.jar"
echo "4. Reinicie o Policy Studio com a opção -clean"
echo "5. O filtro 'AWS Lambda Filter' estará disponível na paleta de filtros"
echo ""
echo "🔧 Para verificar se o filtro está funcionando:"
echo "- Abra o Policy Studio"
echo "- Crie uma nova política"
echo "- Procure por 'AWS Lambda' na paleta de filtros"
echo "- Configure o filtro com os parâmetros necessários" 