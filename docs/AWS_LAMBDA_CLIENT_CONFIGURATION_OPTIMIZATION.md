# Otimização dos Campos - Evitando Duplicação

## Problema Identificado

Após análise, descobrimos que alguns campos da seção "Advanced Configuration" estavam duplicando funcionalidades já existentes no `clientConfiguration`.

## Análise dos Campos

### **Campos Duplicados (Removidos):**

| Campo Avançado | Campo clientConfiguration | Status |
|----------------|--------------------------|---------|
| `maxRetries` | `maxErrorRetry` | ❌ **DUPLICADO** |
| `timeout` | `connectionTimeout` | ❌ **DUPLICADO** |

### **Campos Específicos do Lambda (Mantidos):**

| Campo | Descrição | Status |
|-------|-----------|---------|
| `retryDelay` | Delay entre tentativas (ms) | ✅ **MANTIDO** |
| `memorySize` | Tamanho de memória (MB) | ✅ **MANTIDO** |

## Implementação Otimizada

### **1. XML Simplificado**

```xml
<!-- Advanced Configuration -->
<group label="AWS_LAMBDA_ADVANCED_SETTINGS_LABEL" columns="2" span="2" fill="false">
    
    <TextAttribute field="retryDelay" label="AWS_LAMBDA_RETRY_DELAY_LABEL"
        displayName="AWS_LAMBDA_RETRY_DELAY_NAME" description="AWS_LAMBDA_RETRY_DELAY_DESCRIPTION" />
    
    <TextAttribute field="memorySize" label="AWS_LAMBDA_MEMORY_SIZE_LABEL"
        displayName="AWS_LAMBDA_MEMORY_SIZE_NAME" description="AWS_LAMBDA_MEMORY_SIZE_DESCRIPTION" />
    
</group>
```

### **2. Java Otimizado**

```java
// Selectors simplificados
protected Selector<String> functionName;
protected Selector<String> awsRegion;
protected Selector<String> invocationType;
protected Selector<String> logType;
protected Selector<String> qualifier;
protected Selector<Integer> retryDelay;        // Específico do Lambda
protected Selector<Integer> memorySize;        // Específico do Lambda
protected Selector<Boolean> useIAMRole;
```

### **3. Configurações do clientConfiguration**

O `clientConfiguration` já fornece:

```java
// Configurações de conexão
clientConfig.setConnectionTimeout(entity.getIntegerValue("connectionTimeout"));
clientConfig.setSocketTimeout(entity.getIntegerValue("socketTimeout"));
clientConfig.setMaxErrorRetry(entity.getIntegerValue("maxErrorRetry"));
clientConfig.setMaxConnections(entity.getIntegerValue("maxConnections"));

// Configurações de proxy
clientConfig.setProxyHost(entity.getStringValue("proxyHost"));
clientConfig.setProxyPort(entity.getIntegerValue("proxyPort"));
clientConfig.setProxyUsername(entity.getStringValue("proxyUsername"));
clientConfig.setProxyPassword(proxyPassword);

// Configurações avançadas
clientConfig.setProtocol(Protocol.valueOf(entity.getStringValue("protocol")));
clientConfig.setUserAgent(entity.getStringValue("userAgent"));
```

## Benefícios da Otimização

### **1. Evita Duplicação**
- ✅ **Sem campos redundantes** - Não duplica funcionalidades
- ✅ **Configuração centralizada** - Timeouts no clientConfiguration
- ✅ **Manutenção simplificada** - Menos campos para gerenciar

### **2. Segue Padrão S3**
- ✅ **Mesma estrutura** - Usa clientConfiguration como S3
- ✅ **Consistência** - Interface uniforme entre filtros AWS
- ✅ **Reutilização** - Mesma configuração para S3, Lambda, etc.

### **3. Campos Específicos**
- ✅ **retryDelay** - Específico para lógica de retry do Lambda
- ✅ **memorySize** - Específico para configuração de memória do Lambda

## Como Configurar

### **1. Configurações de Cliente (clientConfiguration)**
- **Connection Timeout** - Tempo limite de conexão
- **Socket Timeout** - Tempo limite de socket
- **Max Error Retry** - Número máximo de tentativas
- **Max Connections** - Número máximo de conexões
- **Protocol** - HTTP/HTTPS
- **Proxy Settings** - Configurações de proxy

### **2. Configurações Específicas do Lambda**
- **Retry Delay** - Delay entre tentativas (ms)
- **Memory Size** - Tamanho de memória (MB)

## Fluxo de Configuração

### **1. Criar clientConfiguration**
1. Configurar timeouts, proxy, protocolo
2. Definir maxErrorRetry para tentativas
3. Salvar configuração

### **2. Configurar Lambda**
1. Referenciar clientConfiguration criado
2. Configurar retryDelay específico do Lambda
3. Configurar memorySize específico do Lambda

### **3. Resultado**
- **Timeouts** - Controlados pelo clientConfiguration
- **Retentativas** - Controladas pelo clientConfiguration
- **Retry Delay** - Controlado pelo Lambda
- **Memory Size** - Controlado pelo Lambda

## Comparação Antes vs Depois

### **Antes (Duplicado):**
```xml
<TextAttribute field="maxRetries" />      <!-- ❌ Duplicado -->
<TextAttribute field="timeout" />         <!-- ❌ Duplicado -->
<TextAttribute field="retryDelay" />      <!-- ✅ Específico -->
<TextAttribute field="memorySize" />      <!-- ✅ Específico -->
```

### **Depois (Otimizado):**
```xml
<TextAttribute field="retryDelay" />      <!-- ✅ Específico -->
<TextAttribute field="memorySize" />      <!-- ✅ Específico -->
<!-- maxRetries e timeout vêm do clientConfiguration -->
```

A otimização elimina duplicação e segue o padrão estabelecido pelo S3! 🎯 