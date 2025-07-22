package com.axway.aws.lambda;

/**
 * Enum para os tipos de invocação do AWS Lambda
 */
public enum AWSInvocationType {
    
    REQUEST_RESPONSE("RequestResponse", "RequestResponse"),
    EVENT("Event", "Event"),
    DRY_RUN("DryRun", "DryRun");
    
    private final String value;
    private final String displayName;
    
    AWSInvocationType(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    /**
     * Retorna todos os valores como array de strings para o ComboAttribute
     */
    public static String[] getValues() {
        AWSInvocationType[] types = values();
        String[] result = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            result[i] = types[i].getValue();
        }
        return result;
    }
    
    /**
     * Retorna todos os display names como array de strings para o ComboAttribute
     */
    public static String[] getDisplayNames() {
        AWSInvocationType[] types = values();
        String[] result = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            result[i] = types[i].getDisplayName();
        }
        return result;
    }
} 