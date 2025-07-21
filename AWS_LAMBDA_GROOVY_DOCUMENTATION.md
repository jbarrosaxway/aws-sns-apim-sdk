# AWS Lambda Integration for Axway API Gateway

Este documento descreve como integrar AWS Lambda com o Axway API Gateway usando scripts Groovy, incluindo configuração para ambientes Kubernetes.

## Visão Geral

A integração permite invocar funções AWS Lambda diretamente do Axway API Gateway através de filtros de script Groovy, oferecendo flexibilidade para autenticação e configuração de credenciais.

## Versão Testada

✅ **Testado e validado no Axway API Gateway versão 7.7.0.20240830**

## Pré-requisitos

- Axway API Gateway 7.7.0.20240830 (testado)
- AWS SDK for Java 1.12.314 (aws-java-sdk-lambda, aws-java-sdk-core)
- Jackson (incluído no gateway)
- Acesso a funções AWS Lambda
- Credenciais AWS configuradas

## Configuração

### 1. Dependências

O script utiliza as seguintes dependências que devem estar disponíveis no classpath:

#### JARs Necessários (Versões Testadas):
- `aws-java-sdk-lambda-1.12.314.jar`
- `aws-java-sdk-core-1.12.314.jar`
- Jackson (incluído no gateway - não requer JARs adicionais)

#### Localização dos JARs:
Os JARs devem estar no diretório `ext/lib` do gateway. Exemplo de estrutura:
```
<VORDEL_HOME>/groups/group-<X>/instance-<Y>/ext/lib/
```

**Exemplo**: `/opt/axway/Axway-7.7.0.20240830/apigateway/groups/group-2/instance-1/ext/lib/`

**Nota**: O caminho pode variar dependendo da sua instalação. Ajuste conforme necessário.

### 2. Configuração do Policy Studio

**IMPORTANTE**: Os JARs no diretório `ext/lib` não são automaticamente incluídos no classpath do Policy Studio. É necessário adicioná-los manualmente:

1. Abra o Policy Studio
2. Vá em **Window > Preferences > Runtime Dependencies**
3. Clique em **Add** e navegue até o diretório `ext/lib`
4. Selecione os JARs necessários:
   - `aws-java-sdk-lambda-1.12.314.jar`
   - `aws-java-sdk-core-1.12.314.jar`
5. Clique em **Apply** para salvar
6. Reinicie o Policy Studio com a opção `-clean`

### 3. Instalação

1. Copie o conteúdo do arquivo `aws-lambda-filter.groovy` para o filtro de script do Policy Studio
2. Configure os parâmetros necessários
3. Configure as credenciais AWS
4. Teste a integração

## Script Groovy para AWS Lambda

### Script Principal

O script principal está disponível no arquivo `aws-lambda-filter.groovy`. Este script implementa:

- Autenticação flexível com AWS (variáveis de ambiente, arquivo de credenciais, IAM Roles)
- Configuração dinâmica via atributos da mensagem
- Sistema de retry automático
- Processamento de requisições HTTP
- Invocação de funções Lambda
- Tratamento de respostas JSON e não-JSON
- Logging detalhado para troubleshooting
- Gerenciamento adequado de recursos

Para usar o script:

1. Abra o arquivo `aws-lambda-filter.groovy` em um editor de texto
2. Copie todo o conteúdo do arquivo
3. No Policy Studio, crie um filtro de script e cole o conteúdo
4. Configure os parâmetros necessários
5. Teste a integração

### Parâmetros de Configuração

O script aceita os seguintes parâmetros via atributos da mensagem:

#### Parâmetros Obrigatórios:

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `aws.lambda.function.name` | String | Nome da função Lambda |

#### Parâmetros Opcionais:

| Parâmetro | Tipo | Padrão | Descrição |
|-----------|------|--------|-----------|
| `aws.lambda.region` | String | `AWS_DEFAULT_REGION` | Região AWS |
| `aws.lambda.payload` | String | `content.body` ou `"{}"` | Payload para a função |
| `aws.lambda.invocation.type` | String | `"RequestResponse"` | Tipo de invocação |
| `aws.lambda.log.type` | String | `"None"` | Tipo de log |
| `aws.lambda.qualifier` | String | - | Versão ou alias da função |
| `aws.lambda.client.context` | String | - | Contexto do cliente (JSON string) |
| `aws.lambda.custom.headers` | String | - | Headers customizados (JSON string) |
| `aws.lambda.max.retries` | String | `"3"` | Número máximo de tentativas |
| `aws.lambda.retry.delay.ms` | String | `"1000"` | Delay entre tentativas em ms |

