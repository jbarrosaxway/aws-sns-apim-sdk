# Melhorias de Autenticação AWS - Filtro Java

## ✅ **Implementação Completa de Autenticação Flexível**

O `AWSLambdaProcessor.java` foi atualizado para implementar o mesmo suporte flexível de autenticação AWS que está documentado e implementado no script Groovy.

## 🔧 **Funcionalidades Implementadas**

### **1. Múltiplas Estratégias de Autenticação**

#### **Estratégia 1: Variáveis de Ambiente (Mais Seguro para Desenvolvimento)**
```java
// Verifica AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_SESSION_TOKEN
if (envAccessKey != null && envSecretKey != null) {
    if (envSessionToken != null) {
        // Credenciais temporárias (STS)
        BasicSessionCredentials credentials = new BasicSessionCredentials(envAccessKey, envSecretKey, envSessionToken);
        return new AWSStaticCredentialsProvider(credentials);
    } else {
        // Credenciais permanentes
        BasicAWSCredentials credentials = new BasicAWSCredentials(envAccessKey, envSecretKey);
        return new AWSStaticCredentialsProvider(credentials);
    }
}
```

#### **Estratégia 2: Arquivo de Credenciais (Mais Seguro para Produção)**
```java
// Verifica AWS_SHARED_CREDENTIALS_FILE e AWS_PROFILE
else if (envCredentialsFile != null) {
    File credentialsFile = new File(envCredentialsFile);
    if (credentialsFile.exists()) {
        return new ProfileCredentialsProvider(envCredentialsFile, envProfile);
    }
}
```

#### **Estratégia 3: DefaultAWSCredentialsProviderChain (Fallback)**
```java
// Fallback para IAM Roles, EC2 Instance Profile, etc.
else {
    return new DefaultAWSCredentialsProviderChain();
}
```

### **2. Configurações Avançadas**

#### **Parâmetros Suportados:**
- `functionName`: Nome da função Lambda (obrigatório)
- `awsRegion`: Região AWS (opcional - usa AWS_DEFAULT_REGION)
- `invocationType`: Tipo de invocação (padrão: "RequestResponse")
- `logType`: Tipo de log (padrão: "None")
- `qualifier`: Versão ou alias da função (opcional)
- `maxRetries`: Número máximo de tentativas (padrão: 3)
- `retryDelay`: Delay entre tentativas em ms (padrão: 1000)

### **3. Sistema de Retry Robusto**

```java
for (int attempt = 1; attempt <= maxRetriesInt; attempt++) {
    try {
        // Invocação Lambda
        InvokeResult invokeResult = awsLambda.invoke(invokeRequest);
        return processInvokeResult(invokeResult, msg);
    } catch (Exception e) {
        // Log e retry
        if (attempt < maxRetriesInt) {
            Thread.sleep(retryDelayInt);
        }
    }
}
```

### **4. Processamento Avançado de Resposta**

```java
private boolean processInvokeResult(InvokeResult invokeResult, Message msg) {
    // Extrai resposta
    String response = new String(invokeResult.getPayload().array(), "UTF-8");
    int statusCode = invokeResult.getStatusCode();
    
    // Armazena atributos
    msg.put("aws.lambda.response", response);
    msg.put("aws.lambda.http.status.code", statusCode);
    msg.put("aws.lambda.executed.version", invokeResult.getExecutedVersion());
    msg.put("aws.lambda.log.result", invokeResult.getLogResult());
    
    // Verifica erros
    if (invokeResult.getFunctionError() != null) {
        msg.put("aws.lambda.error", invokeResult.getFunctionError());
        return false;
    }
    
    // Verifica status HTTP
    if (statusCode >= 400) {
        msg.put("aws.lambda.error", "Erro HTTP: " + statusCode);
        return false;
    }
    
    return true;
}
```

## 📋 **Configuração no Policy Studio**

### **1. Parâmetros do Filtro**

| Parâmetro | Tipo | Obrigatório | Padrão | Descrição |
|-----------|------|-------------|--------|-----------|
| `functionName` | String | ✅ | - | Nome da função Lambda |
| `awsRegion` | String | ❌ | AWS_DEFAULT_REGION | Região AWS |
| `invocationType` | String | ❌ | RequestResponse | Tipo de invocação |
| `logType` | String | ❌ | None | Tipo de log |
| `qualifier` | String | ❌ | - | Versão ou alias |
| `maxRetries` | String | ❌ | 3 | Máximo de tentativas |
| `retryDelay` | String | ❌ | 1000 | Delay entre tentativas (ms) |

