#!/bin/bash

# Script para versionamento semântico automático
# Analisa as mudanças e incrementa a versão apropriadamente

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para log colorido
log() {
    echo -e "${GREEN}[VERSION]${NC} $1"
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
    exit 0
fi

log "Arquivos modificados:"
echo "$MODIFIED_FILES"

# Análise de mudanças para determinar tipo de versão
MAJOR_CHANGES=false
MINOR_CHANGES=false
PATCH_CHANGES=false

# Verificar mudanças que quebram compatibilidade (MAJOR)
if echo "$MODIFIED_FILES" | grep -q -E "(build\.gradle|\.java|\.groovy)" && \
   git diff $BASE_REF $HEAD_REF | grep -q -E "(BREAKING CHANGE|breaking change|!:|feat!|fix!)"; then
    MAJOR_CHANGES=true
    log "🔴 Mudanças MAJOR detectadas (breaking changes)"
fi

# Verificar novas funcionalidades (MINOR)
if echo "$MODIFIED_FILES" | grep -q -E "(\.java|\.groovy|\.yaml)" && \
   git diff $BASE_REF $HEAD_REF | grep -q -E "(feat:|feature:|new:|add:)" && \
   [ "$MAJOR_CHANGES" = false ]; then
    MINOR_CHANGES=true
    log "🟡 Mudanças MINOR detectadas (novas funcionalidades)"
fi

# Verificar correções e melhorias (PATCH)
if echo "$MODIFIED_FILES" | grep -q -E "(\.java|\.groovy|\.yaml|\.md|\.txt)" && \
   git diff $BASE_REF $HEAD_REF | grep -q -E "(fix:|bugfix:|patch:|docs:|style:|refactor:|perf:|test:|chore:)" && \
   [ "$MAJOR_CHANGES" = false ] && [ "$MINOR_CHANGES" = false ]; then
    PATCH_CHANGES=true
    log "🟢 Mudanças PATCH detectadas (correções e melhorias)"
fi

# Se não detectou mudanças específicas, assume PATCH
if [ "$MAJOR_CHANGES" = false ] && [ "$MINOR_CHANGES" = false ] && [ "$PATCH_CHANGES" = false ]; then
    PATCH_CHANGES=true
    log "🟢 Assumindo mudanças PATCH (padrão)"
fi

# Ler versão atual do build.gradle
CURRENT_VERSION=$(grep "^version " build.gradle | sed 's/version //' | tr -d "'")

if [ -z "$CURRENT_VERSION" ]; then
    error "Não foi possível obter a versão atual do build.gradle"
    exit 1
fi

log "Versão atual: $CURRENT_VERSION"

# Separar componentes da versão
IFS='.' read -ra VERSION_PARTS <<< "$CURRENT_VERSION"
MAJOR=${VERSION_PARTS[0]}
MINOR=${VERSION_PARTS[1]}
PATCH=${VERSION_PARTS[2]}

# Calcular nova versão
if [ "$MAJOR_CHANGES" = true ]; then
    NEW_MAJOR=$((MAJOR + 1))
    NEW_MINOR=0
    NEW_PATCH=0
    VERSION_TYPE="MAJOR"
elif [ "$MINOR_CHANGES" = true ]; then
    NEW_MAJOR=$MAJOR
    NEW_MINOR=$((MINOR + 1))
    NEW_PATCH=0
    VERSION_TYPE="MINOR"
else
    NEW_MAJOR=$MAJOR
    NEW_MINOR=$MINOR
    NEW_PATCH=$((PATCH + 1))
    VERSION_TYPE="PATCH"
fi

NEW_VERSION="$NEW_MAJOR.$NEW_MINOR.$NEW_PATCH"

log "Nova versão calculada: $NEW_VERSION ($VERSION_TYPE)"

# Atualizar build.gradle
log "Atualizando build.gradle..."
sed -i "s/^version '.*'/version '$NEW_VERSION'/" build.gradle

# Verificar se a mudança foi aplicada
UPDATED_VERSION=$(grep "^version " build.gradle | sed 's/version //' | tr -d "'")

if [ "$UPDATED_VERSION" = "$NEW_VERSION" ]; then
    log "✅ Versão atualizada com sucesso: $CURRENT_VERSION → $NEW_VERSION"
else
    error "❌ Falha ao atualizar versão"
    exit 1
fi

# Criar arquivo com informações da versão para uso no workflow
echo "VERSION_TYPE=$VERSION_TYPE" > .version_info
echo "OLD_VERSION=$CURRENT_VERSION" >> .version_info
echo "NEW_VERSION=$NEW_VERSION" >> .version_info
echo "CHANGES_DETECTED=true" >> .version_info

# Log das mudanças detectadas
log "📋 Resumo das mudanças:"
echo "   Tipo de versão: $VERSION_TYPE"
echo "   Versão anterior: $CURRENT_VERSION"
echo "   Nova versão: $NEW_VERSION"
echo "   Arquivos modificados: $(echo "$MODIFIED_FILES" | wc -l)"

# Se for um PR, não fazer commit automático
if [ "$GITHUB_EVENT_NAME" = "pull_request" ]; then
    log "📝 Pull Request detectado - versão será atualizada no merge"
    echo "PR_DETECTED=true" >> .version_info
else
    log "🚀 Push direto detectado - preparando commit da nova versão"
    echo "PR_DETECTED=false" >> .version_info
fi

log "✅ Versionamento semântico concluído!" 