### Atributos de Saída

O script define os seguintes atributos na mensagem:

| Atributo | Tipo | Descrição |
|----------|------|-----------|
| `aws.lambda.response` | String | Resposta da função Lambda |
| `aws.lambda.http.status.code` | Integer | Código de status HTTP |
| `aws.lambda.executed.version` | String | Versão executada da função |
| `aws.lambda.log.result` | String | Resultado dos logs |
| `aws.lambda.error` | String | Erro (se houver) |

## Configuração de Credenciais AWS

### 1. Arquivo de Credenciais (Recomendado - Mais Seguro)

**⚠️ Recomendação de Segurança**: Use arquivo de credenciais em vez de variáveis de ambiente, especialmente em Kubernetes, pois variáveis de ambiente podem ser facilmente interceptadas ou lidas pela aplicação.

Configure o arquivo `~/.aws/credentials`:

```ini
[default]
aws_access_key_id = sua_access_key
aws_secret_access_key = sua_secret_key
aws_session_token = seu_session_token  # opcional
```

### 2. Variáveis de Ambiente (Menos Seguro)

**⚠️ Aviso**: Variáveis de ambiente em Kubernetes podem ser facilmente interceptadas ou lidas pela aplicação. Use apenas para desenvolvimento/teste.

```bash
export AWS_ACCESS_KEY_ID="sua_access_key"
export AWS_SECRET_ACCESS_KEY="sua_secret_key"
export AWS_SESSION_TOKEN="seu_session_token"  # opcional
export AWS_DEFAULT_REGION="us-east-1"
```

### 3. IAM Roles (Mais Seguro - para EKS/EC2)

Configure IAM Roles para instâncias EC2 ou pods EKS. Esta é a opção mais segura para ambientes de produção.

## Configuração para Kubernetes

### 1. Secret para Credenciais AWS

#### Opção 1: Secret com Arquivo de Credenciais (Recomendado - Mais Seguro)

```bash
kubectl create secret generic aws-credentials \
  --from-file=credentials=/home/USUARIO/.aws/credentials \
  --namespace=axway
```

**Nota**: Substitua `/home/USUARIO` pelo caminho completo do seu diretório home. O `~` não funciona no kubectl.

Esta opção monta o arquivo de credenciais AWS completo no container, permitindo o uso de múltiplos perfis e é mais segura que variáveis de ambiente.

#### Opção 2: Secret com Variáveis de Ambiente (Menos Seguro)

**⚠️ Aviso**: Variáveis de ambiente podem ser facilmente interceptadas. Use apenas para desenvolvimento/teste.

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: aws-credentials
  namespace: axway
type: Opaque
data:
  AWS_ACCESS_KEY_ID: <base64-encoded-access-key>
  AWS_SECRET_ACCESS_KEY: <base64-encoded-secret-key>
  AWS_SESSION_TOKEN: <base64-encoded-session-token>  # opcional
```

### 2. Configuração no values.yaml (Testado)

Para ambientes Kubernetes com Helm, configure o `values.yaml` do APIM com as seguintes seções:

#### Para apimgr e apitraffic:

```yaml
apimgr:
  # ... outras configurações ...
  extraVolumeMounts:
    # ... outros volumes ...
    # Configuração AWS - Diretório .aws padrão
    - name: aws-config-volume
      mountPath: /opt/axway/apigateway/system/conf/.aws
      readOnly: true
  extraVolumes:
    # ... outros volumes ...
    # Volume para credenciais AWS
    - name: aws-config-volume
      secret:
        secretName: aws-credentials
        items:
          - key: credentials
            path: credentials
  extraEnvVars:
    # ... outras variáveis ...
    # Configurações AWS
    - name: AWS_SHARED_CREDENTIALS_FILE
      value: "/opt/axway/apigateway/system/conf/.aws/credentials"
    - name: AWS_DEFAULT_REGION
      value: "us-east-1"
    # Opcional: Configurar perfil específico se necessário
    # - name: AWS_PROFILE
    #   value: "default"

