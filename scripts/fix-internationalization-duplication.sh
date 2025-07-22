#!/bin/bash

# Script para corrigir duplicação no arquivo de internacionalização

set -e

echo "🔧 Corrigindo duplicação no arquivo de internacionalização..."
echo ""

# Verificar se o arquivo existe
PROJECT_PATH="${1:-}"
if [ -z "$PROJECT_PATH" ]; then
    echo "❌ Caminho do projeto não informado!"
    echo "   Uso: $0 <caminho-do-projeto>"
    echo "   Exemplo: $0 /c/Users/jbarros/apiprojects/DIGIO-POC-AKS"
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

# Verificar se há duplicação
if grep -c "type: AWSLambdaFilter" "$INTL_FILE" | grep -q "2"; then
    echo "🔄 Detectada duplicação. Corrigindo..."
    
    # Criar arquivo temporário
    TEMP_FILE=$(mktemp)
    
    # Processar o arquivo removendo duplicações
    awk '
    BEGIN { 
        skipSection = 0; 
        inInternationalizationFilter = 0; 
        foundAWSLambdaFilter = 0;
    }
    {
        line = $0
        trimmedLine = line
        gsub(/^[ \t]+/, "", trimmedLine)
        
        # Detectar início de InternationalizationFilter
        if (trimmedLine == "type: InternationalizationFilter") {
            inInternationalizationFilter = 1
            skipSection = 0
            next
        }
        
        # Se estamos dentro de InternationalizationFilter, verificar se contém AWSLambdaFilter
        if (inInternationalizationFilter && trimmedLine == "type: AWSLambdaFilter") {
            if (foundAWSLambdaFilter) {
                # Já encontramos uma seção AWSLambdaFilter, pular esta
                skipSection = 1
                print "   ⏭️  Pulando seção duplicada AWSLambdaFilter" > "/dev/stderr"
            } else {
                # Primeira ocorrência, manter
                foundAWSLambdaFilter = 1
                skipSection = 0
            }
            next
        }
        
        # Detectar fim da seção InternationalizationFilter
        if (inInternationalizationFilter && trimmedLine ~ /^-/ && trimmedLine != "type: InternationalizationFilter") {
            inInternationalizationFilter = 0
            skipSection = 0
            # Não adicionar esta linha, pois é o início da próxima seção
            next
        }
        
        # Se estamos pulando a seção, continuar até o fim
        if (skipSection) {
            print "   ⏭️  Pulando linha: " trimmedLine > "/dev/stderr"
            next
        }
        
        print line
    }' "$INTL_FILE" > "$TEMP_FILE"
    
    # Substituir o arquivo original
    mv "$TEMP_FILE" "$INTL_FILE"
    
    echo "✅ Duplicação corrigida!"
else
    echo "✅ Nenhuma duplicação encontrada."
fi

echo ""
echo "📄 Conteúdo final do arquivo:"
echo "---"
cat "$INTL_FILE"
echo "---"

echo ""
echo "🎉 Correção concluída!"
echo "💾 Backup disponível em: $BACKUP_FILE" 