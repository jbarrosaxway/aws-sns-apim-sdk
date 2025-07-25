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
        // ADVANCED DYNAMIC CONFIGURATION
        // ========================================
        
        def functionName = msg.get("aws.lambda.function.name")
        def awsRegion = msg.get("aws.lambda.region")
        def payload = msg.get("aws.lambda.payload") ?: msg.get("content.body") ?: "{}"
        def invocationType = msg.get("aws.lambda.invocation.type") ?: "RequestResponse"
        def logType = msg.get("aws.lambda.log.type") ?: "None"
        
        // Advanced new parameters
        def qualifier = msg.get("aws.lambda.qualifier") // Version or alias
        def clientContextData = msg.get("aws.lambda.client.context") // JSON string
        def customHeaders = msg.get("aws.lambda.custom.headers") // JSON string
        def maxRetries = msg.get("aws.lambda.max.retries") ?: "3"
        def retryDelay = msg.get("aws.lambda.retry.delay.ms") ?: "1000"
        
        // Mandatory validation
        if (!functionName || functionName.trim().isEmpty()) {
            Trace.error("Lambda function name not specified")
            msg.put("aws.lambda.error", "Lambda function name not specified")
            return false
        }
        
        // ========================================
        // AWS CONFIGURATION (FLEXIBLE - BOTH METHODS)
        // ========================================
        
        Trace.info("=== Lambda Configuration (Advanced - Fixed) ===")
        Trace.info("Function: " + functionName)
        Trace.info("Qualifier: " + (qualifier ?: "not specified"))
        Trace.info("Region: " + (awsRegion ?: "inferred"))
        Trace.info("Type: " + invocationType)
        Trace.info("Log Type: " + logType)
        Trace.info("Max Retries: " + maxRetries)

        // 1. Configure credentials - Multiple strategies
        def credentialsProvider = configureCredentials()
        if (!credentialsProvider) {
            return false
        }
        
        // 2. Create Invoke Lambda Function client
        AWSLambdaClientBuilder builder = AWSLambdaClientBuilder.standard()
            .withCredentials(credentialsProvider)
        
        // Use environment variable or message region
        def regionToUse = awsRegion ?: System.getenv("AWS_DEFAULT_REGION")
        if (regionToUse != null && regionToUse.trim() != "") {
            builder = builder.withRegion(regionToUse)
            Trace.info("Using region: " + regionToUse)
        } else {
            Trace.error("AWS region not specified")
            msg.put("aws.lambda.error", "AWS region not specified. Set AWS_DEFAULT_REGION or aws.lambda.region")
            return false
        }
        
        awsLambda = builder.build()
        
        // ========================================
        // LAMBDA INVOCATION WITH RETRY
        // ========================================
        
        Trace.info("Invoking Lambda function with retry...")
        
        def maxRetriesInt = Integer.parseInt(maxRetries)
        def retryDelayInt = Integer.parseInt(retryDelay)
        def lastException = null
        
        for (int attempt = 1; attempt <= maxRetriesInt; attempt++) {
            try {
                Trace.info("Attempt " + attempt + " of " + maxRetriesInt)
                
                // 3. Create advanced request
                InvokeRequest invokeRequest = createInvokeRequest(
                    functionName, payload, invocationType, logType, 
                    qualifier, clientContextData, customHeaders
                )
                
                // 4. Invoke Lambda function
                InvokeResult invokeResult = awsLambda.invoke(invokeRequest)
                
                // 5. Process response
                return processInvokeResult(invokeResult, msg)
                
            } catch (Exception e) {
                lastException = e
                Trace.warn("Attempt " + attempt + " failed: " + e.getMessage())
                
                // If not the last attempt, wait before retrying
                if (attempt < maxRetriesInt) {
                    Trace.info("Waiting " + retryDelayInt + "ms before next attempt...")
                    Thread.sleep(retryDelayInt)
                }
            }
        }
        
        // If reached here, all attempts failed
        Trace.error("All " + maxRetriesInt + " attempts failed")
        msg.put("aws.lambda.error", "Failure after " + maxRetriesInt + " attempts: " + lastException.getMessage())
        return false
        
    } catch (Exception e) {
        Trace.error("Error invoking Lambda: " + e.getMessage(), e)
        msg.put("aws.lambda.error", e.getMessage())
        return false
        
    } finally {
        // Clean up resources
        if (awsLambda != null) {
            try {
                awsLambda.shutdown()
            } catch (Exception e) {
                Trace.error("Error closing AWS client: " + e.getMessage())
            }
        }
    }
}

// ========================================
// HELPER METHODS
// ========================================

