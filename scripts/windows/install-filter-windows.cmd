@echo off
REM Script de instalação do filtro AWS Lambda para Axway API Gateway (Windows CMD)
REM Autor: Assistente
REM Data: %date% %time%

REM Configurações
set POLICY_STUDIO_PROJECT=C:\Users\jbarros\apiprojects\POC-CUSTOM-FILTER
set PROJECT_DIR=%~dp0
set YAML_SOURCE_DIR=%PROJECT_DIR%src\main\resources\yaml

echo === Instalação do Filtro AWS Lambda para Policy Studio (Windows) ===
echo Projeto Policy Studio: %POLICY_STUDIO_PROJECT%
echo Diretório do projeto: %PROJECT_DIR%
echo Diretório YAML fonte: %YAML_SOURCE_DIR%
echo.

REM Verificar se o diretório do projeto Policy Studio existe
if not exist "%POLICY_STUDIO_PROJECT%" (
    echo ❌ Erro: Projeto Policy Studio não encontrado: %POLICY_STUDIO_PROJECT%
    echo Ajuste a variável POLICY_STUDIO_PROJECT no script se necessário
    pause
    exit /b 1
)

REM Verificar se o diretório YAML fonte existe
if not exist "%YAML_SOURCE_DIR%" (
    echo ❌ Erro: Diretório YAML fonte não encontrado: %YAML_SOURCE_DIR%
    echo Execute o build do projeto primeiro
    pause
    exit /b 1
)

REM Função para copiar arquivos YAML
:CopyYamlFiles
set SourcePath=%~1
set DestPath=%~2
set Description=%~3

echo 📁 Copiando %Description%...

REM Criar diretório de destino se não existir
if not exist "%DestPath%" (
    mkdir "%DestPath%" 2>nul
    echo   Criado diretório: %DestPath%
)

REM Copiar arquivos
xcopy "%SourcePath%\*" "%DestPath%\" /E /Y /Q >nul 2>&1
if %errorlevel% equ 0 (
    echo   ✅ %Description% copiado com sucesso
    set /a success=1
) else (
    echo   ❌ Erro ao copiar %Description%
    set /a success=0
)
goto :eof

REM Função para adicionar conteúdo ao final do arquivo Internationalization Default.yaml
:AppendInternationalizationContent
set SourceFile=%~1
set DestFile=%~2

echo 📝 Adicionando conteúdo ao Internationalization Default.yaml...

REM Verificar se o arquivo de destino existe
if exist "%DestFile%" (
    REM Adicionar conteúdo ao final do arquivo existente
    echo. >> "%DestFile%"
    type "%SourceFile%" >> "%DestFile%"
    echo   ✅ Conteúdo adicionado ao final do arquivo existente
) else (
    REM Criar novo arquivo se não existir
    copy "%SourceFile%" "%DestFile%" >nul 2>&1
    echo   ✅ Arquivo criado com o conteúdo
)

if %errorlevel% equ 0 (
    set /a success=1
) else (
    echo   ❌ Erro ao adicionar conteúdo
    set /a success=0
)
goto :eof

REM 1. Copiar AWSLambdaFilter.yaml
set sourceFilter=%YAML_SOURCE_DIR%\META-INF\types\Entity\Filter\AWSFilter
set destFilter=%POLICY_STUDIO_PROJECT%\META-INF\types\Entity\Filter\AWSFilter

call :CopyYamlFiles "%sourceFilter%" "%destFilter%" "AWSLambdaFilter.yaml"
set filterSuccess=%success%

REM 2. Adicionar conteúdo ao Internationalization Default.yaml
set sourceSystemFile=%YAML_SOURCE_DIR%\System\Internationalization Default.yaml
set destSystemFile=%POLICY_STUDIO_PROJECT%\System\Internationalization Default.yaml

REM Criar diretório System se não existir
set destSystemDir=%POLICY_STUDIO_PROJECT%\System
if not exist "%destSystemDir%" (
    mkdir "%destSystemDir%" 2>nul
    echo   Criado diretório: %destSystemDir%
)

call :AppendInternationalizationContent "%sourceSystemFile%" "%destSystemFile%"
set systemSuccess=%success%

REM Verificar se ambas as operações foram bem-sucedidas
if %filterSuccess% equ 1 if %systemSuccess% equ 1 (
    echo.
    echo === Instalação Concluída ===
    echo.
    echo 📝 Próximos passos:
    echo 1. Abra o projeto no Policy Studio
    echo 2. No Policy Studio, vá em Window ^> Preferences ^> Runtime Dependencies
    echo 3. Adicione os JARs AWS SDK se necessário:
    echo    - aws-java-sdk-lambda-1.12.314.jar
    echo    - aws-java-sdk-core-1.12.314.jar
    echo 4. Reinicie o Policy Studio com a opção -clean
    echo 5. O filtro 'AWS Lambda Filter' estará disponível na paleta de filtros
    echo.
    echo 🔧 Para verificar se o filtro está funcionando:
    echo - Abra o Policy Studio
    echo - Crie uma nova política
    echo - Procure por 'AWS Lambda' na paleta de filtros
    echo - Configure o filtro com os parâmetros necessários
    echo.
    echo 📋 Arquivos copiados:
    echo - %destFilter%\AWSLambdaFilter.yaml
    echo - %destSystem%\Internationalization Default.yaml
    echo.
    echo 💡 Dica: Ajuste a variável POLICY_STUDIO_PROJECT no script se seu projeto estiver em outro local
) else (
    echo.
    echo ❌ Erro na instalação. Verifique as mensagens acima.
    pause
    exit /b 1
)

pause 