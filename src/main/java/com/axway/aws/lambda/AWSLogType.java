package com.axway.aws.lambda;

/**
 * Enum para os tipos de log do AWS Lambda
 */
public enum AWSLogType {
    
    NONE("None", "None"),
    TAIL("Tail", "Tail");
    
    private final String value;
    private final String displayName;
    
    AWSLogType(String value, String displayName) {
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
        AWSLogType[] types = values();
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
        AWSLogType[] types = values();
        String[] result = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            result[i] = types[i].getDisplayName();
        }
        return result;
    }
} 