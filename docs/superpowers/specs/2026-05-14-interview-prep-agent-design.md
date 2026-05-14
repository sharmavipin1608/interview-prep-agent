# Interview Prep Agent — Design Spec

**Date:** 2026-05-14  
**Project:** interview-prep-agent  
**Stack:** Java 21, Spring Boot 3, Spring AI, Gradle, PostgreSQL, Oracle Cloud Free Tier  
**Status:** Approved

---

## 1. Goal

Build a REST API-backed AI agent that helps a job seeker prepare for interviews. Given a company name and job description, it researches the company, conducts a multi-turn mock interview, and coaches the user with scored feedback. Deployed to Oracle Cloud Free Tier with GitHub Actions CI/CD.

**Learning objectives:**
- Agent design with Spring AI (tools, memory, multi-step reasoning)
- Cloud deployment on Oracle Free Tier
- GitHub Actions CI/CD pipeline
- Foundation for later migration to LangGraph (Python) for more complex agentic systems

---

## 2. Architecture Overview

```
REST API (Spring Boot 3)
        ↓
Service Layer (orchestrates agents and persistence)
        ↓
Agent Layer (Spring AI ChatClient — ResearchAgent, InterviewerAgent, CoachAgent)
        ↓
OpenAI API (gpt-4o-mini, swappable via Spring AI provider abstraction)
        ↓
Persistence (H2 in dev → PostgreSQL on Oracle Free Tier)
```

Services orchestrate agents — agents do not call each other directly. The "handover" between agents is data (CompanyBrief, transcript) passed through services.

---

## 3. Agents

### 3.1 ResearchAgent

**Role:** Autonomous multi-step researcher.

Given a company name and job title, it decides what to search for, calls `TavilySearchTool` multiple times (e.g. company tech stack, engineering culture, recent news, interview style), evaluates results, and synthesizes a `CompanyBrief`.

- **Tools:** `TavilySearchTool`
- **Memory:** None (stateless per research call)
- **Output:** `CompanyBrief` — structured JSON with: company summary, tech stack, culture signals, recent news, likely interview topics
- **Why agentic:** The agent decides how many searches to run and when it has enough — the caller does not prescribe the steps

### 3.2 InterviewerAgent

**Role:** Stateful multi-turn mock interviewer.

Loaded with `CompanyBrief` + job description + historical weak areas as system prompt context. Plays the role of a senior interviewer at the target company. Adapts questions based on candidate answers and known weak areas from past sessions.

- **Tools:** None (conversational only)
- **Memory:** `MessageWindowChatMemory` — keeps last 20 messages in context (configurable); all messages persisted to DB
- **Input per turn:** User's answer
- **Output per turn:** Next interviewer question or follow-up
- **Why agentic:** Decides question sequencing, follow-up depth, and pivot points based on conversation state

### 3.3 CoachAgent

**Role:** Stateless analytical evaluator.

Receives full session transcript + aggregated weak areas from past sessions. Returns structured feedback: per-answer scores, patterns across sessions, and specific improvement suggestions.

- **Tools:** None (analytical only)
- **Memory:** None (stateless; receives all context in the prompt)
- **Input:** Full transcript + weak area history
- **Output:** `SessionFeedback` — JSON with overall score, per-answer scores (1–5), top 3 weak areas, improvement suggestions
- **Why agentic:** Synthesizes and reasons across the full conversation, not just the last message

---

## 4. Services

| Service | Responsibility |
|---|---|
| `ResearchService` | Calls `ResearchAgent`, stores `CompanyBrief` to DB |
| `InterviewSessionService` | Creates sessions, loads `CompanyBrief`, initialises `InterviewerAgent` context |
| `ChatService` | Handles per-turn conversation, persists messages, calls `InterviewerAgent` |
| `ScoringService` | Loads transcript + weak area history, calls `CoachAgent`, persists scores |
| `SessionHistoryService` | Reads past sessions, aggregates weak areas for cross-session memory |

---

