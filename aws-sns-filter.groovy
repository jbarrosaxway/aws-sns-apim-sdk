import com.vordel.trace.Trace;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import groovy.json.JsonSlurper;
import java.nio.ByteBuffer;
import java.util.Base64;

def invoke(msg) {
    AmazonSNS amazonSNS = null
    
    try {
        // ========================================
        // ADVANCED DYNAMIC CONFIGURATION
        // ========================================
        
        def topicArn = msg.get("aws.sns.topic.arn")
        def awsRegion = msg.get("aws.sns.region")
        def payload = msg.get("aws.sns.payload") ?: msg.get("content.body") ?: "{}"
        def messageSubject = msg.get("aws.sns.message.subject")
        def messageStructure = msg.get("aws.sns.message.structure")
        
        // Message attributes JSON (prefer messageAttributes; legacy aws.sns.message.attributes)
        def messageAttributes = msg.get("messageAttributes") ?: msg.get("aws.sns.message.attributes")
        def maxRetries = msg.get("aws.sns.max.retries") ?: "3"
        def retryDelay = msg.get("aws.sns.retry.delay.ms") ?: "1000"
        
        // Mandatory validation
        if (!topicArn || topicArn.trim().isEmpty()) {
            Trace.error("SNS topic ARN not specified")
            msg.put("aws.sns.error", "SNS topic ARN not specified")
            return false
        }
        
        // ========================================
        // AWS CONFIGURATION (FLEXIBLE - BOTH METHODS)
        // ========================================
        
        Trace.info("=== SNS Configuration (Advanced - Fixed) ===")
        Trace.info("Topic ARN: " + topicArn)
        Trace.info("Subject: " + (messageSubject ?: "not specified"))
        Trace.info("Region: " + (awsRegion ?: "inferred"))
        Trace.info("Structure: " + (messageStructure ?: "not specified"))
        Trace.info("Max Retries: " + maxRetries)

        // 1. Configure credentials - Multiple strategies
        def credentialsProvider = configureCredentials()
        if (!credentialsProvider) {
            return false
        }
        
        // 2. Create SNS client
        AmazonSNSClientBuilder builder = AmazonSNSClientBuilder.standard()
            .withCredentials(credentialsProvider)
        
        // Use environment variable or message region
        def regionToUse = awsRegion ?: System.getenv("AWS_DEFAULT_REGION")
        if (regionToUse != null && regionToUse.trim() != "") {
            builder = builder.withRegion(regionToUse)
            Trace.info("Using region: " + regionToUse)
        } else {
            Trace.error("AWS region not specified")
            msg.put("aws.sns.error", "AWS region not specified. Set AWS_DEFAULT_REGION or aws.sns.region")
            return false
        }
        
        amazonSNS = builder.build()
        
        // ========================================
        // SNS PUBLISH WITH RETRY
        // ========================================
        
        Trace.info("Publishing message to SNS with retry...")

        Map messageAttributesMap
        try {
            messageAttributesMap = parseMessageAttributesJson(messageAttributes)
        } catch (IllegalArgumentException e) {
            Trace.error("Invalid message attributes: " + e.getMessage())
            msg.put("aws.sns.error", e.getMessage())
            return false
        }
        
        def maxRetriesInt = Integer.parseInt(maxRetries)
        def retryDelayInt = Integer.parseInt(retryDelay)
        def lastException = null
        
        for (int attempt = 1; attempt <= maxRetriesInt; attempt++) {
            try {
                Trace.info("Attempt " + attempt + " of " + maxRetriesInt)
                
                // 3. Create advanced request
                PublishRequest publishRequest = createPublishRequest(
                    topicArn, payload, messageSubject, messageStructure, messageAttributesMap
                )
                
                // 4. Publish message to SNS
                PublishResult publishResult = amazonSNS.publish(publishRequest)
                
                // 5. Process response
                return processPublishResult(publishResult, msg)
                
            } catch (Exception e) {
                lastException = e
                Trace.error("Attempt " + attempt + " failed: " + e.getMessage())
                
                // If not the last attempt, wait before retrying
                if (attempt < maxRetriesInt) {
                    Trace.info("Waiting " + retryDelayInt + "ms before next attempt...")
                    try {
                        Thread.sleep(retryDelayInt)
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt()
                        Trace.error("Thread interrupted during retry")
                        return false
                    }
                }
            }
        }
        
        // If reached here, all attempts failed
        Trace.error("All " + maxRetriesInt + " attempts failed")
        msg.put("aws.sns.error", "Failure after " + maxRetriesInt + " attempts: " + 
            (lastException != null ? lastException.getMessage() : "Unknown error"))
        return false
        
    } catch (Exception e) {
        Trace.error("Error in SNS filter: " + e.getMessage(), e)
        msg.put("aws.sns.error", "Error in SNS filter: " + e.getMessage())
        return false
    } finally {
        if (amazonSNS != null) {
            try {
                amazonSNS.shutdown()
            } catch (Exception e) {
                Trace.error("Error shutting down SNS client: " + e.getMessage())
            }
        }
    }
}

