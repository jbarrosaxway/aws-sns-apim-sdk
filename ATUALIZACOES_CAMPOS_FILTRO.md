# Atualizações dos Campos do Filtro AWS Lambda

## ✅ **Arquivos Atualizados**

### **1. ✅ `AWSLambdaProcessor.java`**
- **Implementação completa de autenticação flexível**
- **Sistema de retry configurável**
- **Processamento avançado de resposta**
- **Logging detalhado**

### **2. ✅ `AWSLambdaFilter.yaml`**
- **Novos parâmetros de configuração**
- **Valores padrão apropriados**
- **Suporte a configurações opcionais**

### **3. ✅ `AWSLambdaDesc.xml`**
- **Definição dos novos campos no Entity Store**
- **Documentação atualizada**
- **Mensagens de internacionalização melhoradas**

## 📋 **Campos Implementados**

### **Campos Obrigatórios:**
| Campo | Tipo | Descrição |
|-------|------|-----------|
| `functionName` | String | Nome da função Lambda |

### **Campos Opcionais:**
| Campo | Tipo | Padrão | Descrição |
|-------|------|--------|-----------|
| `awsRegion` | String | AWS_DEFAULT_REGION | Região AWS |
| `invocationType` | String | RequestResponse | Tipo de invocação |
| `logType` | String | None | Tipo de log |
| `qualifier` | String | - | Versão ou alias da função |
| `maxRetries` | String | 3 | Máximo de tentativas |
| `retryDelay` | String | 1000 | Delay entre tentativas (ms) |

## 🔧 **Configuração no Policy Studio**

### **1. Parâmetros do Filtro:**
```
functionName: minha-funcao-lambda
awsRegion: us-east-1
invocationType: RequestResponse
logType: None
qualifier: $LATEST
maxRetries: 3
retryDelay: 1000
```

### **2. Atributos de Saída:**
```
aws.lambda.response: Resposta da função Lambda
aws.lambda.http.status.code: Código de status HTTP
aws.lambda.executed.version: Versão executada da função
aws.lambda.log.result: Resultado dos logs
aws.lambda.error: Erro (se houver)
```

## 📁 **Estrutura de Arquivos**

```
src/main/resources/
├── yaml/
│   └── META-INF/types/Entity/Filter/AWSFilter/
│       └── AWSLambdaFilter.yaml          ✅ Atualizado
├── fed/
│   ├── AWSLambdaDesc.xml                 ✅ Atualizado
│   └── AWSLambdaTypeSet.xml              ✅ OK
└── com/
    └── axway/aws/lambda/
        └── AWSLambdaProcessor.java        ✅ Atualizado
```

## 🚀 **Funcionalidades Implementadas**

### **✅ Autenticação Flexível:**
- Variáveis de ambiente (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`)
- Arquivo de credenciais (`AWS_SHARED_CREDENTIALS_FILE`)
- IAM Roles (`DefaultAWSCredentialsProviderChain`)

### **✅ Configuração Avançada:**
- Parâmetros opcionais com valores padrão
- Configuração dinâmica via parâmetros do filtro
- Suporte a múltiplas estratégias de credenciais

### **✅ Sistema de Retry:**
- Número máximo de tentativas configurável
- Delay entre tentativas configurável
- Logging detalhado de cada tentativa

### **✅ Processamento de Resposta:**
- Extração da resposta UTF-8
- Verificação de erros da função Lambda
- Verificação de status HTTP
- Armazenamento de atributos completos

## 📝 **Exemplo de Uso**

### **Configuração Básica:**
```xml
<AWSLambdaFilter>
    <functionName>minha-funcao-lambda</functionName>
    <awsRegion>us-east-1</awsRegion>
</AWSLambdaFilter>
```

### **Configuração Avançada:**
```xml
<AWSLambdaFilter>
    <functionName>minha-funcao-lambda</functionName>
    <awsRegion>us-east-1</awsRegion>
    <invocationType>RequestResponse</invocationType>
    <logType>Tail</logType>
    <qualifier>$LATEST</qualifier>
    <maxRetries>5</maxRetries>
    <retryDelay>2000</retryDelay>
</AWSLambdaFilter>
```

## ✅ **Conclusão**

Todos os arquivos de configuração do filtro foram atualizados para suportar:

1. **✅ Autenticação Flexível**: Mesmas estratégias do script Groovy
2. **✅ Configuração Avançada**: Parâmetros opcionais com valores padrão
3. **✅ Sistema de Retry**: Configurável e robusto
4. **✅ Logging Detalhado**: Para troubleshooting
5. **✅ Compatibilidade**: Mesmo comportamento do script Groovy
6. **✅ Segurança**: Suporte a múltiplas estratégias de credenciais

O filtro Java agora está **completamente alinhado** com as funcionalidades do script Groovy! 🚀 