package com.axway.aws.sns;

import java.security.GeneralSecurityException;
import java.nio.charset.StandardCharsets;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.EC2ContainerCredentialsProviderWrapper;
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
 * AWS SNS Message Publisher Processor
 * Thread-safe and optimized for performance
 */
public class PublishSNSMessageProcessor extends MessageProcessor {
	
	// Selectors for dynamic field resolution
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
	
	// SNS client builder - created once and reused
	protected AmazonSNSClientBuilder snsClientBuilder;
	
	// Content body selector
	private final Selector<String> contentBody = new Selector<>("${content.body}", String.class);
	
	// Default configuration values
	private static final int DEFAULT_RETRY_DELAY = 1000;
	private static final int DEFAULT_MAX_RETRIES = 3;
	private static final String DEFAULT_CREDENTIAL_TYPE = "local";
	private static final String DEFAULT_MESSAGE_BODY = "{}";

	/**
	 * Default constructor for Axway API Gateway
	 */
	public PublishSNSMessageProcessor() {
	}

	@Override
	public void filterAttached(ConfigContext ctx, Entity entity) throws EntityStoreException {
		super.filterAttached(ctx, entity);
		
		// Initialize selectors for all fields
		this.topicArn = new Selector<String>(entity.getStringValue("topicArn"), String.class);
		this.awsRegion = new Selector<String>(entity.getStringValue("awsRegion"), String.class);
		this.messageSubject = new Selector<String>(entity.getStringValue("messageSubject"), String.class);
		this.messageStructure = new Selector<String>(entity.getStringValue("messageStructure"), String.class);
		this.messageAttributes = new Selector<String>(entity.getStringValue("messageAttributes"), String.class);
		this.retryDelay = new Selector<Integer>(entity.getStringValue("retryDelay"), Integer.class);
		this.credentialType = new Selector<String>(entity.getStringValue("credentialType"), String.class);
		this.useIAMRole = new Selector<Boolean>(entity.getStringValue("useIAMRole"), Boolean.class);
		this.awsCredential = new Selector<String>(entity.getStringValue("awsCredential"), String.class);
		this.clientConfiguration = new Selector<String>(entity.getStringValue("clientConfiguration"), String.class);
		this.credentialsFilePath = new Selector<String>(entity.getStringValue("credentialsFilePath") != null ? entity.getStringValue("credentialsFilePath") : "", String.class);
		
		// Get client configuration
		Entity clientConfig = ctx.getEntity(entity.getReferenceValue("clientConfiguration"));
		
		// Configure SNS client builder
		this.snsClientBuilder = getSNSClientBuilder(ctx, entity, clientConfig);
		
		Trace.debug("=== SNS Configuration ===");
		Trace.debug("Topic ARN: " + (topicArn != null ? topicArn.getLiteral() : "dynamic"));
		Trace.debug("Region: " + (awsRegion != null ? awsRegion.getLiteral() : "dynamic"));
		Trace.debug("Message Subject: " + (messageSubject != null ? messageSubject.getLiteral() : "dynamic"));
		Trace.debug("Message Structure: " + (messageStructure != null ? messageStructure.getLiteral() : "dynamic"));
		Trace.debug("Message Attributes: " + (messageAttributes != null ? messageAttributes.getLiteral() : "dynamic"));
		Trace.debug("Retry Delay: " + (retryDelay != null ? retryDelay.getLiteral() : "dynamic"));
		Trace.debug("Credential Type: " + (credentialType != null ? credentialType.getLiteral() : "dynamic"));
		Trace.debug("Use IAM Role: " + (useIAMRole != null ? useIAMRole.getLiteral() : "false"));
		Trace.debug("AWS Credential: " + (awsCredential != null ? awsCredential.getLiteral() : "dynamic"));
		Trace.debug("Client Configuration: " + (clientConfiguration != null ? clientConfiguration.getLiteral() : "dynamic"));
		Trace.debug("Credentials File Path: " + (credentialsFilePath != null ? credentialsFilePath.getLiteral() : "dynamic"));
		Trace.debug("Client Config Entity: " + (clientConfig != null ? "configured" : "default"));
	}

