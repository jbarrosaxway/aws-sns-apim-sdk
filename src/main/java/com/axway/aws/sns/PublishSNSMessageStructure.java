package com.axway.aws.sns;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Message structure types for Publish SNS Message
 * Thread-safe and immutable
 */
public class PublishSNSMessageStructure {
    
    // Immutable map for message structure options
    public static final Map<String, String> MESSAGE_STRUCTURE;
    
    static {
        Map<String, String> init = new HashMap<>();
        init.put("default", "Default");
        init.put("json", "JSON");
        MESSAGE_STRUCTURE = Collections.unmodifiableMap(init);
    }
    
    /**
     * Private constructor to prevent instantiation
     */
    private PublishSNSMessageStructure() {
        // Utility class - should not be instantiated
    }
} 