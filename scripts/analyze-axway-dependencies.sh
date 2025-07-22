#!/bin/bash

# Script para analisar dependências do AWS SDK no Axway API Gateway
# Uso: ./scripts/analyze-axway-dependencies.sh [AXWAY_BASE_PATH]

set -e

AXWAY_BASE="${1:-/opt/axway/Axway-7.7.0.20240830}"
SYSTEM_LIB_DIR="${AXWAY_BASE}/apigateway/system/lib"

echo "🔍 Analisando dependências do Axway API Gateway..."
echo "📁 Base: ${AXWAY_BASE}"
echo "📁 Lib: ${SYSTEM_LIB_DIR}"
echo ""

if [ ! -d "$SYSTEM_LIB_DIR" ]; then
    echo "❌ Diretório não encontrado: ${SYSTEM_LIB_DIR}"
    exit 1
fi

echo "📋 AWS SDK JARs encontrados:"
echo "================================"

# Procurar por JARs do AWS SDK
find "$SYSTEM_LIB_DIR" -name "*.jar" | grep -i aws | while read -r jar; do
    echo "📦 $(basename "$jar")"
    
    # Extrair informações do JAR se possível
    if command -v unzip >/dev/null 2>&1; then
        version=$(unzip -p "$jar" META-INF/MANIFEST.MF 2>/dev/null | grep -i "implementation-version\|bundle-version" | head -1 | cut -d: -f2 | tr -d ' \r\n' || echo "N/A")
        echo "   📊 Versão: $version"
    fi
done

echo ""
echo "📋 Outras dependências relevantes:"
echo "=================================="

# Procurar por outras dependências importantes
find "$SYSTEM_LIB_DIR" -name "*.jar" | grep -E "(jackson|gson|slf4j|logback|spring)" | while read -r jar; do
    echo "📦 $(basename "$jar")"
done

echo ""
echo "📊 Resumo de dependências:"
echo "=========================="
echo "Total de JARs: $(find "$SYSTEM_LIB_DIR" -name "*.jar" | wc -l)"
echo "AWS SDK JARs: $(find "$SYSTEM_LIB_DIR" -name "*.jar" | grep -i aws | wc -l)"
echo "Jackson JARs: $(find "$SYSTEM_LIB_DIR" -name "*.jar" | grep -i jackson | wc -l)"
echo "Spring JARs: $(find "$SYSTEM_LIB_DIR" -name "*.jar" | grep -i spring | wc -l)"

echo ""
echo "💡 Recomendações:"
echo "=================="
echo "1. Verifique se as versões do AWS SDK são compatíveis"
echo "2. Considere usar as versões já presentes no Axway"
echo "3. Teste a compatibilidade antes de fazer override" 