apitraffic:
  # ... outras configurações ...
  extraVolumeMounts:
    # ... outros volumes ...
    # Configuração AWS - Diretório .aws padrão
    - name: aws-config-volume
      mountPath: /opt/axway/apigateway/system/conf/.aws
      readOnly: true
  extraVolumes:
    # ... outros volumes ...
    # Volume para credenciais AWS
    - name: aws-config-volume
      secret:
        secretName: aws-credentials
        items:
          - key: credentials
            path: credentials
  extraEnvVars:
    # ... outras variáveis ...
    # Configurações AWS
    - name: AWS_SHARED_CREDENTIALS_FILE
      value: "/opt/axway/apigateway/system/conf/.aws/credentials"
    - name: AWS_DEFAULT_REGION
      value: "us-east-1"
    # Opcional: Configurar perfil específico se necessário
    # - name: AWS_PROFILE
    #   value: "default"
```

#### Configuração Alternativa: Deployment Simples

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: axway-api-gateway
  namespace: axway
spec:
  replicas: 1
  selector:
    matchLabels:
      app: axway-api-gateway
  template:
    metadata:
      labels:
        app: axway-api-gateway
    spec:
      containers:
      - name: axway-gateway
        image: axway/api-gateway:latest
        env:
        - name: AWS_ACCESS_KEY_ID
          valueFrom:
            secretKeyRef:
              name: aws-credentials
              key: AWS_ACCESS_KEY_ID
        - name: AWS_SECRET_ACCESS_KEY
          valueFrom:
            secretKeyRef:
              name: aws-credentials
              key: AWS_SECRET_ACCESS_KEY
        - name: AWS_SESSION_TOKEN
          valueFrom:
            secretKeyRef:
              name: aws-credentials
              key: AWS_SESSION_TOKEN
        - name: AWS_DEFAULT_REGION
          value: "us-east-1"

### 3. Criação do Secret

#### Para Secret com Arquivo de Credenciais (Recomendado - Mais Seguro):

```bash
kubectl create secret generic aws-credentials \
  --from-file=credentials=/home/USUARIO/.aws/credentials \
  --namespace=axway
```

**Nota**: Substitua `/home/USUARIO` pelo caminho completo do seu diretório home. O `~` não funciona no kubectl.

#### Para Secret com Variáveis de Ambiente (Menos Seguro):

```bash
kubectl create secret generic aws-credentials \
  --from-literal=AWS_ACCESS_KEY_ID="sua_access_key" \
  --from-literal=AWS_SECRET_ACCESS_KEY="sua_secret_key" \
  --namespace=axway
```

## Configuração Alternativa com IAM Roles

Para ambientes EKS, você pode usar IAM Roles em vez de credenciais:

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
        # Sem variáveis de ambiente - usa IAM Role
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: axway-gateway-sa
  annotations:
    eks.amazonaws.com/role-arn: arn:aws:iam::ACCOUNT:role/axway-lambda-role
```

## Monitoramento e Troubleshooting

### Logs

O script gera logs detalhados que podem ser monitorados:

- `Trace.info()`: Informações de sucesso e configuração
- `Trace.warning()`: Avisos sobre configuração
- `Trace.error()`: Erros de execução

### Variáveis de Ambiente Suportadas

- `AWS_ACCESS_KEY_ID`: Chave de acesso AWS
- `AWS_SECRET_ACCESS_KEY`: Chave secreta AWS
- `AWS_SESSION_TOKEN`: Token de sessão (opcional)
- `AWS_DEFAULT_REGION`: Região padrão
- `AWS_PROFILE`: Perfil AWS
- `AWS_SHARED_CREDENTIALS_FILE`: Caminho para arquivo de credenciais

### Solução de Problemas Comuns

1. **Erro de credenciais**: Verifique se as variáveis de ambiente estão definidas ou se o arquivo de credenciais existe
2. **Erro de região**: Verifique se a região está correta e a função existe
3. **Erro de timeout**: Aumente o valor do parâmetro `aws.lambda.retry.delay.ms`
4. **Erro de função não encontrada**: Verifique o nome da função e a região
5. **Erro de classpath**: Verifique se os JARs foram adicionados ao Policy Studio em **Window > Preferences > Runtime Dependencies**

## Segurança

- Use IAM Roles quando possível em vez de credenciais estáticas
- Rotacione credenciais regularmente
- Use políticas IAM com privilégios mínimos
- Monitore logs de acesso e execução
- Considere usar AWS Secrets Manager para credenciais sensíveis

## Exemplo de Uso

1. Configure o filtro no Policy Studio com os parâmetros necessários
2. Configure credenciais AWS (variáveis de ambiente, arquivo ou IAM Role)
3. Defina os atributos da mensagem com os parâmetros desejados
4. Teste a integração com uma requisição HTTP
5. Monitore logs para verificar funcionamento

A integração está pronta para uso em ambientes de produção com configurações de segurança apropriadas. 