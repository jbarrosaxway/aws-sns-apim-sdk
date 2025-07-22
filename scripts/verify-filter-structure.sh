#!/bin/bash

# Script para verificar a estrutura do filtro AWS Lambda
# Verifica se todos os arquivos estão alinhados e corretos

set -e

echo "🔍 Verificando estrutura do filtro AWS Lambda..."
echo ""

# Verificar se o YAML está correto
echo "📄 Verificando YAML do filtro..."
YAML_FILE="src/main/resources/yaml/META-INF/types/Entity/Filter/AWSFilter/AWSLambdaFilter.yaml"

if [ -f "$YAML_FILE" ]; then
    echo "✅ YAML encontrado: $YAML_FILE"
    
    # Verificar campos definidos no YAML
    echo "📋 Campos definidos no YAML:"
    grep -A 1 "  [a-zA-Z]*:" "$YAML_FILE" | grep -v "^--$" | while read -r line; do
        if [[ $line =~ ^[[:space:]]*([a-zA-Z]+): ]]; then
            field_name="${BASH_REMATCH[1]}"
            echo "   - $field_name"
        fi
    done
else
    echo "❌ YAML não encontrado: $YAML_FILE"
fi

echo ""

# Verificar se o XML está correto
echo "📄 Verificando XML da interface..."
XML_FILE="src/main/resources/com/axway/aws/lambda/aws_lambda.xml"

if [ -f "$XML_FILE" ]; then
    echo "✅ XML encontrado: $XML_FILE"
    
    # Verificar campos definidos no XML
    echo "📋 Campos definidos no XML:"
    grep -o 'field="[^"]*"' "$XML_FILE" | sed 's/field="//g' | sed 's/"//g' | while read -r field; do
        echo "   - $field"
    done
    
    # Verificar ComboBox
    echo ""
    echo "📋 ComboBox encontrados:"
    grep -A 10 "ComboAttribute" "$XML_FILE" | grep -E "(field=|option)" | while read -r line; do
        if [[ $line =~ field=\"([^\"]+)\" ]]; then
            echo "   - ComboBox: ${BASH_REMATCH[1]}"
        elif [[ $line =~ value=\"([^\"]+)\" ]]; then
            echo "     Opção: ${BASH_REMATCH[1]}"
        fi
    done
else
    echo "❌ XML não encontrado: $XML_FILE"
fi

echo ""

# Verificar recursos de internacionalização
echo "📄 Verificando recursos de internacionalização..."
PROPERTIES_FILE="src/main/resources/com/axway/aws/lambda/resources.properties"

if [ -f "$PROPERTIES_FILE" ]; then
    echo "✅ Arquivo de recursos encontrado: $PROPERTIES_FILE"
    
    # Verificar labels definidos
    echo "📋 Labels definidos:"
    grep "_LABEL=" "$PROPERTIES_FILE" | while read -r line; do
        field=$(echo "$line" | cut -d'=' -f1 | sed 's/_LABEL//')
        echo "   - $field"
    done
else
    echo "❌ Arquivo de recursos não encontrado: $PROPERTIES_FILE"
fi

echo ""

# Verificar se há inconsistências
echo "🔍 Verificando inconsistências..."

# Verificar se todos os campos do YAML têm correspondência no XML
echo "📋 Verificando correspondência YAML -> XML:"
yaml_fields=$(grep -A 1 "  [a-zA-Z]*:" "$YAML_FILE" | grep -v "^--$" | grep -o "^[[:space:]]*[a-zA-Z]*:" | sed 's/://g' | sed 's/^[[:space:]]*//')

for field in $yaml_fields; do
    if grep -q "field=\"$field\"" "$XML_FILE"; then
        echo "   ✅ $field: presente no XML"
    else
        echo "   ❌ $field: NÃO encontrado no XML"
    fi
done

echo ""

# Verificar se todos os campos do XML têm correspondência no YAML
echo "📋 Verificando correspondência XML -> YAML:"
xml_fields=$(grep -o 'field="[^"]*"' "$XML_FILE" | sed 's/field="//g' | sed 's/"//g')

for field in $xml_fields; do
    if grep -q "  $field:" "$YAML_FILE"; then
        echo "   ✅ $field: presente no YAML"
    else
        echo "   ❌ $field: NÃO encontrado no YAML"
    fi
done

echo ""
echo "🎉 Verificação concluída!"
echo ""
echo "💡 Dicas:"
echo "   - Se houver inconsistências, corrija os arquivos"
echo "   - Execute ./scripts/clean-and-reinstall.sh para limpar cache"
echo "   - Reinicie o Policy Studio com -clean" 