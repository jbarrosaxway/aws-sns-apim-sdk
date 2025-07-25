package com.axway.aws.sns;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to provide boolean options for ComboAttribute
 */
public class AWSBooleanOptions {
    
    /**
     * Provides boolean options for UI components
     * @return Map with boolean options
     */
    public static Map<String, String> booleanOptions() {
        Map<String, String> options = new HashMap<>();
        options.put("true", "Yes");
        options.put("false", "No");
        return options;
    }
} 