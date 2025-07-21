# AWS Lambda Integration for Axway API Gateway

Este projeto oferece integração com AWS Lambda através de filtros customizados para o Axway API Gateway, suportando tanto filtros Java quanto scripts Groovy.

## API Management Version Compatibility

Este artefato foi testado com sucesso nas seguintes versões:
- **Axway API Gateway 7.7.0.20240830** ✅

## Visão Geral

O projeto oferece duas abordagens para integração com AWS Lambda:

### 1. Filtro Java (Recomendado)
- Interface gráfica no Policy Studio
- Configuração via parâmetros visuais
- Performance nativa do gateway
- Build automatizado

### 2. Script Groovy (Alternativa)
- Flexibilidade total
- Edição direta do script
- Configuração dinâmica
- Debugging detalhado

## Build e Instalação

### Linux
```bash
# Build e instalação automática
./gradlew clean build installLinux
```

### Windows
```bash
# Build e instalação interativa
./gradlew clean build installWindows
```

### Comandos Úteis
```bash
# Ver todas as tasks disponíveis
./gradlew showTasks

# Mostrar links dos JARs AWS SDK
./gradlew showAwsJars

# Apenas build
./gradlew clean build
```

## Instalação Manual (Alternativa)

### Linux

1. **Build e instalação automática:**
   ```bash
   ./gradlew clean build
   ./scripts/linux/install-filter.sh
   ```

2. **Configurar Policy Studio:**
   - Abra o Policy Studio
   - Vá em **Window > Preferences > Runtime Dependencies**
   - Adicione o JAR: `/opt/axway/Axway-7.7.0.20240830/apigateway/groups/group-2/instance-1/ext/lib/aws-lambda-apim-sdk-1.0.1.jar`
   - Reinicie o Policy Studio com `-clean`

### Windows

1. **Configurar projeto (primeira vez):**
   ```powershell
   .\scripts\windows\configurar-projeto-windows.ps1
   ```

2. **Instalar arquivos YAML:**
   ```powershell
   .\scripts\windows\install-filter-windows.ps1
   ```
   ou
   ```cmd
   scripts\windows\install-filter-windows.cmd
   ```

3. **Download manual dos JARs AWS SDK:**
   - [aws-java-sdk-lambda-1.12.314.jar](https://repo1.maven.org/maven2/com/amazonaws/aws-java-sdk-lambda/1.12.314/aws-java-sdk-lambda-1.12.314.jar)
   - [aws-java-sdk-core-1.12.314.jar](https://repo1.maven.org/maven2/com/amazonaws/aws-java-sdk-core/1.12.314/aws-java-sdk-core-1.12.314.jar)

4. **Configurar Policy Studio:**
   - Adicione os JARs AWS SDK ao classpath
   - Reinicie o Policy Studio com `-clean`

## Configuração AWS

### Credenciais

#### 1. Arquivo de Credenciais (Recomendado)
```ini
# ~/.aws/credentials
[default]
aws_access_key_id = sua_access_key
aws_secret_access_key = sua_secret_key
aws_session_token = seu_session_token  # opcional
```

#### 2. Variáveis de Ambiente
```bash
export AWS_ACCESS_KEY_ID="sua_access_key"
export AWS_SECRET_ACCESS_KEY="sua_secret_key"
export AWS_SESSION_TOKEN="seu_session_token"  # opcional
export AWS_DEFAULT_REGION="us-east-1"
```

#### 3. IAM Roles (para EKS/EC2)
Configure IAM Roles para instâncias EC2 ou pods EKS.

## Uso

### Filtro Java

1. **Adicionar filtro à política:**
   - Abra o Policy Studio
   - Procure por **"AWS Lambda Filter"** na paleta
   - Arraste o filtro para a política

2. **Configurar parâmetros:**
   - `functionName` (obrigatório): Nome da função Lambda
   - `awsProfileName` (opcional): Perfil AWS (padrão: `default`)

3. **Atributos de saída:**
   - `aws.lambda.response`: Resposta da função Lambda
   - `aws.lambda.http.status.code`: Código de status HTTP

### Script Groovy

Para informações detalhadas sobre o script Groovy, incluindo configuração Kubernetes, troubleshooting e parâmetros específicos, consulte o arquivo `AWS_LAMBDA_GROOVY_DOCUMENTATION.md`.

**Uso básico:**
1. **Copiar script:**
   - Use o arquivo `aws-lambda-filter.groovy`
   - Cole no filtro de script do Policy Studio

2. **Configurar credenciais AWS**
3. **Testar com requisição HTTP**

## Estrutura do Projeto

```
aws-lambda-apim-sdk/
├── README.md                                # Documentação principal
├── AWS_LAMBDA_GROOVY_DOCUMENTATION.md      # Guia específico Groovy
├── build.gradle                             # Configuração build + tasks
├── aws-lambda-filter.groovy                # Script Groovy
├── scripts/
│   ├── linux/
│   │   └── install-filter.sh               # Instalação Linux
│   └── windows/
│       ├── install-filter-windows.ps1      # PowerShell
│       ├── install-filter-windows.cmd       # CMD
│       ├── configurar-projeto-windows.ps1  # Configuração
│       └── test-internationalization.ps1   # Teste
├── src/main/                               # Código fonte
└── build/
    └── build/libs/aws-lambda-apim-sdk-1.0.1.jar
```

## Troubleshooting

### Problemas Comuns

1. **Filtro não aparece na paleta:**
   - Verifique se o JAR foi adicionado ao classpath
   - Reinicie o Policy Studio com `-clean`

2. **Erro de credenciais AWS:**
   - Verifique se as credenciais estão configuradas
   - Teste com `aws sts get-caller-identity`

3. **Erro de função não encontrada:**
   - Verifique o nome da função e a região
   - Confirme se a função existe na AWS

### Logs

O filtro gera logs detalhados:
- **Sucesso**: "Success in the AWS Lambda filter"
- **Falha**: "Failed in the AWS Lambda filter"
- **Erro**: "Error in the AWS Lambda Error: ${circuit.exception}"

## Comparação das Abordagens

| Aspecto | Filtro Java | Script Groovy |
|---------|-------------|---------------|
| **Interface** | Gráfica no Policy Studio | Script de texto |
| **Configuração** | Parâmetros visuais | Variáveis no script |
| **Manutenção** | Requer rebuild do JAR | Edição direta do script |
| **Performance** | Nativo do gateway | Interpretado |
| **Flexibilidade** | Limitada aos parâmetros definidos | Totalmente customizável |
| **Debugging** | Logs estruturados | Logs detalhados |

## Segurança

- Use IAM Roles quando possível
- Rotacione credenciais regularmente
- Use políticas IAM com privilégios mínimos
- Monitore logs de acesso e execução
- Considere usar AWS Secrets Manager para credenciais sensíveis

## Contributing

Please read [Contributing.md](https://github.com/Axway-API-Management-Plus/Common/blob/master/Contributing.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Team

![alt text][Axwaylogo] Axway Team

[Axwaylogo]: https://github.com/Axway-API-Management/Common/blob/master/img/AxwayLogoSmall.png  "Axway logo"

## License
[Apache License 2.0](LICENSE)
