#!/bin/bash

# Script para limpar cache e reinstalar filtro AWS Lambda
# Útil quando há problemas de cache ou configurações antigas

set -e

echo "🧹 Limpando cache e reinstalando filtro AWS Lambda..."
echo ""

# Verificar se o Policy Studio está rodando
if pgrep -f "PolicyStudio" > /dev/null; then
    echo "⚠️  Policy Studio está rodando. Feche-o antes de continuar."
    echo "   Execute: pkill -f PolicyStudio"
    exit 1
fi

# Limpar cache do Policy Studio
echo "🗑️  Limpando cache do Policy Studio..."

# Diretórios de cache comuns
CACHE_DIRS=(
    "$HOME/.PolicyStudio"
    "$HOME/.eclipse"
    "$HOME/.metadata"
    "/tmp/.PolicyStudio"
)

for dir in "${CACHE_DIRS[@]}"; do
    if [ -d "$dir" ]; then
        echo "   Removendo: $dir"
        rm -rf "$dir"
    fi
done

# Limpar workspace do Policy Studio
WORKSPACE_DIRS=(
    "$HOME/PolicyStudioWorkspace"
    "$HOME/.PolicyStudioWorkspace"
)

for dir in "${WORKSPACE_DIRS[@]}"; do
    if [ -d "$dir" ]; then
        echo "   Removendo workspace: $dir"
        rm -rf "$dir"
    fi
done

echo "✅ Cache limpo!"

# Reinstalar filtro
echo ""
echo "🔧 Reinstalando filtro AWS Lambda..."

# Build do projeto
echo "   Build do projeto..."
./gradlew clean build

# Instalar YAML
echo "   Instalando YAML..."
./gradlew installLinux

echo ""
echo "🎉 Reinstalação concluída!"
echo ""
echo "📋 Próximos passos:"
echo "1. Abra o Policy Studio"
echo "2. Vá em Window > Preferences > Runtime Dependencies"
echo "3. Adicione o JAR: aws-lambda-apim-sdk-1.0.1.jar"
echo "4. Reinicie o Policy Studio com -clean"
echo "5. Procure por 'Invoke Lambda Function' na paleta"
echo ""
echo "⚠️  Se ainda houver problemas:"
echo "   - Verifique se o JAR foi adicionado corretamente"
echo "   - Reinicie o Policy Studio com: ./PolicyStudio -clean"
echo "   - Verifique os logs do Policy Studio" 