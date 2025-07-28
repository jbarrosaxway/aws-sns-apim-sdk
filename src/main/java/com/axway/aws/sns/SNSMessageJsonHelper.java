package com.axway.aws.sns;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.vordel.trace.Trace;

public class SNSMessageJsonHelper {
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	/**
	 * Formata o corpo da mensagem para o formato esperado pelo SNS quando messageStructure = "json".
	 * Se o body já for um JSON com a chave "default", retorna como está.
	 * Se não, encapsula o body em {"default": ...} usando Jackson para manipulação segura.
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
			// Verificar se já tem a chave "default"
			if (trimmed.startsWith("{") && trimmed.contains("\"default\"")) {
				// Já está no formato esperado
				Trace.info("Body already has default key, returning as is");
				return trimmed;
			}

			// Se é um JSON válido, envolver como valor de default
			if (trimmed.startsWith("{")) {
				// Parse o JSON para validar e depois criar a estrutura correta
				JsonNode jsonNode = objectMapper.readTree(trimmed);
				
				// Criar a estrutura {"default": <json_original>}
				JsonNode resultNode = objectMapper.createObjectNode().set("default", jsonNode);
				String result = objectMapper.writeValueAsString(resultNode);
				
				Trace.info("Body is JSON without default, wrapping with Jackson: '" + result + "'");
				return result;
			}

			// Se não é JSON, tratar como string
			JsonNode resultNode = objectMapper.createObjectNode().put("default", trimmed);
			String result = objectMapper.writeValueAsString(resultNode);
			
			Trace.info("Body is not JSON, treating as string: '" + result + "'");
			return result;
			
		} catch (Exception e) {
			Trace.info("Error parsing JSON, treating as string: " + e.getMessage());
			
			// Fallback: tratar como string
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