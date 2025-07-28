package com.axway.aws.sns;

public class SNSMessageJsonHelper {
    /**
     * Formata o corpo da mensagem para o formato esperado pelo SNS quando messageStructure = "json".
     * Se o body já for um JSON com a chave "default", retorna como está.
     * Se não, encapsula o body em {"default": ...} escapando corretamente aspas.
     */
    public static String formatJsonMessage(String body) {
        if (body == null || body.trim().isEmpty()) {
            return "{\"default\":\"\"}";
        }
        String trimmed = body.trim();
        if (trimmed.startsWith("{") && trimmed.contains("\"default\"")) {
            // Já está no formato esperado
            return trimmed;
        }
        if (trimmed.startsWith("{")) {
            // É um JSON, mas não tem "default". Envolve como valor de default
            return "{\"default\": " + trimmed + "}";
        }
        // Não é JSON, escapa aspas e envolve
        String escaped = trimmed.replace("\"", "\\\"");
        return "{\"default\": \"" + escaped + "\"}";
    }
} 