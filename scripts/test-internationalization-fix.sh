#!/bin/bash

# Script para testar a lógica de substituição do arquivo de internacionalização

set -e

echo "🧪 Testando lógica de substituição do arquivo de internacionalização..."
echo ""

# Criar arquivo de teste com conteúdo duplicado (problema atual)
cat > test-internationalization.yaml << 'EOF'
- type: InternationalizationFilter
  fields:
    fatal: "Error in the AWS Lambda  Error:\n\t\t\t\t${circuit.exception}"
    failure: Failed in the AWS Lambda filter
    success: Success in the AWS Lambda filter
- type: InternationalizationFilter
  fields:
    type: AWSLambdaFilter
  logging:
    fatal: "Error in the AWS Lambda  Error:\n\t\t\t\t${circuit.exception}"
    failure: Failed in the AWS Lambda filter
    success: Success in the AWS Lambda filter
EOF

# Conteúdo novo para substituir
cat > test-new-content.yaml << 'EOF'
- type: InternationalizationFilter
  fields:
    type: AWSLambdaFilter
  logging:
    fatal: "Error in the AWS Lambda Error (CORRIGIDO):\n\t\t\t\t${circuit.exception}"
    failure: Failed in the AWS Lambda filter (CORRIGIDO)
    success: Success in the AWS Lambda filter (CORRIGIDO)
EOF

echo "📄 Arquivo original criado:"
cat test-internationalization.yaml
echo "---"

# Aplicar a lógica de substituição corrigida
destContent=$(cat test-internationalization.yaml)
sourceContent=$(cat test-new-content.yaml)

if echo "$destContent" | grep -q "type: AWSLambdaFilter"; then
    echo "🔄 Filtro AWSLambdaFilter já existe. Substituindo..."
    
    # Usar awk para processar linha por linha
    awk '
    BEGIN { 
        skipSection = 0; 
        inInternationalizationFilter = 0; 
        foundAWSLambdaFilter = 0;
        indentLevel = 0;
        sectionIndent = 0;
    }
    {
        line = $0
        trimmedLine = line
        gsub(/^[ \t]+/, "", trimmedLine)
        currentIndent = length(line) - length(trimmedLine)
        
        # Detectar início de InternationalizationFilter
        if (trimmedLine == "type: InternationalizationFilter") {
            inInternationalizationFilter = 1
            sectionIndent = currentIndent
            skipSection = 0
            next
        }
        
        # Se estamos dentro de InternationalizationFilter, verificar se contém AWSLambdaFilter
        if (inInternationalizationFilter && trimmedLine == "type: AWSLambdaFilter") {
            if (foundAWSLambdaFilter) {
                # Já encontramos uma seção AWSLambdaFilter, pular esta seção inteira
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
        if (inInternationalizationFilter && currentIndent <= sectionIndent && trimmedLine ~ /^-/ && trimmedLine != "type: InternationalizationFilter") {
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
    }' test-internationalization.yaml > test-temp.yaml
    
    # Substituir o arquivo
    mv test-temp.yaml test-internationalization.yaml
    
    # Adicionar o novo conteúdo
    echo "" >> test-internationalization.yaml
    cat test-new-content.yaml >> test-internationalization.yaml
    
    echo "✅ Seção AWSLambdaFilter atualizada!"
fi

echo "📄 Arquivo após substituição:"
cat test-internationalization.yaml

# Limpar arquivos de teste
rm -f test-internationalization.yaml test-new-content.yaml

echo ""
echo "🧹 Arquivos de teste removidos" 