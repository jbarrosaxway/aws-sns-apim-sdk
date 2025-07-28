package com.axway.aws.sns;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.vordel.trace.Trace;

public class SNSMessageJsonHelper {
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	/**
	 * Formata o corpo da mensagem para o formato esperado pelo SNS quando messageStructure = "json".
	 * O valor de "default" deve ser sempre uma string, conforme a documentação da AWS SNS.
	 */
	public static String formatJsonMessage(String body) {
		Trace.info("=== SNSMessageJsonHelper Debug ===");
		Trace.info("Input body: '" + body + "'");

		if (body == null || body.trim().isEmpty()) {
			Trace.info("Body is null or empty, returning default");
			return "{\"default\":\"\"}";
		}

		String trimmed = body.trim();
		Trace.info("Trimmed body: '" + trimmed + "'");

		try {
			// Verificar se já tem a chave "default" e é uma string
			if (trimmed.startsWith("{") && trimmed.contains("\"default\"")) {
				// Já está no formato esperado
				Trace.info("Body already has default key, returning as is");
				return trimmed;
			}

			// Para qualquer JSON (válido ou não), converter para string
			// O SNS espera que o valor de "default" seja uma string
			JsonNode resultNode = objectMapper.createObjectNode().put("default", trimmed);
			String result = objectMapper.writeValueAsString(resultNode);
			
			Trace.info("Body converted to string format: '" + result + "'");
			return result;
			
		} catch (Exception e) {
			Trace.info("Error processing JSON, treating as plain string: " + e.getMessage());
			
			// Fallback: tratar como string simples
			try {
				JsonNode resultNode = objectMapper.createObjectNode().put("default", trimmed);
				String result = objectMapper.writeValueAsString(resultNode);
				
				Trace.info("Fallback result: '" + result + "'");
				return result;
			} catch (Exception fallbackError) {
				Trace.info("Fallback also failed, returning simple default");
				return "{\"default\":\"" + trimmed.replace("\"", "\\\"") + "\"}";
			}
		}
	}
} 