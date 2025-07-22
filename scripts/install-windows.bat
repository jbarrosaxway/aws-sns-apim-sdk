@echo off
setlocal enabledelayedexpansion

echo ========================================
echo AWS Lambda APIM SDK - Instalador Windows
echo ========================================
echo.

:: Verificar se Axway está instalado
set AXWAY_HOME=%AXWAY_HOME%
if "%AXWAY_HOME%"=="" (
    echo ERRO: Variavel AXWAY_HOME nao definida
    echo.
    echo Por favor, defina a variavel de ambiente AXWAY_HOME
    echo Exemplo: set AXWAY_HOME=C:\Axway\API_Gateway\7.7.0.20240830
    echo.
    pause
    exit /b 1
)

if not exist "%AXWAY_HOME%" (
    echo ERRO: Diretorio do Axway nao encontrado: %AXWAY_HOME%
    echo.
    echo Verifique se o caminho esta correto e se o Axway esta instalado
    echo.
    pause
    exit /b 1
)

echo Axway encontrado em: %AXWAY_HOME%
echo.

:: Verificar se JAR principal existe
set JAR_FILE=aws-lambda-apim-sdk-*.jar
for %%f in (%JAR_FILE%) do (
    if exist "%%f" (
        set MAIN_JAR=%%f
        goto :found_jar
    )
)

echo ERRO: JAR principal nao encontrado
echo.
echo Certifique-se de que o arquivo aws-lambda-apim-sdk-*.jar esta presente
echo.
pause
exit /b 1

:found_jar
echo JAR principal encontrado: !MAIN_JAR!
echo.

:: Criar backup do diretorio lib
set BACKUP_DIR=lib_backup_%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set BACKUP_DIR=%BACKUP_DIR: =0%
echo Criando backup em: %AXWAY_HOME%\ext\lib\%BACKUP_DIR%
if not exist "%AXWAY_HOME%\ext\lib" mkdir "%AXWAY_HOME%\ext\lib"
if exist "%AXWAY_HOME%\ext\lib" (
    xcopy "%AXWAY_HOME%\ext\lib" "%AXWAY_HOME%\ext\lib\%BACKUP_DIR%" /E /I /Y >nul 2>&1
    echo Backup criado com sucesso
) else (
    echo Diretorio ext\lib nao existe, sera criado
)
echo.

:: Copiar JAR principal
echo Copiando JAR principal...
copy "!MAIN_JAR!" "%AXWAY_HOME%\ext\lib\" >nul 2>&1
if errorlevel 1 (
    echo ERRO: Falha ao copiar JAR principal
    pause
    exit /b 1
)
echo JAR principal copiado com sucesso
echo.

:: Copiar dependencias se existirem
if exist "dependencies" (
  echo Copiando dependencias...
  if not exist "%AXWAY_HOME%\ext\lib\dependencies" mkdir "%AXWAY_HOME%\ext\lib\dependencies"
  xcopy "dependencies\*" "%AXWAY_HOME%\ext\lib\dependencies\" /Y >nul 2>&1
  if errorlevel 1 (
    echo AVISO: Algumas dependencias nao puderam ser copiadas
  ) else (
    echo Dependencias copiadas com sucesso
  )
  echo.
) else (
  echo Nenhuma dependencia encontrada para copiar
  echo.
)

:: Copiar recursos do Policy Studio se existirem
if exist "resources" (
  echo Copiando recursos do Policy Studio...
  if exist "resources\fed" (
    if not exist "%AXWAY_HOME%\ext\lib\fed" mkdir "%AXWAY_HOME%\ext\lib\fed"
    xcopy "resources\fed\*" "%AXWAY_HOME%\ext\lib\fed\" /Y >nul 2>&1
    echo Recursos FED copiados com sucesso
  )
  if exist "resources\yaml" (
    if not exist "%AXWAY_HOME%\ext\lib\yaml" mkdir "%AXWAY_HOME%\ext\lib\yaml"
    xcopy "resources\yaml\*" "%AXWAY_HOME%\ext\lib\yaml\" /Y >nul 2>&1
    echo Recursos YAML copiados com sucesso
  )
  echo.
) else (
  echo Nenhum recurso encontrado para copiar
  echo.
)

:: Verificar se Policy Studio esta rodando
echo Verificando se Policy Studio esta em execucao...
tasklist /FI "IMAGENAME eq policystudio.exe" 2>NUL | find /I /N "policystudio.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo AVISO: Policy Studio esta em execucao
    echo Recomenda-se fechar o Policy Studio antes de continuar
    echo.
    set /p CONTINUE="Deseja continuar mesmo assim? (S/N): "
    if /i not "!CONTINUE!"=="S" (
        echo Instalacao cancelada
        pause
        exit /b 0
    )
    echo.
)

:: Verificar se API Gateway esta rodando
echo Verificando se API Gateway esta em execucao...
tasklist /FI "IMAGENAME eq apigateway.exe" 2>NUL | find /I /N "apigateway.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo AVISO: API Gateway esta em execucao
    echo Recomenda-se parar o servico antes de continuar
    echo.
    set /p CONTINUE="Deseja continuar mesmo assim? (S/N): "
    if /i not "!CONTINUE!"=="S" (
        echo Instalacao cancelada
        pause
        exit /b 0
    )
    echo.
)

echo ========================================
echo Instalacao concluida com sucesso!
echo ========================================
echo.
echo Arquivos instalados:
echo - !MAIN_JAR! -> %AXWAY_HOME%\ext\lib\
if exist "dependencies" (
    echo - Dependencias -> %AXWAY_HOME%\ext\lib\dependencies\
)
echo.
echo Backup criado em: %AXWAY_HOME%\ext\lib\%BACKUP_DIR%
echo.
echo Prximos passos:
echo 1. Reinicie o Policy Studio
echo 2. Reinicie o API Gateway
echo 3. O filtro AWS Lambda estara disponivel no Policy Studio
echo.
echo Para desinstalar, restaure o backup ou delete os arquivos copiados
echo.
pause 