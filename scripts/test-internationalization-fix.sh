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
    
    # Remover a seção existente do AWSLambdaFilter
    # Usar awk para processar linha por linha
    awk '
    BEGIN { skipSection = 0; inAWSLambdaSection = 0; indentLevel = 0; }
    {
        line = $0
        trimmedLine = line
        gsub(/^[ \t]+/, "", trimmedLine)
        currentIndent = length(line) - length(trimmedLine)
        
        # Detectar início da seção InternationalizationFilter que contém AWSLambdaFilter
        if (trimmedLine == "type: InternationalizationFilter") {
            # Verificar se a próxima seção contém AWSLambdaFilter
            hasAWSLambdaFilter = 0
            for (i = NR; i <= NR + 10 && i <= NF; i++) {
                if (trimmedLine == "type: AWSLambdaFilter") {
                    hasAWSLambdaFilter = 1
                    break
                }
            }
            
            if (hasAWSLambdaFilter) {
                skipSection = 1
                inAWSLambdaSection = 1
                indentLevel = currentIndent
                print "   🔍 Encontrada seção InternationalizationFilter com AWSLambdaFilter" > "/dev/stderr"
                next
            }
        }
        
        # Detectar fim da seção
        if (skipSection && inAWSLambdaSection) {
            # Se encontrou um item no mesmo nível ou superior, é o fim da seção
            if (currentIndent <= indentLevel && trimmedLine != "" && substr(trimmedLine, 1, 2) != "  ") {
                skipSection = 0
                inAWSLambdaSection = 0
                print "   🔍 Fim da seção detectado: " trimmedLine > "/dev/stderr"
                # Não adicionar esta linha, pois é o início da próxima seção
                next
            } else {
                # Ainda dentro da seção InternationalizationFilter, pular
                print "   ⏭️  Pulando linha: " trimmedLine > "/dev/stderr"
                next
            }
        }
        
        print line
    }' test-internationalization.yaml > test-temp.yaml
    
    # Adicionar o novo conteúdo
    cat test-temp.yaml > test-internationalization.yaml
    echo "" >> test-internationalization.yaml
    cat test-new-content.yaml >> test-internationalization.yaml
    
    echo "✅ Seção AWSLambdaFilter atualizada!"
fi

echo "📄 Arquivo após substituição:"
cat test-internationalization.yaml

# Limpar arquivos de teste
rm -f test-internationalization.yaml test-new-content.yaml test-temp.yaml

echo ""
echo "🧹 Arquivos de teste removidos" 