#!/bin/bash

# Script para copiar o instalador da Axway para o contexto do Docker
echo "📦 Copiando instalador da Axway..."

# Verificar se o instalador existe
if [ ! -f "/home/joaojbarros/APIGateway_7.7.20240830_Install_linux-x86-64_BN04.run" ]; then
    echo "❌ Erro: Instalador não encontrado em /home/joaojbarros/APIGateway_7.7.20240830_Install_linux-x86-64_BN04.run"
    exit 1
fi

# Copiar o instalador para o diretório atual
cp /home/joaojbarros/APIGateway_7.7.20240830_Install_linux-x86-64_BN04.run .

# Verificar se o arquivo de licença existe
if [ ! -f "/home/joaojbarros/license.txt" ]; then
    echo "⚠️  Aviso: Arquivo de licença não encontrado em /home/joaojbarros/license.txt"
    echo "📝 Criando arquivo de licença placeholder..."
    echo "placeholder-license" > license.txt
else
    # Copiar o arquivo de licença para o diretório atual
    cp /home/joaojbarros/license.txt .
fi

echo "✅ Instalador copiado com sucesso!"
echo "📁 Arquivo: APIGateway_7.7.20240830_Install_linux-x86-64_BN04.run"
echo "📁 Arquivo: license.txt" 