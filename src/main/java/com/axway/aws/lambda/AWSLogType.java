package com.axway.aws.lambda;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Tipos de log do AWS Lambda
 */
public class AWSLogType {
    
    public static Map<String, String> logType;
    
    static {
        Map<String, String> init = new HashMap<>();
        init.put("None", "None");
        init.put("Tail", "Tail");
        logType = Collections.unmodifiableMap(init);
    }
} 