def configureCredentials() {
    // Check environment variables
    def envAccessKey = System.getenv("AWS_ACCESS_KEY_ID")
    def envSecretKey = System.getenv("AWS_SECRET_ACCESS_KEY")
    def envSessionToken = System.getenv("AWS_SESSION_TOKEN")
    def envCredentialsFile = System.getenv("AWS_SHARED_CREDENTIALS_FILE")
    def envProfile = System.getenv("AWS_PROFILE") ?: "default"
    
    Trace.info("Checking AWS configuration...")
    Trace.info("AWS_ACCESS_KEY_ID present: " + (envAccessKey != null))
    Trace.info("AWS_SECRET_ACCESS_KEY present: " + (envSecretKey != null))
    Trace.info("AWS_SHARED_CREDENTIALS_FILE: " + (envCredentialsFile ?: "not set"))
    Trace.info("AWS_PROFILE: " + envProfile)
    
    // Strategy 1: Direct environment variables
    if (envAccessKey && envSecretKey) {
        Trace.info("Using environment variable credentials")
        
        if (envSessionToken) {
            def credentials = new BasicSessionCredentials(envAccessKey, envSecretKey, envSessionToken)
            return new AWSStaticCredentialsProvider(credentials)
        } else {
            def credentials = new BasicAWSCredentials(envAccessKey, envSecretKey)
            return new AWSStaticCredentialsProvider(credentials)
        }
    }
    // Strategy 2: Credentials file
    else if (envCredentialsFile) {
        Trace.info("Using credentials file: " + envCredentialsFile)
        
        try {
            def credentialsFile = new File(envCredentialsFile)
            if (credentialsFile.exists()) {
                Trace.info("Credentials file found")
                return new ProfileCredentialsProvider(envCredentialsFile, envProfile)
            } else {
                Trace.error("Credentials file not found: " + envCredentialsFile)
                return null
            }
        } catch (Exception e) {
            Trace.error("Error configuring credentials file: " + e.getMessage())
            return null
        }
    }
    // Strategy 3: Fallback
    else {
        Trace.info("Using DefaultAWSCredentialsProviderChain (fallback)")
        try {
            return new DefaultAWSCredentialsProviderChain()
        } catch (Exception e) {
            Trace.error("Error configuring DefaultAWSCredentialsProviderChain: " + e.getMessage())
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
    
    // Add qualifier if specified
    if (qualifier && qualifier.trim() != "") {
        invokeRequest.setQualifier(qualifier)
        Trace.info("Using qualifier: " + qualifier)
    }
    
    // Add Client Context if specified (without using ClientContext class)
    if (clientContextData && clientContextData.trim() != "") {
        try {
            // As we don't have access to the ClientContext class, we'll just log
            // and include data in the payload if necessary
            Trace.info("Client Context specified (not supported in this version): " + clientContextData)
            Trace.info("To use Client Context, update AWS SDK to a newer version")
            
            // Alternative: include client context data in the payload
            if (payload && payload.trim() != "{}") {
                try {
                    // Try to add client context to the JSON payload
                    def payloadObj = new groovy.json.JsonSlurper().parseText(payload)
                    payloadObj.clientContext = new groovy.json.JsonSlurper().parseText(clientContextData)
                    payload = new groovy.json.JsonOutput().toJson(payloadObj)
                    Trace.info("Client Context added to payload")
                } catch (Exception e) {
                    Trace.warn("Could not add Client Context to payload: " + e.getMessage())
                }
            }
        } catch (Exception e) {
            Trace.warn("Error processing Client Context: " + e.getMessage())
    }
    }
    
    // Add custom headers if specified
    if (customHeaders && customHeaders.trim() != "") {
        try {
            Trace.info("Custom Headers specified: " + customHeaders)
            Trace.info("To use Custom Headers, update AWS SDK to a newer version")
        } catch (Exception e) {
            Trace.warn("Error processing Custom Headers: " + e.getMessage())
        }
    }
    
    return invokeRequest
}

def processInvokeResult(InvokeResult invokeResult, msg) {
    String response = new String(invokeResult.getPayload().array(), "UTF-8")
    int statusCode = invokeResult.getStatusCode()
    
    Trace.info("=== Lambda Response ===")
    Trace.info("Status Code: " + statusCode)
    Trace.info("Response: " + response)
    Trace.info("Executed Version: " + invokeResult.getExecutedVersion())
    
    if (invokeResult.getLogResult()) {
        Trace.info("Log Result: " + invokeResult.getLogResult())
    }
    
    // Store results
    msg.put("aws.lambda.response", response)
    msg.put("aws.lambda.http.status.code", statusCode)
    msg.put("aws.lambda.executed.version", invokeResult.getExecutedVersion())
    msg.put("aws.lambda.log.result", invokeResult.getLogResult())
    
    // Check Lambda function error
    if (invokeResult.getFunctionError() != null) {
        Trace.error("Lambda function error: " + invokeResult.getFunctionError())
        msg.put("aws.lambda.error", invokeResult.getFunctionError())
        msg.put("aws.lambda.function.error", invokeResult.getFunctionError())
        return false
    }
    
    // Check HTTP status code
    if (statusCode >= 400) {
        Trace.error("HTTP error in Lambda invocation: " + statusCode)
        msg.put("aws.lambda.error", "HTTP Error: " + statusCode)
        return false
    }
    
    Trace.info("Lambda invocation successful")
    return true
} 