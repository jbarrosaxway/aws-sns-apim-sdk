# 🔧 Configuração Dinâmica do Axway API Gateway

## 📋 Visão Geral

O projeto agora suporta **configuração dinâmica** do caminho do Axway API Gateway, eliminando referências hardcoded e permitindo flexibilidade para diferentes instalações.

## 🎯 Problema Resolvido

**Antes:**
```gradle
def apim_folder = '/opt/axway/Axway-7.7.0.20240830/apigateway/system'
def ps_folder = '/opt/axway/Axway-7.7.0.20240830/policystudio'
```

**Depois:**
```gradle
def axway_base = System.getProperty('axway.base', '/opt/axway/Axway-7.7.0.20240830')
def apim_folder = "${axway_base}/apigateway/system"
def ps_folder = "${axway_base}/policystudio"
```

## 🚀 Como Usar

### 1. **Configuração Padrão**
```bash
./gradlew build
./gradlew installLinux
```
Usa o caminho padrão: `/opt/axway/Axway-7.7.0.20240830`

### 2. **Configuração Customizada**
```bash
# Para versão diferente
./gradlew -Daxway.base=/opt/axway/Axway-7.7.0.20210830 build

# Para instalação em diretório customizado
./gradlew -Daxway.base=/home/user/axway/Axway-7.7.0.20240830 installLinux

# Para Windows
./gradlew -Daxway.base=C:\Axway\Axway-7.7.0.20240830 installWindows
```

### 3. **Verificar Configuração Atual**
```bash
./gradlew setAxwayPath
```

## 📁 Estrutura Esperada

O sistema espera a seguinte estrutura no caminho especificado:

```
{caminho_axway}/
├── apigateway/
│   └── system/
│       ├── lib/
│       ├── lib/modules/
│       └── lib/plugins/
└── policystudio/
    └── plugins/
```

## 🔍 Exemplos Práticos

### **Exemplo 1: Versão Diferente**
```bash
# Usar versão 20210830
./gradlew -Daxway.base=/opt/axway/Axway-7.7.0.20210830 build
```

### **Exemplo 2: Instalação Customizada**
```bash
# Instalação em diretório do usuário
./gradlew -Daxway.base=/home/joao/axway/Axway-7.7.0.20240830 installLinux
```

### **Exemplo 3: Windows**
```bash
# Windows com caminho customizado
./gradlew -Daxway.base=C:\Program Files\Axway\Axway-7.7.0.20240830 installWindows
```

## 🛠️ Tasks Disponíveis

### **Configuração**
```bash
./gradlew setAxwayPath          # Mostra configuração atual
```

### **Build com Caminho Customizado**
```bash
./gradlew -Daxway.base=/path build
./gradlew -Daxway.base=/path clean build
```

### **Instalação com Caminho Customizado**
```bash
./gradlew -Daxway.base=/path installLinux
./gradlew -Daxway.base=/path installWindows
```

## ⚠️ Validações

O sistema **não valida automaticamente** se o caminho existe. Certifique-se de que:

1. ✅ O caminho especificado existe
2. ✅ A estrutura de diretórios está correta
3. ✅ As permissões de acesso estão adequadas

## 🔧 Troubleshooting

### **Erro: "Cannot find directory"**
```bash
# Verificar se o caminho existe
ls -la /opt/axway/Axway-7.7.0.20240830

# Usar caminho correto
./gradlew -Daxway.base=/caminho/correto build
```

### **Erro: "Permission denied"**
```bash
# Verificar permissões
ls -la /opt/axway/Axway-7.7.0.20240830

# Executar com sudo se necessário
sudo ./gradlew -Daxway.base=/opt/axway/Axway-7.7.0.20240830 installLinux
```

## 📋 Variáveis de Ambiente (Alternativa)

Você também pode usar variáveis de ambiente:

```bash
# Definir variável de ambiente
export AXWAY_BASE=/opt/axway/Axway-7.7.0.20240830

# Usar no Gradle
./gradlew -Daxway.base=$AXWAY_BASE build
```

## 🎉 Benefícios

- ✅ **Flexibilidade**: Suporta diferentes versões do Axway
- ✅ **Portabilidade**: Funciona em diferentes ambientes
- ✅ **Manutenibilidade**: Sem referências hardcoded
- ✅ **Compatibilidade**: Mantém compatibilidade com instalações padrão
- ✅ **Simplicidade**: Fácil de configurar e usar 