#!/bin/bash

# Script helper para Docker - Axway API Gateway

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para imprimir com cores
print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Função para mostrar ajuda
show_help() {
    echo "🐳 Docker Helper - Axway API Gateway"
    echo ""
    echo "Uso: $0 [comando]"
    echo ""
    echo "Comandos:"
    echo "  build          - Build da imagem Docker"
    echo "  start          - Iniciar container"
    echo "  stop           - Parar container"
    echo "  restart        - Reiniciar container"
    echo "  logs           - Ver logs do container"
    echo "  shell          - Entrar no container"
    echo "  install        - Instalar filtro AWS Lambda"
    echo "  status         - Verificar status"
    echo "  clean          - Limpar containers e imagens"
    echo "  help           - Mostrar esta ajuda"
    echo ""
    echo "Exemplos:"
    echo "  $0 build"
    echo "  $0 start"
    echo "  $0 install"
}

# Função para build
build_image() {
    print_info "Build da imagem Docker..."
    docker build -t axway-api-gateway .
    print_success "Imagem buildada com sucesso!"
}

# Função para iniciar
start_container() {
    print_info "Iniciando container..."
    docker-compose up -d
    print_success "Container iniciado!"
}

# Função para parar
stop_container() {
    print_info "Parando container..."
    docker-compose down
    print_success "Container parado!"
}

# Função para reiniciar
restart_container() {
    print_info "Reiniciando container..."
    docker-compose restart
    print_success "Container reiniciado!"
}

# Função para logs
show_logs() {
    print_info "Mostrando logs..."
    docker-compose logs -f
}

# Função para shell
enter_shell() {
    print_info "Entrando no container..."
    docker exec -it axway-api-gateway bash
}

# Função para instalar filtro
install_filter() {
    print_info "Instalando filtro AWS Lambda..."
    docker exec -it axway-api-gateway bash -c "
        cd /workspace
        ./gradlew -Daxway.base=/opt/axway/Axway-7.7.0.20240830 buildJarLinux
        ./gradlew -Daxway.base=/opt/axway/Axway-7.7.0.20240830 installLinux
    "
    print_success "Filtro instalado com sucesso!"
}

# Função para status
show_status() {
    print_info "Status do container:"
    docker-compose ps
    
    echo ""
    print_info "Logs recentes:"
    docker-compose logs --tail=10
    
    echo ""
    print_info "Health check:"
    docker inspect axway-api-gateway | grep -A 5 Health || print_warning "Health check não configurado"
}

# Função para limpar
clean_docker() {
    print_warning "Limpando containers e imagens..."
    docker-compose down
    docker rmi axway-api-gateway 2>/dev/null || true
    print_success "Limpeza concluída!"
}

# Verificar se o comando foi fornecido
if [ $# -eq 0 ]; then
    show_help
    exit 1
fi

# Processar comando
case "$1" in
    build)
        build_image
        ;;
    start)
        start_container
        ;;
    stop)
        stop_container
        ;;
    restart)
        restart_container
        ;;
    logs)
        show_logs
        ;;
    shell)
        enter_shell
        ;;
    install)
        install_filter
        ;;
    status)
        show_status
        ;;
    clean)
        clean_docker
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        print_error "Comando desconhecido: $1"
        echo ""
        show_help
        exit 1
        ;;
esac 