# fiap-14soat-tc-fase5-video-download-service

![Java 21](https://img.shields.io/badge/Java_21-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.4.5-%236DB33F.svg?style=for-the-badge&logo=springboot&logoColor=white)
![Swagger](https://img.shields.io/badge/OpenAPI_3-%2385EA2D.svg?style=for-the-badge&logo=swagger&logoColor=black)
![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazonwebservices&logoColor=white)
![Amazon S3](https://img.shields.io/badge/Amazon_S3-%23569A31.svg?style=for-the-badge&logo=amazons3&logoColor=white)
![LocalStack](https://img.shields.io/badge/LocalStack-%23000000.svg?style=for-the-badge&logo=localstack&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
![Kubernetes](https://img.shields.io/badge/Kubernetes-%23326CE5.svg?style=for-the-badge&logo=kubernetes&logoColor=white)
![New Relic](https://img.shields.io/badge/New_Relic-%231CE783.svg?style=for-the-badge&logo=newrelic&logoColor=white)
![Hexagonal Architecture](https://img.shields.io/badge/Hexagonal-Architecture-7B2D8B?style=for-the-badge)
![DDD](https://img.shields.io/badge/Domain--Driven_Design-430098?style=for-the-badge)
![BDD](https://img.shields.io/badge/BDD-Cucumber-23D96C?style=for-the-badge&logo=cucumber&logoColor=white)
![Cucumber](https://img.shields.io/badge/Cucumber_7.18-%2323D96C.svg?style=for-the-badge&logo=cucumber&logoColor=white)
![JUnit 5](https://img.shields.io/badge/JUnit_5-%2325A162.svg?style=for-the-badge&logo=junit5&logoColor=white)
![JaCoCo](https://img.shields.io/badge/JaCoCo_%E2%89%A580%25-green?style=for-the-badge)
![Mockito](https://img.shields.io/badge/Mockito_5-%23EE4C2C.svg?style=for-the-badge)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-%232671E5.svg?style=for-the-badge&logo=githubactions&logoColor=white)
![Maven](https://img.shields.io/badge/Apache_Maven-%23C71A36.svg?style=for-the-badge&logo=apachemaven&logoColor=white)

---

## 📑 Sumário

- [👤 Autor](#-autor)
- [📋 Descrição](#-descrição)
- [🏗️ Arquitetura](#️-arquitetura)
- [🛠️ Tecnologias Utilizadas](#️-tecnologias-utilizadas)
- [🔒 Proteção da Branch main](#-proteção-da-branch-main)
- [🚀 Execução Local](#-execução-local)
- [🔌 API — Swagger e Endpoints](#-api--swagger-e-endpoints)
- [🧪 Testes](#-testes)
- [🔗 Repositórios Relacionados](#-repositórios-relacionados)

---

## 👤 Autor

| Nome                 | E-mail                  | RM        | Discord          | WhatsApp        |
|----------------------|-------------------------|-----------|------------------|-----------------|
| Jonas Fernando Schuh | jonasschuh@hotmail.com  | rm369458  | jonasf.schuh     | 47 9 9960-1396  |

**Grupo:** 2 · FIAP 14SOAT Fase 5 — Hackathon

---

## 📋 Descrição

Este repositório contém o **microserviço Video Download** da plataforma **FIAP X** — responsável por gerar **presigned URLs do Amazon S3** para download do arquivo ZIP de frames processados. Valida a propriedade (*ownership*) do vídeo pelo `userId` via estrutura da S3 key, sem consultar banco de dados.

A aplicação é desenvolvida em **Spring Boot 3 (Java 21)** com arquitetura hexagonal (Ports & Adapters / Clean Architecture).

> ⚠️ **Serviço 100% stateless:** não possui banco de dados, não consome nem publica em filas SQS. Depende do LocalStack provido pelo repositório `fiap-14soat-tc-fase5-video-upload-service` via `fiap-network`.

### Principais funcionalidades

| Funcionalidade | Descrição |
|----------------|-----------|
| **Geração de Presigned URL** | Gera URL temporária (TTL 15 min) para download direto do ZIP no S3 |
| **Validação de Ownership** | Valida se o `userId` do header é o dono do vídeo pela estrutura da S3 key |
| **Verificação de Existência** | Usa `S3 HeadObject` para checar se o ZIP existe antes de gerar a URL |
| **Autenticação** | Proxy para o `auth-lambda` (login) — o `userId` é extraído do header `X-User-Id` injetado pelo API Gateway |

### Estrutura de Módulos Maven

```
fiap-14soat-tc-fase5-video-download-service/
├── application/      → Controllers REST, DTOs, mappers, exception handlers, testes BDD (Cucumber)
├── domain/           → Modelos, use cases, ports de entrada e saída, exceções de domínio
├── infrastructure/   → Adapters S3, configurações AWS
└── report-aggregate/ → Agregador de cobertura JaCoCo (multi-módulo)
```

---

## 🏗️ Arquitetura

### Arquitetura Hexagonal (Ports & Adapters)

```
┌────────────────────────────────────────────────────────────┐
│                    Application Layer                        │
│   VideoDownloadController  │  AuthProxyController  │  DTOs │
│   GlobalExceptionHandler   │  SwaggerConfig                 │
└─────────────────────────┬──────────────────────────────────┘
                          │  Input Ports
┌─────────────────────────▼──────────────────────────────────┐
│                     Domain Layer                            │
│   PresignedUrlResult (model)                                │
│   GenerateDownloadUrlUseCase                                │
│   GenerateDownloadUrlInputPort                              │
│   VideoPresignStoragePort                                   │
└─────────────────────────┬──────────────────────────────────┘
                          │  Output Ports
┌─────────────────────────▼──────────────────────────────────┐
│                  Infrastructure Layer                       │
│   S3PresignedUrlAdapter (HeadObject + S3Presigner)         │
│   NoOpPresignStorageAdapter (testes)                        │
│   AwsS3Configuration  │  DownloadBeanConfiguration         │
│   RestTemplateConfiguration  │  SwaggerConfiguration       │
└────────────────────────────────────────────────────────────┘
```

### Fluxo de Download

```
[Usuário]
    │  GET /api/videos/{videoId}/download
    │  Header: X-User-Id: {userId}
    ▼
[API Gateway] ──── [auth-lambda] ← valida JWT, injeta X-User-Id
    │
    ▼
[VideoDownloadController]
    │  extrai userId do header X-User-Id
    ▼
[GenerateDownloadUrlUseCase]
    │  constrói S3 key: outputs/{userId}/{videoId}/frames.zip
    ├──► [VideoPresignStoragePort]
    │        ├── S3PresignedUrlAdapter
    │        │     ├── S3Client.headObject()   → verifica existência
    │        │     └── S3Presigner.presignGetObject() → gera URL
    │        └── NoOpPresignStorageAdapter  (profile: local-test)
    │
    ▼
200 OK → { "presignedUrl": "https://...", "expiresInMinutes": 15 }
ou
404 Not Found   ← ZIP não encontrado / userId incorreto
```

### Infraestrutura Local (Docker Compose)

```
┌─────────────────────────────────────────────────────────────────┐
│  fiap-network (bridge — compartilhada entre todos os serviços)   │
│                                                                   │
│  ┌───────────────────┐   ┌──────────────────┐                   │
│  │  video-download   │   │   LocalStack *    │                   │
│  │  :8086            │   │   :4566           │                   │
│  │  (Spring Boot)    │   │   S3 + SQS + SNS  │                   │
│  └───────────────────┘   └──────────────────┘                   │
│                                                                   │
│  * LocalStack provido pelo video-upload-service                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Tecnologias Utilizadas

### Core

| Tecnologia | Versão | Uso |
|------------|--------|-----|
| **Java** | 21 | Linguagem da aplicação |
| **Spring Boot** | 3.4.5 | Framework principal |
| **AWS SDK v2 S3** | 2.28.0 | S3Client (HeadObject) + S3Presigner |
| **Swagger / OpenAPI** | 3.x | Documentação interativa da API |

### Storage

| Tecnologia | Ambiente | Uso |
|------------|----------|-----|
| **Amazon S3** | AWS | Leitura dos ZIPs processados e geração de presigned URL |
| **LocalStack** | Local/Docker | Emulação de S3 (provido pelo video-upload-service) |

### Testes

| Ferramenta | Uso |
|------------|-----|
| **JUnit 5** | Testes unitários |
| **Mockito 5.x** | Mocks para testes unitários |
| **Cucumber 7.18** | Testes BDD (Behavior Driven Development) |
| **JaCoCo** | Cobertura de código (mínimo 80%) |

### DevOps & Infraestrutura

| Ferramenta | Versão | Uso |
|------------|--------|-----|
| **Docker** | 24.x | Containerização da aplicação |
| **Docker Compose** | 2.x | Orquestração local (conecta à fiap-network) |
| **Kubernetes** | Latest | Orquestração em produção (EKS) |
| **Terraform** | Latest | IaC AWS (repositório iac-terraform) |
| **Maven** | 3.9+ | Build e gerenciamento de dependências |
| **New Relic** | 8.x | APM / Observabilidade |
| **GitHub Actions** | Latest | CI/CD |

---

## 🔒 Proteção da Branch main

As regras abaixo foram aplicadas em todos os repositórios da stack para atender ao requisito do Tech Challenge:

> *"Branch main protegida (sem commits diretos). Uso obrigatório de Pull Requests para merge. Deploy automático das branches de produção."*

### Regras configuradas no GitHub → Settings → Branches

| Regra | Valor |
|---|---|
| **Require a pull request before merging** | ✅ Ativado — bloqueia commits diretos na `main` |
| **Required approvals** | `1` revisão obrigatória antes do merge (OBS: desabilitado neste estudo — grupo com 1 pessoa) |
| **Dismiss stale reviews on new commits** | ✅ Ativado — revalida aprovação se o PR for atualizado |
| **Require status checks to pass** | ✅ Ativado — bloqueia merge se o PR Validation falhar |
| **Require branches to be up to date** | ✅ Ativado — evita merge de branch desatualizada |
| **Do not allow bypassing** | ✅ Ativado — nem o owner ignora as regras |

### Status check obrigatório neste repositório

| Check | Job no `pr-validation.yaml` |
|---|---|
| `build-and-test` | Build Maven + testes unitários + cobertura JaCoCo |

> ⚠️ O status check só aparece para seleção no GitHub após a **primeira execução bem-sucedida** do PR Validation.

---

## 🚀 Execução Local

### Pré-requisitos

- [Java 21+](https://adoptium.net/)
- [Maven 3.9+](https://maven.apache.org/)
- [Docker Desktop 4.25+](https://www.docker.com/products/docker-desktop/)
- `fiap-14soat-tc-fase5-video-upload-service` rodando (provê LocalStack + S3 + `fiap-network`)

---

### ⚙️ Configuração da rede Docker compartilhada

Antes de subir qualquer serviço, crie a rede externa `fiap-network` (necessária uma única vez por máquina):

```bash
docker network create fiap-network
```

---

### Opção A — Stack completa com Docker Compose *(recomendado)*

> ⚠️ Certifique-se de que o `video-upload-service` está rodando primeiro (LocalStack + S3).

```bash
# Build e start do serviço
docker compose up --build

# Apenas start (sem rebuild)
docker compose up

# Em background
docker compose up -d
```

| Serviço | URL | Descrição |
|---------|-----|-----------|
| **API** | http://localhost:8086 | Video Download Service |
| **Swagger UI** | http://localhost:8086/swagger-ui.html | Documentação interativa |
| **LocalStack** | http://localhost:4566 | Emulação de S3 (via video-upload-service) |

```bash
# Parar os containers
docker compose down
```

---

### Opção B — Aplicação rodando na IDE

```bash
# No repositório video-upload-service (provê LocalStack + fiap-network)
cd ../fiap-14soat-tc-fase5-video-upload-service
docker compose up -d localstack
```

Em seguida, execute a aplicação com o profile `local`:

```bash
./mvnw spring-boot:run -pl application \
  -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

Ou use as variáveis de ambiente na IDE:

```
SPRING_PROFILES_ACTIVE=docker
SERVER_PORT=8086
APP_STORAGE_TYPE=s3
S3_BUCKET=fiap-video-uploads
AWS_ENDPOINT_OVERRIDE=http://localhost:4566
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=test
AWS_SECRET_ACCESS_KEY=test
```

---

### Variáveis de Ambiente

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `SERVER_PORT` | `8086` | Porta da API |
| `APP_STORAGE_TYPE` | `local-test` | `s3` em docker/prod, `local-test` em testes |
| `S3_BUCKET` | `fiap-video-uploads` | Bucket S3 |
| `PRESIGN_TTL_MINUTES` | `15` | Validade da presigned URL em minutos |
| `AWS_ENDPOINT_OVERRIDE` | — | URL do LocalStack (ex.: `http://fiap-localstack:4566`) |
| `AWS_REGION` | `us-east-1` | Região AWS |
| `AWS_ACCESS_KEY_ID` | `test` | Access key |
| `AWS_SECRET_ACCESS_KEY` | `test` | Secret key |
| `AUTH_LAMBDA_URL` | — | URL do auth-lambda |
| `NEW_RELIC_LICENSE_KEY` | — | License key do New Relic |

---

### Build da Aplicação (sem Docker)

```bash
# Compilar e empacotar
mvn clean package -DskipTests

# Executar (requer LocalStack rodando)
java -jar application/target/video-download-application-*.jar \
  --spring.profiles.active=local
```

---

## 🔌 API — Swagger e Endpoints

### 📄 Swagger UI

| Ambiente | URL |
|----------|-----|
| **Local (Docker Compose)** | http://localhost:8086/swagger-ui.html |
| **OpenAPI JSON** | http://localhost:8086/v3/api-docs |

### Endpoints Disponíveis

| Método | Path | Auth | Descrição |
|--------|------|------|-----------|
| `POST` | `/auth/login` | ❌ | Proxy para auth-lambda (retorna JWT) |
| `GET` | `/api/videos/{videoId}/download` | ✅ | Gera presigned URL para o ZIP de frames |
| `GET` | `/actuator/health` | ❌ | Health check |

> ✅ = requer header `X-User-Id` (injetado pelo API Gateway após validação JWT)

### Exemplo — Login

```bash
curl -X POST http://localhost:8086/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user@email.com","password":"senha123"}'
```

**Response 200 OK:**
```json
{
  "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600
}
```

### Exemplo — Gerar Presigned URL

```bash
curl http://localhost:8086/api/videos/550e8400-e29b-41d4-a716-446655440000/download \
  -H "X-User-Id: user-123"
```

**Response 200 OK:**
```json
{
  "presignedUrl": "https://fiap-video-uploads.s3.amazonaws.com/outputs/user-123/550e8400.../frames.zip?X-Amz-Expires=900&...",
  "expiresInMinutes": 15
}
```

**Response 404 Not Found** *(ZIP não encontrado ou userId incorreto):*
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Video not found or not ready for download"
}
```

### Estrutura da S3 Key

```
outputs/{userId}/{videoId}/frames.zip
```

A presença desta key valida simultaneamente a **existência** do ZIP e a **propriedade** do usuário. Se o `userId` do header não corresponder ao da key, o `HeadObject` retornará `NoSuchKeyException` → `404`.

---

## 🧪 Testes

### Executar todos os testes

```bash
mvn clean test
```

### Executar apenas testes unitários (Domain)

```bash
mvn test -pl domain
```

### Executar apenas testes BDD (Cucumber - Application)

```bash
mvn test -pl application
```

### Executar com relatório de cobertura

```bash
mvn clean verify

# Abrir relatório (Windows)
start report-aggregate/target/site/jacoco-aggregate/index.html
```

### Estratégia de Testes

| Tipo | Ferramenta | Localização | Cobertura alvo |
|------|------------|-------------|----------------|
| Unitários (domain) | JUnit 5 + Mockito | `domain/` | ≥ 80% |
| BDD | Cucumber | `application/` | Fluxos principais |
| Unitários (infra) | JUnit 5 + Mockito | `infrastructure/` | ≥ 80% |

---

### 🎬 Vídeos de Apresentação

| Fase | Link |
|------|------|
| Fase 1 | [Apresentação Tech Challenge 1 — RaceForce](https://youtu.be/EKwE8l4yE1M) |
| Fase 2 | [Apresentação Tech Challenge 2 — RaceForce](https://youtu.be/95ml0-H9Vf4) |
| Fase 3 | [Apresentação Tech Challenge 3 — RaceForce](https://www.youtube.com/watch?v=KB-FC_4zsPE) |
| Fase 4 | [Apresentação Tech Challenge 4 — RaceForce](https://www.youtube.com/watch?v=vR3x4kW0l90) |
| Fase 5 | *(em desenvolvimento)* |

---

## 🔗 Repositórios Relacionados

| Ordem | Repositório | Descrição |
|-------|-------------|-----------|
| 1 | [fiap-14soat-tc-fase5-iac-terraform](https://github.com/jonasfschuh/fiap-14soat-tc-fase5-iac-terraform) | VPC, ECS/EKS, S3, SQS, RDS, Cognito — infraestrutura AWS |
| 2 | [fiap-14soat-tc-fase5-auth-lambda](https://github.com/jonasfschuh/fiap-14soat-tc-fase5-auth-lambda) | Lambda Authorizer + Cognito + API Gateway |
| 3 | [fiap-14soat-tc-fase5-video-upload-service](https://github.com/jonasfschuh/fiap-14soat-tc-fase5-video-upload-service) | Upload + SQS publisher — **master do ambiente local** |
| 4 | [fiap-14soat-tc-fase5-video-processing-service](https://github.com/jonasfschuh/fiap-14soat-tc-fase5-video-processing-service) | Processa vídeo, extrai frames, gera ZIP |
| 5 | [fiap-14soat-tc-fase5-video-status-service](https://github.com/jonasfschuh/fiap-14soat-tc-fase5-video-status-service) | Status e metadados dos vídeos por usuário |
| 6 | [fiap-14soat-tc-fase5-video-download-service](https://github.com/jonasfschuh/fiap-14soat-tc-fase5-video-download-service) | **Este repositório** — Download do ZIP via presigned URL S3 |
| 7 | [fiap-14soat-tc-fase5-notification-service](https://github.com/jonasfschuh/fiap-14soat-tc-fase5-notification-service) | Notificação por e-mail em caso de erro/conclusão |
| 8 | [fiap-14soat-tc-fase5-observability](https://github.com/jonasfschuh/fiap-14soat-tc-fase5-observability) | Prometheus + Grafana — dashboards e alertas |

---

<div align="center">

**🎓 Desenvolvido para o Tech Challenge FIAP 14SOAT — Fase 5 (Hackathon)**

*Projeto Acadêmico — Pós-Graduação em Arquitetura de Software · FIAP 2025/2026*

[⬆ Voltar ao topo](#fiap-14soat-tc-fase5-video-download-service)

</div>
