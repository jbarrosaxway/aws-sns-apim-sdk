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

## 📦 Releases do GitHub

### **Downloads Automáticos**

Os releases são criados automaticamente no GitHub e incluem:

#### **Arquivos Disponíveis em Cada Release:**
- **JAR Principal** - `aws-lambda-apim-sdk-*.jar` (compilado para múltiplas versões do Axway)
- **Dependências Externas** - pasta `dependencies/` com JARs AWS SDK
- **Recursos Policy Studio** - `src/main/resources/fed/` e `src/main/resources/yaml/`
- **Gradle Wrapper** - `gradlew`, `gradlew.bat` e pasta `gradle/`
- **Configuração Gradle** - `build.gradle` com tarefas de instalação
- **Script Linux** - `install-linux.sh` para instalação automática

#### **Instalação a partir do Release:**

**Windows (Recomendado):**
```bash
# Extraia o ZIP do release
# Navegue até a pasta extraída
# Execute a tarefa Gradle:
.\gradlew "-Dproject.path=C:\Users\jbarros\apiprojects\DIGIO-POC-AKS-NEW" installWindowsToProject
```

**Linux:**
```bash
# Extraia o ZIP do release
# Execute o script de instalação:
./install-linux.sh
```

### **Versões Suportadas:**
- **Axway API Gateway 7.7.0.20240830** ✅
- **Axway API Gateway 7.7.0.20250230** ✅

---

## Build e Instalação

### 🔧 Configuração Dinâmica

O projeto suporta **configuração dinâmica** do caminho do Axway API Gateway:

```bash
# Configuração padrão
./gradlew clean build installLinux

# Configuração customizada
./gradlew -Daxway.base=/opt/axway/Axway-7.7.0.20210830 clean build installLinux

# Verificar configuração atual
./gradlew setAxwayPath
```

### Linux
```bash
# Build do JAR (apenas Linux)
./gradlew buildJarLinux

# Build e instalação automática
./gradlew clean build installLinux

# Com caminho customizado
./gradlew -Daxway.base=/caminho/para/axway clean build installLinux
```

### Windows
```bash
# Instalação apenas dos arquivos YAML em projeto Policy Studio
./gradlew installWindows

# Instalação em projeto específico (com caminho)
./gradlew "-Dproject.path=C:\Users\jbarros\apiprojects\DIGIO-POC-AKS" installWindowsToProject

# Instalação interativa (se não especificar caminho)
./gradlew installWindowsToProject
```

> 📖 **Guia Completo Windows**: Veja **[📋 Guia de Instalação Windows](docs/INSTALACAO_WINDOWS.md)** para instruções detalhadas.

### 🐳 **Docker**

#### **Imagem Docker Publicada**

Este projeto usa a imagem Docker publicada `axwayjbarros/aws-lambda-apim-sdk:1.0.0` que contém:
- Axway API Gateway 7.7.0.20240830
- Java 11 OpenJDK
- AWS SDK for Java 1.12.314
- Gradle para build
- Todas as dependências necessárias

#### **Build usando Docker**

```bash
# Build do JAR usando a imagem publicada
./scripts/build-with-docker-image.sh

# Ou manualmente:
docker pull axwayjbarros/aws-lambda-apim-sdk:1.0.0
docker run --rm \
  -v "$(pwd):/workspace" \
  -v "$(pwd)/build:/workspace/build" \
  -w /workspace \
  axwayjbarros/aws-lambda-apim-sdk:1.0.0 \
  bash -c "
    export JAVA_HOME=/opt/java/openjdk-11
    export PATH=\$JAVA_HOME/bin:\$PATH
    gradle clean build
  "
```
```

> 💡 **Dica**: O GitHub Actions usa a imagem publicada `axwayjbarros/aws-lambda-apim-sdk:1.0.0`.

#### **Testar Imagem Publicada**

```bash
# Testar a imagem publicada


