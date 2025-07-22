#!/bin/bash

# Script para verificar valores corretos dos campos AWS Lambda
# Baseado na documentação oficial da AWS Lambda

set -e

echo "🔍 Verificando valores dos campos AWS Lambda..."
echo ""

# Valores corretos segundo a documentação da AWS
CORRECT_INVOCATION_TYPES=("RequestResponse" "Event" "DryRun")
CORRECT_LOG_TYPES=("None" "Tail")

echo "📋 Valores corretos para invocationType (AWS Lambda):"
for type in "${CORRECT_INVOCATION_TYPES[@]}"; do
    echo "   ✅ $type"
done

echo ""
echo "📋 Valores corretos para logType (AWS Lambda):"
for type in "${CORRECT_LOG_TYPES[@]}"; do
    echo "   ✅ $type"
done

echo ""

# Verificar valores no YAML
echo "📄 Verificando valores no YAML..."
YAML_FILE="src/main/resources/yaml/META-INF/types/Entity/Filter/AWSFilter/AWSLambdaFilter.yaml"

if [ -f "$YAML_FILE" ]; then
    echo "✅ YAML encontrado: $YAML_FILE"
    
    # Verificar invocationType
    echo ""
    echo "📋 invocationType no YAML:"
    grep -A 5 "invocationType:" "$YAML_FILE" | grep "data:" | while read -r line; do
        value=$(echo "$line" | sed 's/.*data: //')
        echo "   - $value"
    done
    
    # Verificar logType
    echo ""
    echo "📋 logType no YAML:"
    grep -A 5 "logType:" "$YAML_FILE" | grep "data:" | while read -r line; do
        value=$(echo "$line" | sed 's/.*data: //')
        echo "   - $value"
    done
else
    echo "❌ YAML não encontrado: $YAML_FILE"
fi

echo ""

# Verificar valores no XML
echo "📄 Verificando valores no XML..."
XML_FILE="src/main/resources/com/axway/aws/lambda/aws_lambda.xml"

if [ -f "$XML_FILE" ]; then
    echo "✅ XML encontrado: $XML_FILE"
    
    # Verificar invocationType
    echo ""
    echo "📋 invocationType no XML:"
    grep -A 5 "invocationType" "$XML_FILE" | grep "value=" | while read -r line; do
        value=$(echo "$line" | sed 's/.*value="//' | sed 's/".*//')
        echo "   - $value"
    done
    
    # Verificar logType
    echo ""
    echo "📋 logType no XML:"
    grep -A 5 "logType" "$XML_FILE" | grep "value=" | while read -r line; do
        value=$(echo "$line" | sed 's/.*value="//' | sed 's/".*//')
        echo "   - $value"
    done
else
    echo "❌ XML não encontrado: $XML_FILE"
fi

echo ""
echo "🎉 Verificação concluída!"
echo ""
echo "💡 Referência AWS Lambda:"
echo "   - InvocationType: https://docs.aws.amazon.com/lambda/latest/dg/API_Invoke.html"
echo "   - LogType: https://docs.aws.amazon.com/lambda/latest/dg/API_Invoke.html" 