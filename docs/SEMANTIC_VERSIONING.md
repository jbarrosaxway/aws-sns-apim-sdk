# Versionamento Semântico Automático

Este projeto implementa versionamento semântico automático que analisa as mudanças e incrementa a versão apropriadamente.

## Como Funciona

### Análise de Mudanças

O sistema analisa automaticamente:

1. **Arquivos modificados** - através do `git diff`
2. **Conteúdo das mudanças** - procurando por padrões específicos
3. **Tipo de commit** - baseado em convenções de commit

### Tipos de Versão

#### 🔴 MAJOR (X.0.0)
- **Quando:** Mudanças que quebram compatibilidade
- **Detectado por:**
  - Palavras-chave: `BREAKING CHANGE`, `breaking change`, `!:`, `feat!`, `fix!`
  - Arquivos modificados: `build.gradle`, `.java`, `.groovy`

#### 🟡 MINOR (0.X.0)
- **Quando:** Novas funcionalidades (compatível com versões anteriores)
- **Detectado por:**
  - Palavras-chave: `feat:`, `feature:`, `new:`, `add:`
  - Arquivos modificados: `.java`, `.groovy`, `.yaml`

#### 🟢 PATCH (0.0.X)
- **Quando:** Correções de bugs e melhorias
- **Detectado por:**
  - Palavras-chave: `fix:`, `bugfix:`, `patch:`, `docs:`, `style:`, `refactor:`, `perf:`, `test:`, `chore:`
  - Arquivos modificados: `.java`, `.groovy`, `.yaml`, `.md`, `.txt`

## Convenções de Commit

### Para MAJOR (Breaking Changes)
```bash
git commit -m "feat!: nova funcionalidade que quebra compatibilidade"
git commit -m "fix!: correção que quebra compatibilidade"
git commit -m "feat: nova funcionalidade

BREAKING CHANGE: esta mudança quebra compatibilidade"
```

### Para MINOR (Novas Funcionalidades)
```bash
git commit -m "feat: adiciona nova funcionalidade"
git commit -m "feature: implementa novo filtro"
git commit -m "add: suporte para AWS Lambda"
```

### Para PATCH (Correções)
```bash
git commit -m "fix: corrige bug na autenticação"
git commit -m "docs: atualiza documentação"
git commit -m "style: formata código"
git commit -m "refactor: melhora performance"
git commit -m "test: adiciona testes"
git commit -m "chore: atualiza dependências"
```

## Workflow do GitHub Actions

### Pull Requests
- ✅ Analisa mudanças
- ✅ Calcula nova versão
- ✅ Mostra informações no comentário do PR
- ❌ **NÃO** faz commit automático

### Push Direto para Master
- ✅ Analisa mudanças
- ✅ Calcula nova versão
- ✅ Atualiza `build.gradle`
- ✅ Faz commit da nova versão
- ✅ Push para o repositório

## Arquivos do Sistema

### Script Principal
- **`scripts/version-bump.sh`** - Script que analisa mudanças e atualiza versão

### Workflow
- **`.github/workflows/build-jar.yml`** - Workflow que executa o versionamento

### Arquivo Temporário
- **`.version_info`** - Criado durante o build com informações da versão

## Exemplo de Output

```
[VERSION] Analisando mudanças em push direto...
[VERSION] Obtendo arquivos modificados...
[VERSION] Arquivos modificados:
src/main/java/com/axway/aws/lambda/AWSLambdaProcessor.java
[VERSION] 🟡 Mudanças MINOR detectadas (novas funcionalidades)
[VERSION] Versão atual: 1.0.1
[VERSION] Nova versão calculada: 1.1.0 (MINOR)
[VERSION] Atualizando build.gradle...
[VERSION] ✅ Versão atualizada com sucesso: 1.0.1 → 1.1.0
[VERSION] 📋 Resumo das mudanças:
   Tipo de versão: MINOR
   Versão anterior: 1.0.1
   Nova versão: 1.1.0
   Arquivos modificados: 1
[VERSION] 🚀 Push direto detectado - preparando commit da nova versão
[VERSION] ✅ Versionamento semântico concluído!
```

## Configuração

### Variáveis de Ambiente
O sistema usa as seguintes variáveis do GitHub Actions:
- `GITHUB_EVENT_NAME` - Tipo do evento (push, pull_request)
- `GITHUB_BASE_REF` - Branch base (em PRs)
- `GITHUB_HEAD_REF` - Branch head (em PRs)

### Permissões
O workflow precisa de permissões para:
- `contents: write` - Para fazer commits
- `pull-requests: write` - Para comentar em PRs

## Troubleshooting

### Problema: "Não foi possível obter a versão atual"
**Solução:** Verifique se o `build.gradle` tem a linha `version 'X.Y.Z'` no formato correto.

### Problema: "Falha ao atualizar versão"
**Solução:** Verifique se o `build.gradle` tem permissões de escrita e está no formato esperado.

### Problema: "Nenhum arquivo modificado encontrado"
**Solução:** Isso é normal em alguns casos. O sistema assume PATCH por padrão.

## Contribuição

Para contribuir com melhorias no sistema de versionamento:

1. Modifique o script `scripts/version-bump.sh`
2. Teste localmente: `./scripts/version-bump.sh`
3. Faça commit seguindo as convenções
4. Abra um PR

## Histórico de Versões

- **1.0.1** - Implementação inicial do versionamento semântico
- **1.1.0** - Melhorias na análise de mudanças e documentação 