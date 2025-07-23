# Implementação do ClientConfiguration - Seguindo o Padrão S3

## Análise do Padrão S3

### **Como o S3 usa clientConfiguration:**

```java
// S3 Implementation - SendToS3BucketProcessor.java
private AmazonS3Builder<?, ?> getS3ClientBuilder(ConfigContext pack, Entity entity) throws EntityStoreException {
    Entity clientConfig = pack.getEntity(entity.getReferenceValue("clientConfiguration"));
    // ...
    return (AmazonS3Builder<?, ?>)AWSFactory.createS3ClientBuilder(pack, AWSFactory.getCredentials(pack, entity), clientConfig);
}
```

### **Como o AWSFactory processa clientConfig:**

```java
// AWSFactory.java
public static AmazonS3ClientBuilder createS3ClientBuilder(ConfigContext ctx, AWSCredentials awsCredentials, Entity clientConfig) throws EntityStoreException {
    return (AmazonS3ClientBuilder)((AmazonS3ClientBuilder)AmazonS3Client.builder()
        .withClientConfiguration(createClientConfiguration(ctx, clientConfig)))
        .withCredentials(getAWSCredentialsProvider(awsCredentials));
}

static ClientConfiguration createClientConfiguration(ConfigContext ctx, Entity entity) throws EntityStoreException {
    ClientConfiguration clientConfig = new ClientConfiguration();
    if (entity == null) {
        Trace.debug("using empty default ClientConfiguration");
        return clientConfig;
    }
    
    if (containsKey(entity, "connectionTimeout"))
        clientConfig.setConnectionTimeout(entity.getIntegerValue("connectionTimeout"));
    if (containsKey(entity, "maxConnections"))
        clientConfig.setMaxConnections(entity.getIntegerValue("maxConnections"));
    // ... mais configurações
}
```

## Implementação no Lambda

### **1. Referência no XML (✅ Implementado)**

```xml
<ReferenceSelector field="clientConfiguration" required="true"
    selectableTypes="AWSClientConfiguration" label="AWS_CLIENT_CONFIGURATION"
    title="AWS_CLIENT_CONFIGURATION_DIALOG_TITLE" searches="AWSSettings" />
```

**Exatamente igual ao S3:**
- ✅ `selectableTypes="AWSClientConfiguration"` - Tipo de entidade
- ✅ `searches="AWSSettings"` - Local de busca
- ✅ Referencia configuração existente

### **2. Processamento no Java (✅ Implementado)**

```java
// Lambda Implementation - Seguindo S3 pattern
private AWSLambdaClientBuilder getLambdaClientBuilder(ConfigContext ctx, Entity entity, Entity clientConfig) 
        throws EntityStoreException {
    
    // Get credentials using AWSFactory (following S3 pattern)
    AWSCredentials awsCredentials = AWSFactory.getCredentials(ctx, entity);
    
    // Create client builder with credentials and client configuration (following S3 pattern)
    AWSLambdaClientBuilder builder = AWSLambdaClientBuilder.standard()
        .withCredentials(getAWSCredentialsProvider(awsCredentials));
    
    // Apply client configuration if available (following S3 pattern exactly)
    if (clientConfig != null) {
        ClientConfiguration clientConfiguration = createClientConfiguration(ctx, clientConfig);
        builder.withClientConfiguration(clientConfiguration);
        Trace.info("Applied custom client configuration");
    } else {
        Trace.debug("Using default client configuration");
    }
    
    return builder;
}
```

### **3. Configuração de Cliente (✅ Implementado)**

