@echo off
echo ============================================
echo  Iniciando Ambiente Completo (Container)
echo  Microservico: ms-video-download
echo ============================================
echo.
echo  ATENCAO: LocalStack (S3) e gerenciado pelo repositorio:
echo    fiap-14soat-tc-fase5-video-upload-service
echo  Certifique-se de que ele esteja rodando antes.
echo.
echo  Criando rede compartilhada (se nao existir)...
docker network create fiap-network 2>nul
echo.

docker-compose up --build -d

echo.
echo    Servicos iniciados:
echo    - API:      http://localhost:8086
echo    - Swagger:  http://localhost:8086/swagger-ui.html
echo    - Actuator: http://localhost:8086/actuator/health
echo.
echo    LocalStack (gerenciado pelo upload-service):
echo    - LocalStack: http://localhost:4566
echo    - StackPort:  http://localhost:8080
echo.
echo    Endpoint principal:
echo    GET http://localhost:8086/api/videos/{videoId}/download
echo    Header: X-User-Id: seu-user-id
echo.