/**
 * Creates PublishRequest with all parameters
 */
def createPublishRequest(topicArn, payload, messageSubject, messageStructure, messageAttributesMap) {
    PublishRequest request = new PublishRequest()
        .withTopicArn(topicArn)
        .withMessage(payload)
    
    // Add subject if specified
    if (messageSubject && !messageSubject.trim().isEmpty()) {
        request.setSubject(messageSubject)
        Trace.info("Using subject: " + messageSubject)
    }
    
    // Add message structure if specified
    if (messageStructure && !messageStructure.trim().isEmpty()) {
        request.setMessageStructure(messageStructure)
        Trace.info("Using message structure: " + messageStructure)
    }
    
    if (messageAttributesMap != null && !messageAttributesMap.isEmpty()) {
        request.setMessageAttributes(messageAttributesMap)
        Trace.info("Using " + messageAttributesMap.size() + " message attribute(s): " + messageAttributesMap.keySet())
    }
    
    return request
}

/**
 * Parses SNS MessageAttributes JSON (AWS map format). Returns null when empty.
 */
def parseMessageAttributesJson(messageAttributesJson) {
    if (messageAttributesJson == null) {
        return null
    }
    def trimmed = messageAttributesJson.toString().trim()
    if (trimmed.isEmpty()) {
        return null
    }

    def parsed = new JsonSlurper().parseText(trimmed)
    if (!(parsed instanceof Map)) {
        throw new IllegalArgumentException("Message attributes JSON must be an object")
    }
    if (parsed.isEmpty()) {
        return null
    }
    if (parsed.size() > 10) {
        throw new IllegalArgumentException("Maximum of 10 attributes reached")
    }

    def attributes = [:]
    parsed.each { name, spec ->
        if (name == null || name.toString().trim().isEmpty()) {
            throw new IllegalArgumentException("Message attribute name cannot be empty")
        }
        if (!(spec instanceof Map)) {
            throw new IllegalArgumentException("Message attribute '${name}' must be an object")
        }
        def dataType = spec.DataType
        if (dataType == null || dataType.toString().trim().isEmpty()) {
            throw new IllegalArgumentException("Message attribute '${name}' requires DataType")
        }

        def attr = new MessageAttributeValue().withDataType(dataType.toString().trim())
        def hasString = spec.containsKey("StringValue") && spec.StringValue != null
        def hasBinary = spec.containsKey("BinaryValue") && spec.BinaryValue != null
        if (!hasString && !hasBinary) {
            throw new IllegalArgumentException("Message attribute '${name}' requires StringValue or BinaryValue")
        }
        if (hasBinary) {
            def binaryText = spec.BinaryValue.toString().trim()
            if (binaryText.isEmpty()) {
                throw new IllegalArgumentException("Message attribute '${name}' BinaryValue cannot be empty")
            }
            attr.withBinaryValue(ByteBuffer.wrap(Base64.decoder.decode(binaryText)))
        }
        if (hasString) {
            attr.withStringValue(spec.StringValue.toString())
        }
        attributes[name.toString()] = attr
    }
    return attributes
}

/**
 * Processes the SNS publish result
 */
def processPublishResult(PublishResult publishResult, msg) {
    try {
        String messageId = publishResult.getMessageId()
        
        // === SNS Response ===
        Trace.info("=== SNS Response ===")
        Trace.info("Message ID: " + messageId)
        
        // Store results
        msg.put("aws.sns.message.id", messageId)
        msg.put("aws.sns.response", "Message published successfully")
        
        Trace.info("SNS message published successfully")
        return true
        
    } catch (Exception e) {
        Trace.error("Error processing SNS response: " + e.getMessage(), e)
        msg.put("aws.sns.error", "Error processing response: " + e.getMessage())
        return false
    }
}

/**
 * Configures AWS credentials using multiple strategies
 */
def configureCredentials() {
    try {
        // Strategy 1: Environment variables
        def accessKey = System.getenv("AWS_ACCESS_KEY_ID")
        def secretKey = System.getenv("AWS_SECRET_ACCESS_KEY")
        def sessionToken = System.getenv("AWS_SESSION_TOKEN")
        
        if (accessKey && secretKey) {
            Trace.info("Using AWS credentials from environment variables")
            if (sessionToken) {
                return new AWSStaticCredentialsProvider(new BasicSessionCredentials(accessKey, secretKey, sessionToken))
            } else {
                return new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey))
            }
        }
        
        // Strategy 2: IAM Role (EC2 Instance Profile or ECS Task Role)
        Trace.info("Trying IAM Role credentials...")
        return new DefaultAWSCredentialsProviderChain()
        
    } catch (Exception e) {
        Trace.error("Error configuring credentials: " + e.getMessage())
        return null
    }
} 