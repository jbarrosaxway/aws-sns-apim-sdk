# Suporte a IAM Roles - AWS Lambda

## Visão Geral

Implementei suporte a AWS IAM Roles no filtro Lambda, permitindo usar credenciais automáticas de instâncias EC2, containers ECS, ou outros ambientes que suportam IAM Roles.

## Implementação

### **1. Interface XML**

```xml
<CheckboxAttribute field="useIAMRole" label="AWS_LAMBDA_USE_IAM_ROLE_LABEL"
    displayName="AWS_LAMBDA_USE_IAM_ROLE_NAME" description="AWS_LAMBDA_USE_IAM_ROLE_DESCRIPTION" />

<ReferenceSelector field="awsCredential" 
    selectableTypes="ApiKeyProfile" label="CHOOSE_AWS_CREDENTTIAL_LABEL"
    title="CHOOSE_AWS_CREDENTTIAL_DIALOG_TITLE" searches="AuthProfilesGroup,ApiKeyGroup,ApiKeyProviderProfile" />
```

**Características:**
- ✅ **Checkbox para IAM Role** - Ativa/desativa uso de IAM Role
- ✅ **Credencial opcional** - Não obrigatória quando IAM Role está ativo
- ✅ **Interface intuitiva** - Fácil de configurar

### **2. Lógica de Credenciais**

```java
private AWSCredentialsProvider getCredentialsProvider(ConfigContext ctx, Entity entity) throws EntityStoreException {
    Boolean useIAMRoleValue = Boolean.valueOf(useIAMRole.getLiteral());
    
    if (useIAMRoleValue != null && useIAMRoleValue) {
        // Use IAM Role (EC2 Instance Profile or ECS Task Role)
        Trace.info("Using IAM Role credentials (Instance Profile/Task Role)");
        return new EC2ContainerCredentialsProviderWrapper();
    } else {
        // Use explicit credentials via AWSFactory (following S3 pattern)
        try {
            AWSCredentials awsCredentials = AWSFactory.getCredentials(ctx, entity);
            Trace.info("Using explicit AWS credentials");
            return getAWSCredentialsProvider(awsCredentials);
        } catch (Exception e) {
            Trace.error("Error getting explicit credentials: " + e.getMessage());
            Trace.info("Falling back to DefaultAWSCredentialsProviderChain");
            return new DefaultAWSCredentialsProviderChain();
        }
    }
}
```

## Cenários de Uso

### **1. EC2 Instance Profile**
```java
// Quando executando em EC2 com IAM Role
// O EC2ContainerCredentialsProviderWrapper automaticamente:
// - Detecta que está rodando em EC2
// - Usa o Instance Profile associado
// - Renova credenciais automaticamente
```

### **2. ECS Task Role**
```java
// Quando executando em ECS com Task Role
// O EC2ContainerCredentialsProviderWrapper automaticamente:
// - Detecta que está rodando em ECS
// - Usa o Task Role associado
// - Renova credenciais automaticamente
```

### **3. Lambda Function Role**
```java
// Quando executando em Lambda
// O EC2ContainerCredentialsProviderWrapper automaticamente:
// - Detecta que está rodando em Lambda
// - Usa o Execution Role da função
// - Renova credenciais automaticamente
```

### **4. Credenciais Explícitas**
```java
// Quando IAM Role não está ativo
// Usa credenciais explícitas via AWSFactory
// - ApiKeyProfile configurado
// - Fallback para DefaultAWSCredentialsProviderChain
```

## Vantagens do IAM Role

### **1. Segurança**
- ✅ **Sem credenciais hardcoded** - Não expõe access keys
- ✅ **Rotação automática** - Credenciais renovadas automaticamente
- ✅ **Princípio do menor privilégio** - Permissões específicas por role

### **2. Simplicidade**
- ✅ **Configuração zero** - Não precisa configurar credenciais
- ✅ **Deploy fácil** - Apenas associa IAM Role ao recurso
- ✅ **Manutenção reduzida** - Sem gerenciar credenciais

### **3. Flexibilidade**
- ✅ **Múltiplos ambientes** - EC2, ECS, Lambda, etc.
- ✅ **Escalabilidade** - Funciona com qualquer número de instâncias
- ✅ **Auditoria** - Logs de uso de credenciais

## Como Configurar

### **1. Para EC2**
```bash
# Criar IAM Role
aws iam create-role --role-name EC2LambdaRole --assume-role-policy-document file://trust-policy.json

# Anexar política
aws iam attach-role-policy --role-name EC2LambdaRole --policy-arn arn:aws:iam::aws:policy/AWSLambdaFullAccess

# Anexar à instância EC2
aws ec2 associate-iam-instance-profile --instance-id i-1234567890abcdef0 --iam-instance-profile Name=EC2LambdaRole
```

### **2. Para ECS**
```json
{
  "family": "lambda-task",
  "taskRoleArn": "arn:aws:iam::123456789012:role/ECSLambdaRole",
  "executionRoleArn": "arn:aws:iam::123456789012:role/ECSExecutionRole",
  "containerDefinitions": [
    {
      "name": "lambda-container",
      "image": "your-lambda-image"
    }
  ]
}
```

### **3. Para Lambda**
```json
{
  "FunctionName": "my-lambda-function",
  "Role": "arn:aws:iam::123456789012:role/LambdaExecutionRole"
}
```

## Configuração no Policy Studio

### **1. Usar IAM Role**
1. Marcar checkbox "Use IAM Role"
2. Deixar campo "AWS Credential" vazio
3. Configurar região e outras opções

### **2. Usar Credenciais Explícitas**
1. Desmarcar checkbox "Use IAM Role"
2. Selecionar "AWS Credential" apropriado
3. Configurar região e outras opções

## Logs e Debugging

### **1. Logs de Credenciais**
```
INFO: Using IAM Role credentials (Instance Profile/Task Role)
INFO: Using explicit AWS credentials
INFO: Falling back to DefaultAWSCredentialsProviderChain
```

### **2. Verificação de Permissões**
```bash
# Verificar se a instância tem IAM Role
curl http://169.254.169.254/latest/meta-data/iam/security-credentials/

# Verificar credenciais temporárias
curl http://169.254.169.254/latest/meta-data/iam/security-credentials/role-name
```

## Benefícios da Implementação

### **1. Compatibilidade**
- ✅ **Backward compatible** - Funciona com credenciais existentes
- ✅ **Flexível** - Escolha entre IAM Role ou credenciais explícitas
- ✅ **Padrão AWS** - Segue melhores práticas da AWS

### **2. Segurança**
- ✅ **Sem credenciais expostas** - IAM Role não expõe access keys
- ✅ **Rotação automática** - Credenciais renovadas pelo AWS
- ✅ **Auditoria completa** - Logs de uso de credenciais

### **3. Operacional**
- ✅ **Deploy simplificado** - Menos configuração manual
- ✅ **Manutenção reduzida** - Sem gerenciar credenciais
- ✅ **Escalabilidade** - Funciona com qualquer número de instâncias

A implementação agora suporta tanto IAM Roles quanto credenciais explícitas, proporcionando máxima flexibilidade e segurança! 🚀 