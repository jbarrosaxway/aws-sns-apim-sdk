#!/usr/bin/env groovy

// Script de teste para verificar a lógica de substituição do arquivo de internacionalização

def testInternationalizationLogic() {
    println "🧪 Testando lógica de substituição do arquivo de internacionalização..."
    
    // Criar arquivo de teste com conteúdo duplicado (problema atual)
    def testContent = """
- type: InternationalizationFilter
  fields:
    fatal: "Error in the AWS Lambda  Error:\\n\\t\\t\\t\\t\\t\\${circuit.exception}"
    failure: Failed in the AWS Lambda filter
    success: Success in the AWS Lambda filter
- type: InternationalizationFilter
  fields:
    type: AWSLambdaFilter
  logging:
    fatal: "Error in the AWS Lambda  Error:\\n\\t\\t\\t\\t\\t\\${circuit.exception}"
    failure: Failed in the AWS Lambda filter
    success: Success in the AWS Lambda filter
"""
    
    def testFile = new File("test-internationalization.yaml")
    testFile.text = testContent
    
    // Conteúdo novo para substituir
    def newContent = """
- type: InternationalizationFilter
  fields:
    type: AWSLambdaFilter
  logging:
    fatal: "Error in the AWS Lambda Error (CORRIGIDO):\\n\\t\\t\\t\\t\\t\\${circuit.exception}"
    failure: Failed in the AWS Lambda filter (CORRIGIDO)
    success: Success in the AWS Lambda filter (CORRIGIDO)
"""
    
    def newContentFile = new File("test-new-content.yaml")
    newContentFile.text = newContent
    
    println "📄 Arquivo original criado:"
    println testFile.text
    println "---"
    
    // Aplicar a lógica de substituição corrigida
    def destContent = testFile.text
    def sourceContent = newContentFile.text.trim()
    
    if (destContent.contains("type: AWSLambdaFilter")) {
        println "🔄 Filtro AWSLambdaFilter já existe. Substituindo..."
        
        // Remover a seção existente do AWSLambdaFilter
        def lines = destContent.split('\n')
        def newLines = []
        def skipSection = false
        def indentLevel = 0
        def inAWSLambdaSection = false
        
        for (int i = 0; i < lines.length; i++) {
            def line = lines[i]
            def trimmedLine = line.trim()
            def currentIndent = line.length() - line.trim().length()
            
            // Detectar início da seção InternationalizationFilter que contém AWSLambdaFilter
            if (trimmedLine == "type: InternationalizationFilter") {
                // Verificar se a próxima seção contém AWSLambdaFilter
                def nextLines = lines[i..Math.min(i+10, lines.length-1)]
                def hasAWSLambdaFilter = nextLines.any { it.trim() == "type: AWSLambdaFilter" }
                
                if (hasAWSLambdaFilter) {
                    skipSection = true
                    inAWSLambdaSection = true
                    indentLevel = currentIndent
                    println "   🔍 Encontrada seção InternationalizationFilter com AWSLambdaFilter"
                    continue
                }
            }
            
            // Detectar fim da seção
            if (skipSection && inAWSLambdaSection) {
                // Se encontrou um item no mesmo nível ou superior, é o fim da seção
                if (currentIndent <= indentLevel && trimmedLine != "" && !trimmedLine.startsWith("  ")) {
                    skipSection = false
                    inAWSLambdaSection = false
                    println "   🔍 Fim da seção detectado: $trimmedLine"
                    // Não adicionar esta linha, pois é o início da próxima seção
                    continue
                } else {
                    // Ainda dentro da seção InternationalizationFilter, pular
                    println "   ⏭️  Pulando linha: $trimmedLine"
                    continue
                }
            }
            
            newLines.add(line)
        }
        
        // Adicionar o novo conteúdo
        def updatedContent = newLines.join('\n')
        if (updatedContent.endsWith('\n')) {
            testFile.text = updatedContent + sourceContent
        } else {
            testFile.text = updatedContent + '\n' + sourceContent
        }
        
        println "✅ Seção AWSLambdaFilter atualizada!"
    }
    
    println "📄 Arquivo após substituição:"
    println testFile.text
    
    // Limpar arquivos de teste
    testFile.delete()
    newContentFile.delete()
    
    println "🧹 Arquivos de teste removidos"
}

// Executar teste
testInternationalizationLogic() 