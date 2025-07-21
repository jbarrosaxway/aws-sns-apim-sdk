# Script de instalação do filtro AWS Lambda para Axway API Gateway (Windows)
# Autor: Assistente
# Data: $(Get-Date)

# Configurações
$POLICY_STUDIO_PROJECT = "C:\Users\jbarros\apiprojects\POC-CUSTOM-FILTER"
$PROJECT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path
$YAML_SOURCE_DIR = Join-Path $PROJECT_DIR "src\main\resources\yaml"

Write-Host "=== Instalação do Filtro AWS Lambda para Policy Studio (Windows) ===" -ForegroundColor Green
Write-Host "Projeto Policy Studio: $POLICY_STUDIO_PROJECT" -ForegroundColor Yellow
Write-Host "Diretório do projeto: $PROJECT_DIR" -ForegroundColor Yellow
Write-Host "Diretório YAML fonte: $YAML_SOURCE_DIR" -ForegroundColor Yellow
Write-Host ""

# Verificar se o diretório do projeto Policy Studio existe
if (-not (Test-Path $POLICY_STUDIO_PROJECT)) {
    Write-Host "❌ Erro: Projeto Policy Studio não encontrado: $POLICY_STUDIO_PROJECT" -ForegroundColor Red
    Write-Host "Ajuste a variável `$POLICY_STUDIO_PROJECT no script se necessário" -ForegroundColor Yellow
    exit 1
}

# Verificar se o diretório YAML fonte existe
if (-not (Test-Path $YAML_SOURCE_DIR)) {
    Write-Host "❌ Erro: Diretório YAML fonte não encontrado: $YAML_SOURCE_DIR" -ForegroundColor Red
    Write-Host "Execute o build do projeto primeiro" -ForegroundColor Yellow
    exit 1
}

# Função para copiar arquivos YAML
function Copy-YamlFiles {
    param(
        [string]$SourcePath,
        [string]$DestPath,
        [string]$Description
    )
    
    Write-Host "📁 Copiando $Description..." -ForegroundColor Cyan
    
    # Criar diretório de destino se não existir
    if (-not (Test-Path $DestPath)) {
        New-Item -ItemType Directory -Path $DestPath -Force | Out-Null
        Write-Host "  Criado diretório: $DestPath" -ForegroundColor Gray
    }
    
    # Copiar arquivos
    try {
        Copy-Item -Path "$SourcePath\*" -Destination $DestPath -Recurse -Force
        Write-Host "  ✅ $Description copiado com sucesso" -ForegroundColor Green
    }
    catch {
        Write-Host "  ❌ Erro ao copiar $Description`: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
    
    return $true
}

# Função para adicionar conteúdo ao final do arquivo Internationalization Default.yaml
function Append-InternationalizationContent {
    param(
        [string]$SourceFile,
        [string]$DestFile
    )
    
    Write-Host "📝 Adicionando conteúdo ao Internationalization Default.yaml..." -ForegroundColor Cyan
    
    try {
        # Ler conteúdo do arquivo fonte
        $sourceContent = Get-Content $SourceFile -Raw
        
        # Verificar se o arquivo de destino existe
        if (Test-Path $DestFile) {
            # Adicionar conteúdo ao final do arquivo existente
            Add-Content -Path $DestFile -Value "`n$sourceContent"
            Write-Host "  ✅ Conteúdo adicionado ao final do arquivo existente" -ForegroundColor Green
        } else {
            # Criar novo arquivo se não existir
            Copy-Item -Path $SourceFile -Destination $DestFile -Force
            Write-Host "  ✅ Arquivo criado com o conteúdo" -ForegroundColor Green
        }
        
        return $true
    }
    catch {
        Write-Host "  ❌ Erro ao adicionar conteúdo: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# 1. Copiar AWSLambdaFilter.yaml
$sourceFilter = Join-Path $YAML_SOURCE_DIR "META-INF\types\Entity\Filter\AWSFilter"
$destFilter = Join-Path $POLICY_STUDIO_PROJECT "META-INF\types\Entity\Filter\AWSFilter"

$filterSuccess = Copy-YamlFiles -SourcePath $sourceFilter -DestPath $destFilter -Description "AWSLambdaFilter.yaml"

# 2. Adicionar conteúdo ao Internationalization Default.yaml
$sourceSystemFile = Join-Path $YAML_SOURCE_DIR "System\Internationalization Default.yaml"
$destSystemFile = Join-Path $POLICY_STUDIO_PROJECT "System\Internationalization Default.yaml"

# Criar diretório System se não existir
$destSystemDir = Join-Path $POLICY_STUDIO_PROJECT "System"
if (-not (Test-Path $destSystemDir)) {
    New-Item -ItemType Directory -Path $destSystemDir -Force | Out-Null
    Write-Host "  Criado diretório: $destSystemDir" -ForegroundColor Gray
}

$systemSuccess = Append-InternationalizationContent -SourceFile $sourceSystemFile -DestFile $destSystemFile

# Verificar se ambas as operações foram bem-sucedidas
if ($filterSuccess -and $systemSuccess) {
    Write-Host ""
    Write-Host "=== Instalação Concluída ===" -ForegroundColor Green
    Write-Host ""
    Write-Host "📝 Próximos passos:" -ForegroundColor Yellow
    Write-Host "1. Abra o projeto no Policy Studio" -ForegroundColor White
    Write-Host "2. Vá em Window > Preferences > Runtime Dependencies" -ForegroundColor White
    Write-Host "3. Adicione os JARs AWS SDK se necessário:" -ForegroundColor White
    Write-Host "   - aws-java-sdk-lambda-1.12.314.jar" -ForegroundColor Gray
    Write-Host "   - aws-java-sdk-core-1.12.314.jar" -ForegroundColor Gray
    Write-Host "4. Reinicie o Policy Studio com a opção -clean" -ForegroundColor White
    Write-Host "5. O filtro 'AWS Lambda Filter' estará disponível na paleta de filtros" -ForegroundColor White
    Write-Host ""
    Write-Host "🔧 Para verificar se o filtro está funcionando:" -ForegroundColor Yellow
    Write-Host "- Abra o Policy Studio" -ForegroundColor White
    Write-Host "- Crie uma nova política" -ForegroundColor White
    Write-Host "- Procure por 'AWS Lambda' na paleta de filtros" -ForegroundColor White
    Write-Host "- Configure o filtro com os parâmetros necessários" -ForegroundColor White
    Write-Host ""
    Write-Host "📋 Arquivos copiados:" -ForegroundColor Yellow
    Write-Host "- $destFilter\AWSLambdaFilter.yaml" -ForegroundColor Gray
    Write-Host "- $destSystem\Internationalization Default.yaml" -ForegroundColor Gray
    Write-Host ""
    Write-Host "💡 Dica: Ajuste a variável `$POLICY_STUDIO_PROJECT no script se seu projeto estiver em outro local" -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "❌ Erro na instalação. Verifique as mensagens acima." -ForegroundColor Red
    exit 1
} 