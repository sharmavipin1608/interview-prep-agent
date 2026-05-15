# interview-prep-agent

AI agent that researches companies and simulates mock technical interviews. Given a company name and job description, it researches the company, conducts a multi-turn mock interview, and delivers scored coaching feedback.

**Stack:** Java 21, Spring Boot 3.3.4, Spring AI 1.0.0, PostgreSQL, Oracle Cloud Free Tier

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

All responses are wrapped in `ApiResponse<T>`: `{ data, error: { code, message }, meta: { timestamp } }`.

Swagger UI is available at `/swagger-ui.html` when the app is running.

---

## Local Development

### Prerequisites
- Java 21
- Gradle 8.10 (or use the included `./gradlew` wrapper)

### Environment variables
```bash
export OPENAI_API_KEY=sk-...
export TAVILY_API_KEY=tvly-...
```

### Run
```bash
./gradlew bootRun
# App starts on http://localhost:8080 with H2 in-memory DB
```

### Test
```bash
./gradlew test
# 110 tests, all pass
```

---

## Production Deployment

The app is designed to run on an **Oracle Cloud Free Tier Ampere A1 VM (ARM64, Ubuntu 22.04)** behind a **Caddy v2** reverse proxy.

### 1. GitHub Actions secrets

Add these secrets to your GitHub repo (`Settings → Secrets → Actions`):

| Secret | Description |
|--------|-------------|
| `ORACLE_SSH_PRIVATE_KEY` | SSH private key for the Oracle VM |
| `ORACLE_SSH_KNOWN_HOSTS` | Output of `ssh-keyscan <your-oracle-vm-ip>` |
| `ORACLE_HOST` | Public IP or hostname of the Oracle VM |
| `ORACLE_USER` | SSH username (typically `ubuntu`) |
| `ORACLE_DEPLOY_PATH` | Remote path for the JAR, e.g. `/opt/interview-prep-agent/` |

### 2. Oracle VM setup

Install dependencies:
```bash
sudo apt update
sudo apt install -y openjdk-21-jre-headless postgresql caddy
```

Create the app directory and environment file:
```bash
sudo mkdir -p /opt/interview-prep-agent
sudo nano /opt/interview-prep-agent/.env
```

Contents of `.env`:
```
SPRING_PROFILES_ACTIVE=prod
OPENAI_API_KEY=sk-...
TAVILY_API_KEY=tvly-...
DB_HOST=localhost
DB_USER=interviewprep
DB_PASSWORD=<strong-password>
```

Set up PostgreSQL:
```bash
sudo -u postgres psql -c "CREATE USER interviewprep WITH PASSWORD '<strong-password>';"
sudo -u postgres psql -c "CREATE DATABASE interviewprepdb OWNER interviewprep;"
```

### 3. Register the systemd service

```bash
sudo cp deploy/interview-prep-agent.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable interview-prep-agent
sudo systemctl start interview-prep-agent
```

### 4. Configure Caddy

Edit `deploy/Caddyfile` — replace `interview-prep.example.com` with your actual domain:
```
your-domain.com {
    reverse_proxy localhost:8080
    ...
}
```

Then copy it to the server:
```bash
sudo cp deploy/Caddyfile /etc/caddy/Caddyfile
sudo systemctl reload caddy
```

Caddy automatically provisions a TLS certificate via Let's Encrypt.

### 5. Deploy

Push to `master` — GitHub Actions runs tests, builds the JAR, scps it to the VM, and restarts the service automatically.

---

## Project Structure

```
interview-prep-agent/
├── src/
│   ├── main/java/com/vipinsharma/interviewprep/
│   │   ├── agent/          # ResearchAgent, InterviewerAgent, CoachAgent
│   │   ├── api/            # REST controllers + GlobalExceptionHandler
│   │   ├── config/         # AiConfig (ChatClient beans)
│   │   ├── dto/            # Java records (request/response DTOs)
│   │   ├── model/          # JPA entities (Session, Message, Score, WeakArea)
│   │   ├── repository/     # Spring Data JPA repositories
│   │   └── service/        # Business logic services
│   └── main/resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-test.yml
│       └── application-prod.yml
├── deploy/
│   ├── interview-prep-agent.service   # systemd unit
│   └── Caddyfile                      # Caddy v2 reverse proxy
└── .github/workflows/
    └── deploy.yml                     # CI/CD pipeline
```
