# interview-prep-agent

AI agent that researches companies and simulates mock technical interviews. Given a company name and job description, it researches the company, conducts a multi-turn mock interview, and delivers scored coaching feedback.

**Stack:** Java 21, Spring Boot 3.3.4, Spring AI 1.0.0, React 18 + Vite 5, Groq (llama-3.3-70b-versatile), PostgreSQL, Oracle Cloud Free Tier

---

## Architecture

```mermaid
flowchart TD
    Client(["Client"])

    subgraph API ["REST API Layer"]
        RC["ResearchController\nPOST /api/v1/research"]
        SC["SessionController\n/api/v1/sessions/**"]
    end

    subgraph Services ["Service Layer"]
        RS["ResearchService"]
        ISS["InterviewSessionService"]
        CS["ChatService"]
        SS["ScoringService"]
        SHS["SessionHistoryService"]
    end

    subgraph Agents ["Agent Layer (Spring AI)"]
        RA["ResearchAgent\n(autonomous researcher)"]
        IA["InterviewerAgent\n(stateful multi-turn)"]
        CA["CoachAgent\n(stateless evaluator)"]
    end

    subgraph External ["External APIs"]
        GROQ["Groq\nllama-3.3-70b-versatile"]
        TAV["Tavily Search API"]
    end

    subgraph DB ["Persistence"]
        PG[("PostgreSQL\n(prod)")]
        H2[("H2\n(dev/test)")]
    end

    Client --> RC
    Client --> SC
    RC --> RS
    SC --> ISS
    SC --> CS
    SC --> SS
    SC --> SHS
    RS --> RA
    CS --> IA
    SS --> CA
    RA -->|tool call| TAV
    RA --> GROQ
    IA --> GROQ
    CA --> GROQ
    RS --> PG
    ISS --> PG
    CS --> PG
    SS --> PG
    SHS --> PG
    PG -. dev/test .-> H2
```

---

## UI

A React 18 + Vite SPA is bundled into the Spring Boot JAR and served at `/`. No separate frontend server is needed in production.

| Page | Route | Description |
|------|-------|-------------|
| Dashboard | `/` | Session stats, recent sessions table, top weak areas |
| New Interview | `/new` | Research form → company brief preview → start session |
| Interview | `/sessions/:id` | Chat interface with company brief panel |
| Results | `/sessions/:id/results` | Score, coach feedback, weak areas |

