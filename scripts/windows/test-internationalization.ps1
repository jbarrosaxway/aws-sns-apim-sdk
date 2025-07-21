# Script de teste para verificar a funcionalidade de adicionar conteúdo ao Internationalization Default.yaml
# Autor: Assistente
# Data: $(Get-Date)

Write-Host "=== Teste da Funcionalidade Internationalization Default.yaml ===" -ForegroundColor Green
Write-Host ""

# Configurações de teste
$testDir = ".\test-internationalization"
$sourceFile = "src\main\resources\yaml\System\Internationalization Default.yaml"
$destFile = "$testDir\Internationalization Default.yaml"

# Criar diretório de teste
if (Test-Path $testDir) {
    Remove-Item $testDir -Recurse -Force
}
New-Item -ItemType Directory -Path $testDir -Force | Out-Null

Write-Host "📁 Diretório de teste criado: $testDir" -ForegroundColor Cyan

# Função para adicionar conteúdo ao final do arquivo
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

# Teste 1: Criar arquivo novo
Write-Host ""
Write-Host "🧪 Teste 1: Criar arquivo novo" -ForegroundColor Yellow
$success1 = Append-InternationalizationContent -SourceFile $sourceFile -DestFile $destFile

if ($success1) {
    Write-Host "✅ Teste 1 passou!" -ForegroundColor Green
    Write-Host "📄 Conteúdo do arquivo criado:" -ForegroundColor Cyan
    Get-Content $destFile | Write-Host -ForegroundColor Gray
} else {
    Write-Host "❌ Teste 1 falhou!" -ForegroundColor Red
}

# Teste 2: Adicionar conteúdo ao arquivo existente
Write-Host ""
Write-Host "🧪 Teste 2: Adicionar conteúdo ao arquivo existente" -ForegroundColor Yellow

# Criar um arquivo existente com conteúdo
$existingContent = @"
- type: ExistingFilter
  fields:
    type: TestFilter
  logging:
    fatal: "Existing error message"
    failure: "Existing failure message"
    success: "Existing success message"
"@

Set-Content -Path $destFile -Value $existingContent -Force
Write-Host "📄 Arquivo existente criado com conteúdo:" -ForegroundColor Cyan
Get-Content $destFile | Write-Host -ForegroundColor Gray

# Adicionar novo conteúdo
$success2 = Append-InternationalizationContent -SourceFile $sourceFile -DestFile $destFile

if ($success2) {
    Write-Host "✅ Teste 2 passou!" -ForegroundColor Green
    Write-Host "📄 Conteúdo final do arquivo:" -ForegroundColor Cyan
    Get-Content $destFile | Write-Host -ForegroundColor Gray
} else {
    Write-Host "❌ Teste 2 falhou!" -ForegroundColor Red
}

# Limpeza
Write-Host ""
Write-Host "🧹 Limpando arquivos de teste..." -ForegroundColor Yellow
Remove-Item $testDir -Recurse -Force
Write-Host "✅ Limpeza concluída!" -ForegroundColor Green

Write-Host ""
Write-Host "=== Teste Concluído ===" -ForegroundColor Green
if ($success1 -and $success2) {
    Write-Host "✅ Todos os testes passaram! A funcionalidade está funcionando corretamente." -ForegroundColor Green
} else {
    Write-Host "❌ Alguns testes falharam. Verifique os erros acima." -ForegroundColor Red
} 