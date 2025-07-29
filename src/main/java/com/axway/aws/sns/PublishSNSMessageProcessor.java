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
 * Following Axway standard patterns
 */
public class PublishSNSMessageProcessor extends MessageProcessor {
	
	// SNS client builder following Axway pattern
	protected AmazonSNSClientBuilder snsClientBuilder;
	
	// Selectors following Axway pattern
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
	
	// Content body selector
	private final Selector<String> contentBody = new Selector<>("${content.body}", String.class);

	/**
	 * Default constructor for Axway API Gateway
	 */
	public PublishSNSMessageProcessor() {
	}

	@Override
	public void filterAttached(ConfigContext ctx, Entity entity) throws EntityStoreException {
		super.filterAttached(ctx, entity);
		
		// Initialize selectors following Axway pattern
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
		
		// Get SNS client builder following Axway pattern
		this.snsClientBuilder = getSNSClientBuilder(ctx, entity);
		
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
	}

	/**
	 * Creates SNS client builder following Axway pattern
	 */
	private AmazonSNSClientBuilder getSNSClientBuilder(ConfigContext ctx, Entity entity) throws EntityStoreException {
		// Get client configuration following Axway pattern
		Entity clientConfig = ctx.getEntity(entity.getReferenceValue("clientConfiguration"));
		
		// Use AWSFactory following Axway pattern (similar to S3)
		AWSCredentials awsCredentials = AWSFactory.getCredentials(ctx, entity);
		AWSCredentialsProvider credentialsProvider = new AWSCredentialsProvider() {
			public AWSCredentials getCredentials() {
				return awsCredentials;
			}
			public void refresh() {}
		};
		
		AmazonSNSClientBuilder builder = AmazonSNSClientBuilder.standard()
			.withCredentials(credentialsProvider);
		
		// Apply client configuration if available
		if (clientConfig != null) {
			ClientConfiguration clientConfiguration = new ClientConfiguration();
			// Apply basic configuration settings
			if (clientConfig.containsKey("connectionTimeout")) {
				clientConfiguration.setConnectionTimeout(clientConfig.getIntegerValue("connectionTimeout"));
			}
			if (clientConfig.containsKey("socketTimeout")) {
				clientConfiguration.setSocketTimeout(clientConfig.getIntegerValue("socketTimeout"));
			}
			if (clientConfig.containsKey("maxErrorRetry")) {
				clientConfiguration.setMaxErrorRetry(clientConfig.getIntegerValue("maxErrorRetry"));
			}
			builder.withClientConfiguration(clientConfiguration);
		}
		
		return builder;
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
		Trace.debug("Using IAM Role: " + useIAMRoleValue);
		
		Exception lastException = null;
		
		// Get maxRetries from clientConfiguration (default 3)
		int maxRetriesValue = 3;
		
		// Create SNS client with region following Axway pattern
		snsClientBuilder.withRegion(regionValue);
		AmazonSNS snsClient = snsClientBuilder.build();
		
		for (int attempt = 1; attempt <= maxRetriesValue; attempt++) {
			try {
				Trace.debug("Attempt " + attempt + " of " + maxRetriesValue);
				
				// Create the publish request
				PublishRequest publishRequest = new PublishRequest()
					.withTopicArn(topicArnValue)
					.withMessage(body)
					.withSubject(messageSubjectValue)
					.withMessageStructure(messageStructureValue != null ? messageStructureValue.toLowerCase() : null);

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