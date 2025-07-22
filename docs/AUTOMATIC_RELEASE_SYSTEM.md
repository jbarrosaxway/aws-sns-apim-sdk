# Sistema de Release Automático

Este documento explica o sistema de release automático implementado no projeto.

## Visão Geral

O sistema analisa automaticamente as mudanças e determina se um release é necessário, criando tags e releases automaticamente quando apropriado.

## Como Funciona

### 🔍 **Análise Inteligente de Mudanças**

O sistema verifica se as mudanças são relevantes para gerar um release:

#### **Arquivos que NÃO geram release:**
- 📚 Documentação: `README.md`, `docs/`, `*.md`
- 🔧 Configuração: `.gitignore`, `.github/`, `LICENSE`
- 📝 Temporários: `*.txt`, `*.log`, `*.bak`, `*.backup`
- 🛠️ IDE: `*.iml`, `*.ipr`, `*.iws`, `.idea/`, `.vscode/`
- 📦 Build: `node_modules/`, `__pycache__/`, `*.pyc`
- 📄 Documentos: `*.docx`, `*.doc`, `*.pdf`
- 🔧 Instaladores: `*.run`, `license.txt`

#### **Arquivos que GERAM release:**
- 💻 Código fonte: `src/`, `*.java`, `*.groovy`
- 🔧 Build: `build.gradle`, `gradle/`, `gradlew`, `settings.gradle`
- 🐳 Docker: `Dockerfile`, `docker-compose`
- 📋 Configuração: `*.yaml`, `*.yml`, `*.xml`, `*.properties`
- 🔧 Scripts: `*.sh`, `*.ps1`, `*.cmd`, `*.bat`

### 🔄 **Fluxo Automático**

```
1. Push para master
   ↓
2. Análise de mudanças relevantes
   ↓
3. Se mudanças relevantes → Versionamento semântico
   ↓
4. Se não é PR → Criação automática de tag
   ↓
5. Push da tag → Aciona workflow de release
   ↓
6. Release criado automaticamente
```

## Scripts do Sistema

### `scripts/check-release-needed.sh`
- **Função:** Analisa mudanças e determina se release é necessário
- **Entrada:** Lista de arquivos modificados
- **Saída:** Arquivo `.release_check` com informações

### `scripts/version-bump.sh`
- **Função:** Executa versionamento semântico
- **Entrada:** Mudanças detectadas
- **Saída:** Nova versão calculada

## Workflow Atualizado

### **Trigger**
- Push para `master` ou `main`
- Pull Requests
- Execução manual

### **Steps**
1. **Checkout** com histórico completo
2. **Análise de Release** - Verifica se é necessário
3. **Build** - Se necessário, executa build
4. **Criação de Tag** - Automaticamente (apenas em push direto)
5. **Comentário no PR** - Informações detalhadas

## Exemplos de Cenários

### ✅ **Release Necessário**
```bash
# Mudança em arquivo Java
git commit -m "feat: adiciona nova funcionalidade" src/main/java/MyClass.java
git push origin master
# → Release automático criado
```

### ❌ **Release Não Necessário**
```bash
# Mudança apenas em documentação
git commit -m "docs: atualiza README" README.md
git push origin master
# → Nenhum release criado
```

### 🔄 **Pull Request**
```bash
# Qualquer mudança em PR
git commit -m "fix: corrige bug" src/main/java/BugFix.java
git push origin feature/bugfix
# → Análise feita, mas sem release (aguarda merge)
```

## Configuração

### **Arquivos de Configuração**
- **`.release_check`** - Criado durante análise
- **`.version_info`** - Criado durante versionamento

### **Variáveis de Ambiente**
- `GITHUB_EVENT_NAME` - Tipo do evento
- `GITHUB_BASE_REF` - Branch base (PRs)
- `GITHUB_HEAD_REF` - Branch head (PRs)

## Benefícios

### ✅ **Automatização Completa**
- Análise inteligente de mudanças
- Versionamento semântico automático
- Criação de tags automática
- Release automático

### ✅ **Filtros Inteligentes**
- Evita releases desnecessários
- Foca apenas em mudanças relevantes
- Economiza recursos de CI/CD

### ✅ **Transparência**
- Informações detalhadas em PRs
- Logs claros de decisões
- Rastreabilidade completa

## Troubleshooting

### **Problema: "Release não foi criado"**
**Verificar:**
1. Se as mudanças são relevantes
2. Se não é um PR
3. Se está na branch master
4. Logs do step "Check if Release is Needed"

### **Problema: "Tag já existe"**
**Solução:**
```bash
# Remover tag local
git tag -d v1.0.1

# Remover tag remota
git push origin --delete v1.0.1

# Recriar (o sistema fará automaticamente)
```

### **Problema: "Build falhou"**
**Verificar:**
1. Logs do workflow
2. Se o Docker image está disponível
3. Se as secrets estão configuradas

## Personalização

### **Adicionar Novos Padrões**
Edite `scripts/check-release-needed.sh`:

```bash
# Adicionar arquivo que NÃO gera release
NON_RELEASE_FILES+=("novo-padrao")

# Adicionar arquivo que GERA release
RELEASE_FILES+=("novo-padrao")
```

### **Modificar Lógica de Extensões**
```bash
case "$ext" in
    # Adicionar nova extensão
    novaext)
        return 0  # true - deve gerar release
        ;;
esac
```

## Monitoramento

### **Logs Importantes**
- `[RELEASE-CHECK]` - Análise de mudanças
- `[VERSION]` - Versionamento semântico
- `✅ Relevante para release` - Arquivo detectado
- `⏭️ Não relevante para release` - Arquivo ignorado

### **Arquivos de Status**
- `.release_check` - Resultado da análise
- `.version_info` - Informações da versão

## Próximos Passos

1. **Testar** o sistema com diferentes tipos de mudanças
2. **Monitorar** logs e resultados
3. **Ajustar** padrões conforme necessário
4. **Documentar** experiências e melhorias

## Links Relacionados

- **[📊 Versionamento Semântico](SEMANTIC_VERSIONING.md)** - Detalhes do versionamento
- **[🏷️ Guia de Releases](RELEASE_GUIDE.md)** - Como criar releases manualmente
- **[🔧 Configuração Dinâmica](CONFIGURACAO_DINAMICA.md)** - Configurações do projeto 