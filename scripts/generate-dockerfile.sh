#!/bin/bash

# Script para gerar Dockerfiles dinamicamente para diferentes versões do Axway
# Uso: ./scripts/generate-dockerfile.sh [VERSION]

set -e

VERSION="${1:-7.7.0.20240830}"
CONFIG_FILE="axway-versions.json"

if [ ! -f "$CONFIG_FILE" ]; then
    echo "❌ Arquivo de configuração não encontrado: $CONFIG_FILE"
    exit 1
fi

# Extrair informações da versão do JSON
VERSION_INFO=$(jq -r ".versions[\"$VERSION\"]" "$CONFIG_FILE" 2>/dev/null)

if [ "$VERSION_INFO" = "null" ]; then
    echo "❌ Versão não encontrada: $VERSION"
    echo "📋 Versões disponíveis:"
    jq -r '.versions | keys[]' "$CONFIG_FILE"
    exit 1
fi

# Extrair valores
INSTALLER=$(echo "$VERSION_INFO" | jq -r '.installer')
BASE_PATH=$(echo "$VERSION_INFO" | jq -r '.base_path')
JAVA_VERSION=$(echo "$VERSION_INFO" | jq -r '.java_version')
GRADLE_VERSION=$(echo "$VERSION_INFO" | jq -r '.gradle_version')
DOCKER_IMAGE=$(echo "$VERSION_INFO" | jq -r '.docker_image')

echo "🔧 Gerando Dockerfile para versão: $VERSION"
echo "📦 Instalador: $INSTALLER"
echo "📁 Base path: $BASE_PATH"
echo "☕ Java: $JAVA_VERSION"
echo "🔨 Gradle: $GRADLE_VERSION"
echo "🐳 Docker image: $DOCKER_IMAGE"
echo ""

# Criar diretório para a versão
mkdir -p "docker/$VERSION"

# Gerar Dockerfile
cat > "docker/$VERSION/Dockerfile" << EOF
# Dockerfile para BUILD - Axway Gateway $VERSION
FROM ubuntu:20.04

# Evitar prompts interativos durante a instalação
ENV DEBIAN_FRONTEND=noninteractive

# Instalar dependências necessárias
RUN apt-get update && apt-get install -y \\
    wget \\
    curl \\
    unzip \\
    tar \\
    gzip \\
    openjdk-${JAVA_VERSION}-jdk \\
    openjdk-${JAVA_VERSION}-jdk-headless \\
    git \\
    libc6-dev \\
    libstdc++6 \\
    jq \\
    && apt-get clean \\
    && rm -rf /var/lib/apt/lists/*

# Configurar Java $JAVA_VERSION
ENV JAVA_HOME=/usr/lib/jvm/java-${JAVA_VERSION}-openjdk-amd64
ENV PATH=\$JAVA_HOME/bin:\$PATH

# Criar diretório para instalação
RUN mkdir -p /opt/axway

# Copiar o instalador da Axway
COPY $INSTALLER /tmp/

# Copiar arquivo de licença
COPY license.txt /opt/axway/license.txt

# Dar permissão de execução ao instalador
RUN chmod +x /tmp/$INSTALLER

# Instalar Axway Gateway com Policy Studio
RUN /tmp/$INSTALLER \\
    --licenseFilePath "/opt/axway/license.txt" \\
    --disable-components cassandra \\
    --enable-components nodemanager,apigateway,apimgmt,packagedeploytools,qstart,analytics,policystudio,configurationstudio \\
    --setup_type advanced \\
    --unattendedmodeui none \\
    --prefix '$BASE_PATH' \\
    --debuglevel 4 \\
    --mode unattended

# Definir variáveis de ambiente
ENV AXWAY_HOME=$BASE_PATH
ENV PATH=\$JAVA_HOME/bin:\$PATH

# Instalar Gradle
RUN curl -O https://downloads.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip && \\
    unzip gradle-${GRADLE_VERSION}-bin.zip -d /opt && \\
    ln -s /opt/gradle-${GRADLE_VERSION}/bin/gradle /usr/local/bin/gradle && \\
    rm gradle-${GRADLE_VERSION}-bin.zip

# Criar diretório de trabalho
WORKDIR /workspace

# Copiar código fonte
COPY . .

# Comando para build
CMD ["gradle", "buildJarLinux"]

# Limpar arquivos sensíveis após o build
RUN rm -f /tmp/$INSTALLER /opt/axway/license.txt
EOF

echo "✅ Dockerfile gerado: docker/$VERSION/Dockerfile"

# Gerar script de build
cat > "docker/$VERSION/build.sh" << EOF
#!/bin/bash

# Script para build da imagem Docker para versão $VERSION
set -e

VERSION="$VERSION"
DOCKER_IMAGE="$DOCKER_IMAGE"

echo "🔧 Build da imagem Docker para Axway $VERSION"
echo "🐳 Imagem: \$DOCKER_IMAGE"

# Build da imagem
docker build -t "\$DOCKER_IMAGE" .

echo "✅ Build concluído!"
echo "🐳 Imagem: \$DOCKER_IMAGE"
echo ""
echo "📋 Para testar:"
echo "docker run --rm -v \$(pwd):/workspace \$DOCKER_IMAGE gradle build"
EOF

chmod +x "docker/$VERSION/build.sh"

echo "✅ Script de build gerado: docker/$VERSION/build.sh"
echo ""
echo "📋 Próximos passos:"
echo "1. Copie o instalador $INSTALLER para docker/$VERSION/"
echo "2. Execute: cd docker/$VERSION && ./build.sh"
echo "3. Teste: docker run --rm -v \$(pwd):/workspace $DOCKER_IMAGE gradle build" 