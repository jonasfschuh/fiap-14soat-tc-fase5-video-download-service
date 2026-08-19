# fiap-14soat-tc-fase5-video-download-service

![Java 21](https://img.shields.io/badge/Java_21-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.4.5-%236DB33F.svg?style=for-the-badge&logo=springboot&logoColor=white)
![Swagger](https://img.shields.io/badge/OpenAPI_3-%2385EA2D.svg?style=for-the-badge&logo=swagger&logoColor=black)
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

Este repositório contém o **microserviço Video Download** da plataforma **FIAP X** — responsável pelo download do arquivo ZIP de frames processados. Valida a propriedade (*ownership*) do vídeo pelo `userId`, lê o arquivo do armazenamento local (volume K8s compartilhado) e o serve como download direto.

A aplicação é desenvolvida em **Spring Boot 3 (Java 21)** com arquitetura hexagonal (Ports & Adapters / Clean Architecture).

> ℹ️ **Serviço 100% stateless:** não possui banco de dados, não consome nem publica em filas. O armazenamento compartilhado é provisionado pelo [`fiap-14soat-tc-fase5-iac-terraform`](https://github.com/jonasfschuh/fiap-14soat-tc-fase5-iac-terraform) via Kubernetes.

### Principais funcionalidades

| Funcionalidade | Descrição |
|----------------|-----------|
| **Download de ZIP** | Serve o arquivo ZIP de frames diretamente via `StreamingResponseBody` |
| **Validação de Ownership** | Valida se o `userId` do header é o dono do vídeo pela estrutura do caminho do arquivo |
| **Verificação de Existência** | Verifica se o ZIP existe no storage local antes de servir |
| **Autenticação** | Proxy para o `auth-lambda` (login) — o `userId` é extraído do header `X-User-Id` injetado pelo API Gateway |

### Estrutura de Módulos Maven

```
fiap-14soat-tc-fase5-video-download-service/
├── application/      → Controllers REST, DTOs, mappers, exception handlers, testes BDD (Cucumber)
├── domain/           → Modelos, use cases, ports de entrada e saída, exceções de domínio
├── infrastructure/   → Adapters de armazenamento local, configurações
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
│   LocalFileDownloadAdapter (verifica existência + lê arquivo)      │
│   NoOpPresignStorageAdapter (testes)                                │
│   DownloadBeanConfiguration  │  RestTemplateConfiguration          │
│   SwaggerConfiguration                                              │
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
    │  constrói path local: outputs/{userId}/{videoId}/frames.zip
    ├──► [VideoPresignStoragePort]
    │        ├── LocalFileDownloadAdapter
    │        │     ├── Files.exists()            → verifica existência
    │        │     └── StreamingResponseBody      → serve o arquivo
    │        └── NoOpPresignStorageAdapter  (profile: local-test)
    │
    ▼
200 OK → arquivo ZIP em streaming
ou
404 Not Found   ← ZIP não encontrado / userId incorreto
```

### Infraestrutura Local (K8s + Docker Compose)

```
┌─────────────────────────────────────────────────────────────────┐
│  Kubernetes (Docker Desktop) — namespace fiapx                   │
│  provisionado pelo fiap-14soat-tc-fase5-iac-terraform            │
│                                                                   │
│  ┌───────────────────┐   ┌────────────────────────────────┐      │
│  │  video-download   │   │  PVC: fiapx-video-pvc           │      │
│  │  :8086            │◄──│  /app/videos   (uploads)        │      │
│  │  (Spring Boot)    │   │  /app/processed (ZIPs)         │      │
│  └───────────────────┘   └────────────────────────────────┘      │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Tecnologias Utilizadas

### Core

| Tecnologia | Versão | Uso |
|------------|--------|-----|
| **Java** | 21 | Linguagem da aplicação |
| **Spring Boot** | 3.4.5 | Framework principal |
| **Swagger / OpenAPI** | 3.x | Documentação interativa da API |

### Storage

| Tecnologia | Ambiente | Uso |
|------------|----------|-----|
| **Armazenamento local** | Local K8s / Prod | Leitura dos ZIPs processados (PVC `fiapx-video-pvc`) |

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
| **Docker Compose** | 2.x | Orquestração local |
| **Kubernetes** | Latest | Orquestração com volume compartilhado (PVC) |
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
- [Docker Desktop 4.25+](https://www.docker.com/products/docker-desktop/) com Kubernetes habilitado
- **[`fiap-14soat-tc-fase5-iac-terraform`](https://github.com/jonasfschuh/fiap-14soat-tc-fase5-iac-terraform) provisionado** (cria o PVC `fiapx-video-pvc`)

---

### ⚙️ Pré-requisito: provisionar o iac-terraform

```bash
# No diretório do iac-terraform:
bash scripts/setup-cluster.sh
```

---

### Opção A — Stack completa com Docker Compose *(recomendado)*

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

```bash
# Parar os containers
docker compose down
```

---

### Opção B — Aplicação rodando na IDE

Execute a aplicação com o profile `local`:

```bash
./mvnw spring-boot:run -pl application \
  -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

Ou use as variáveis de ambiente na IDE:

```
SPRING_PROFILES_ACTIVE=local
SERVER_PORT=8086
STORAGE_OUTPUT_PATH=/app/processed
```

---

### Variáveis de Ambiente

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `SERVER_PORT` | `8086` | Porta da API |
| `STORAGE_OUTPUT_PATH` | `/app/processed` | Diretório onde os ZIPs processados estão armazenados |
| `AUTH_LAMBDA_URL` | — | URL do auth-lambda |
| `NEW_RELIC_LICENSE_KEY` | — | License key do New Relic |

---

### Build da Aplicação (sem Docker)

```bash
# Compilar e empacotar
mvn clean package -DskipTests

# Executar
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
| `GET` | `/api/videos/{videoId}/download` | ✅ | Faz download do ZIP de frames processados |
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

### Exemplo — Download do ZIP de Frames

```bash
curl -OJ http://localhost:8086/api/videos/550e8400-e29b-41d4-a716-446655440000/download \
  -H "X-User-Id: user-123"
```

**Response 200 OK:** arquivo `frames.zip` em streaming (`Content-Disposition: attachment`)

**Response 404 Not Found** *(ZIP não encontrado ou userId incorreto):*
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Video not found or not ready for download"
}
```

### Estrutura do path local

```
outputs/{userId}/{videoId}/frames.zip
```

A presença deste arquivo valida simultaneamente a **existência** do ZIP e a **propriedade** do usuário. Se o `userId` do header não corresponder ao do caminho, o arquivo não será encontrado → `404`.

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
| 1 | [fiap-14soat-tc-fase5-iac-terraform](https://github.com/jonasfschuh/fiap-14soat-tc-fase5-iac-terraform) | Kubernetes (Docker Desktop) — RabbitMQ, PostgreSQL, Prometheus, Grafana |
| 2 | [fiap-14soat-tc-fase5-auth-lambda](https://github.com/jonasfschuh/fiap-14soat-tc-fase5-auth-lambda) | Lambda Authorizer + Cognito + API Gateway |
| 3 | [fiap-14soat-tc-fase5-video-upload-service](https://github.com/jonasfschuh/fiap-14soat-tc-fase5-video-upload-service) | Upload + publisher de eventos no RabbitMQ |
| 4 | [fiap-14soat-tc-fase5-video-processing-service](https://github.com/jonasfschuh/fiap-14soat-tc-fase5-video-processing-service) | Processa vídeo, extrai frames, gera ZIP |
| 5 | [fiap-14soat-tc-fase5-video-status-service](https://github.com/jonasfschuh/fiap-14soat-tc-fase5-video-status-service) | Status e metadados dos vídeos por usuário |
| 6 | [fiap-14soat-tc-fase5-video-download-service](https://github.com/jonasfschuh/fiap-14soat-tc-fase5-video-download-service) | **Este repositório** — Download do ZIP de frames processados |
| 7 | [fiap-14soat-tc-fase5-notification-service](https://github.com/jonasfschuh/fiap-14soat-tc-fase5-notification-service) | Notificação por e-mail em caso de erro/conclusão |
| 8 | [fiap-14soat-tc-fase5-observability](https://github.com/jonasfschuh/fiap-14soat-tc-fase5-observability) | Prometheus + Grafana — dashboards e alertas |

---

## ⚙️ CI/CD — Configurando o Self-Hosted Runner

O pipeline de deploy deste repositório utiliza um **GitHub Actions self-hosted runner** rodando na máquina local com acesso ao cluster Kubernetes (Docker Desktop).

### Pré-requisitos do runner

Certifique-se de que a máquina possui instalado:

| Ferramenta | Versão mínima | Verificar |
|-----------|---------------|-----------|
| Docker Desktop (com K8s habilitado) | 4.x+ | `docker version` |
| kubectl | 1.28+ | `kubectl version --client` |
| Java 21 (JDK) | 21+ | `java -version` |
| Maven Wrapper | — | `.\mvnw.cmd -version` |

> Para o repositório IAC, também é necessário `terraform` (1.5+) e `helm` (3.x+).

### Passo a passo — configurar o runner

#### 1. Acesse as configurações do repositório no GitHub

```
GitHub → Repositório → Settings → Actions → Runners → New self-hosted runner
```

#### 2. Escolha o sistema operacional

Selecione **Windows** e a arquitetura **x64**.

#### 3. Baixe e configure o runner

Execute os comandos exibidos pelo GitHub na sua máquina local (PowerShell como Administrador):

```powershell
# Criar pasta para o runner (ajuste o caminho se necessário)
mkdir C:\actions-runner; cd C:\actions-runner

# Baixar o runner (substitua a URL pela exibida no GitHub)
Invoke-WebRequest -Uri https://github.com/actions/runner/releases/download/vX.X.X/actions-runner-win-x64-X.X.X.zip -OutFile actions-runner.zip

# Extrair
Add-Type -AssemblyName System.IO.Compression.FileSystem
[System.IO.Compression.ZipFile]::ExtractToDirectory("$PWD\actions-runner.zip", "$PWD")

# Configurar (use o token gerado pelo GitHub na tela de configuração)
.\config.cmd --url https://github.com/<org>/<repo> --token <TOKEN-GERADO-PELO-GITHUB>
```

#### 4. Instalar como serviço Windows (recomendado)

```powershell
# Instalar e iniciar como serviço Windows (executa automaticamente no boot)
.\svc.cmd install
.\svc.cmd start

# Verificar status
.\svc.cmd status
```

#### 5. Verificar o runner no GitHub

```
GitHub → Repositório → Settings → Actions → Runners
```

O runner deve aparecer com status **Idle** (verde). A partir daí, qualquer push para `main` ou `develop` disparará o pipeline de deploy automaticamente.

### Verificar o deploy após o pipeline

```powershell
# Listar pods no namespace fiapx
kubectl get pods -n fiapx

# Verificar logs do serviço
kubectl logs -l app=<nome-do-app> -n fiapx --tail=50

# Acessar via Swagger (após NGINX Ingress estar ativo)
# http://localhost/<caminho>/swagger-ui.html
```

### Gerenciar o runner

```powershell
# Parar o serviço
.\svc.cmd stop

# Remover o serviço
.\svc.cmd uninstall

# Remover o runner do GitHub
.\config.cmd remove --token <TOKEN>
```

> 💡 **Dica:** Para múltiplos repositórios, crie uma pasta separada para cada runner (ex: `C:\actions-runner\auth`, `C:\actions-runner\upload`) e repita o processo para cada um.

---

<div align="center">

**🎓 Desenvolvido para o Tech Challenge FIAP 14SOAT — Fase 5 (Hackathon)**

*Projeto Acadêmico — Pós-Graduação em Arquitetura de Software · FIAP 2025/2026*

[⬆ Voltar ao topo](#fiap-14soat-tc-fase5-video-download-service)

</div>
