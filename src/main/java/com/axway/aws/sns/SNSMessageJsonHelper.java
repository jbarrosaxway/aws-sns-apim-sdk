package com.axway.aws.sns;

import com.vordel.trace.Trace;

public class SNSMessageJsonHelper {
	/**
	 * Formata o corpo da mensagem para o formato esperado pelo SNS quando messageStructure = "json".
	 * Se o body já for um JSON com a chave "default", retorna como está.
	 * Se não, encapsula o body em {"default": ...} escapando corretamente aspas.
	 */
	public static String formatJsonMessage(String body) {
		Trace.info("=== SNSMessageJsonHelper Debug ===");
		Trace.info("Input body: '" + body + "'");

		if (body == null || body.trim().isEmpty()) {
			Trace.info("Body is null or empty, returning default");
			return "{\"default\":\"\"}";
		}

		// Remove quebras de linha e espaços extras
		String cleaned = body.replaceAll("\\s+", " ").trim();
		Trace.info("Cleaned body: '" + cleaned + "'");

		if (cleaned.startsWith("{") && cleaned.contains("\"default\"")) {
			// Já está no formato esperado
			Trace.info("Body already has default key, returning as is");
			return cleaned;
		}

		if (cleaned.startsWith("{")) {
			// É um JSON, mas não tem "default". Envolve como valor de default
			String result = "{\"default\": " + cleaned + "}";
			Trace.info("Body is JSON without default, wrapping: '" + result + "'");
			return result;
		}

		// Não é JSON, escapa aspas e envolve
		String escaped = cleaned.replace("\"", "\\\"");
		String result = "{\"default\": \"" + escaped + "\"}";
		Trace.info("Body is not JSON, escaping and wrapping: '" + result + "'");
		return result;
	}
} 