	/**
	 * Creates SNS client builder with optimized configuration
	 */
	private AmazonSNSClientBuilder getSNSClientBuilder(ConfigContext ctx, Entity entity, Entity clientConfig) 
			throws EntityStoreException {
		
		// Get credentials provider based on configuration
		AWSCredentialsProvider credentialsProvider = getCredentialsProvider(ctx, entity);
		
		// Create client builder with credentials and client configuration
		AmazonSNSClientBuilder builder = AmazonSNSClientBuilder.standard()
			.withCredentials(credentialsProvider);
		
		// Apply client configuration if available
		if (clientConfig != null) {
			ClientConfiguration clientConfiguration = createClientConfiguration(ctx, clientConfig);
			builder.withClientConfiguration(clientConfiguration);
			Trace.debug("Applied custom client configuration");
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
		Trace.debug("=== Credentials Provider Debug ===");
		Trace.debug("Credential Type Value: " + credentialTypeValue);
		
		if ("iam".equals(credentialTypeValue)) {
			// Use IAM Role (EC2 Instance Profile or ECS Task Role)
			Trace.debug("Using IAM Role credentials (Instance Profile/Task Role)");
			return new EC2ContainerCredentialsProviderWrapper();
		} else if ("file".equals(credentialTypeValue)) {
			// Use credentials file
			Trace.debug("Credentials Type is 'file', checking credentialsFilePath...");
			String filePath = credentialsFilePath.getLiteral();
			Trace.debug("File Path: " + filePath);
			Trace.debug("File Path is null: " + (filePath == null));
			Trace.debug("File Path is empty: " + (filePath != null && filePath.trim().isEmpty()));
			if (filePath != null && !filePath.trim().isEmpty()) {
				try {
					Trace.debug("Using AWS credentials file: " + filePath);
					// Create ProfileCredentialsProvider with file path and default profile
					return new ProfileCredentialsProvider(filePath, "default");
				} catch (Exception e) {
					Trace.error("Error loading credentials file: " + e.getMessage());
					Trace.debug("Falling back to DefaultAWSCredentialsProviderChain");
					return new DefaultAWSCredentialsProviderChain();
				}
			} else {
				Trace.debug("Credentials file path not specified, using DefaultAWSCredentialsProviderChain");
				return new DefaultAWSCredentialsProviderChain();
			}
		} else {
			// Use explicit credentials via AWSFactory
			Trace.debug("Using explicit AWS credentials via AWSFactory");
			try {
				AWSCredentials awsCredentials = AWSFactory.getCredentials(ctx, entity);
				Trace.debug("AWSFactory.getCredentials() successful");
				return getAWSCredentialsProvider(awsCredentials);
			} catch (Exception e) {
				Trace.error("Error getting explicit credentials: " + e.getMessage());
				Trace.debug("Falling back to DefaultAWSCredentialsProviderChain");
				return new DefaultAWSCredentialsProviderChain();
			}
		}
	}
	
	/**
	 * Creates ClientConfiguration from entity with optimized access patterns
	 */
	private ClientConfiguration createClientConfiguration(ConfigContext ctx, Entity entity) throws EntityStoreException {
		ClientConfiguration clientConfig = new ClientConfiguration();
		
		if (entity == null) {
			Trace.debug("Using empty default ClientConfiguration");
			return clientConfig;
		}
		
		// Apply configuration settings using optimized helper methods
		setIntegerConfig(clientConfig, entity, "connectionTimeout", ClientConfiguration::setConnectionTimeout);
		setIntegerConfig(clientConfig, entity, "maxConnections", ClientConfiguration::setMaxConnections);
		setIntegerConfig(clientConfig, entity, "maxErrorRetry", ClientConfiguration::setMaxErrorRetry);
		setIntegerConfig(clientConfig, entity, "socketTimeout", ClientConfiguration::setSocketTimeout);
		setIntegerConfig(clientConfig, entity, "proxyPort", ClientConfiguration::setProxyPort);
		setIntegerConfig(clientConfig, entity, "socketSendBufferSizeHint", (config, value) -> {
			Integer receiveValue = getIntegerValue(entity, "socketReceiveBufferSizeHint");
			if (receiveValue != null) {
				config.setSocketBufferSizeHints(value, receiveValue);
			}
		});
		
		setStringConfig(clientConfig, entity, "protocol", (config, value) -> {
			try {
				config.setProtocol(Protocol.valueOf(value));
			} catch (IllegalArgumentException e) {
				Trace.error("Invalid protocol value: " + value);
			}
		});
		setStringConfig(clientConfig, entity, "userAgent", ClientConfiguration::setUserAgent);
		setStringConfig(clientConfig, entity, "proxyHost", ClientConfiguration::setProxyHost);
		setStringConfig(clientConfig, entity, "proxyUsername", ClientConfiguration::setProxyUsername);
		setStringConfig(clientConfig, entity, "proxyDomain", ClientConfiguration::setProxyDomain);
		setStringConfig(clientConfig, entity, "proxyWorkstation", ClientConfiguration::setProxyWorkstation);
		
		// Handle encrypted proxy password
		setEncryptedConfig(clientConfig, ctx, entity, "proxyPassword", ClientConfiguration::setProxyPassword);
		
		return clientConfig;
	}
	
	/**
	 * Helper method to set integer configuration values
	 */
	private void setIntegerConfig(ClientConfiguration config, Entity entity, String fieldName, 
			java.util.function.BiConsumer<ClientConfiguration, Integer> setter) {
		Integer value = getIntegerValue(entity, fieldName);
		if (value != null) {
			setter.accept(config, value);
		}
	}
	
	/**
	 * Helper method to set string configuration values
	 */
	private void setStringConfig(ClientConfiguration config, Entity entity, String fieldName, 
			java.util.function.BiConsumer<ClientConfiguration, String> setter) {
		String value = getStringValue(entity, fieldName);
		if (value != null && !value.trim().isEmpty()) {
			setter.accept(config, value);
		}
	}
	
	/**
	 * Helper method to set encrypted configuration values
	 */
	private void setEncryptedConfig(ClientConfiguration config, ConfigContext ctx, Entity entity, String fieldName, 
			java.util.function.BiConsumer<ClientConfiguration, String> setter) {
		String value = getStringValue(entity, fieldName);
		if (value != null && !value.trim().isEmpty()) {
			try {
				byte[] decryptedBytes = ctx.getCipher().decrypt(entity.getEncryptedValue(fieldName));
				String decryptedValue = new String(decryptedBytes, StandardCharsets.UTF_8);
				setter.accept(config, decryptedValue);
			} catch (GeneralSecurityException e) {
				Trace.error("Error decrypting " + fieldName + ": " + e.getMessage());
			}
		}
	}
	
	/**
	 * Optimized method to get integer value with single access
	 */
	private Integer getIntegerValue(Entity entity, String fieldName) {
		String valueStr = getStringValue(entity, fieldName);
		if (valueStr != null && !valueStr.trim().isEmpty()) {
			try {
				return Integer.valueOf(valueStr.trim());
			} catch (NumberFormatException e) {
				Trace.error("Invalid " + fieldName + " value: " + valueStr);
			}
		}
		return null;
	}
	
	/**
	 * Optimized method to get string value with single access
	 */
	private String getStringValue(Entity entity, String fieldName) {
		if (!entity.containsKey(fieldName)) {
			return null;
		}
		return entity.getStringValue(fieldName);
	}
	
	/**
	 * Creates AWSCredentialsProvider
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
		
		// Get dynamic values using selectors
		String topicArnValue = topicArn.substitute(msg);
		String regionValue = awsRegion.substitute(msg);
		String messageSubjectValue = messageSubject.substitute(msg);
		String messageStructureValue = messageStructure.substitute(msg);
		String messageAttributesValue = messageAttributes.substitute(msg);
		Integer retryDelayValue = retryDelay.substitute(msg);
		String credentialTypeValue = credentialType.substitute(msg);
		Boolean useIAMRoleValue = useIAMRole.substitute(msg);
		String credentialsFilePathValue = credentialsFilePath.substitute(msg);

		Trace.debug("=== SNS Invocation Debug ===");
		Trace.debug("Topic ARN: " + topicArnValue);
		Trace.debug("Region: " + regionValue);
		Trace.debug("Message Subject: " + messageSubjectValue);
		Trace.debug("Message Structure: " + messageStructureValue);
		Trace.debug("Message Attributes: " + messageAttributesValue);
		Trace.debug("Retry Delay: " + retryDelayValue);
		Trace.debug("Credential Type: " + credentialTypeValue);
		Trace.debug("Use IAM Role: " + useIAMRoleValue);
		Trace.debug("Credentials File Path: " + credentialsFilePathValue);
		
		// Set default values
		if (retryDelayValue == null) {
			retryDelayValue = DEFAULT_RETRY_DELAY;
		}
		if (credentialTypeValue == null || credentialTypeValue.trim().isEmpty()) {
			credentialTypeValue = DEFAULT_CREDENTIAL_TYPE;
		}
		// Determine IAM Role usage based on credential type
		useIAMRoleValue = "iam".equals(credentialTypeValue);
		
		String body = contentBody.substitute(msg);
		if (body == null || body.trim().isEmpty()) {
			body = DEFAULT_MESSAGE_BODY;
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
		Trace.debug("Using IAM Role: " + useIAMRoleValue);
		
		Exception lastException = null;
		
		// Get maxRetries from clientConfiguration (default 3)
		int maxRetriesValue = DEFAULT_MAX_RETRIES;
		
		// Create SNS client once and reuse
		AmazonSNS snsClient = snsClientBuilder.withRegion(regionValue).build();
		
		// Prepare message body once
		final String finalBody = body;
		final String finalMessageStructure = messageStructureValue != null ? messageStructureValue.toLowerCase() : null;
		
		for (int attempt = 1; attempt <= maxRetriesValue; attempt++) {
			try {
				Trace.debug("Attempt " + attempt + " of " + maxRetriesValue);
				
				// Create the publish request
				PublishRequest publishRequest = new PublishRequest()
					.withTopicArn(topicArnValue)
					.withMessage(finalBody)
					.withSubject(messageSubjectValue)
					.withMessageStructure(finalMessageStructure);

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
				
				// If not the last attempt, wait before retrying
				if (attempt < maxRetriesValue) {
					Trace.debug("Waiting " + retryDelayValue + "ms before next attempt...");
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