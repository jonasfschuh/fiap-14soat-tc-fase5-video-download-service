@echo off
setlocal ENABLEDELAYEDEXPANSION

REM =============================================
REM Script: Cobertura + SonarCloud
REM Microservico: ms-video-download
REM =============================================

set SONAR_TOKEN=4f67e76b9b4af9f0a280c0cad1cb50ea8dfc3297

call .\mvnw.cmd clean verify -Pcoverage
if errorlevel 1 (
  echo ERRO: Falha ao executar cobertura.
  pause
  exit /b 1
)

call .\mvnw.cmd sonar:sonar -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=!SONAR_TOKEN! -Dsonar.projectKey=fiap-14soat-tc-fase5-video-download
if errorlevel 1 (
  echo ERRO: Falha ao executar analise no SonarCloud.
  pause
  exit /b 1
)

echo ✅ Processo concluido: cobertura gerada e analise enviada ao SonarCloud.
pause
endlocal