# Ou manualmente:
docker pull axwayjbarros/aws-lambda-apim-sdk:1.0.0
docker run --rm axwayjbarros/aws-lambda-apim-sdk:1.0.0 java -version
docker run --rm axwayjbarros/aws-lambda-apim-sdk:1.0.0 ls -la /opt/Axway/
```

> ⚠️ **Nota**: Esta imagem é **apenas para build**, não para execução de aplicação.

#### **Estrutura de JARs na Imagem**

A imagem inclui os seguintes JARs organizados:

```
/opt/Axway/apigateway/lib/
├── aws-java-sdk-lambda-*.jar          # AWS Lambda SDK
├── aws-java-sdk-core-*.jar            # AWS Core SDK
└── jackson-*.jar                      # Jackson JSON library
```

#### **Uso da Imagem para Build**

A imagem `axwayjbarros/aws-lambda-apim-sdk:1.0.0` é usada **apenas para build**, não para execução. Ela contém todas as bibliotecas do Axway API Gateway necessárias para compilar o projeto:

```bash
# Build usando a imagem (apenas bibliotecas)
docker run --rm \
  -v "$(pwd):/workspace" \
  -v "$(pwd)/build:/workspace/build" \
  -w /workspace \
  axwayjbarros/aws-lambda-apim-sdk:1.0.0 \
  bash -c "
    export JAVA_HOME=/opt/java/openjdk-11
    export PATH=\$JAVA_HOME/bin:\$PATH
    gradle clean build
  "
```

#### **Especificações da Imagem:**
- **Base**: Axway API Gateway 7.7.0.20240830-4-BN0145-ubi9
- **Java**: OpenJDK 11.0.27
- **Bibliotecas**: Todas as libs do Axway API Gateway disponíveis
- **Uso**: Apenas para build do projeto, não para execução

#### **GitHub Actions**

O projeto usa a imagem para build automatizado:

- **Build Contínuo**: `.github/workflows/build-jar.yml`
- **Release**: `.github/workflows/release.yml`
- **Imagem**: `axwayjbarros/aws-lambda-apim-sdk:1.0.0`

> 📖 **Docker**: A documentação Docker está integrada nesta seção do README.

### ⚠️ **Importante: Build do JAR**

O **build do JAR deve ser feito no Linux** devido às dependências do Axway API Gateway. Para Windows:

1. **Build no Linux:**
   ```bash
   ./gradlew buildJarLinux
   ```

2. **Copiar JAR para Windows:**
   ```bash
   # Copie o arquivo: build/libs/aws-lambda-apim-sdk-1.0.1.jar
   # Para o ambiente Windows
   ```

3. **Instalar YAML no Windows:**
   ```bash
   ./gradlew installWindows
   ```

### 🔄 **Processo Linux vs Windows**

| Linux | Windows |
|-------|---------|
| ✅ Build do JAR | ❌ Build do JAR |
| ✅ Instalação completa | ✅ Instalação YAML |
| ✅ Dependências nativas | ⚠️ JARs externos |
| ✅ Configuração automática | ⚠️ Configuração manual |

**Linux**: Processo completo (JAR + YAML + instalação)  
**Windows**: Apenas YAML (JAR deve ser buildado no Linux)

### Comandos Úteis
```bash
# Ver todas as tasks disponíveis
./gradlew showTasks

# Mostrar links dos JARs AWS SDK
./gradlew showAwsJars

# Verificar configuração do Axway
./gradlew setAxwayPath

