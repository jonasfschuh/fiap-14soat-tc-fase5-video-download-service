@echo off
echo ============================================
echo  Iniciando Ambiente de Desenvolvimento
echo  Microservico: ms-video-download
echo ============================================
echo.
echo  ATENCAO: Este servico NAO possui banco de dados.
echo  O LocalStack (S3) e gerenciado pelo repositorio:
echo    fiap-14soat-tc-fase5-video-upload-service
echo  Certifique-se de que ele esteja rodando antes.
echo.
echo  Criando rede compartilhada (se nao existir)...
docker network create fiap-network 2>nul
echo.
echo  Execute a aplicacao no IntelliJ com as seguintes variaveis de ambiente:
echo    SPRING_PROFILES_ACTIVE=docker
echo    SERVER_PORT=8086
echo    APP_STORAGE_TYPE=s3
echo    S3_BUCKET=fiap-video-uploads
echo    PRESIGN_TTL_MINUTES=15
echo    AWS_ENDPOINT_OVERRIDE=http://localhost:4566
echo    AWS_REGION=us-east-1
echo    AWS_ACCESS_KEY_ID=test
echo    AWS_SECRET_ACCESS_KEY=test
echo.
echo  Porta local da API: 8086
echo  http://localhost:8086/swagger-ui.html
echo  http://localhost:8086/api/videos/{videoId}/download
echo.
