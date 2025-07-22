# 🖥️ Instalação no Windows

## 📋 Visão Geral

No Windows, o processo de instalação é **diferente** do Linux devido às limitações de build do JAR. O Windows apenas instala os arquivos YAML, enquanto o JAR deve ser buildado no Linux.

## 🔄 Processo Completo

### **Passo 1: Build do JAR no Linux**
```bash
# No ambiente Linux
./gradlew buildJarLinux
```

### **Passo 2: Copiar JAR para Windows**
```bash
# Copie o arquivo: build/libs/aws-lambda-apim-sdk-1.0.1.jar
# Para o ambiente Windows
```

### **Passo 3: Instalar YAML no Windows**
```bash
# No ambiente Windows
./gradlew installWindows
```

## 🚀 Instalação Rápida

### **1. Instalação Interativa (Recomendado)**
```powershell
# Solicita o caminho do projeto Policy Studio
./gradlew installWindows
```

### **2. Instalação em Projeto Específico**
```powershell
# Instalação direta em projeto específico
./gradlew installWindowsToProject
```

## 📁 Estrutura do Projeto Policy Studio

O sistema copia os arquivos YAML para um **projeto Policy Studio** específico:

```
C:\Users\jbarros\apiprojects\DIGIO-POC-AKS\
├── META-INF\types\Entity\Filter\AWSFilter\
│   └── AWSLambdaFilter.yaml
└── System\
    └── Internationalization Default.yaml
```

### **Exemplos de Projetos:**
- `C:\Users\jbarros\apiprojects\DIGIO-POC-AKS`
- `C:\Users\jbarros\apiprojects\POC-CUSTOM-FILTER`
- `C:\Projects\API-Gateway\MyProject`

## 🔧 Configuração do Policy Studio

### **1. Adicionar JAR ao Runtime Dependencies**
- Abra o Policy Studio
- Vá em **Window > Preferences > Runtime Dependencies**
- Adicione o JAR: `aws-lambda-apim-sdk-1.0.1.jar`

### **2. Adicionar JARs AWS SDK**
- [aws-java-sdk-lambda-1.12.314.jar](https://repo1.maven.org/maven2/com/amazonaws/aws-java-sdk-lambda/1.12.314/aws-java-sdk-lambda-1.12.314.jar)
- [aws-java-sdk-core-1.12.314.jar](https://repo1.maven.org/maven2/com/amazonaws/aws-java-sdk-core/1.12.314/aws-java-sdk-core-1.12.314.jar)

### **3. Reiniciar Policy Studio**
```cmd
# Reinicie com -clean
policystudio.exe -clean
```

## 📋 Comandos Disponíveis

### **Verificar Configuração**
```powershell
./gradlew "-Daxway.base=C:\Axway-7.7.0-20240830" setAxwayPath
```

### **Mostrar Links dos JARs**
```powershell
./gradlew showAwsJars
```

### **Instalar YAML**
```powershell
# Instalação interativa (solicita caminho do projeto)
./gradlew installWindows

# Instalação em projeto específico (com caminho)
./gradlew "-Dproject.path=C:\Users\jbarros\apiprojects\DIGIO-POC-AKS" installWindowsToProject

# Instalação interativa (se não especificar caminho)
./gradlew installWindowsToProject
```

## ⚠️ Troubleshooting

### **Erro: "Project '.base=C' not found"**
```powershell
# Use aspas duplas
./gradlew "-Daxway.base=C:\Axway-7.7.0-20240830" installWindows
```

### **Erro: "Cannot find directory"**
```powershell
# Verifique se o caminho do projeto existe
dir C:\Users\jbarros\apiprojects\DIGIO-POC-AKS

# O sistema criará o diretório se não existir
```

### **Erro: "Permission denied"**
```powershell
# Execute como administrador
# Clique com botão direito no PowerShell e "Executar como administrador"
```

## 🎯 Exemplo Completo

```powershell
# 1. Verificar configuração
./gradlew setAxwayPath

# 2. Instalar YAML em projeto Policy Studio (interativo)
./gradlew installWindows

# 3. Instalar YAML em projeto específico (direto)
./gradlew "-Dproject.path=C:\Users\jbarros\apiprojects\DIGIO-POC-AKS" installWindowsToProject

# 4. Mostrar links dos JARs AWS
./gradlew showAwsJars
```

### **Exemplos de Uso:**
```powershell
# Interativo (solicita caminho)
./gradlew installWindows

# Direto com caminho específico
./gradlew "-Dproject.path=C:\Users\jbarros\apiprojects\DIGIO-POC-AKS" installWindowsToProject
./gradlew "-Dproject.path=C:\Projects\API-Gateway\MyProject" installWindowsToProject
```

## 📝 Notas Importantes

- ✅ **JAR deve ser buildado no Linux**
- ✅ **Windows apenas instala YAML**
- ✅ **Use aspas duplas para propriedades do sistema**
- ✅ **Execute como administrador se necessário**
- ✅ **Verifique se os caminhos existem antes da instalação**

## 🔗 Links Úteis

- [JAR AWS Lambda SDK](https://repo1.maven.org/maven2/com/amazonaws/aws-java-sdk-lambda/1.12.314/aws-java-sdk-lambda-1.12.314.jar)
- [JAR AWS Core SDK](https://repo1.maven.org/maven2/com/amazonaws/aws-java-sdk-core/1.12.314/aws-java-sdk-core-1.12.314.jar) 