#!/bin/bash

# Script para corrigir o arquivo de internacionalização preservando outros filtros

set -e

echo "🔧 Corrigindo arquivo de internacionalização preservando outros filtros..."
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

# Conteúdo novo para AWSLambdaFilter
NEW_CONTENT="- type: InternationalizationFilter
  fields:
    type: AWSLambdaFilter
  logging:
    fatal: \"Error in the AWS Lambda filter. Error: \${circuit.exception}\"
    failure: \"Failed in the AWS Lambda filter\"
    success: \"Success in the AWS Lambda filter\""

# Usar awk para processar o arquivo
awk -v newContent="$NEW_CONTENT" '
BEGIN { 
    skipSection = 0; 
    inInternationalizationFilter = 0; 
    foundAWSLambdaFilter = 0;
    outputNewContent = 0;
}
{
    line = $0
    trimmedLine = line
    gsub(/^[ \t]+/, "", trimmedLine)
    
    # Detectar início de InternationalizationFilter
    if (trimmedLine == "type: InternationalizationFilter") {
        inInternationalizationFilter = 1
        skipSection = 0
        print line
        next
    }
    
    # Se estamos dentro de InternationalizationFilter, verificar se contém AWSLambdaFilter
    if (inInternationalizationFilter && trimmedLine == "type: AWSLambdaFilter") {
        if (foundAWSLambdaFilter) {
            # Já encontramos uma seção AWSLambdaFilter, pular esta
            skipSection = 1
            print "   ⏭️  Pulando seção duplicada AWSLambdaFilter" > "/dev/stderr"
        } else {
            # Primeira ocorrência, pular e substituir com novo conteúdo
            foundAWSLambdaFilter = 1
            skipSection = 1
            outputNewContent = 1
            print "   ⏭️  Substituindo seção AWSLambdaFilter" > "/dev/stderr"
        }
        next
    }
    
    # Detectar fim da seção InternationalizationFilter
    if (inInternationalizationFilter && trimmedLine ~ /^-/ && trimmedLine != "type: InternationalizationFilter") {
        inInternationalizationFilter = 0
        skipSection = 0
        # Adicionar novo conteúdo antes da próxima seção
        if (outputNewContent) {
            print newContent
            outputNewContent = 0
        }
        print line
        next
    }
    
    # Se estamos pulando a seção, continuar até o fim
    if (skipSection) {
        print "   ⏭️  Pulando linha: " trimmedLine > "/dev/stderr"
        next
    }
    
    print line
}

END {
    # Se ainda não adicionamos o novo conteúdo, adicionar no final
    if (outputNewContent) {
        print ""
        print newContent
    }
}' "$INTL_FILE" > "$INTL_FILE.tmp"

# Substituir o arquivo
mv "$INTL_FILE.tmp" "$INTL_FILE"

echo "✅ Arquivo corrigido!"
echo ""
echo "📄 Conteúdo final do arquivo:"
echo "---"
cat "$INTL_FILE"
echo "---"

echo ""
echo "🎉 Correção concluída!"
echo "💾 Backup disponível em: $BACKUP_FILE" 