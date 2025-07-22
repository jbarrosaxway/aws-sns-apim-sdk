#!/bin/bash

# Script para testar se a lógica preserva outros filtros

set -e

echo "🧪 Testando preservação de outros filtros..."
echo ""

# Criar arquivo de teste com múltiplos filtros
cat > test-multiple-filters.yaml << 'EOF'
- type: InternationalizationFilter
  fields:
    type: OtherFilter
  logging:
    fatal: "Error in other filter"
    failure: "Failed in other filter"
    success: "Success in other filter"

- type: InternationalizationFilter
  fields:
    type: AWSLambdaFilter
  logging:
    fatal: "Error in the AWS Lambda  Error:\n\t\t\t\t${circuit.exception}"
    failure: Failed in the AWS Lambda filter
    success: Success in the AWS Lambda filter

- type: InternationalizationFilter
  fields:
    type: AnotherFilter
  logging:
    fatal: "Error in another filter"
    failure: "Failed in another filter"
    success: "Success in another filter"
EOF

# Conteúdo novo para substituir apenas AWSLambdaFilter
cat > test-new-awslambda.yaml << 'EOF'
- type: InternationalizationFilter
  fields:
    type: AWSLambdaFilter
  logging:
    fatal: "Error in the AWS Lambda filter. Error: ${circuit.exception}"
    failure: "Failed in the AWS Lambda filter"
    success: "Success in the AWS Lambda filter"
EOF

echo "📄 Arquivo original criado:"
cat test-multiple-filters.yaml
echo "---"

echo "📄 Conteúdo novo para AWSLambdaFilter:"
cat test-new-awslambda.yaml
echo "---"

# Simular a lógica do build.gradle
destContent=$(cat test-multiple-filters.yaml)
sourceContent=$(cat test-new-awslambda.yaml)

if echo "$destContent" | grep -q "type: AWSLambdaFilter"; then
    echo "🔄 Filtro AWSLambdaFilter já existe. Atualizando seção..."
    
    # Usar awk para processar linha por linha
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
                print "   ⏭️  Pulando seção AWSLambdaFilter para substituição" > "/dev/stderr"
            }
            next
        }
        
        # Detectar fim da seção InternationalizationFilter (próximo item no mesmo nível)
        if (inInternationalizationFilter && trimmedLine ~ /^-/ && trimmedLine != "type: InternationalizationFilter") {
            inInternationalizationFilter = 0
            skipSection = 0
            print "   🔍 Fim da seção InternationalizationFilter detectado: " trimmedLine > "/dev/stderr"
            # Não adicionar esta linha, pois é o início da próxima seção
            next
        }
        
        # Se estamos pulando a seção, continuar até o fim
        if (skipSection) {
            print "   ⏭️  Pulando linha: " trimmedLine > "/dev/stderr"
            next
        }
        
        print line
    }' test-multiple-filters.yaml > test-temp.yaml
    
    # Substituir o arquivo
    mv test-temp.yaml test-multiple-filters.yaml
    
    # Adicionar o novo conteúdo
    echo "" >> test-multiple-filters.yaml
    cat test-new-awslambda.yaml >> test-multiple-filters.yaml
    
    echo "✅ Seção AWSLambdaFilter atualizada!"
fi

echo "📄 Arquivo após substituição:"
cat test-multiple-filters.yaml

# Verificar se outros filtros foram preservados
echo ""
echo "🔍 Verificando preservação de outros filtros:"
echo "   OtherFilter: $(grep -c "OtherFilter" test-multiple-filters.yaml || echo "0")"
echo "   AnotherFilter: $(grep -c "AnotherFilter" test-multiple-filters.yaml || echo "0")"
echo "   AWSLambdaFilter: $(grep -c "AWSLambdaFilter" test-multiple-filters.yaml || echo "0")"

# Limpar arquivos de teste
rm -f test-multiple-filters.yaml test-new-awslambda.yaml

echo ""
echo "🧹 Arquivos de teste removidos" 