# Apenas build
./gradlew clean build
```

## 📚 Documentação

Este projeto possui documentação completa organizada por tópicos:

### 🚀 **Guias de Instalação**
- **[📋 Guia de Instalação Windows](docs/INSTALACAO_WINDOWS.md)** - Instruções detalhadas para Windows
- **[🔧 Configuração Dinâmica](docs/CONFIGURACAO_DINAMICA.md)** - Como configurar caminhos do Axway dinamicamente

### 🔧 **Desenvolvimento e Build**
- **[🏷️ Guia de Releases](docs/RELEASE_GUIDE.md)** - Como criar releases e versionamento
- **[📊 Versionamento Semântico](docs/SEMANTIC_VERSIONING.md)** - Sistema automático de versionamento
- **[🤖 Sistema de Release Automático](docs/AUTOMATIC_RELEASE_SYSTEM.md)** - Análise inteligente e criação automática de releases
- **[🔧 Referência dos Scripts](docs/SCRIPTS_REFERENCE.md)** - Documentação dos scripts essenciais

### 📝 **Documentação Técnica**
- **[🔍 Atualizações de Campos](docs/ATUALIZACOES_CAMPOS_FILTRO.md)** - Histórico de mudanças nos campos do filtro
- **[🔐 Melhorias de Autenticação AWS](docs/MELHORIAS_AUTENTICACAO_AWS.md)** - Configurações avançadas de autenticação
- **[📖 Documentação Groovy](docs/AWS_LAMBDA_GROOVY_DOCUMENTATION.md)** - Guia completo para scripts Groovy

### 📋 **Estrutura da Documentação**
```
docs/
├── 📋 INSTALACAO_WINDOWS.md              # Instalação no Windows
├── 🔧 CONFIGURACAO_DINAMICA.md           # Configuração dinâmica
├── 🏷️ RELEASE_GUIDE.md                   # Guia de releases
├── 📊 SEMANTIC_VERSIONING.md             # Versionamento semântico
├── 🔍 ATUALIZACOES_CAMPOS_FILTRO.md     # Histórico de campos
├── 🔐 MELHORIAS_AUTENTICACAO_AWS.md     # Autenticação AWS
└── 📖 AWS_LAMBDA_GROOVY_DOCUMENTATION.md # Documentação Groovy
```

---

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

1. **Instalar arquivos YAML (interativo):**
   ```bash
   ./gradlew installWindows
   ```
   O Gradle solicitará o caminho do projeto Policy Studio.

2. **Instalar arquivos YAML em projeto específico:**
   ```bash
   ./gradlew -Dproject.path=C:\caminho\do\projeto installWindowsToProject
   ```

3. **Ver links dos JARs AWS SDK:**
   ```bash
   ./gradlew showAwsJars
   ```

4. **Configurar Policy Studio:**
   - Abra o Policy Studio
   - Vá em **Window > Preferences > Runtime Dependencies**
   - Adicione o JAR: `aws-lambda-apim-sdk-1.0.1.jar`
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

#### 3. IAM Roles (Recomendado para Produção)

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
        # Sem variáveis de ambiente - usa IAM Role automaticamente
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

**Vantagens:**
- ✅ Segurança máxima (sem credenciais estáticas)
- ✅ Rotação automática de credenciais
- ✅ Auditoria via CloudTrail
- ✅ Funciona com filtro Java e script Groovy

## Uso

### Filtro Java

1. **Adicionar filtro à política:**
   - Abra o Policy Studio
   - Procure por **"AWS Lambda Filter"** na paleta
   - Arraste o filtro para a política

2. **Configurar parâmetros:**
   - `functionName` (obrigatório): Nome da função Lambda
   - `awsRegion` (opcional): Região AWS (padrão: `us-east-1`)
   - `invocationType` (opcional): Tipo de invocação (padrão: `RequestResponse`)
   - `logType` (opcional): Tipo de log (padrão: `None`)
   - `qualifier` (opcional): Versão ou alias da função
   - `maxRetries` (opcional): Número máximo de tentativas (padrão: `3`)
   - `retryDelay` (opcional): Delay entre tentativas em ms (padrão: `1000`)

3. **Atributos de saída:**
   - `aws.lambda.response`: Resposta da função Lambda
   - `aws.lambda.http.status.code`: Código de status HTTP

4. **Autenticação AWS (Ordem de Prioridade):**
   - **Variáveis de ambiente** (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`)
   - **Arquivo de credenciais** (`~/.aws/credentials`)
   - **IAM Roles** (detecção automática para EC2/EKS) ← **Recomendado para produção**

