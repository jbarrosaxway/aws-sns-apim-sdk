package com.axway.aws.sns;

import java.security.GeneralSecurityException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.WebIdentityTokenCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.vordel.circuit.CircuitAbortException;
import com.vordel.circuit.Message;
import com.vordel.circuit.MessageProcessor;
import com.vordel.circuit.aws.AWSFactory;
import com.vordel.config.Circuit;
import com.vordel.config.ConfigContext;
import com.vordel.el.Selector;
import com.vordel.es.Entity;
import com.vordel.es.EntityStoreException;
import com.vordel.trace.Trace;
import com.axway.aws.sns.SNSMessageJsonHelper;

/**
 * AWS SNS Message Publisher with optimized IAM Role support
 * 
 * IAM Role Configuration:
 * - "iam" credential type: Uses WebIdentityTokenCredentialsProvider only
 *   - AWS SDK automatically handles IRSA (IAM Roles for Service Accounts) and EC2 Instance Profile
 *   - Reads environment variables (AWS_WEB_IDENTITY_TOKEN_FILE, AWS_ROLE_ARN) internally
 *   - Supports both ServiceAccount tokens and EC2 instance metadata
 * 
 * - "file" credential type: Uses ProfileCredentialsProvider with specified file
 * - "local" credential type: Uses AWSFactory for explicit credentials
 */
public class PublishSNSMessageProcessor extends MessageProcessor {
	
	// Selectors for dynamic field resolution (following Lambda pattern)
	protected Selector<String> topicArn;
	protected Selector<String> awsRegion;
	protected Selector<String> messageSubject;
	protected Selector<String> messageStructure;
	protected Selector<String> messageAttributes;
	protected Selector<Integer> retryDelay;
	protected Selector<String> credentialType;
	protected Selector<Boolean> useIAMRole;
	protected Selector<String> awsCredential;
	protected Selector<String> clientConfiguration;
	protected Selector<String> credentialsFilePath;
	
	// SNS client builder (following Lambda pattern)
	protected AmazonSNSClientBuilder snsClientBuilder;
	
	// Content body selector
	private Selector<String> contentBody = new Selector<>("${content.body}", String.class);

	public PublishSNSMessageProcessor() {
	}

	@Override
	public void filterAttached(ConfigContext ctx, Entity entity) throws EntityStoreException {
		super.filterAttached(ctx, entity);
		
		// Initialize selectors for all fields (following Lambda pattern)
		this.topicArn = new Selector(entity.getStringValue("topicArn"), String.class);
		this.awsRegion = new Selector(entity.getStringValue("awsRegion"), String.class);
		this.messageSubject = new Selector(entity.getStringValue("messageSubject"), String.class);
		this.messageStructure = new Selector(entity.getStringValue("messageStructure"), String.class);
		this.messageAttributes = new Selector(entity.getStringValue("messageAttributes"), String.class);
		this.retryDelay = new Selector(entity.getStringValue("retryDelay"), Integer.class);
		this.credentialType = new Selector(entity.getStringValue("credentialType"), String.class);
		this.useIAMRole = new Selector(entity.getStringValue("useIAMRole"), Boolean.class);
		this.awsCredential = new Selector(entity.getStringValue("awsCredential"), String.class);
		this.clientConfiguration = new Selector(entity.getStringValue("clientConfiguration"), String.class);
		this.credentialsFilePath = new Selector(entity.getStringValue("credentialsFilePath") != null ? entity.getStringValue("credentialsFilePath") : "", String.class);
		
		// Get client configuration (following Lambda pattern exactly)
		Entity clientConfig = ctx.getEntity(entity.getReferenceValue("clientConfiguration"));
		
		// Configure SNS client builder (following Lambda pattern)
		this.snsClientBuilder = getSNSClientBuilder(ctx, entity, clientConfig);
		
		Trace.info("=== SNS Configuration (Following Lambda Pattern) ===");
		Trace.info("Topic ARN: " + (topicArn != null ? topicArn.getLiteral() : "dynamic"));
		Trace.info("Region: " + (awsRegion != null ? awsRegion.getLiteral() : "dynamic"));
		Trace.info("Message Subject: " + (messageSubject != null ? messageSubject.getLiteral() : "dynamic"));
		Trace.info("Message Structure: " + (messageStructure != null ? messageStructure.getLiteral() : "dynamic"));
		Trace.info("Message Attributes: " + (messageAttributes != null ? messageAttributes.getLiteral() : "dynamic"));
		Trace.info("Retry Delay: " + (retryDelay != null ? retryDelay.getLiteral() : "dynamic"));
		Trace.info("Credential Type: " + (credentialType != null ? credentialType.getLiteral() : "dynamic"));
		Trace.info("Use IAM Role: " + (useIAMRole != null ? useIAMRole.getLiteral() : "false"));
		Trace.info("AWS Credential: " + (awsCredential != null ? awsCredential.getLiteral() : "dynamic"));
		Trace.info("Client Configuration: " + (clientConfiguration != null ? clientConfiguration.getLiteral() : "dynamic"));
		Trace.info("Credentials File Path: " + (credentialsFilePath != null ? credentialsFilePath.getLiteral() : "dynamic"));
		Trace.info("Client Config Entity: " + (clientConfig != null ? "configured" : "default"));
	}