Dark/light mode toggle and mobile-responsive layout included.

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/research` | Research a company — returns session ID + CompanyBrief |
| POST | `/api/v1/sessions` | Start a mock interview session |
| POST | `/api/v1/sessions/{id}/chat` | Send a message, get interviewer reply |
| POST | `/api/v1/sessions/{id}/evaluate` | Trigger coaching feedback and scoring |
| GET  | `/api/v1/sessions/{id}` | Fetch session detail |
| GET  | `/api/v1/sessions` | List all sessions |
| GET  | `/api/v1/sessions/weak-areas` | Top 5 weak areas across all sessions |

All responses are wrapped in `ApiResponse<T>`: `{ data, error: { code, message }, meta: { timestamp } }`.

Interactive API docs available at `/swagger-ui/index.html`.

---

## Local Development

### Prerequisites
- Java 21
- Node.js 20
- Gradle 8.10 (or use the included `./gradlew` wrapper)

### Environment variables
```bash
export GROQ_API_KEY=gsk_...
export TAVILY_API_KEY=tvly-...
```

### Run backend
```bash
./gradlew bootRun
# App starts on http://localhost:8080 with H2 in-memory DB
```

### Run frontend (dev mode with hot reload)
```bash
cd frontend && npm install && npm run dev
# Vite dev server on http://localhost:5173
# API calls are proxied to http://localhost:8080
```

### Test
```bash
./gradlew test          # backend (Java)
cd frontend && npm test # frontend (Vitest)
```

---

## Production Deployment

The app runs on an **Oracle Cloud Free Tier Ampere A1 VM (Ubuntu 22.04)** using **Docker Compose** behind a **Caddy v2** reverse proxy.

### 1. VM setup

SSH into your VM and run the setup script:
```bash
curl -fsSL https://raw.githubusercontent.com/sharmavipin1608/interview-prep-agent/master/deploy/setup-vm.sh | bash
```

This installs Docker, Caddy, opens ports 80/443, and creates `/opt/interview-prep-agent/`.

### 2. Create the `.env` file on the VM

```bash
nano /opt/interview-prep-agent/.env
```

```
GROQ_API_KEY=gsk_...
TAVILY_API_KEY=tvly-...
POSTGRES_PASSWORD=choose-a-strong-password
```

### 3. Configure Caddy

Edit `deploy/Caddyfile` — replace the domain with yours:
```
your-domain.com {
    reverse_proxy localhost:8080
    ...
}
```

Copy to the VM and reload:
```bash
scp deploy/Caddyfile ubuntu@<vm-ip>:/tmp/Caddyfile
ssh ubuntu@<vm-ip> "sudo cp /tmp/Caddyfile /etc/caddy/Caddyfile && sudo systemctl reload caddy"
```

Caddy automatically provisions a TLS certificate via Let's Encrypt when a domain name is used.

### 4. GitHub Actions secrets

Add these to your repo (`Settings → Secrets → Actions`):

| Secret | Description |
|--------|-------------|
| `ORACLE_SSH_PRIVATE_KEY` | SSH private key for the Oracle VM |
| `ORACLE_SSH_KNOWN_HOSTS` | Output of `ssh-keyscan <your-vm-ip> 2>/dev/null \| grep -v "^#"` |
| `ORACLE_HOST` | Public IP or hostname of the Oracle VM |
| `ORACLE_USER` | SSH username (typically `ubuntu`) |
| `ORACLE_DEPLOY_PATH` | Remote path, e.g. `/opt/interview-prep-agent/` |

### 5. Deploy

Push to `master` — GitHub Actions runs tests, builds a Docker image, streams it to the VM, and runs `docker compose up -d` automatically.

---

## Project Structure

```
interview-prep-agent/
├── frontend/                          # React + Vite SPA
│   ├── src/
│   │   ├── api/client.js              # Axios + ApiResponse unwrapper
│   │   ├── hooks/                     # TanStack Query hooks
│   │   ├── components/                # Layout, Sidebar, Topbar, shared UI
│   │   ├── pages/                     # Dashboard, NewInterview, Interview, Results
│   │   └── styles/                    # CSS custom properties (light/dark) + global
│   ├── package.json
│   └── vite.config.js                 # Dev proxy → :8080; build → src/main/resources/static
├── src/
│   ├── main/java/com/vipinsharma/interviewprep/
│   │   ├── agent/          # ResearchAgent, InterviewerAgent, CoachAgent
│   │   ├── api/            # REST controllers + SpaFallbackController + GlobalExceptionHandler
│   │   ├── config/         # AiConfig (ChatClient beans)
│   │   ├── dto/            # Java records (request/response DTOs)
│   │   ├── model/          # JPA entities (Session, Message, Score, WeakArea)
│   │   ├── repository/     # Spring Data JPA repositories
│   │   └── service/        # Business logic services
│   └── main/resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-test.yml
│       ├── application-prod.yml
│       └── static/                    # Vite build output (served by Spring Boot)
├── deploy/
│   ├── Caddyfile                      # Caddy v2 reverse proxy config
│   ├── setup-vm.sh                    # One-shot VM setup script
│   └── interview-prep-agent.service   # systemd unit (alternative to Docker)
├── Dockerfile
├── docker-compose.yml
└── .github/workflows/
    ├── ci.yml                         # PR checks: frontend + backend tests
    └── deploy.yml                     # master push: build frontend → bootJar → deploy
```
