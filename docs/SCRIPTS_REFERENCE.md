# Referência dos Scripts

Este documento lista todos os scripts essenciais mantidos no projeto e suas funções.

## Scripts Principais

### 🔧 **Build e Release**

#### `scripts/check-release-needed.sh`
- **Função:** Analisa mudanças e determina se um release é necessário
- **Uso:** Automático (workflow GitHub Actions)
- **Entrada:** Lista de arquivos modificados
- **Saída:** Arquivo `.release_check` com informações

#### `scripts/version-bump.sh`
- **Função:** Executa versionamento semântico automático
- **Uso:** Automático (workflow GitHub Actions)
- **Entrada:** Mudanças detectadas
- **Saída:** Nova versão calculada e arquivo `.version_info`

#### `scripts/build-with-docker-image.sh`
- **Função:** Build do JAR usando imagem Docker publicada
- **Uso:** Manual (desenvolvimento)
- **Comando:** `./scripts/build-with-docker-image.sh`
- **Saída:** JAR em `build/libs/aws-lambda-apim-sdk-*.jar`



### 📁 **Scripts por Plataforma**

#### **Linux** (`scripts/linux/`)

##### `scripts/linux/install-filter.sh`
- **Função:** Instala o filtro AWS Lambda no Linux
- **Uso:** Automático (task Gradle `installLinux`)
- **Comando:** `./gradlew installLinux`
- **Saída:** Filtro instalado no Axway API Gateway

#### **Windows** (Tasks Gradle)

##### `./gradlew installWindows`
- **Função:** Instalação interativa para Windows
- **Uso:** Manual (Windows)
- **Comando:** `./gradlew installWindows`
- **Saída:** Arquivos YAML instalados no projeto Policy Studio

##### `./gradlew installWindowsToProject`
- **Função:** Instalação em projeto específico
- **Uso:** Manual (Windows)
- **Comando:** `./gradlew -Dproject.path=C:\caminho\do\projeto installWindowsToProject`
- **Saída:** Arquivos YAML instalados no projeto específico

##### `./gradlew showAwsJars`
- **Função:** Mostra links dos JARs AWS SDK
- **Uso:** Manual (Windows)
- **Comando:** `./gradlew showAwsJars`
- **Saída:** Links para download dos JARs necessários



## Estrutura Final

```
scripts/
├── 🔧 Scripts Principais
│   ├── check-release-needed.sh          # Análise de release
│   ├── version-bump.sh                  # Versionamento semântico
│   └── build-with-docker-image.sh       # Build com Docker
└── 📁 linux/
    └── install-filter.sh                # Instalação Linux

📋 **Tasks Gradle para Windows:**
├── ./gradlew installWindows             # Instalação interativa
├── ./gradlew installWindowsToProject    # Instalação em projeto específico
└── ./gradlew showAwsJars               # Links dos JARs AWS
```

## Scripts Removidos

Os seguintes scripts foram removidos por não serem essenciais:

### 🧪 **Scripts de Teste/Validação (Removidos):**
- `verify-aws-lambda-values.sh` - Verificação de valores AWS
- `verify-filter-structure.sh` - Verificação de estrutura do filtro
- `test-preserve-other-filters.sh` - Teste de preservação de filtros
- `clean-and-reinstall.sh` - Limpeza e reinstalação

### 🔧 **Scripts de Fix (Removidos):**
- `fix-internationalization-simple.sh` - Correção simples de internacionalização
- `fix-internationalization-correct.sh` - Correção correta de internacionalização
- `fix-internationalization-duplication.sh` - Correção de duplicação
- `test-internationalization-fix.sh` - Teste de correção

### 🪟 **Scripts Windows (Substituídos por Tasks Gradle):**
- `install-filter-windows.ps1` - Substituído por `./gradlew installWindows`
- `install-filter-windows.cmd` - Substituído por `./gradlew installWindowsToProject`
- `configurar-projeto-windows.ps1` - Funcionalidade integrada nas tasks
- `test-internationalization.ps1` - Funcionalidade integrada nas tasks

### 🐳 **Scripts Docker (Removidos):**
- `check-axway-jars.sh` - Verificação de JARs Axway
- `debug-image.sh` - Debug da imagem
- `docker-helper.sh` - Helper Docker
- `start-gateway.sh` - Iniciar gateway

## Uso Recomendado

### 🔄 **Desenvolvimento Diário:**
```bash
# Build local
./scripts/build-with-docker-image.sh

# Testar imagem
./scripts/test-published-image.sh

# Instalar no Linux
./gradlew installLinux
```

### 🏷️ **Releases:**
```bash
# Automático via GitHub Actions
# (não precisa de comandos manuais)
```

### 🐳 **Docker:**
```bash
# Build da imagem
./scripts/docker/build-image.sh

# Build com Docker
./scripts/docker/build-with-docker.sh
```

### 🪟 **Windows:**
```powershell
# Configurar projeto
.\scripts\windows\configurar-projeto-windows.ps1

# Instalar filtro
.\scripts\windows\install-filter-windows.ps1

# Testar internacionalização
.\scripts\windows\test-internationalization.ps1
```

## Benefícios da Limpeza

### ✅ **Organização:**
- Scripts essenciais mantidos
- Documentação clara
- Estrutura lógica

### ✅ **Manutenção:**
- Menos scripts para manter
- Foco nos essenciais
- Redução de complexidade

### ✅ **Performance:**
- Menos arquivos no repositório
- Builds mais rápidos
- Menos overhead

## Próximos Passos

1. **Testar** os scripts mantidos
2. **Documentar** experiências de uso
3. **Melhorar** scripts conforme necessário
4. **Adicionar** novos scripts apenas se essenciais 