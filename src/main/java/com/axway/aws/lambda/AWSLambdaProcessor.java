package com.axway.aws.lambda;

import java.security.GeneralSecurityException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
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
import java.io.File;

public class AWSLambdaProcessor extends MessageProcessor {
	
	private String functionName;
	private AWSLambda awsLambda;
	private String awsRegion;
	private String invocationType;
	private String logType;
	private String qualifier;
	private String maxRetries;
	private String retryDelay;
	
	private Selector<String> contentBody = new Selector<>("${content.body}", String.class);

	public AWSLambdaProcessor() {
	}

	@Override
	public void filterAttached(ConfigContext ctx, com.vordel.es.Entity entity) throws EntityStoreException {
		super.filterAttached(ctx, entity);
		
		// Configurações básicas
		functionName = new Selector<>(entity.getStringValue("functionName"), String.class).getLiteral();
		awsRegion = new Selector<>(entity.getStringValue("awsRegion"), String.class).getLiteral();
		invocationType = new Selector<>(entity.getStringValue("invocationType"), String.class).getLiteral();
		logType = new Selector<>(entity.getStringValue("logType"), String.class).getLiteral();
		qualifier = new Selector<>(entity.getStringValue("qualifier"), String.class).getLiteral();
		maxRetries = new Selector<>(entity.getStringValue("maxRetries"), String.class).getLiteral();
		retryDelay = new Selector<>(entity.getStringValue("retryDelay"), String.class).getLiteral();
		
		// Valores padrão
		if (invocationType == null || invocationType.trim().isEmpty()) {
			invocationType = "RequestResponse";
		}
		if (logType == null || logType.trim().isEmpty()) {
			logType = "None";
		}
		if (maxRetries == null || maxRetries.trim().isEmpty()) {
			maxRetries = "3";
		}
		if (retryDelay == null || retryDelay.trim().isEmpty()) {
			retryDelay = "1000";
		}
		
		// Basic settings
		// Default values
		// Possible types: RequestResponse, Event, DryRun
		Trace.info("=== Lambda Configuration (Java Filter) ===");
		Trace.info("Function: " + functionName);
		Trace.info("Region: " + (awsRegion != null ? awsRegion : "inferred"));
		Trace.info("Type: " + invocationType);
		Trace.info("Log Type: " + logType);
		Trace.info("Max Retries: " + maxRetries);
		Trace.info("Retry Delay: " + retryDelay + "ms");
		
		// Configure AWS credentials
		AWSCredentialsProvider credentialsProvider = configureCredentials();
		if (credentialsProvider == null) {
			Trace.error("Could not configure AWS credentials");
			return;
		}
		
		// Criar cliente AWS Lambda
		AWSLambdaClientBuilder builder = AWSLambdaClientBuilder.standard()
			.withCredentials(credentialsProvider);
		
		// Use region from configuration or environment variable
		String regionToUse = awsRegion;
		if (regionToUse == null || regionToUse.trim().isEmpty()) {
			regionToUse = System.getenv("AWS_DEFAULT_REGION");
		}
		
		if (regionToUse != null && !regionToUse.trim().isEmpty()) {
			builder = builder.withRegion(regionToUse);
			Trace.info("Using region: " + regionToUse);
		} else {
			Trace.error("AWS region not specified");
			return;
		}
		
		awsLambda = builder.build();
		Trace.info("AWS Lambda client successfully configured");
	}

	@Override
	public boolean invoke(Circuit arg0, Message msg) throws CircuitAbortException {
		
		if (awsLambda == null) {
			Trace.error("AWS Lambda client was not configured");
			msg.put("aws.lambda.error", "AWS Lambda client was not configured");
			return false;
		}
		
		String body = contentBody.substitute(msg);
		if (body == null || body.trim().isEmpty()) {
			body = "{}";
		}
		
		Trace.info("Invoking Lambda function with retry...");
		
		int maxRetriesInt = Integer.parseInt(maxRetries);
		int retryDelayInt = Integer.parseInt(retryDelay);
		Exception lastException = null;
		
		for (int attempt = 1; attempt <= maxRetriesInt; attempt++) {
			try {
				Trace.info("Attempt " + attempt + " of " + maxRetriesInt);
				
				// Create request
				InvokeRequest invokeRequest = new InvokeRequest()
					.withFunctionName(functionName)
					.withPayload(body)
					.withInvocationType(invocationType)
					.withLogType(logType);
				
				// Add qualifier if specified
				if (qualifier != null && !qualifier.trim().isEmpty()) {
					invokeRequest.setQualifier(qualifier);
					Trace.info("Using qualifier: " + qualifier);
				}
				
				// Invoke Lambda function
				InvokeResult invokeResult = awsLambda.invoke(invokeRequest);
				
				// Process response
				return processInvokeResult(invokeResult, msg);
				
			} catch (Exception e) {
				lastException = e;
				Trace.error("Attempt " + attempt + " failed: " + e.getMessage());
				
				// If not the last attempt, wait before retrying
				if (attempt < maxRetriesInt) {
					Trace.info("Waiting " + retryDelayInt + "ms before next attempt...");
					try {
						Thread.sleep(retryDelayInt);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						Trace.error("Thread interrupted during retry");
						return false;
					}
				}
			}
		}
		
		// If reached here, all attempts failed
		Trace.error("All " + maxRetriesInt + " attempts failed");
		msg.put("aws.lambda.error", "Failure after " + maxRetriesInt + " attempts: " + 
			(lastException != null ? lastException.getMessage() : "Unknown error"));
		return false;
	}
	