	/**
	 * Creates SNS client builder following Lambda pattern exactly
	 */
	private AmazonSNSClientBuilder getSNSClientBuilder(ConfigContext ctx, Entity entity, Entity clientConfig) 
			throws EntityStoreException {
		
		// Get credentials provider based on configuration
		AWSCredentialsProvider credentialsProvider = getCredentialsProvider(ctx, entity);
		
		// Create client builder with credentials and client configuration (following Lambda pattern)
		AmazonSNSClientBuilder builder = AmazonSNSClientBuilder.standard()
			.withCredentials(credentialsProvider);
		
		// Apply client configuration if available (following Lambda pattern exactly)
		if (clientConfig != null) {
			ClientConfiguration clientConfiguration = createClientConfiguration(ctx, clientConfig);
			builder.withClientConfiguration(clientConfiguration);
			Trace.info("Applied custom client configuration");
		} else {
			Trace.debug("Using default client configuration");
		}
		
		return builder;
	}
	
	/**
	 * Gets the appropriate credentials provider based on configuration
	 */
	private AWSCredentialsProvider getCredentialsProvider(ConfigContext ctx, Entity entity) throws EntityStoreException {
		String credentialTypeValue = credentialType.getLiteral();
		Trace.info("=== Credentials Provider Debug ===");
		Trace.info("Credential Type Value: " + credentialTypeValue);
		
		if ("iam".equals(credentialTypeValue)) {
			// Use IAM Role - WebIdentityTokenCredentialsProvider only
			Trace.info("Using IAM Role credentials - WebIdentityTokenCredentialsProvider");
			Trace.info("Credential Type Value: " + credentialTypeValue);
			
			// Debug IRSA configuration
			Trace.info("=== IRSA Debug ===");
			Trace.info("AWS_WEB_IDENTITY_TOKEN_FILE: " + System.getenv("AWS_WEB_IDENTITY_TOKEN_FILE"));
			Trace.info("AWS_ROLE_ARN: " + System.getenv("AWS_ROLE_ARN"));
			Trace.info("AWS_REGION: " + System.getenv("AWS_REGION"));
			
			// Use WebIdentityTokenCredentialsProvider for IAM role
			Trace.info("✅ Using WebIdentityTokenCredentialsProvider for IAM role");
			return new WebIdentityTokenCredentialsProvider();
		} else if ("file".equals(credentialTypeValue)) {
			// Use credentials file
			Trace.info("Credentials Type is 'file', checking credentialsFilePath...");
			String filePath = credentialsFilePath.getLiteral();
			Trace.info("File Path: " + filePath);
			Trace.info("File Path is null: " + (filePath == null));
			Trace.info("File Path is empty: " + (filePath != null && filePath.trim().isEmpty()));
			if (filePath != null && !filePath.trim().isEmpty()) {
				try {
					Trace.info("Using AWS credentials file: " + filePath);
					// Create ProfileCredentialsProvider with file path and default profile
					return new ProfileCredentialsProvider(filePath, "default");
				} catch (Exception e) {
					Trace.error("Error loading credentials file: " + e.getMessage());
					Trace.info("Falling back to DefaultAWSCredentialsProviderChain");
					return new DefaultAWSCredentialsProviderChain();
				}
			} else {
				Trace.info("Credentials file path not specified, using DefaultAWSCredentialsProviderChain");
				return new DefaultAWSCredentialsProviderChain();
			}
		} else {
			// Use explicit credentials via AWSFactory (following Lambda pattern)
			Trace.info("Using explicit AWS credentials via AWSFactory");
			try {
				AWSCredentials awsCredentials = AWSFactory.getCredentials(ctx, entity);
				Trace.info("AWSFactory.getCredentials() successful");
				return getAWSCredentialsProvider(awsCredentials);
			} catch (Exception e) {
				Trace.error("Error getting explicit credentials: " + e.getMessage());
				Trace.info("Falling back to DefaultAWSCredentialsProviderChain");
				return new DefaultAWSCredentialsProviderChain();
			}
		}
	}
	
