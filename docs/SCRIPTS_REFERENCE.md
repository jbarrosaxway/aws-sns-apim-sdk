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

#### `scripts/test-published-image.sh`
- **Função:** Testa a imagem Docker publicada
- **Uso:** Manual (validação)
- **Comando:** `./scripts/test-published-image.sh`
- **Saída:** Relatório de testes da imagem

### 📁 **Scripts por Plataforma**

#### **Linux** (`scripts/linux/`)

##### `scripts/linux/install-filter.sh`
- **Função:** Instala o filtro AWS Lambda no Linux
- **Uso:** Automático (task Gradle `installLinux`)
- **Comando:** `./gradlew installLinux`
- **Saída:** Filtro instalado no Axway API Gateway

#### **Windows** (`scripts/windows/`)

##### `scripts/windows/install-filter-windows.ps1`
- **Função:** Instalação PowerShell para Windows
- **Uso:** Manual (Windows)
- **Comando:** `.\scripts\windows\install-filter-windows.ps1`
- **Saída:** Arquivos YAML instalados no projeto Policy Studio

##### `scripts/windows/install-filter-windows.cmd`
- **Função:** Instalação CMD para Windows
- **Uso:** Manual (Windows)
- **Comando:** `scripts\windows\install-filter-windows.cmd`
- **Saída:** Arquivos YAML instalados no projeto Policy Studio

##### `scripts/windows/configurar-projeto-windows.ps1`
- **Função:** Configura projeto Policy Studio no Windows
- **Uso:** Manual (primeira configuração)
- **Comando:** `.\scripts\windows\configurar-projeto-windows.ps1`
- **Saída:** Projeto configurado

##### `scripts/windows/test-internationalization.ps1`
- **Função:** Testa internacionalização no Windows
- **Uso:** Manual (validação)
- **Comando:** `.\scripts\windows\test-internationalization.ps1`
- **Saída:** Relatório de testes de internacionalização

#### **Docker** (`scripts/docker/`)

##### `scripts/docker/build-image.sh`
- **Função:** Build da imagem Docker
- **Uso:** Manual (desenvolvimento)
- **Comando:** `./scripts/docker/build-image.sh`
- **Saída:** Imagem Docker `axwayjbarros/aws-lambda-apim-sdk:latest`

##### `scripts/docker/build-with-docker.sh`
- **Função:** Build do projeto usando Docker
- **Uso:** Manual (desenvolvimento)
- **Comando:** `./scripts/docker/build-with-docker.sh`
- **Saída:** JAR buildado usando Docker

## Estrutura Final

```
scripts/
├── 🔧 Scripts Principais
│   ├── check-release-needed.sh          # Análise de release
│   ├── version-bump.sh                  # Versionamento semântico
│   ├── build-with-docker-image.sh       # Build com Docker
│   └── test-published-image.sh          # Teste da imagem
├── 📁 linux/
│   └── install-filter.sh                # Instalação Linux
├── 📁 windows/
│   ├── install-filter-windows.ps1       # Instalação PowerShell
│   ├── install-filter-windows.cmd       # Instalação CMD
│   ├── configurar-projeto-windows.ps1   # Configuração projeto
│   └── test-internationalization.ps1    # Teste internacionalização
└── 📁 docker/
    ├── build-image.sh                   # Build da imagem
    └── build-with-docker.sh             # Build com Docker
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