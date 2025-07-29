package com.axway.aws.sns;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.vordel.trace.Trace;

/**
 * Helper class for formatting JSON messages for SNS
 * Thread-safe and optimized for performance
 */
public class SNSMessageJsonHelper {
	
	// Thread-safe ObjectMapper instance
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	/**
	 * Private constructor to prevent instantiation
	 */
	private SNSMessageJsonHelper() {
		// Utility class - should not be instantiated
	}
	
	/**
	 * Formats the message body for the format expected by SNS when messageStructure = "json".
	 * The "default" value must always be a string, according to AWS SNS documentation.
	 * 
	 * @param body The message body to format
	 * @return Formatted JSON string for SNS
	 */
	public static String formatJsonMessage(String body) {
		Trace.debug("=== SNSMessageJsonHelper Debug ===");
		Trace.debug("Input body: '" + body + "'");

		if (body == null || body.trim().isEmpty()) {
			Trace.debug("Body is null or empty, returning default");
			return "{\"default\":\"\"}";
		}

		String trimmed = body.trim();
		Trace.debug("Trimmed body: '" + trimmed + "'");

		try {
			// Check if already has "default" key and is a string
			if (trimmed.startsWith("{") && trimmed.contains("\"default\"")) {
				// Already in expected format
				Trace.debug("Body already has default key, returning as is");
				return trimmed;
			}

			// For any JSON (valid or not), convert to string
			// SNS expects the "default" value to be a string
			JsonNode resultNode = OBJECT_MAPPER.createObjectNode().put("default", trimmed);
			String result = OBJECT_MAPPER.writeValueAsString(resultNode);
			
			Trace.debug("Body converted to string format: '" + result + "'");
			return result;
			
		} catch (Exception e) {
			Trace.debug("Error processing JSON, treating as plain string: " + e.getMessage());
			
			// Fallback: treat as simple string
			try {
				JsonNode resultNode = OBJECT_MAPPER.createObjectNode().put("default", trimmed);
				String result = OBJECT_MAPPER.writeValueAsString(resultNode);
				
				Trace.debug("Fallback result: '" + result + "'");
				return result;
			} catch (Exception fallbackError) {
				Trace.debug("Fallback also failed, returning simple default");
				return "{\"default\":\"" + trimmed.replace("\"", "\\\"") + "\"}";
			}
		}
	}
} 