	/**
	 * Creates ClientConfiguration from entity (following Lambda pattern exactly)
	 */
	private ClientConfiguration createClientConfiguration(ConfigContext ctx, Entity entity) throws EntityStoreException {
		ClientConfiguration clientConfig = new ClientConfiguration();
		
		if (entity == null) {
			Trace.debug("using empty default ClientConfiguration");
			return clientConfig;
		}
		
		// Apply configuration settings (optimized single access pattern)
		try {
			Integer connectionTimeout = entity.getIntegerValue("connectionTimeout");
			if (connectionTimeout != null) {
				clientConfig.setConnectionTimeout(connectionTimeout);
			}
		} catch (Exception e) {
			// Field doesn't exist, skip silently
		}
		
		try {
			Integer maxConnections = entity.getIntegerValue("maxConnections");
			if (maxConnections != null) {
				clientConfig.setMaxConnections(maxConnections);
			}
		} catch (Exception e) {
			// Field doesn't exist, skip silently
		}
		
		try {
			Integer maxErrorRetry = entity.getIntegerValue("maxErrorRetry");
			if (maxErrorRetry != null) {
				clientConfig.setMaxErrorRetry(maxErrorRetry);
			}
		} catch (Exception e) {
			// Field doesn't exist, skip silently
		}
		
		try {
			String protocol = entity.getStringValue("protocol");
			if (protocol != null && !protocol.trim().isEmpty()) {
				clientConfig.setProtocol(Protocol.valueOf(protocol));
			}
		} catch (Exception e) {
			// Field doesn't exist, skip silently
		}
		
		try {
			Integer socketTimeout = entity.getIntegerValue("socketTimeout");
			if (socketTimeout != null) {
				clientConfig.setSocketTimeout(socketTimeout);
			}
		} catch (Exception e) {
			// Field doesn't exist, skip silently
		}
		
		try {
			String userAgent = entity.getStringValue("userAgent");
			if (userAgent != null && !userAgent.trim().isEmpty()) {
				clientConfig.setUserAgent(userAgent);
			}
		} catch (Exception e) {
			// Field doesn't exist, skip silently
		}
		
		try {
			String proxyHost = entity.getStringValue("proxyHost");
			if (proxyHost != null && !proxyHost.trim().isEmpty()) {
				clientConfig.setProxyHost(proxyHost);
			}
		} catch (Exception e) {
			// Field doesn't exist, skip silently
		}
		
		try {
			Integer proxyPort = entity.getIntegerValue("proxyPort");
			if (proxyPort != null) {
				clientConfig.setProxyPort(proxyPort);
			}
		} catch (Exception e) {
			// Field doesn't exist, skip silently
		}
		
		try {
			String proxyUsername = entity.getStringValue("proxyUsername");
			if (proxyUsername != null && !proxyUsername.trim().isEmpty()) {
				clientConfig.setProxyUsername(proxyUsername);
			}
		} catch (Exception e) {
			// Field doesn't exist, skip silently
		}
		
		try {
			byte[] proxyPasswordBytes = ctx.getCipher().decrypt(entity.getEncryptedValue("proxyPassword"));
			clientConfig.setProxyPassword(new String(proxyPasswordBytes));
		} catch (Exception e) {
			// Field doesn't exist or decryption failed, skip silently
		}
		
		try {
			String proxyDomain = entity.getStringValue("proxyDomain");
			if (proxyDomain != null && !proxyDomain.trim().isEmpty()) {
				clientConfig.setProxyDomain(proxyDomain);
			}
		} catch (Exception e) {
			// Field doesn't exist, skip silently
		}
		
		try {
			String proxyWorkstation = entity.getStringValue("proxyWorkstation");
			if (proxyWorkstation != null && !proxyWorkstation.trim().isEmpty()) {
				clientConfig.setProxyWorkstation(proxyWorkstation);
			}
		} catch (Exception e) {
			// Field doesn't exist, skip silently
		}
		
		try {
			Integer socketSendBufferSizeHint = entity.getIntegerValue("socketSendBufferSizeHint");
			Integer socketReceiveBufferSizeHint = entity.getIntegerValue("socketReceiveBufferSizeHint");
			if (socketSendBufferSizeHint != null && socketReceiveBufferSizeHint != null) {
				clientConfig.setSocketBufferSizeHints(socketSendBufferSizeHint, socketReceiveBufferSizeHint);
			}
		} catch (Exception e) {
			// Field doesn't exist, skip silently
		}
		
		return clientConfig;
	}
	

	
	/**
	 * Creates AWSCredentialsProvider (following Lambda pattern)
	 */
	private AWSCredentialsProvider getAWSCredentialsProvider(final AWSCredentials awsCredentials) {
		return new AWSCredentialsProvider() {
			public AWSCredentials getCredentials() {
				return awsCredentials;
			}
			public void refresh() {}
		};
	}

