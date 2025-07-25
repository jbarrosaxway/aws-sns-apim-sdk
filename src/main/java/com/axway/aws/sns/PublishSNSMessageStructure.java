package com.axway.aws.sns;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Tipos de estrutura de mensagem do Publish SNS Message
 */
public class PublishSNSMessageStructure {
    
    public static Map<String, String> messageStructure;
    
    static {
        Map<String, String> init = new HashMap<>();
        init.put("", "Default");
        init.put("json", "JSON");
        messageStructure = Collections.unmodifiableMap(init);
    }
} 