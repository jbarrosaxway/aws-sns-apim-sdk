# AWS Lambda Java Filter Documentation

Este documento descreve o filtro Java AWS Lambda para Axway API Gateway, incluindo configuração, instalação, testes e troubleshooting.

## Visão Geral

O filtro Java AWS Lambda oferece uma interface gráfica no Policy Studio para invocar funções AWS Lambda com configuração avançada, incluindo retry logic, seleção de região e tipos de invocação.

## Versão Testada

✅ **Testado e validado no Axway API Gateway versão 7.7.0.20240830**

## Configuração do Filtro

### Interface Gráfica

O filtro apresenta uma interface gráfica no Policy Studio com os seguintes campos:

#### **Campos de Configuração:**

| Campo | Tipo | Obrigatório | Padrão | Descrição |
|-------|------|-------------|--------|-----------|
| **Name** | String | ✅ | - | Nome de exibição do filtro |
| **Function Name** | String | ✅ | - | Nome da função AWS Lambda |
| **AWS Region** | String | ❌ | `us-east-1` | Região AWS onde a função está localizada |
| **Invocation Type** | Dropdown | ❌ | `RequestResponse` | Tipo de invocação: `RequestResponse`, `Event`, `DryRun` |
| **Log Type** | Dropdown | ❌ | `None` | Tipo de log: `None`, `Tail` |
| **Qualifier** | String | ❌ | - | Versão ou alias da função Lambda |
| **Max Retries** | Number | ❌ | `3` | Número máximo de tentativas |
| **Retry Delay (ms)** | Number | ❌ | `1000` | Delay entre tentativas em milissegundos |

### Tipos de Invocação

- **RequestResponse**: Invocação síncrona (aguarda resposta)
- **Event**: Invocação assíncrona (não aguarda resposta)
- **DryRun**: Simulação (não executa a função)

### Tipos de Log

- **None**: Sem logs de execução
- **Tail**: Inclui logs da execução da função

## Instalação

### 1. Copiar Arquivos JAR

Copie os seguintes arquivos para o diretório `ext/lib` do gateway:

```bash
# Caminho padrão para container
/opt/Axway/apigateway/groups/group-2/instance-1/ext/lib/

# Exemplo específico
/opt/axway/Axway-7.7.0.20240830/apigateway/groups/group-2/instance-1/ext/lib/
```

**Arquivos necessários:**
- `aws-lambda-apim-sdk-<versao>.jar` - Filtro principal
- `dependencies/external-aws-java-sdk-lambda-<versao>.jar` - Dependência AWS SDK

### 2. Adicionar ao Policy Studio

1. Abra o Policy Studio
2. Vá em **Window > Preferences > Runtime Dependencies**
3. Clique em **Add** e navegue até o diretório `ext/lib`
4. Selecione os JARs:
   - `aws-lambda-apim-sdk-<versao>.jar`
   - `external-aws-java-sdk-lambda-<versao>.jar`
5. Clique em **Apply** para salvar
6. Reinicie o Policy Studio com a opção `-clean`

### 3. Reiniciar Gateway

```bash
# Parar o gateway
apigateway stop

# Iniciar o gateway
apigateway start
```

## Configuração de Credenciais AWS

### 1. IAM Roles (Recomendado para Produção)

**Para EKS (Kubernetes):**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: axway-api-gateway
spec:
  template:
    spec:
      serviceAccountName: axway-gateway-sa
      containers:
      - name: axway-gateway
        image: axway/api-gateway:latest
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: axway-gateway-sa
  annotations:
    eks.amazonaws.com/role-arn: arn:aws:iam::ACCOUNT:role/axway-lambda-role
