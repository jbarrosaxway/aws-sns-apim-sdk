#!/bin/bash

# Script para verificar se um release é necessário
# Analisa as mudanças e determina se deve criar um release

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para log colorido
log() {
    echo -e "${GREEN}[RELEASE-CHECK]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

# Verificar se estamos em um PR ou push direto
if [ "$GITHUB_EVENT_NAME" = "pull_request" ]; then
    log "Analisando mudanças em Pull Request..."
    BASE_REF="$GITHUB_BASE_REF"
    HEAD_REF="$GITHUB_HEAD_REF"
else
    log "Analisando mudanças em push direto..."
    BASE_REF="HEAD~1"
    HEAD_REF="HEAD"
fi

# Obter lista de arquivos modificados
log "Obtendo arquivos modificados..."
MODIFIED_FILES=$(git diff --name-only $BASE_REF $HEAD_REF || echo "")

if [ -z "$MODIFIED_FILES" ]; then
    warn "Nenhum arquivo modificado encontrado"
    echo "RELEASE_NEEDED=false" > .release_check
    exit 0
fi

log "Arquivos modificados:"
echo "$MODIFIED_FILES"

# Definir arquivos que NÃO devem gerar release
NON_RELEASE_FILES=(
    "README.md"
    "docs/"
    ".md$"
    ".gitignore"
    ".github/"
    "LICENSE"
    "*.txt"
    "*.log"
    "*.bak"
    "*.backup"
    "*.tmp"
    "*.temp"
    "*.swp"
    "*.swo"
    "*~"
    ".DS_Store"
    "Thumbs.db"
    "*.iml"
    "*.ipr"
    "*.iws"
    ".project"
    ".classpath"
    ".settings/"
    ".idea/"
    ".vscode/"
    "node_modules/"
    "__pycache__/"
    "*.pyc"
    "*.pyo"
    "*.pyd"
    "*.so"
    ".Python"
    "env/"
    "venv/"
    "ENV/"
    "env.bak/"
    "venv.bak/"
    "test-results/"
    "coverage/"
    ".coverage"
    "*.docx"
    "*.doc"
    "*.pdf"
    "*.run"
    "license.txt"
)

# Definir arquivos que DEVEM gerar release
RELEASE_FILES=(
    "src/"
    "build.gradle"
    "gradle/"
    "gradlew"
    "gradlew.bat"
    "settings.gradle"
    "Dockerfile"
    "docker-compose"
    "*.java"
    "*.groovy"
    "*.yaml"
    "*.yml"
    "*.xml"
    "*.properties"
    "*.sh"
    "*.ps1"
    "*.cmd"
    "*.bat"
)

# Função para verificar se arquivo deve gerar release
should_generate_release() {
    local file="$1"
    
    # Verificar se é um arquivo que NÃO deve gerar release
    for pattern in "${NON_RELEASE_FILES[@]}"; do
        if [[ "$file" =~ $pattern ]]; then
            return 1  # false - não deve gerar release
        fi
    done
    
    # Verificar se é um arquivo que DEVE gerar release
    for pattern in "${RELEASE_FILES[@]}"; do
        if [[ "$file" =~ $pattern ]]; then
            return 0  # true - deve gerar release
        fi
    done
    
    # Se não está em nenhuma lista, verificar extensão
    local ext="${file##*.}"
    case "$ext" in
        java|groovy|yaml|yml|xml|properties|sh|ps1|cmd|bat|gradle)
            return 0  # true - deve gerar release
            ;;
        md|txt|log|bak|backup|tmp|temp|swp|swo|iml|ipr|iws|docx|doc|pdf|run)
            return 1  # false - não deve gerar release
            ;;
        *)
            # Se não tem extensão ou é desconhecida, verificar se está em src/
            if [[ "$file" == src/* ]]; then
                return 0  # true - deve gerar release
            else
                return 1  # false - não deve gerar release
            fi
            ;;
    esac
}

# Analisar arquivos modificados
RELEASE_NEEDED=false
RELEVANT_FILES=""

log "Analisando relevância das mudanças..."

for file in $MODIFIED_FILES; do
    if should_generate_release "$file"; then
        RELEASE_NEEDED=true
        RELEVANT_FILES="$RELEVANT_FILES $file"
        log "✅ Relevante para release: $file"
    else
        log "⏭️  Não relevante para release: $file"
    fi
done

# Verificar se há mudanças relevantes
if [ "$RELEASE_NEEDED" = true ]; then
    log "🔴 Release necessário detectado!"
    log "📋 Arquivos relevantes:$RELEVANT_FILES"
    
    # Executar versionamento semântico
    log "🔍 Executando versionamento semântico..."
    ./scripts/version-bump.sh
    
    if [ -f .version_info ]; then
        source .version_info
        log "📊 Informações da versão:"
        log "   Tipo: $VERSION_TYPE"
        log "   Versão anterior: $OLD_VERSION"
        log "   Nova versão: $NEW_VERSION"
        
        # Criar arquivo com informações para o workflow
        echo "RELEASE_NEEDED=true" > .release_check
        echo "VERSION_TYPE=$VERSION_TYPE" >> .release_check
        echo "OLD_VERSION=$OLD_VERSION" >> .release_check
        echo "NEW_VERSION=$NEW_VERSION" >> .release_check
        echo "RELEVANT_FILES='$RELEVANT_FILES'" >> .release_check
        echo "CHANGES_DETECTED=$CHANGES_DETECTED" >> .release_check
        echo "PR_DETECTED=$PR_DETECTED" >> .release_check
    else
        error "❌ Falha ao executar versionamento semântico"
        echo "RELEASE_NEEDED=false" > .release_check
        exit 1
    fi
else
    log "🟢 Nenhuma mudança relevante para release detectada"
    echo "RELEASE_NEEDED=false" > .release_check
    echo "RELEVANT_FILES=''" >> .release_check
fi

log "✅ Análise de release concluída!" 