# Guia de Releases

Este guia explica como criar releases no repositório `aws-lambda-apim-sdk`.

## Visão Geral

O projeto possui um sistema automatizado de releases que é acionado quando uma tag é criada e enviada para o repositório.

## Workflow de Release

### Trigger
- **Evento:** Push de tag com padrão `v*` (ex: `v1.0.1`, `v2.0.0`)
- **Workflow:** `.github/workflows/release.yml`

### Processo Automatizado
1. ✅ **Checkout** do código
2. ✅ **Build** do JAR usando Docker
3. ✅ **Geração** de changelog
4. ✅ **Criação** do release no GitHub
5. ✅ **Upload** do JAR como asset
6. ✅ **Testes** de validação

## Como Criar um Release

### Método 1: Release Manual (Recomendado)

#### 1. Preparar o Release
```bash
# Verificar status atual
git status

# Verificar versão atual
grep "^version " build.gradle

# Verificar se há mudanças não commitadas
git diff
```

#### 2. Criar e Enviar a Tag
```bash
# Criar tag local
git tag v1.0.1

# Enviar tag para o repositório
git push origin v1.0.1
```

#### 3. Monitorar o Release
- Acesse: https://github.com/[seu-usuario]/aws-lambda-apim-sdk/actions
- Verifique o workflow "Release"
- Aguarde a conclusão do build

### Método 2: Release via GitHub CLI

```bash
# Instalar GitHub CLI (se não tiver)
# https://cli.github.com/

# Fazer login
gh auth login

# Criar release
gh release create v1.0.1 \
  --title "Release v1.0.1" \
  --notes "## Mudanças nesta versão

- Implementação do versionamento semântico
- Melhorias na documentação
- Correções de bugs

## Instalação

Baixe o JAR e siga o guia de instalação no README." \
  --draft=false \
  --prerelease=false
```

### Método 3: Release via Interface Web

1. **Acesse** o repositório no GitHub
2. **Clique** em "Releases" no menu lateral
3. **Clique** em "Create a new release"
4. **Preencha:**
   - Tag: `v1.0.1`
   - Title: `Release v1.0.1`
   - Description: (changelog)
5. **Clique** em "Publish release"

## Estrutura de Versionamento

### Convenções de Tag
- **Formato:** `vX.Y.Z` (ex: `v1.0.1`)
- **MAJOR:** Mudanças que quebram compatibilidade
- **MINOR:** Novas funcionalidades
- **PATCH:** Correções de bugs

### Exemplos
```bash
# Patch release (correção)
git tag v1.0.2

# Minor release (nova funcionalidade)
git tag v1.1.0

# Major release (breaking change)
git tag v2.0.0
```

## Assets do Release

### Automático
- ✅ **JAR File:** `aws-lambda-apim-sdk-vX.Y.Z.jar`
- ✅ **Build Info:** Informações do build
- ✅ **Changelog:** Lista de commits

### Manual (Opcional)
- 📋 **Documentation:** PDFs, guias
- 🔧 **Scripts:** Scripts de instalação
- 📦 **Docker:** Imagens Docker

## Exemplo Completo

### 1. Preparar o Release
```bash
# Verificar versão atual
grep "^version " build.gradle
# Output: version '1.0.1'

# Verificar mudanças recentes
git log --oneline -10
```

### 2. Criar Tag
```bash
# Criar tag
git tag v1.0.1

# Verificar tag criada
git tag -l
# Output: v1.0.1

# Enviar tag
git push origin v1.0.1
```

### 3. Monitorar Build
```bash
# Verificar status do workflow
gh run list --workflow=release
```

### 4. Verificar Release
- Acesse: https://github.com/[usuario]/aws-lambda-apim-sdk/releases
- Verifique se o JAR foi criado
- Teste o download do asset

## Troubleshooting

### Problema: "Tag já existe"
```bash
# Remover tag local
git tag -d v1.0.1

# Remover tag remota
git push origin --delete v1.0.1

# Recriar tag
git tag v1.0.1
git push origin v1.0.1
```

### Problema: "Workflow não executou"
- Verifique se a tag segue o padrão `v*`
- Verifique se o push foi feito para `origin`
- Verifique as permissões do repositório

### Problema: "Build falhou"
- Verifique os logs do workflow
- Verifique se o Docker image está disponível
- Verifique se as secrets estão configuradas

## Configuração de Secrets

O workflow precisa das seguintes secrets:

### Obrigatórias
- `GITHUB_TOKEN` - Token automático do GitHub

### Opcionais (para Docker Hub)
- `DOCKERHUB_USERNAME` - Usuário do Docker Hub
- `DOCKERHUB_TOKEN` - Token do Docker Hub

## Exemplo de Changelog

```markdown
## Release v1.0.1

### 🚀 Novas Funcionalidades
- Implementação do versionamento semântico automático
- Script de análise de mudanças
- Workflow GitHub Actions atualizado

### 🔧 Melhorias
- Documentação completa em SEMANTIC_VERSIONING.md
- Suporte para MAJOR, MINOR e PATCH
- Commit automático de versão

### 🐛 Correções
- Ajuste do parâmetro axway.base para /opt/Axway
- Exclusões no .gitignore para arquivos temporários

### 📦 Build
- JAR: aws-lambda-apim-sdk-1.0.1.jar
- Docker: axwayjbarros/aws-lambda-apim-sdk:1.0.0
- Java: OpenJDK 11
- AWS SDK: 1.12.314

## Instalação

1. Baixe o JAR do release
2. Siga o guia de instalação no README
3. Configure o Axway API Gateway
4. Teste a integração com AWS Lambda
```

## Próximos Passos

1. **Criar primeira tag:** `v1.0.1`
2. **Monitorar workflow:** Verificar build automático
3. **Testar release:** Baixar e testar JAR
4. **Documentar:** Atualizar README com instruções

## Links Úteis

- **Workflow:** `.github/workflows/release.yml`
- **Build:** `.github/workflows/build-jar.yml`
- **Versionamento:** `SEMANTIC_VERSIONING.md`
- **Documentação:** `README.md` 