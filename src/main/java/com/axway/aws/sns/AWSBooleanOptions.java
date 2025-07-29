package com.axway.aws.sns;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to provide boolean options for ComboAttribute
 * Thread-safe and immutable
 */
public class AWSBooleanOptions {
    
    // Immutable map for boolean options
    public static final Map<String, String> BOOLEAN_OPTIONS;
    
    static {
        Map<String, String> options = new HashMap<>();
        options.put("true", "Yes");
        options.put("false", "No");
        BOOLEAN_OPTIONS = Collections.unmodifiableMap(options);
    }
    
    /**
     * Private constructor to prevent instantiation
     */
    private AWSBooleanOptions() {
        // Utility class - should not be instantiated
    }
    
    /**
     * Provides boolean options for UI components
     * @return Immutable Map with boolean options
     */
    public static Map<String, String> booleanOptions() {
        return BOOLEAN_OPTIONS;
    }
} 