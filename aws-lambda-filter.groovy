import com.vordel.trace.Trace;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;

def invoke(msg) {
    AWSLambda awsLambda = null
    
    try {
        // ========================================
        // CONFIGURAÇÃO DINÂMICA AVANÇADA
        // ========================================
        
        def functionName = msg.get("aws.lambda.function.name")
        def awsRegion = msg.get("aws.lambda.region")
        def payload = msg.get("aws.lambda.payload") ?: msg.get("content.body") ?: "{}"
        def invocationType = msg.get("aws.lambda.invocation.type") ?: "RequestResponse"
        def logType = msg.get("aws.lambda.log.type") ?: "None"
        
        // Novos parâmetros avançados
        def qualifier = msg.get("aws.lambda.qualifier") // Versão ou alias
        def clientContextData = msg.get("aws.lambda.client.context") // JSON string
        def customHeaders = msg.get("aws.lambda.custom.headers") // JSON string
        def maxRetries = msg.get("aws.lambda.max.retries") ?: "3"
        def retryDelay = msg.get("aws.lambda.retry.delay.ms") ?: "1000"
        
        // Validação obrigatória
        if (!functionName || functionName.trim().isEmpty()) {
            Trace.error("Nome da função Lambda não especificado")
            msg.put("aws.lambda.error", "Nome da função Lambda não especificado")
            return false
        }
        
        // ========================================
        // CONFIGURAÇÃO AWS (FLEXÍVEL - AMBOS OS MÉTODOS)
        // ========================================
        
        Trace.info("=== Configuração Lambda (Avançada - Corrigida) ===")
        Trace.info("Função: " + functionName)
        Trace.info("Qualifier: " + (qualifier ?: "não especificado"))
        Trace.info("Região: " + (awsRegion ?: "inferida"))
        Trace.info("Tipo: " + invocationType)
        Trace.info("Log Type: " + logType)
        Trace.info("Max Retries: " + maxRetries)

        // 1. Configurar credenciais - Múltiplas estratégias
        def credentialsProvider = configureCredentials()
        if (!credentialsProvider) {
            return false
        }
        
        // 2. Criar cliente AWS Lambda
        AWSLambdaClientBuilder builder = AWSLambdaClientBuilder.standard()
            .withCredentials(credentialsProvider)
        
        // Usar região da variável de ambiente ou da mensagem
        def regionToUse = awsRegion ?: System.getenv("AWS_DEFAULT_REGION")
        if (regionToUse != null && regionToUse.trim() != "") {
            builder = builder.withRegion(regionToUse)
            Trace.info("Usando região: " + regionToUse)
        } else {
            Trace.error("Região AWS não especificada")
            msg.put("aws.lambda.error", "Região AWS não especificada. Configure AWS_DEFAULT_REGION ou aws.lambda.region")
            return false
        }
        
        awsLambda = builder.build()
        
        // ========================================
        // INVOCAÇÃO LAMBDA COM RETRY
        // ========================================
        
        Trace.info("Invocando função Lambda com retry...")
        
        def maxRetriesInt = Integer.parseInt(maxRetries)
        def retryDelayInt = Integer.parseInt(retryDelay)
        def lastException = null
        
        for (int attempt = 1; attempt <= maxRetriesInt; attempt++) {
            try {
                Trace.info("Tentativa " + attempt + " de " + maxRetriesInt)
                
                // 3. Criar requisição avançada
                InvokeRequest invokeRequest = createInvokeRequest(
                    functionName, payload, invocationType, logType, 
                    qualifier, clientContextData, customHeaders
                )
                
                // 4. Invocar função Lambda
                InvokeResult invokeResult = awsLambda.invoke(invokeRequest)
                
                // 5. Processar resposta
                return processInvokeResult(invokeResult, msg)
                
            } catch (Exception e) {
                lastException = e
                Trace.warn("Tentativa " + attempt + " falhou: " + e.getMessage())
                
                // Se não é a última tentativa, aguardar antes de tentar novamente
                if (attempt < maxRetriesInt) {
                    Trace.info("Aguardando " + retryDelayInt + "ms antes da próxima tentativa...")
                    Thread.sleep(retryDelayInt)
                }
            }
        }
        
        // Se chegou aqui, todas as tentativas falharam
        Trace.error("Todas as " + maxRetriesInt + " tentativas falharam")
        msg.put("aws.lambda.error", "Falha após " + maxRetriesInt + " tentativas: " + lastException.getMessage())
        return false
        
    } catch (Exception e) {
        Trace.error("Erro ao invocar Lambda: " + e.getMessage(), e)
        msg.put("aws.lambda.error", e.getMessage())
        return false
        
    } finally {
        // Limpar recursos
        if (awsLambda != null) {
            try {
                awsLambda.shutdown()
            } catch (Exception e) {
                Trace.error("Erro ao fechar cliente AWS: " + e.getMessage())
            }
        }
    }
}

// ========================================
// MÉTODOS AUXILIARES
// ========================================