```java
private ClientConfiguration createClientConfiguration(ConfigContext ctx, Entity entity) throws EntityStoreException {
    ClientConfiguration clientConfig = new ClientConfiguration();
    
    if (entity == null) {
        Trace.debug("using empty default ClientConfiguration");
        return clientConfig;
    }
    
    // Apply configuration settings (following S3 pattern exactly)
    if (containsKey(entity, "connectionTimeout")) {
        clientConfig.setConnectionTimeout(entity.getIntegerValue("connectionTimeout"));
    }
    if (containsKey(entity, "maxConnections")) {
        clientConfig.setMaxConnections(entity.getIntegerValue("maxConnections"));
    }
    if (containsKey(entity, "maxErrorRetry")) {
        clientConfig.setMaxErrorRetry(entity.getIntegerValue("maxErrorRetry"));
    }
    if (containsKey(entity, "protocol")) {
        clientConfig.setProtocol(Protocol.valueOf(entity.getStringValue("protocol")));
    }
    if (containsKey(entity, "socketTimeout")) {
        clientConfig.setSocketTimeout(entity.getIntegerValue("socketTimeout"));
    }
    if (containsKey(entity, "userAgent")) {
        clientConfig.setUserAgent(entity.getStringValue("userAgent"));
    }
    if (containsKey(entity, "proxyHost")) {
        clientConfig.setProxyHost(entity.getStringValue("proxyHost"));
    }
    if (containsKey(entity, "proxyPort")) {
        clientConfig.setProxyPort(entity.getIntegerValue("proxyPort"));
    }
    if (containsKey(entity, "proxyUsername")) {
        clientConfig.setProxyUsername(entity.getStringValue("proxyUsername"));
    }
    if (containsKey(entity, "proxyPassword")) {
        try {
            SecureString proxyPassword = new SecureString(ctx.getCipher().decrypt(entity.getEncryptedValue("proxyPassword")));
            try {
                clientConfig.setProxyPassword(proxyPassword.getBytesAsString());
            } finally {
                proxyPassword.close();
            }
        } catch (GeneralSecurityException e) {
            Trace.error("Error decrypting proxy password: " + e.getMessage());
        }
    }
    if (containsKey(entity, "proxyDomain")) {
        clientConfig.setProxyDomain(entity.getStringValue("proxyDomain"));
    }
    if (containsKey(entity, "proxyWorkstation")) {
        clientConfig.setProxyWorkstation(entity.getStringValue("proxyWorkstation"));
    }
    if (containsKey(entity, "socketSendBufferSizeHint") && containsKey(entity, "socketReceiveBufferSizeHint")) {
        clientConfig.setSocketBufferSizeHints(
            entity.getIntegerValue("socketSendBufferSizeHint"),
            entity.getIntegerValue("socketReceiveBufferSizeHint")
        );
    }
    
    return clientConfig;
}
```

## Comparação S3 vs Lambda

| Aspecto | S3 | Lambda | Status |
|---------|----|--------|---------|
| **Referência XML** | ✅ `ReferenceSelector` | ✅ `ReferenceSelector` | Identical |
| **Tipo de Entidade** | ✅ `AWSClientConfiguration` | ✅ `AWSClientConfiguration` | Identical |
| **Busca** | ✅ `AWSSettings` | ✅ `AWSSettings` | Identical |
| **Processamento Java** | ✅ `pack.getEntity(entity.getReferenceValue("clientConfiguration"))` | ✅ `ctx.getEntity(entity.getReferenceValue("clientConfiguration"))` | Identical |
| **AWSFactory** | ✅ `AWSFactory.createS3ClientBuilder(pack, credentials, clientConfig)` | ✅ `AWSFactory.getCredentials(ctx, entity)` | Similar |
| **ClientConfiguration** | ✅ `createClientConfiguration(ctx, clientConfig)` | ✅ `createClientConfiguration(ctx, clientConfig)` | Identical |
| **Configurações** | ✅ Timeouts, Proxy, Protocol | ✅ Timeouts, Proxy, Protocol | Identical |

## Benefícios da Implementação

### **1. Consistência**
- ✅ Mesmo padrão do S3
- ✅ Mesmas configurações disponíveis
- ✅ Mesma interface de usuário

### **2. Reutilização**
- ✅ Configuração de cliente compartilhada
- ✅ Não duplica código
- ✅ Manutenção centralizada

### **3. Segurança**
- ✅ Senhas de proxy criptografadas
- ✅ Credenciais centralizadas
- ✅ Controle de acesso

### **4. Flexibilidade**
- ✅ Configurações granulares
- ✅ Suporte a proxy corporativo
- ✅ Timeouts configuráveis

## Como Usar

### **1. Criar Configuração de Cliente**
1. No Policy Studio, criar uma configuração AWS Client Configuration
2. Definir timeouts, proxy, protocolo, etc.
3. Salvar a configuração

### **2. Referenciar no Lambda**
1. No filtro Lambda, selecionar a configuração criada
2. A configuração será aplicada automaticamente
3. Mesma configuração pode ser usada em S3, SQS, etc.

### **3. Configurações Disponíveis**
- **Connection Timeout** - Tempo limite de conexão
- **Socket Timeout** - Tempo limite de socket
- **Max Connections** - Número máximo de conexões
- **Max Error Retry** - Número máximo de tentativas
- **Protocol** - HTTP/HTTPS
- **Proxy Settings** - Host, porta, usuário, senha
- **User Agent** - User agent customizado

A implementação agora segue **exatamente** o mesmo padrão do S3! 🎯 