```

**Para EC2:**
- Anexe um IAM Role à instância EC2
- O filtro Java detectará automaticamente as credenciais

### 2. Variáveis de Ambiente

```bash
export AWS_ACCESS_KEY_ID="sua_access_key"
export AWS_SECRET_ACCESS_KEY="sua_secret_key"
export AWS_DEFAULT_REGION="us-east-1"
```

### 3. Arquivo de Credenciais

Configure `~/.aws/credentials`:
```ini
[default]
aws_access_key_id = sua_access_key
aws_secret_access_key = sua_secret_key
```

## Uso no Policy Studio

### 1. Adicionar Filtro

1. Abra o Policy Studio
2. Procure por **"AWS Lambda Filter"** na paleta
3. Arraste o filtro para a política

### 2. Configurar Parâmetros

Preencha os campos conforme necessário:

- **Function Name**: Nome da função Lambda (obrigatório)
- **AWS Region**: Região da função (opcional, usa padrão)
- **Invocation Type**: Tipo de invocação (opcional)
- **Log Type**: Tipo de log (opcional)
- **Qualifier**: Versão/alias da função (opcional)
- **Max Retries**: Número de tentativas (opcional)
- **Retry Delay**: Delay entre tentativas (opcional)

### 3. Testar Configuração

1. Clique em **Finish** para salvar
2. Teste a política com uma requisição HTTP
3. Verifique os logs para confirmar funcionamento

## Atributos de Saída

O filtro define os seguintes atributos na mensagem:

| Atributo | Tipo | Descrição |
|----------|------|-----------|
| `aws.lambda.response` | String | Resposta da função Lambda |
| `aws.lambda.http.status.code` | Integer | Código de status HTTP |
| `aws.lambda.executed.version` | String | Versão executada da função |
| `aws.lambda.log.result` | String | Resultado dos logs |
| `aws.lambda.error` | String | Erro (se houver) |

## Testes

### 1. Teste com Entity Store (YAML) ✅

**Status**: Testado e funcionando

O filtro foi testado com Entity Store no formato YAML e está funcionando corretamente.

### 2. Teste com Entity Store (XML) ❌

**Status**: **NÃO TESTADO**

O filtro ainda não foi testado com Entity Store no formato XML. É necessário realizar testes para validar:

- Compatibilidade com formato XML
- Funcionamento com diferentes estruturas de dados
- Performance com arquivos XML grandes

### 3. Teste de Performance

**Recomendações:**
- Teste com diferentes tamanhos de payload
- Monitore tempo de resposta
- Verifique uso de memória
- Teste com múltiplas invocações simultâneas

## Troubleshooting

### Problemas Comuns

1. **Filtro não aparece na paleta:**
   - Verifique se o JAR foi adicionado ao classpath
   - Reinicie o Policy Studio com `-clean`
   - Confirme se os arquivos estão em `ext/lib`

2. **Erro de credenciais AWS:**
   - Verifique se as credenciais estão configuradas
   - Teste com `aws sts get-caller-identity`
   - Confirme se o IAM Role tem permissões adequadas

3. **Erro de função não encontrada:**
   - Verifique o nome da função e a região
   - Confirme se a função existe na AWS
   - Verifique permissões de invocação

4. **Erro de timeout:**
   - Aumente o valor do parâmetro `Retry Delay (ms)`
   - Verifique se a função Lambda não está demorando muito
   - Considere usar `Event` em vez de `RequestResponse`

### Logs

O filtro gera logs detalhados:

- **Sucesso**: "Invocação Lambda realizada com sucesso"
- **Falha**: "Falha após X tentativas"
- **Erro**: "Erro na função Lambda: [detalhes]"

### Variáveis de Ambiente Suportadas

- `AWS_ACCESS_KEY_ID`: Chave de acesso AWS
- `AWS_SECRET_ACCESS_KEY`: Chave secreta AWS
- `AWS_SESSION_TOKEN`: Token de sessão (opcional)
- `AWS_DEFAULT_REGION`: Região padrão
- `AWS_PROFILE`: Perfil AWS
- `AWS_SHARED_CREDENTIALS_FILE`: Caminho para arquivo de credenciais

## Segurança

- Use IAM Roles quando possível em vez de credenciais estáticas
- Rotacione credenciais regularmente
- Use políticas IAM com privilégios mínimos
- Monitore logs de acesso e execução
- Considere usar AWS Secrets Manager para credenciais sensíveis

## Comparação com Script Groovy

| Aspecto | Filtro Java | Script Groovy |
|---------|-------------|---------------|
| **Interface** | Gráfica no Policy Studio | Script de texto |
| **Configuração** | Parâmetros visuais | Variáveis no script |
| **Manutenção** | Requer rebuild do JAR | Edição direta do script |
| **Teste XML** | ❌ Não testado | ❌ Não testado |
| **Teste YAML** | ✅ Testado | ✅ Testado |

## Próximos Passos

1. **Testar Entity Store XML** - Validar compatibilidade
2. **Testes de Performance** - Avaliar performance com diferentes cargas
3. **Documentação de Casos de Uso** - Exemplos práticos
4. **Monitoramento** - Implementar métricas de uso

## Exemplo de Configuração

```
Name: AWS Lambda
Function Name: test-echo-lambda
AWS Region: us-east-1
Invocation Type: RequestResponse
Log Type: None
Qualifier: (vazio)
Max Retries: 3
Retry Delay (ms): 1000
```

Esta configuração invoca a função `test-echo-lambda` na região `us-east-1` de forma síncrona, com até 3 tentativas e delay de 1 segundo entre tentativas. 