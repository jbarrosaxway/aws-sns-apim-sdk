# Dockerfile para RUNTIME - recebe o JAR já buildado
FROM docker.repository.axway.com/apigateway-docker-prod/7.7/gateway:7.7.0.20240830-4-BN0145-ubi9

# Definir variáveis de ambiente
ENV AXWAY_HOME=/opt/Axway
ENV JAVA_HOME=/opt/java/openjdk-11
ENV PATH=$JAVA_HOME/bin:$PATH

# Instalar dependências necessárias
USER root
RUN dnf update -y --allowerasing && \
    dnf install -y --allowerasing \
    unzip \
    java-11-openjdk \
    java-11-openjdk-devel \
    && dnf clean all

# Configurar Java 11 como padrão
RUN alternatives --set java java-11-openjdk.x86_64 && \
    alternatives --set javac java-11-openjdk.x86_64

# Criar diretório para nosso SDK
RUN mkdir -p /opt/aws-lambda-sdk

# Criar diretório ext/lib se não existir
RUN mkdir -p /opt/Axway/apigateway/groups/emt-group/emt-service/ext/lib

# Copiar o JAR do nosso SDK
COPY aws-lambda-apim-sdk-*.jar /opt/aws-lambda-sdk/

# Copiar JARs do Axway para ext/lib
RUN cp /opt/Axway/apigateway/lib/aws-java-sdk-lambda-*.jar /opt/Axway/apigateway/groups/emt-group/emt-service/ext/lib/ 2>/dev/null || echo "AWS SDK JAR não encontrado"
RUN cp /opt/Axway/apigateway/lib/aws-java-sdk-core-*.jar /opt/Axway/apigateway/groups/emt-group/emt-service/ext/lib/ 2>/dev/null || echo "AWS Core JAR não encontrado"
RUN cp /opt/Axway/apigateway/lib/jackson-*.jar /opt/Axway/apigateway/groups/emt-group/emt-service/ext/lib/ 2>/dev/null || echo "Jackson JARs não encontrados"

# Copiar nosso SDK para ext/lib também
RUN cp /opt/aws-lambda-sdk/aws-lambda-apim-sdk-*.jar /opt/Axway/apigateway/groups/emt-group/emt-service/ext/lib/

# Definir permissões
RUN chown -R 1001:1001 /opt/aws-lambda-sdk
RUN chown -R 1001:1001 /opt/Axway/apigateway/groups/emt-group/emt-service/ext/lib

# Voltar para o usuário não-root
USER 1001

# Expor portas padrão do API Gateway (para referência)
EXPOSE 8080 8443 8090 8091

# Comando padrão (herdado da imagem base)
CMD ["/opt/Axway/apigateway/posix/bin/startinstance"] 