	@Override
	public boolean invoke(Circuit arg0, Message msg) throws CircuitAbortException {
		
		if (snsClientBuilder == null) {
			Trace.error("SNS client builder was not configured");
			msg.put("aws.sns.error", "SNS client builder was not configured");
			return false;
		}
		
		// Get dynamic values using selectors (following Lambda pattern)
		String topicArnValue = topicArn.substitute(msg);
		String regionValue = awsRegion.substitute(msg);
		String messageSubjectValue = messageSubject.substitute(msg);
		String messageStructureValue = messageStructure.substitute(msg);
		String messageAttributesValue = messageAttributes.substitute(msg);
		Integer retryDelayValue = retryDelay.substitute(msg);
		String credentialTypeValue = credentialType.substitute(msg);
		Boolean useIAMRoleValue = useIAMRole.substitute(msg);
		String credentialsFilePathValue = credentialsFilePath.substitute(msg);

		Trace.info("=== SNS Invocation Debug ===");
		Trace.info("Topic ARN: " + topicArnValue);
		Trace.info("Region: " + regionValue);
		Trace.info("Message Subject: " + messageSubjectValue);
		Trace.info("Message Structure: " + messageStructureValue);
		Trace.info("Message Attributes: " + messageAttributesValue);
		Trace.info("Retry Delay: " + retryDelayValue);
		Trace.info("Credential Type: " + credentialTypeValue);
		Trace.info("Use IAM Role: " + useIAMRoleValue);
		Trace.info("Credentials File Path: " + credentialsFilePathValue);
		
		// Set default values
		if (retryDelayValue == null) {
			retryDelayValue = 1000;
		}
		if (credentialTypeValue == null || credentialTypeValue.trim().isEmpty()) {
			credentialTypeValue = "local";
		}
		// Determine IAM Role usage based on credential type
		useIAMRoleValue = "iam".equals(credentialTypeValue);
		
		String body = contentBody.substitute(msg);
		if (body == null || body.trim().isEmpty()) {
			body = "{}";
		}
		
		// Handle JSON message structure format
		if ("json".equalsIgnoreCase(messageStructureValue)) {
			Trace.debug("=== JSON Message Structure Debug ===");
			Trace.debug("messageStructureValue: '" + messageStructureValue + "'");
			Trace.debug("Original body: '" + body + "'");
			body = SNSMessageJsonHelper.formatJsonMessage(body);
			Trace.debug("Formatted message for JSON structure: " + body);
		} else {
			Trace.debug("messageStructureValue is not 'json': '" + messageStructureValue + "'");
		}

		Trace.debug("=== Final Message Debug ===");
		Trace.debug("Final body to be sent: '" + body + "'");
		Trace.debug("Message structure value: '" + messageStructureValue + "'");
		Trace.debug("Subject: '" + messageSubjectValue + "'");
		Trace.debug("Topic ARN: '" + topicArnValue + "'");
		
		Trace.info("Publishing message to SNS with retry...");
		Trace.info("Using IAM Role: " + useIAMRoleValue);
		
		// Debug IRSA during actual invocation
		Trace.info("=== IRSA Debug During Invoke ===");
		Trace.info("AWS_WEB_IDENTITY_TOKEN_FILE: " + System.getenv("AWS_WEB_IDENTITY_TOKEN_FILE"));
		Trace.info("AWS_ROLE_ARN: " + System.getenv("AWS_ROLE_ARN"));
		Trace.info("AWS_REGION: " + System.getenv("AWS_REGION"));
		
		Exception lastException = null;
		
		// Get maxRetries from clientConfiguration (default 3)
		int maxRetriesValue = 3; // Default value
		
		for (int attempt = 1; attempt <= maxRetriesValue; attempt++) {
			try {
				Trace.info("Attempt " + attempt + " of " + maxRetriesValue);
				
				// Create SNS client with region (following Lambda pattern)
				AmazonSNS snsClient = snsClientBuilder.withRegion(regionValue).build();
				
				// Create the publish request
				PublishRequest publishRequest = new PublishRequest()
					.withTopicArn(topicArnValue)
					.withMessage(body)
					.withSubject(messageSubjectValue)
					.withMessageStructure(messageStructureValue.toLowerCase());

				Trace.debug("=== PublishRequest Debug ===");
				Trace.debug("PublishRequest.topicArn: '" + publishRequest.getTopicArn() + "'");
				Trace.debug("PublishRequest.message: '" + publishRequest.getMessage() + "'");
				Trace.debug("PublishRequest.subject: '" + publishRequest.getSubject() + "'");
				Trace.debug("PublishRequest.messageStructure: '" + publishRequest.getMessageStructure() + "'");
				Trace.debug("PublishRequest.messageAttributes: " + publishRequest.getMessageAttributes());
				
				// Publish message to SNS
				PublishResult publishResult = snsClient.publish(publishRequest);
				
				// Process response
				return processPublishResult(publishResult, msg);
				
			} catch (Exception e) {
				lastException = e;
				Trace.error("Attempt " + attempt + " failed: " + e.getMessage());
				
				// Debug the specific error for IRSA issues
				if (e.getMessage().contains("AccessDeniedException")) {
					Trace.error("=== Access Denied Debug ===");
					Trace.error("Error message: " + e.getMessage());
					
					// Check if it's still using node group role
					if (e.getMessage().contains("axway-first-ng-role")) {
						Trace.error("❌ Still using node group role instead of ServiceAccount");
						Trace.error("This indicates IRSA is not properly configured");
					} else if (e.getMessage().contains("axway-sns-role")) {
						Trace.error("✅ Using ServiceAccount role but permission denied");
						Trace.error("This indicates IRSA is working but role lacks permissions");
					}
				}
				
				// If not the last attempt, wait before retrying
				if (attempt < maxRetriesValue) {
					Trace.info("Waiting " + retryDelayValue + "ms before next attempt...");
					try {
						Thread.sleep(retryDelayValue);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						Trace.error("Thread interrupted during retry");
						return false;
					}
				}
			}
		}
		
		// If reached here, all attempts failed
		Trace.error("All " + maxRetriesValue + " attempts failed");
		msg.put("aws.sns.error", "Failure after " + maxRetriesValue + " attempts: " + 
			(lastException != null ? lastException.getMessage() : "Unknown error"));
		return false;
	}
	
	/**
	 * Processes the result of the SNS publish operation
	 */
	private boolean processPublishResult(PublishResult publishResult, Message msg) {
		try {
			String messageId = publishResult.getMessageId();
			
			// === SNS Response ===
			Trace.info("=== SNS Response ===");
			Trace.info("Message ID: " + messageId);
			
			// Store results
			msg.put("aws.sns.message.id", messageId);
			msg.put("aws.sns.response", "Message published successfully");
			
			Trace.info("SNS message published successfully");
			return true;
			
		} catch (Exception e) {
			Trace.error("Error processing SNS response: " + e.getMessage(), e);
			msg.put("aws.sns.error", "Error processing response: " + e.getMessage());
			return false;
		}
	}
} 