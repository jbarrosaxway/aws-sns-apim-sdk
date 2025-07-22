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
		
		Trace.info("=== Configuração Lambda (Java Filter) ===");
		Trace.info("Função: " + functionName);
		Trace.info("Região: " + (awsRegion != null ? awsRegion : "inferida"));
		Trace.info("Tipo: " + invocationType);
		Trace.info("Log Type: " + logType);
		Trace.info("Max Retries: " + maxRetries);
		Trace.info("Retry Delay: " + retryDelay + "ms");
		
		// Configurar credenciais AWS
		AWSCredentialsProvider credentialsProvider = configureCredentials();
		if (credentialsProvider == null) {
			Trace.error("Não foi possível configurar credenciais AWS");
			return;
		}
		
		// Criar cliente AWS Lambda
		AWSLambdaClientBuilder builder = AWSLambdaClientBuilder.standard()
			.withCredentials(credentialsProvider);
		
		// Usar região da configuração ou da variável de ambiente
		String regionToUse = awsRegion;
		if (regionToUse == null || regionToUse.trim().isEmpty()) {
			regionToUse = System.getenv("AWS_DEFAULT_REGION");
		}
		
		if (regionToUse != null && !regionToUse.trim().isEmpty()) {
			builder = builder.withRegion(regionToUse);
			Trace.info("Usando região: " + regionToUse);
		} else {
			Trace.error("Região AWS não especificada");
			return;
		}
		
		awsLambda = builder.build();
		Trace.info("Cliente AWS Lambda configurado com sucesso");
	}

	@Override
	public boolean invoke(Circuit arg0, Message msg) throws CircuitAbortException {
		
		if (awsLambda == null) {
			Trace.error("Cliente AWS Lambda não foi configurado");
			msg.put("aws.lambda.error", "Cliente AWS Lambda não foi configurado");
			return false;
		}
		
		String body = contentBody.substitute(msg);
		if (body == null || body.trim().isEmpty()) {
			body = "{}";
		}
		
		Trace.info("Invocando função Lambda com retry...");
		
		int maxRetriesInt = Integer.parseInt(maxRetries);
		int retryDelayInt = Integer.parseInt(retryDelay);
		Exception lastException = null;
		
		for (int attempt = 1; attempt <= maxRetriesInt; attempt++) {
			try {
				Trace.info("Tentativa " + attempt + " de " + maxRetriesInt);
				
				// Criar requisição
				InvokeRequest invokeRequest = new InvokeRequest()
					.withFunctionName(functionName)
					.withPayload(body)
					.withInvocationType(invocationType)
					.withLogType(logType);
				
				// Adicionar qualifier se especificado
				if (qualifier != null && !qualifier.trim().isEmpty()) {
					invokeRequest.setQualifier(qualifier);
					Trace.info("Usando qualifier: " + qualifier);
				}
				
				// Invocar função Lambda
				InvokeResult invokeResult = awsLambda.invoke(invokeRequest);
				
				// Processar resposta
				return processInvokeResult(invokeResult, msg);
				
			} catch (Exception e) {
				lastException = e;
				Trace.error("Tentativa " + attempt + " falhou: " + e.getMessage());
				
				// Se não é a última tentativa, aguardar antes de tentar novamente
				if (attempt < maxRetriesInt) {
					Trace.info("Aguardando " + retryDelayInt + "ms antes da próxima tentativa...");
					try {
						Thread.sleep(retryDelayInt);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						Trace.error("Thread interrompida durante retry");
						return false;
					}
				}
			}
		}
		
		// Se chegou aqui, todas as tentativas falharam
		Trace.error("Todas as " + maxRetriesInt + " tentativas falharam");
		msg.put("aws.lambda.error", "Falha após " + maxRetriesInt + " tentativas: " + 
			(lastException != null ? lastException.getMessage() : "Erro desconhecido"));
		return false;
	}
	
	/**
	 * Configura credenciais AWS usando múltiplas estratégias
	 */
	private AWSCredentialsProvider configureCredentials() {
		// Verificar variáveis de ambiente
		String envAccessKey = System.getenv("AWS_ACCESS_KEY_ID");
		String envSecretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
		String envSessionToken = System.getenv("AWS_SESSION_TOKEN");
		String envCredentialsFile = System.getenv("AWS_SHARED_CREDENTIALS_FILE");
		String envProfile = System.getenv("AWS_PROFILE");
		if (envProfile == null || envProfile.trim().isEmpty()) {
			envProfile = "default";
		}
		
		Trace.info("Verificando configuração AWS...");
		Trace.info("AWS_ACCESS_KEY_ID presente: " + (envAccessKey != null));
		Trace.info("AWS_SECRET_ACCESS_KEY presente: " + (envSecretKey != null));
		Trace.info("AWS_SHARED_CREDENTIALS_FILE: " + (envCredentialsFile != null ? envCredentialsFile : "não definido"));
		Trace.info("AWS_PROFILE: " + envProfile);
		
		// Estratégia 1: Variáveis de ambiente diretas
		if (envAccessKey != null && envSecretKey != null) {
			Trace.info("Usando credenciais de variáveis de ambiente");
			
			if (envSessionToken != null && !envSessionToken.trim().isEmpty()) {
				BasicSessionCredentials credentials = new BasicSessionCredentials(envAccessKey, envSecretKey, envSessionToken);
				return new AWSStaticCredentialsProvider(credentials);
			} else {
				BasicAWSCredentials credentials = new BasicAWSCredentials(envAccessKey, envSecretKey);
				return new AWSStaticCredentialsProvider(credentials);
			}
		}
		// Estratégia 2: Arquivo de credenciais
		else if (envCredentialsFile != null && !envCredentialsFile.trim().isEmpty()) {
			Trace.info("Usando arquivo de credenciais: " + envCredentialsFile);
			
			try {
				File credentialsFile = new File(envCredentialsFile);
				if (credentialsFile.exists()) {
					Trace.info("Arquivo de credenciais encontrado");
					return new ProfileCredentialsProvider(envCredentialsFile, envProfile);
				} else {
					Trace.error("Arquivo de credenciais não encontrado: " + envCredentialsFile);
					return null;
				}
			} catch (Exception e) {
				Trace.error("Erro ao configurar arquivo de credenciais: " + e.getMessage());
				return null;
			}
		}
		// Estratégia 3: Fallback para DefaultAWSCredentialsProviderChain
		else {
			Trace.info("Usando DefaultAWSCredentialsProviderChain (fallback)");
			try {
				return new DefaultAWSCredentialsProviderChain();
			} catch (Exception e) {
				Trace.error("Erro ao configurar DefaultAWSCredentialsProviderChain: " + e.getMessage());
				return null;
			}
		}
	}
	
	/**
	 * Processa o resultado da invocação Lambda
	 */
	private boolean processInvokeResult(InvokeResult invokeResult, Message msg) {
		try {
			String response = new String(invokeResult.getPayload().array(), "UTF-8");
			int statusCode = invokeResult.getStatusCode();
			
			Trace.info("=== Resposta Lambda ===");
			Trace.info("Status Code: " + statusCode);
			Trace.info("Response: " + response);
			Trace.info("Executed Version: " + invokeResult.getExecutedVersion());
			
			if (invokeResult.getLogResult() != null) {
				Trace.info("Log Result: " + invokeResult.getLogResult());
			}
			
			// Armazenar resultados
			msg.put("aws.lambda.response", response);
			msg.put("aws.lambda.http.status.code", statusCode);
			msg.put("aws.lambda.executed.version", invokeResult.getExecutedVersion());
			msg.put("aws.lambda.log.result", invokeResult.getLogResult());
			
			// Verificar erro da função Lambda
			if (invokeResult.getFunctionError() != null) {
				Trace.error("Erro na função Lambda: " + invokeResult.getFunctionError());
				msg.put("aws.lambda.error", invokeResult.getFunctionError());
				msg.put("aws.lambda.function.error", invokeResult.getFunctionError());
				return false;
			}
			
			// Verificar status code HTTP
			if (statusCode >= 400) {
				Trace.error("Erro HTTP na invocação Lambda: " + statusCode);
				msg.put("aws.lambda.error", "Erro HTTP: " + statusCode);
				return false;
			}
			
			Trace.info("Invocação Lambda realizada com sucesso");
			return true;
			
		} catch (Exception e) {
			Trace.error("Erro ao processar resposta Lambda: " + e.getMessage(), e);
			msg.put("aws.lambda.error", "Erro ao processar resposta: " + e.getMessage());
			return false;
		}
	}
}