def configureCredentials() {
    // Verificar variáveis de ambiente
    def envAccessKey = System.getenv("AWS_ACCESS_KEY_ID")
    def envSecretKey = System.getenv("AWS_SECRET_ACCESS_KEY")
    def envSessionToken = System.getenv("AWS_SESSION_TOKEN")
    def envCredentialsFile = System.getenv("AWS_SHARED_CREDENTIALS_FILE")
    def envProfile = System.getenv("AWS_PROFILE") ?: "default"
    
    Trace.info("Verificando configuração AWS...")
    Trace.info("AWS_ACCESS_KEY_ID presente: " + (envAccessKey != null))
    Trace.info("AWS_SECRET_ACCESS_KEY presente: " + (envSecretKey != null))
    Trace.info("AWS_SHARED_CREDENTIALS_FILE: " + (envCredentialsFile ?: "não definido"))
    Trace.info("AWS_PROFILE: " + envProfile)
    
    // Estratégia 1: Variáveis de ambiente diretas
    if (envAccessKey && envSecretKey) {
        Trace.info("Usando credenciais de variáveis de ambiente")
        
        if (envSessionToken) {
            def credentials = new BasicSessionCredentials(envAccessKey, envSecretKey, envSessionToken)
            return new AWSStaticCredentialsProvider(credentials)
        } else {
            def credentials = new BasicAWSCredentials(envAccessKey, envSecretKey)
            return new AWSStaticCredentialsProvider(credentials)
        }
    }
    // Estratégia 2: Arquivo de credenciais
    else if (envCredentialsFile) {
        Trace.info("Usando arquivo de credenciais: " + envCredentialsFile)
        
        try {
            def credentialsFile = new File(envCredentialsFile)
            if (credentialsFile.exists()) {
                Trace.info("Arquivo de credenciais encontrado")
                return new ProfileCredentialsProvider(envCredentialsFile, envProfile)
            } else {
                Trace.error("Arquivo de credenciais não encontrado: " + envCredentialsFile)
                return null
            }
        } catch (Exception e) {
            Trace.error("Erro ao configurar arquivo de credenciais: " + e.getMessage())
            return null
        }
    }
    // Estratégia 3: Fallback
    else {
        Trace.info("Usando DefaultAWSCredentialsProviderChain (fallback)")
        try {
            return new DefaultAWSCredentialsProviderChain()
        } catch (Exception e) {
            Trace.error("Erro ao configurar DefaultAWSCredentialsProviderChain: " + e.getMessage())
            return null
        }
    }
}

def createInvokeRequest(functionName, payload, invocationType, logType, qualifier, clientContextData, customHeaders) {
    InvokeRequest invokeRequest = new InvokeRequest()
        .withFunctionName(functionName)
        .withPayload(payload)
        .withInvocationType(invocationType)
        .withLogType(logType)
    
    // Adicionar qualifier se especificado
    if (qualifier && qualifier.trim() != "") {
        invokeRequest.setQualifier(qualifier)
        Trace.info("Usando qualifier: " + qualifier)
    }
    
    // Adicionar Client Context se especificado (sem usar a classe ClientContext)
    if (clientContextData && clientContextData.trim() != "") {
        try {
            // Como não temos acesso à classe ClientContext, vamos apenas logar
            // e incluir os dados no payload se necessário
            Trace.info("Client Context especificado (não suportado nesta versão): " + clientContextData)
            Trace.info("Para usar Client Context, atualize o AWS SDK para uma versão mais recente")
            
            // Alternativa: incluir dados do client context no payload
            if (payload && payload.trim() != "{}") {
                try {
                    // Tentar adicionar client context ao payload JSON
                    def payloadObj = new groovy.json.JsonSlurper().parseText(payload)
                    payloadObj.clientContext = new groovy.json.JsonSlurper().parseText(clientContextData)
                    payload = new groovy.json.JsonOutput().toJson(payloadObj)
                    Trace.info("Client Context adicionado ao payload")
                } catch (Exception e) {
                    Trace.warn("Não foi possível adicionar Client Context ao payload: " + e.getMessage())
                }
            }
        } catch (Exception e) {
            Trace.warn("Erro ao processar Client Context: " + e.getMessage())
    }
    }
    
    // Adicionar custom headers se especificado
    if (customHeaders && customHeaders.trim() != "") {
        try {
            Trace.info("Custom Headers especificado: " + customHeaders)
            Trace.info("Para usar Custom Headers, atualize o AWS SDK para uma versão mais recente")
        } catch (Exception e) {
            Trace.warn("Erro ao processar Custom Headers: " + e.getMessage())
        }
    }
    
    return invokeRequest
}

def processInvokeResult(InvokeResult invokeResult, msg) {
    String response = new String(invokeResult.getPayload().array(), "UTF-8")
    int statusCode = invokeResult.getStatusCode()
    
    Trace.info("=== Resposta Lambda ===")
    Trace.info("Status Code: " + statusCode)
    Trace.info("Response: " + response)
    Trace.info("Executed Version: " + invokeResult.getExecutedVersion())
    
    if (invokeResult.getLogResult()) {
        Trace.info("Log Result: " + invokeResult.getLogResult())
    }
    
    // Armazenar resultados
    msg.put("aws.lambda.response", response)
    msg.put("aws.lambda.http.status.code", statusCode)
    msg.put("aws.lambda.executed.version", invokeResult.getExecutedVersion())
    msg.put("aws.lambda.log.result", invokeResult.getLogResult())
    
    // Verificar erro da função Lambda
    if (invokeResult.getFunctionError() != null) {
        Trace.error("Erro na função Lambda: " + invokeResult.getFunctionError())
        msg.put("aws.lambda.error", invokeResult.getFunctionError())
        msg.put("aws.lambda.function.error", invokeResult.getFunctionError())
        return false
    }
    
    // Verificar status code HTTP
    if (statusCode >= 400) {
        Trace.error("Erro HTTP na invocação Lambda: " + statusCode)
        msg.put("aws.lambda.error", "Erro HTTP: " + statusCode)
        return false
    }
    
    Trace.info("Invocação Lambda realizada com sucesso")
    return true
} 