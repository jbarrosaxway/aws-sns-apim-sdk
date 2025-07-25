package com.axway.aws.lambda;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Tipos de invocação do Invoke Lambda Function
 */
public class AWSInvocationType {
    
    public static Map<String, String> invocationType;
    
    static {
        Map<String, String> init = new HashMap<>();
        init.put("RequestResponse", "RequestResponse");
        init.put("Event", "Event");
        init.put("DryRun", "DryRun");
        invocationType = Collections.unmodifiableMap(init);
    }
} 