### Script Groovy

Para informações detalhadas sobre o script Groovy, incluindo configuração Kubernetes, troubleshooting e parâmetros específicos, consulte o arquivo **[📖 Documentação Groovy](docs/AWS_LAMBDA_GROOVY_DOCUMENTATION.md)**.

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
├── docs/                                    # 📚 Documentação organizada
│   ├── 📋 INSTALACAO_WINDOWS.md            # Instalação Windows
│   ├── 🔧 CONFIGURACAO_DINAMICA.md         # Configuração dinâmica
│   ├── 🏷️ RELEASE_GUIDE.md                 # Guia de releases
│   ├── 📊 SEMANTIC_VERSIONING.md           # Versionamento semântico
│   ├── 🔍 ATUALIZACOES_CAMPOS_FILTRO.md   # Histórico de campos
│   ├── 🔐 MELHORIAS_AUTENTICACAO_AWS.md   # Autenticação AWS
│   └── 📖 AWS_LAMBDA_GROOVY_DOCUMENTATION.md # Documentação Groovy
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

## 🚀 **CI/CD Pipeline**

### **GitHub Actions**

O projeto inclui workflows automatizados que usam Docker para build:

#### **CI (Continuous Integration)**
- **Trigger**: Push para `main`, `develop` ou Pull Requests
- **Ações**:
  - ✅ Login no registry Axway (para imagem base)
  - ✅ Build da imagem Docker de build (com Axway + Gradle)
  - ✅ Build do JAR dentro do container Docker
  - ✅ Upload do JAR como artifact
  - ✅ Testes do JAR

#### **Release**
- **Trigger**: Push de tags (`v*`)
- **Ações**:
  - ✅ Login no registry Axway
  - ✅ Build da imagem Docker de build
  - ✅ Build do JAR dentro do container
  - ✅ Geração de changelog
  - ✅ Criação de GitHub Release
  - ✅ Upload do JAR para o release
  - ✅ Testes do JAR

### **Fluxo de Build**

```
1. Login no Axway Registry
   ↓
2. Build da imagem Docker (com Axway + Gradle)
   ↓
3. Execução do build do JAR dentro do container
   ↓
4. Geração do JAR final
   ↓
5. Upload para GitHub Release/Artifacts
```

### **Por que usar Docker?**

- **✅ Ambiente Consistente**: Mesmo ambiente Axway sempre
- **✅ Dependências Garantidas**: Axway + Gradle + Java 11
- **✅ Isolamento**: Build isolado em container
- **✅ Reproduzibilidade**: Mesmo resultado sempre
- **✅ Não Publica Imagem**: Apenas usa para build

### **Artefatos Gerados**

#### **JAR Principal**
```
aws-lambda-apim-sdk-1.0.1.jar
├── Filtro Java AWS Lambda
├── Classes de UI do Policy Studio
├── Dependências AWS SDK
└── Configurações YAML
```

#### **Localização**
- **GitHub Releases**: Disponível para download
- **GitHub Actions Artifacts**: Durante CI/CD
- **Local**: `build/libs/aws-lambda-apim-sdk-*.jar`

### **Como Usar**

#### **Download do JAR**
1. Vá para **Releases** no GitHub
2. Baixe o JAR da versão desejada
3. Siga o guia de instalação

#### **Build Local**
```bash
# Build do JAR (requer Axway local)
./gradlew buildJarLinux

# Ou usando Docker (recomendado)
./scripts/docker/build-with-docker.sh
```

#### **Docker para Desenvolvimento**
```bash
# Build da imagem para desenvolvimento
./scripts/docker/build-image.sh

# Testar
docker run --rm aws-lambda-apim-sdk:latest java -version
```
