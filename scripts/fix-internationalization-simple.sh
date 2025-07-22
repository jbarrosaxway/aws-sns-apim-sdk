#!/bin/bash

# Script simples para corrigir o arquivo de internacionalização

set -e

echo "🔧 Corrigindo arquivo de internacionalização..."
echo ""

# Verificar se o arquivo existe
PROJECT_PATH="${1:-}"
if [ -z "$PROJECT_PATH" ]; then
    echo "❌ Caminho do projeto não informado!"
    echo "   Uso: $0 <caminho-do-projeto>"
    echo "   Exemplo: $0 src/main/resources/yaml"
    exit 1
fi

INTL_FILE="$PROJECT_PATH/System/Internationalization Default.yaml"

if [ ! -f "$INTL_FILE" ]; then
    echo "❌ Arquivo não encontrado: $INTL_FILE"
    exit 1
fi

echo "📄 Arquivo encontrado: $INTL_FILE"
echo ""

# Fazer backup
BACKUP_FILE="$INTL_FILE.backup.$(date +%Y%m%d_%H%M%S)"
cp "$INTL_FILE" "$BACKUP_FILE"
echo "💾 Backup criado: $BACKUP_FILE"

# Criar novo conteúdo
cat > "$INTL_FILE" << 'EOF'
- type: InternationalizationFilter
  fields:
    type: AWSLambdaFilter
  logging:
    fatal: "Error in the AWS Lambda filter. Error: ${circuit.exception}"
    failure: "Failed in the AWS Lambda filter"
    success: "Success in the AWS Lambda filter"
EOF

echo "✅ Arquivo corrigido!"
echo ""
echo "📄 Conteúdo final do arquivo:"
echo "---"
cat "$INTL_FILE"
echo "---"

echo ""
echo "🎉 Correção concluída!"
echo "💾 Backup disponível em: $BACKUP_FILE" 