## 5. REST API

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/research` | Research a company. Body: `{ companyName, jobTitle, jobDescription }`. Returns `CompanyBrief`. |
| `POST` | `/api/sessions` | Start a mock interview session. Body: `{ companyBriefId, jobDescription }`. Returns `{ sessionId }`. |
| `POST` | `/api/sessions/{id}/chat` | Send a message in an active session. Body: `{ message }`. Returns `{ reply }`. |
| `POST` | `/api/sessions/{id}/evaluate` | Trigger end-of-session coaching. Returns `SessionFeedback`. |
| `GET` | `/api/sessions/{id}` | Fetch session detail: transcript + scores. |
| `GET` | `/api/sessions` | List all past sessions with summary scores and weak areas. |

---

## 6. Persistence

**Two environments, one schema:**
- **Dev:** H2 in-memory — no setup, configured in `application-dev.yml`
- **Prod:** PostgreSQL on Oracle Free Tier — configured in `application-prod.yml`

### Schema

```sql
sessions (
  id            UUID PRIMARY KEY,
  company_name  VARCHAR(255),
  job_title     VARCHAR(255),
  company_brief TEXT,          -- JSON blob from ResearchAgent
  status        VARCHAR(20),   -- ACTIVE | COMPLETED
  created_at    TIMESTAMP
)

messages (
  id            UUID PRIMARY KEY,
  session_id    UUID REFERENCES sessions(id),
  role          VARCHAR(20),   -- USER | ASSISTANT
  content       TEXT,
  created_at    TIMESTAMP
)

scores (
  id            UUID PRIMARY KEY,
  session_id    UUID REFERENCES sessions(id),
  overall_score INT,
  feedback      TEXT,          -- JSON blob from CoachAgent
  created_at    TIMESTAMP
)

weak_areas (
  id            UUID PRIMARY KEY,
  topic         VARCHAR(255),  -- e.g. "system design", "behavioral"
  frequency     INT,           -- sessions flagged this topic
  last_seen     TIMESTAMP
)
```

`weak_areas` is read at session start by `InterviewSessionService` and injected into `InterviewerAgent`'s system prompt to bias questions toward known gaps.

---

## 7. Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3 |
| AI framework | Spring AI |
| LLM (dev/test) | OpenAI `gpt-4o-mini` (free tier) |
| LLM (future) | Swappable via Spring AI — Claude, Gemini, or Ollama (local) |
| Search tool | Tavily Search API (free tier) |
| Build tool | Gradle |
| DB (dev) | H2 in-memory |
| DB (prod) | PostgreSQL |
| Cloud | Oracle Cloud Free Tier (Ampere A1 VM — 4 OCPUs, 24 GB RAM) |
| Reverse proxy | Caddy (automatic TLS via Let's Encrypt) |
| Process manager | systemd |
| CI/CD | GitHub Actions |

---

## 8. Deployment

### Oracle VM Setup (one-time)
- Provision Ampere A1 VM on Oracle Free Tier
- Install Java 21, PostgreSQL, Caddy
- Create systemd service for the Spring Boot JAR
- Configure Caddy to reverse proxy port 80/443 → 8080
- Add SSH public key to `~/.ssh/authorized_keys`

### GitHub Actions CI/CD

Triggered on merge to `main`:

```
1. ./gradlew test
2. ./gradlew bootJar
3. scp build/libs/interview-prep-agent.jar → oracle-vm:~/app/
4. ssh oracle-vm "sudo systemctl restart interview-prep-agent"
```

**GitHub Secrets required:**
- `ORACLE_HOST` — VM public IP
- `ORACLE_SSH_USER` — e.g. `opc`
- `ORACLE_SSH_KEY` — private SSH key (PEM format)

### Spring Profiles
- `dev` — H2, console logging, debug level
- `prod` — PostgreSQL, structured logging, info level

---

## 9. Package Structure

```
com.yourname.interviewprep/
  api/
    ResearchController.java
    SessionController.java
  service/
    ResearchService.java
    InterviewSessionService.java
    ChatService.java
    ScoringService.java
    SessionHistoryService.java
  agent/
    ResearchAgent.java
    InterviewerAgent.java
    CoachAgent.java
    tools/
      TavilySearchTool.java
  model/
    Session.java
    Message.java
    Score.java
    WeakArea.java
  dto/                     -- all DTOs are Java records
    ResearchRequest.java
    CompanyBrief.java
    StartSessionRequest.java
    ChatRequest.java
    ChatResponse.java
    SessionFeedback.java
  config/
    AiConfig.java        -- ChatClient beans, memory config
    DatabaseConfig.java
```

---

## 10. Roadmap (Out of Scope for v1)

| Item | Notes |
|---|---|
| Web chat UI | Thymeleaf or React, same Spring Boot app |
| Telegram bot | Spring Boot + Telegram Bot API, calls same services |
| Swagger UI | Springdoc OpenAPI, live demo for interviews |
| Ollama support | Local model, zero API cost for heavy testing |
| Autonomous research scheduler | GitHub Actions cron — researches applied companies nightly |
| LangGraph rewrite | Python, graph-based multi-agent orchestration (next project) |
