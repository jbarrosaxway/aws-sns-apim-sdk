#!/bin/bash

echo "========================================"
echo "AWS Lambda APIM SDK - Instalador Linux"
echo "========================================"
echo

# Verificar se Axway está instalado
if [ -z "$AXWAY_HOME" ]; then
    echo "ERRO: Variável AXWAY_HOME não definida"
    echo
    echo "Por favor, defina a variável de ambiente AXWAY_HOME"
    echo "Exemplo: export AXWAY_HOME=/opt/Axway/API_Gateway/7.7.0.20240830"
    echo
    exit 1
fi

if [ ! -d "$AXWAY_HOME" ]; then
    echo "ERRO: Diretório do Axway não encontrado: $AXWAY_HOME"
    echo
    echo "Verifique se o caminho está correto e se o Axway está instalado"
    echo
    exit 1
fi

echo "Axway encontrado em: $AXWAY_HOME"
echo

# Verificar se JAR principal existe
MAIN_JAR=$(find . -name "aws-lambda-apim-sdk-*.jar" | head -1)
if [ -z "$MAIN_JAR" ]; then
    echo "ERRO: JAR principal não encontrado"
    echo
    echo "Certifique-se de que o arquivo aws-lambda-apim-sdk-*.jar está presente"
    echo
    exit 1
fi

echo "JAR principal encontrado: $MAIN_JAR"
echo

# Criar backup do diretório lib
BACKUP_DIR="lib_backup_$(date +%Y%m%d_%H%M%S)"
echo "Criando backup em: $AXWAY_HOME/ext/lib/$BACKUP_DIR"
mkdir -p "$AXWAY_HOME/ext/lib"
if [ -d "$AXWAY_HOME/ext/lib" ]; then
    cp -r "$AXWAY_HOME/ext/lib" "$AXWAY_HOME/ext/lib/$BACKUP_DIR" 2>/dev/null
    echo "Backup criado com sucesso"
else
    echo "Diretório ext/lib não existe, será criado"
fi
echo

# Copiar JAR principal
echo "Copiando JAR principal..."
cp "$MAIN_JAR" "$AXWAY_HOME/ext/lib/"
if [ $? -ne 0 ]; then
    echo "ERRO: Falha ao copiar JAR principal"
    exit 1
fi
echo "JAR principal copiado com sucesso"
echo

# Copiar dependências se existirem
if [ -d "dependencies" ]; then
  echo "Copiando dependências..."
  mkdir -p "$AXWAY_HOME/ext/lib/dependencies"
  cp dependencies/* "$AXWAY_HOME/ext/lib/dependencies/" 2>/dev/null
  if [ $? -eq 0 ]; then
    echo "Dependências copiadas com sucesso"
  else
    echo "AVISO: Algumas dependências não puderam ser copiadas"
  fi
  echo
else
  echo "Nenhuma dependência encontrada para copiar"
  echo
fi

# Copiar recursos do Policy Studio se existirem
if [ -d "resources" ]; then
  echo "Copiando recursos do Policy Studio..."
  if [ -d "resources/fed" ]; then
    mkdir -p "$AXWAY_HOME/ext/lib/fed"
    cp resources/fed/* "$AXWAY_HOME/ext/lib/fed/" 2>/dev/null
    echo "Recursos FED copiados com sucesso"
  fi
  if [ -d "resources/yaml" ]; then
    mkdir -p "$AXWAY_HOME/ext/lib/yaml"
    cp resources/yaml/* "$AXWAY_HOME/ext/lib/yaml/" 2>/dev/null
    echo "Recursos YAML copiados com sucesso"
  fi
  echo
else
  echo "Nenhum recurso encontrado para copiar"
  echo
fi

# Verificar se Policy Studio está rodando
echo "Verificando se Policy Studio está em execução..."
if pgrep -f "policystudio" > /dev/null; then
    echo "AVISO: Policy Studio está em execução"
    echo "Recomenda-se fechar o Policy Studio antes de continuar"
    echo
    read -p "Deseja continuar mesmo assim? (S/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Ss]$ ]]; then
        echo "Instalação cancelada"
        exit 0
    fi
    echo
fi

# Verificar se API Gateway está rodando
echo "Verificando se API Gateway está em execução..."
if pgrep -f "apigateway" > /dev/null; then
    echo "AVISO: API Gateway está em execução"
    echo "Recomenda-se parar o serviço antes de continuar"
    echo
    read -p "Deseja continuar mesmo assim? (S/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Ss]$ ]]; then
        echo "Instalação cancelada"
        exit 0
    fi
    echo
fi

echo "========================================"
echo "Instalação concluída com sucesso!"
echo "========================================"
echo
echo "Arquivos instalados:"
echo "- $MAIN_JAR -> $AXWAY_HOME/ext/lib/"
if [ -d "dependencies" ]; then
    echo "- Dependências -> $AXWAY_HOME/ext/lib/dependencies/"
fi
echo
echo "Backup criado em: $AXWAY_HOME/ext/lib/$BACKUP_DIR"
echo
echo "Próximos passos:"
echo "1. Reinicie o Policy Studio"
echo "2. Reinicie o API Gateway"
echo "3. O filtro AWS Lambda estará disponível no Policy Studio"
echo
echo "Para desinstalar, restaure o backup ou delete os arquivos copiados"
echo 