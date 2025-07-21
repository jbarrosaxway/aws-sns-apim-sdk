# Script para configurar o caminho do projeto Policy Studio
# Autor: Assistente
# Data: $(Get-Date)

Write-Host "=== Configuração do Projeto Policy Studio ===" -ForegroundColor Green
Write-Host ""

# Solicitar caminho do projeto
$defaultPath = "C:\Users\jbarros\apiprojects\POC-CUSTOM-FILTER"
Write-Host "Caminho padrão: $defaultPath" -ForegroundColor Yellow
Write-Host ""

$projectPath = Read-Host "Digite o caminho do seu projeto Policy Studio (ou pressione Enter para usar o padrão)"

if ([string]::IsNullOrWhiteSpace($projectPath)) {
    $projectPath = $defaultPath
}

Write-Host ""
Write-Host "Caminho selecionado: $projectPath" -ForegroundColor Cyan

# Verificar se o diretório existe
if (Test-Path $projectPath) {
    Write-Host "✅ Diretório encontrado!" -ForegroundColor Green
} else {
    Write-Host "❌ Diretório não encontrado!" -ForegroundColor Red
    Write-Host "Deseja criar o diretório? (S/N)" -ForegroundColor Yellow
    $createDir = Read-Host
    
    if ($createDir -eq "S" -or $createDir -eq "s") {
        try {
            New-Item -ItemType Directory -Path $projectPath -Force | Out-Null
            Write-Host "✅ Diretório criado com sucesso!" -ForegroundColor Green
        }
        catch {
            Write-Host "❌ Erro ao criar diretório: $($_.Exception.Message)" -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host "Operação cancelada." -ForegroundColor Yellow
        exit 1
    }
}

# Atualizar scripts com o novo caminho
$scripts = @("install-filter-windows.ps1", "install-filter-windows.cmd")

foreach ($script in $scripts) {
    if (Test-Path $script) {
        Write-Host "Atualizando $script..." -ForegroundColor Cyan
        
        if ($script -eq "install-filter-windows.ps1") {
            $content = Get-Content $script -Raw
            $content = $content -replace '\$POLICY_STUDIO_PROJECT = ".*?"', "`$POLICY_STUDIO_PROJECT = `"$projectPath`""
            Set-Content $script $content -Encoding UTF8
        } else {
            $content = Get-Content $script -Raw
            $content = $content -replace 'set POLICY_STUDIO_PROJECT=.*?', "set POLICY_STUDIO_PROJECT=$projectPath"
            Set-Content $script $content -Encoding UTF8
        }
        
        Write-Host "✅ $script atualizado!" -ForegroundColor Green
    } else {
        Write-Host "⚠️ $script não encontrado" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "=== Configuração Concluída ===" -ForegroundColor Green
Write-Host ""
Write-Host "📝 Próximos passos:" -ForegroundColor Yellow
Write-Host "1. Execute o script de instalação:" -ForegroundColor White
Write-Host "   PowerShell: .\install-filter-windows.ps1" -ForegroundColor Gray
Write-Host "   CMD: install-filter-windows.cmd" -ForegroundColor Gray
Write-Host "2. Abra o projeto no Policy Studio" -ForegroundColor White
Write-Host "3. Configure os JARs AWS SDK se necessário" -ForegroundColor White
Write-Host ""
Write-Host "💡 Dica: Os scripts agora estão configurados para o projeto: $projectPath" -ForegroundColor Cyan 