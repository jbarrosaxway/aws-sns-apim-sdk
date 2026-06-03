package com.axway.aws.sns;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vordel.trace.Trace;

/**
 * Parses SNS MessageAttributes from JSON (AWS map format) for PublishRequest.
 */
public class SNSMessageAttributesHelper {

	public static final int MAX_ATTRIBUTES = 10;
	public static final String MAX_ATTRIBUTES_MESSAGE = "Maximum of 10 attributes reached";

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private SNSMessageAttributesHelper() {
	}

	/**
	 * Parses JSON into SNS MessageAttributes map. Returns null when input is null/blank
	 * (caller should omit MessageAttributes on PublishRequest).
	 */
	public static Map<String, MessageAttributeValue> parseMessageAttributes(String json) {
		if (json == null || json.trim().isEmpty()) {
			Trace.debug("Message attributes JSON is empty, omitting MessageAttributes");
			return null;
		}

		String trimmed = json.trim();
		Trace.debug("=== SNSMessageAttributesHelper Debug ===");
		Trace.debug("Input JSON length: " + trimmed.length());

		try {
			JsonNode root = objectMapper.readTree(trimmed);
			if (!root.isObject()) {
				throw new IllegalArgumentException("Message attributes JSON must be an object");
			}

			int count = root.size();
			if (count == 0) {
				Trace.debug("Message attributes object is empty, omitting MessageAttributes");
				return null;
			}
			if (count > MAX_ATTRIBUTES) {
				throw new IllegalArgumentException(MAX_ATTRIBUTES_MESSAGE);
			}

			Map<String, MessageAttributeValue> attributes = new LinkedHashMap<>();
			Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
			while (fields.hasNext()) {
				Map.Entry<String, JsonNode> entry = fields.next();
				String name = entry.getKey();
				if (name == null || name.trim().isEmpty()) {
					throw new IllegalArgumentException("Message attribute name cannot be empty");
				}
				MessageAttributeValue value = toMessageAttributeValue(name, entry.getValue());
				attributes.put(name, value);
			}

			Trace.debug("Parsed " + attributes.size() + " message attribute(s): " + attributes.keySet());
			return attributes;

		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid message attributes JSON: " + e.getMessage(), e);
		}
	}

	private static MessageAttributeValue toMessageAttributeValue(String name, JsonNode node) {
		if (node == null || !node.isObject()) {
			throw new IllegalArgumentException("Message attribute '" + name + "' must be an object");
		}

		JsonNode dataTypeNode = node.get("DataType");
		if (dataTypeNode == null || dataTypeNode.isNull() || dataTypeNode.asText().trim().isEmpty()) {
			throw new IllegalArgumentException("Message attribute '" + name + "' requires DataType");
		}
		String dataType = dataTypeNode.asText().trim();

		JsonNode stringValueNode = node.get("StringValue");
		JsonNode binaryValueNode = node.get("BinaryValue");

		boolean hasString = stringValueNode != null && !stringValueNode.isNull();
		boolean hasBinary = binaryValueNode != null && !binaryValueNode.isNull();

		if (!hasString && !hasBinary) {
			throw new IllegalArgumentException(
				"Message attribute '" + name + "' requires StringValue or BinaryValue");
		}

		MessageAttributeValue value = new MessageAttributeValue().withDataType(dataType);

		if (hasBinary) {
			String binaryText = binaryValueNode.asText();
			if (binaryText == null || binaryText.trim().isEmpty()) {
				throw new IllegalArgumentException("Message attribute '" + name + "' BinaryValue cannot be empty");
			}
			byte[] decoded = Base64.getDecoder().decode(binaryText.trim());
			value.withBinaryValue(ByteBuffer.wrap(decoded));
		}

		if (hasString) {
			value.withStringValue(stringValueNode.asText());
		}

		return value;
	}
}
