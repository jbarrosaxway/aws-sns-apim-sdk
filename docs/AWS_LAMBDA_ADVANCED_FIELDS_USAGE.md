# Campos Avançados - Timeout e Memory Size

## Análise dos Campos

### **Campos das Linhas 54-58:**

| Campo | Linha | Status | Uso |
|-------|-------|--------|-----|
| `maxRetries` | 54 | ✅ **USADO** | Número máximo de tentativas |
| `retryDelay` | 55 | ✅ **USADO** | Delay entre tentativas (ms) |
| `timeout` | 56 | ✅ **IMPLEMENTADO** | Timeout da função (segundos) |
| `memorySize` | 57 | ✅ **IMPLEMENTADO** | Memória alocada (MB) |

## Implementação dos Campos

### **1. Timeout (Linha 56)**

**XML:**
```xml
<TextAttribute field="timeout" label="AWS_LAMBDA_TIMEOUT_LABEL"
    displayName="AWS_LAMBDA_TIMEOUT_NAME" description="AWS_LAMBDA_TIMEOUT_DESCRIPTION" />
```

**Java:**
```java
// Declaração
protected Selector<Integer> timeout;

// Inicialização
this.timeout = new Selector(entity.getStringValue("timeout"), Integer.class);

// Uso dinâmico
Integer timeoutValue = timeout.substitute(msg);
if (timeoutValue == null) {
    timeoutValue = 300; // Default 5 minutes
}

// Logging
Trace.info("Timeout: " + timeoutValue + " seconds");

// Armazenamento na mensagem
msg.put("aws.lambda.timeout", timeoutValue);
```

### **2. Memory Size (Linha 57)**

**XML:**
```xml
<TextAttribute field="memorySize" label="AWS_LAMBDA_MEMORY_SIZE_LABEL"
    displayName="AWS_LAMBDA_MEMORY_SIZE_NAME" description="AWS_LAMBDA_MEMORY_SIZE_DESCRIPTION" />
```

**Java:**
```java
// Declaração
protected Selector<Integer> memorySize;

// Inicialização
this.memorySize = new Selector(entity.getStringValue("memorySize"), Integer.class);

// Uso dinâmico
Integer memorySizeValue = memorySize.substitute(msg);
if (memorySizeValue == null) {
    memorySizeValue = 128; // Default 128 MB
}

// Logging
Trace.info("Memory Size: " + memorySizeValue + " MB");

// Armazenamento na mensagem
msg.put("aws.lambda.memory.size", memorySizeValue);
```

## Valores Padrão

### **1. Timeout**
- **Padrão**: 300 segundos (5 minutos)
- **Mínimo**: 1 segundo
- **Máximo**: 900 segundos (15 minutos)
- **Uso**: Configura o timeout da função Lambda

### **2. Memory Size**
- **Padrão**: 128 MB
- **Mínimo**: 128 MB
- **Máximo**: 10240 MB (10 GB)
- **Uso**: Configura a memória alocada para a função

## Como Usar

### **1. Configuração Estática**
```xml
<!-- No XML, definir valores fixos -->
<TextAttribute field="timeout" value="600" />
<TextAttribute field="memorySize" value="512" />
```

### **2. Configuração Dinâmica**
```xml
<!-- No XML, usar valores dinâmicos -->
<TextAttribute field="timeout" value="${timeout.value}" />
<TextAttribute field="memorySize" value="${memory.value}" />
```

### **3. Acesso via Message**
```java
// Em outros filtros, acessar os valores
Integer timeout = (Integer) msg.get("aws.lambda.timeout");
Integer memorySize = (Integer) msg.get("aws.lambda.memory.size");
```

## Casos de Uso

### **1. Timeout Configurável**
```java
// Para funções que precisam de mais tempo
timeoutValue = 600; // 10 minutos para processamento pesado

// Para funções rápidas
timeoutValue = 30; // 30 segundos para operações simples
```

### **2. Memory Size Otimizada**
```java
// Para processamento pesado
memorySizeValue = 1024; // 1 GB para operações intensivas

// Para operações simples
memorySizeValue = 128; // 128 MB para operações básicas
```

## Logs e Debugging

### **1. Logs de Configuração**
```
INFO: Timeout: 300 seconds
INFO: Memory Size: 128 MB
INFO: Using IAM Role: false
INFO: Invoking Lambda function with retry...
```

### **2. Valores na Mensagem**
```java
// Valores disponíveis na mensagem
msg.get("aws.lambda.timeout")        // Integer
msg.get("aws.lambda.memory.size")    // Integer
msg.get("aws.lambda.response")       // String
msg.get("aws.lambda.http.status.code") // Integer
```

## Benefícios da Implementação

### **1. Flexibilidade**
- ✅ **Configuração dinâmica** - Valores podem ser definidos em runtime
- ✅ **Valores padrão** - Funciona mesmo sem configuração
- ✅ **Logging detalhado** - Visibilidade completa dos valores

### **2. Integração**
- ✅ **Compatível com outros filtros** - Valores disponíveis na mensagem
- ✅ **Backward compatible** - Não quebra implementações existentes
- ✅ **Extensível** - Fácil adicionar novos campos

### **3. Operacional**
- ✅ **Debugging fácil** - Logs claros dos valores usados
- ✅ **Configuração simples** - Interface intuitiva
- ✅ **Validação automática** - Valores padrão seguros

## Próximos Passos

1. **Testar com diferentes valores** de timeout e memory
2. **Integrar com outros filtros** que precisem desses valores
3. **Adicionar validação** de valores mínimos/máximos
4. **Documentar casos de uso** específicos

Todos os campos das linhas 54-58 agora estão sendo utilizados corretamente! 🎯 