	/**
	 * Configures AWS credentials using multiple strategies
	 */
	private AWSCredentialsProvider configureCredentials() {
		// Check environment variables
		String envAccessKey = System.getenv("AWS_ACCESS_KEY_ID");
		String envSecretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
		String envSessionToken = System.getenv("AWS_SESSION_TOKEN");
		String envCredentialsFile = System.getenv("AWS_SHARED_CREDENTIALS_FILE");
		String envProfile = System.getenv("AWS_PROFILE");
		if (envProfile == null || envProfile.trim().isEmpty()) {
			envProfile = "default";
		}
		
		// Strategy 1: Direct environment variables
		if (envAccessKey != null && envSecretKey != null) {
			Trace.info("Using environment variable credentials");
			
			if (envSessionToken != null && !envSessionToken.trim().isEmpty()) {
				BasicSessionCredentials credentials = new BasicSessionCredentials(envAccessKey, envSecretKey, envSessionToken);
				return new AWSStaticCredentialsProvider(credentials);
			} else {
				BasicAWSCredentials credentials = new BasicAWSCredentials(envAccessKey, envSecretKey);
				return new AWSStaticCredentialsProvider(credentials);
			}
		}
		// Strategy 2: Credentials file
		else if (envCredentialsFile != null && !envCredentialsFile.trim().isEmpty()) {
			Trace.info("Using credentials file: " + envCredentialsFile);
			
			try {
				File credentialsFile = new File(envCredentialsFile);
				if (credentialsFile.exists()) {
					Trace.info("Credentials file found");
					return new ProfileCredentialsProvider(envCredentialsFile, envProfile);
				} else {
					Trace.error("Credentials file not found: " + envCredentialsFile);
					return null;
				}
			} catch (Exception e) {
				Trace.error("Error configuring credentials file: " + e.getMessage());
				return null;
			}
		}
		// Strategy 3: Fallback to DefaultAWSCredentialsProviderChain
		else {
			Trace.info("Using DefaultAWSCredentialsProviderChain (fallback)");
			try {
				return new DefaultAWSCredentialsProviderChain();
			} catch (Exception e) {
				Trace.error("Error configuring DefaultAWSCredentialsProviderChain: " + e.getMessage());
				return null;
			}
		}
	}
	
	/**
	 * Processes the result of the Lambda invocation
	 */
	private boolean processInvokeResult(InvokeResult invokeResult, Message msg) {
		try {
			String response = new String(invokeResult.getPayload().array(), "UTF-8");
			int statusCode = invokeResult.getStatusCode();
			
			// === Lambda Response ===
			Trace.info("=== Lambda Response ===");
			Trace.info("Status Code: " + statusCode);
			Trace.info("Response: " + response);
			Trace.info("Executed Version: " + invokeResult.getExecutedVersion());
			
			if (invokeResult.getLogResult() != null) {
				Trace.info("Log Result: " + invokeResult.getLogResult());
			}
			
			// Store results
			msg.put("aws.lambda.response", response);
			msg.put("aws.lambda.http.status.code", statusCode);
			msg.put("aws.lambda.executed.version", invokeResult.getExecutedVersion());
			msg.put("aws.lambda.log.result", invokeResult.getLogResult());
			
			// Check Lambda function error
			if (invokeResult.getFunctionError() != null) {
				Trace.error("Lambda function error: " + invokeResult.getFunctionError());
				msg.put("aws.lambda.error", invokeResult.getFunctionError());
				msg.put("aws.lambda.function.error", invokeResult.getFunctionError());
				return false;
			}
			
			// Check HTTP status code
			if (statusCode >= 400) {
				Trace.error("HTTP error in Lambda invocation: " + statusCode);
				msg.put("aws.lambda.error", "HTTP Error: " + statusCode);
				return false;
			}
			
			Trace.info("Lambda invocation successful");
			return true;
			
		} catch (Exception e) {
			Trace.error("Error processing Lambda response: " + e.getMessage(), e);
			msg.put("aws.lambda.error", "Error processing response: " + e.getMessage());
			return false;
		}
	}
}