### **2. Atributos de Saída**

| Atributo | Tipo | Descrição |
|----------|------|-----------|
| `aws.lambda.response` | String | Resposta da função Lambda |
| `aws.lambda.http.status.code` | Integer | Código de status HTTP |
| `aws.lambda.executed.version` | String | Versão executada da função |
| `aws.lambda.log.result` | String | Resultado dos logs |
| `aws.lambda.error` | String | Erro (se houver) |

## 🔐 **Configuração de Credenciais**

### **1. Variáveis de Ambiente (Desenvolvimento)**
```bash
export AWS_ACCESS_KEY_ID="sua_access_key"
export AWS_SECRET_ACCESS_KEY="sua_secret_key"
export AWS_SESSION_TOKEN="seu_session_token"  # opcional
export AWS_DEFAULT_REGION="us-east-1"
```

### **2. Arquivo de Credenciais (Produção)**
```bash
# ~/.aws/credentials
[default]
aws_access_key_id = sua_access_key
aws_secret_access_key = sua_secret_key
aws_session_token = seu_session_token  # opcional

# Configurar variável de ambiente
export AWS_SHARED_CREDENTIALS_FILE="/path/to/credentials"
export AWS_PROFILE="default"
```

### **3. IAM Roles (Mais Seguro)**
```yaml
# Para EKS/EC2 - sem variáveis de ambiente
# O filtro automaticamente usa DefaultAWSCredentialsProviderChain
```

## 🚀 **Vantagens da Implementação**

### **✅ Compatibilidade Total**
- Mesmo comportamento do script Groovy
- Mesmas estratégias de autenticação
- Mesmos parâmetros de configuração

### **✅ Flexibilidade**
- Múltiplas estratégias de credenciais
- Configuração dinâmica via parâmetros
- Fallback automático

### **✅ Robustez**
- Sistema de retry configurável
- Tratamento de erros detalhado
- Logging completo

### **✅ Segurança**
- Suporte a credenciais temporárias (STS)
- Suporte a arquivo de credenciais
- Suporte a IAM Roles

### **✅ Monitoramento**
- Logs detalhados em cada etapa
- Atributos de resposta completos
- Informações de erro específicas

## 📝 **Exemplo de Uso**

### **1. Configuração Básica**
```
functionName: minha-funcao-lambda
awsRegion: us-east-1
```

### **2. Configuração Avançada**
```
functionName: minha-funcao-lambda
awsRegion: us-east-1
invocationType: RequestResponse
logType: Tail
qualifier: $LATEST
maxRetries: 5
retryDelay: 2000
```

### **3. Configuração para Produção**
```
functionName: minha-funcao-lambda
awsRegion: us-east-1
invocationType: RequestResponse
logType: None
maxRetries: 3
retryDelay: 1000
```

## 🔄 **Migração do Script Groovy**

### **Antes (Script Groovy)**
```groovy
def invoke(msg) {
    // Configuração dinâmica via atributos da mensagem
    def functionName = msg.get("aws.lambda.function.name")
    def awsRegion = msg.get("aws.lambda.region")
    // ... resto da lógica
}
```

### **Agora (Filtro Java)**
```java
// Configuração via parâmetros do filtro
functionName = new Selector<>(entity.getStringValue("functionName"), String.class).getLiteral();
awsRegion = new Selector<>(entity.getStringValue("awsRegion"), String.class).getLiteral();
// ... mesma lógica de autenticação e invocação
```

## ✅ **Conclusão**

O filtro Java agora oferece:

1. **✅ Autenticação Flexível**: Mesmas estratégias do script Groovy
2. **✅ Configuração Avançada**: Parâmetros opcionais com valores padrão
3. **✅ Sistema de Retry**: Configurável e robusto
4. **✅ Logging Detalhado**: Para troubleshooting
5. **✅ Compatibilidade**: Mesmo comportamento do script Groovy
6. **✅ Segurança**: Suporte a múltiplas estratégias de credenciais

A implementação está pronta para uso em ambientes de produção com configurações de segurança apropriadas! 🚀 