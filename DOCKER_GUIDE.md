# 🐳 Docker Guide - Axway API Gateway

## 📋 Visão Geral

Este guia explica como usar Docker para executar o Axway API Gateway com o filtro AWS Lambda integrado.

## 🚀 Quick Start

### **1. Build da Imagem**
```bash
# Build da imagem Docker
docker build -t axway-api-gateway .

# Ou usando docker-compose
docker-compose build
```

### **2. Executar Container**
```bash
# Executar com docker-compose (recomendado)
docker-compose up -d

# Ou executar diretamente
docker run -d \
  --name axway-api-gateway \
  -p 8080:8080 \
  -p 8443:8443 \
  -p 8090:8090 \
  -p 8091:8091 \
  axway-api-gateway
```

### **3. Verificar Status**
```bash
# Verificar logs
docker logs axway-api-gateway

# Verificar health check
docker ps
```

## 📁 Estrutura do Container

```
/opt/axway/Axway-7.7.0.20240830/
├── apigateway/
│   ├── system/
│   │   ├── lib/
│   │   ├── lib/modules/
│   │   └── lib/plugins/
│   ├── groups/
│   ├── logs/
│   └── conf/
└── policystudio/
    └── plugins/
```

## 🔧 Configuração

### **Variáveis de Ambiente**
```bash
AXWAY_HOME=/opt/axway/Axway-7.7.0.20240830
APIGATEWAY_HOME=/opt/axway/Axway-7.7.0.20240830/apigateway
POLICYSTUDIO_HOME=/opt/axway/Axway-7.7.0.20240830/policystudio
JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
```

### **Portas Expostas**
- **8080**: HTTP API Gateway
- **8443**: HTTPS API Gateway
- **8090**: Admin Console
- **8091**: Management API

## 📦 Volumes

### **Volumes Persistentes**
```yaml
volumes:
  - axway-data:/opt/axway/Axway-7.7.0.20240830/apigateway/groups
  - axway-logs:/opt/axway/Axway-7.7.0.20240830/apigateway/logs
  - axway-config:/opt/axway/Axway-7.7.0.20240830/apigateway/conf
```

### **Volumes de Desenvolvimento**
```bash
# Montar código fonte para desenvolvimento
docker run -v $(pwd):/workspace axway-api-gateway
```

## 🛠️ Instalação do Filtro AWS Lambda

### **1. Build do JAR no Container**
```bash
# Entrar no container
docker exec -it axway-api-gateway bash

# Build do JAR
./gradlew -Daxway.base=/opt/axway/Axway-7.7.0.20240830 buildJarLinux
```

### **2. Instalação Automática**
```bash
# Instalar filtro no container
docker exec -it axway-api-gateway ./gradlew -Daxway.base=/opt/axway/Axway-7.7.0.20240830 installLinux
```

### **3. Verificar Instalação**
```bash
# Verificar se o JAR foi instalado
docker exec axway-api-gateway ls -la /opt/axway/Axway-7.7.0.20240830/apigateway/groups/group-2/instance-1/ext/lib/
```

## 🔍 Troubleshooting

### **Problema: Container não inicia**
```bash
# Verificar logs
docker logs axway-api-gateway

# Verificar se o Java está disponível
docker exec axway-api-gateway java -version
```

### **Problema: Portas não acessíveis**
```bash
# Verificar portas expostas
docker port axway-api-gateway

# Verificar se o gateway está rodando
docker exec axway-api-gateway curl -f http://localhost:8080/health
```

### **Problema: JAR não encontrado**
```bash
# Verificar se o build foi executado
docker exec axway-api-gateway ls -la build/libs/

# Executar build novamente
docker exec axway-api-gateway ./gradlew clean build
```

## 🚀 Desenvolvimento

### **Executar em Modo Desenvolvimento**
```bash
# Executar com volume montado
docker run -it --rm \
  -v $(pwd):/workspace \
  -p 8080:8080 \
  -p 8443:8443 \
  axway-api-gateway bash
```

### **Debug do Container**
```bash
# Entrar no container
docker exec -it axway-api-gateway bash

# Verificar variáveis de ambiente
env | grep AXWAY

# Verificar estrutura de diretórios
ls -la /opt/axway/Axway-7.7.0.20240830/
```

## 📊 Monitoramento

### **Health Check**
```bash
# Verificar status do health check
docker inspect axway-api-gateway | grep -A 10 Health

# Testar health check manualmente
docker exec axway-api-gateway curl -f http://localhost:8080/health
```

### **Logs**
```bash
# Ver logs em tempo real
docker logs -f axway-api-gateway

# Ver logs específicos
docker exec axway-api-gateway tail -f /opt/axway/Axway-7.7.0.20240830/apigateway/logs/event.log
```

## 🔗 Links Úteis

- [Docker Hub](https://hub.docker.com/)
- [GitHub Container Registry](https://ghcr.io/)
- [Axway API Gateway Documentation](https://docs.axway.com/)

## 📝 Notas Importantes

- ✅ **Java 8**: O container usa Java 8 para compatibilidade
- ✅ **Volumes**: Dados são persistidos em volumes Docker
- ✅ **Health Check**: Container inclui health check automático
- ✅ **Portas**: Todas as portas padrão do Axway são expostas
- ⚠️ **Licença**: Você precisa de uma licença válida do Axway
- ⚠️ **Arquivos**: Os arquivos de instalação do Axway não estão incluídos 