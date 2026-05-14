# Project Core Memory

**Project:** interview-prep-agent
**Stack:** Java 21, Spring Boot 3, Spring AI, Oracle Cloud Free Tier
**Description:** AI agent that researches companies and simulates mock interviews. Given a company name + job description, it researches the company, conducts a multi-turn mock interview, and coaches with scored feedback.
**Owner:** sharma.vipin1608@gmail.com
**Created:** 2026-05-14

## Architecture Overview

```
REST API (Spring Boot 3) at /api/v1/
  ↓
Service Layer (orchestrates agents + persistence)
  ↓
Agent Layer (Spring AI ChatClient — ResearchAgent, InterviewerAgent, CoachAgent)
  ↓
OpenAI API (gpt-4o-mini, swappable via Spring AI provider abstraction)
  ↓
Persistence (H2 in dev → PostgreSQL on Oracle Free Tier in prod)
```

**Three agents — services pass data between them; agents never call each other:**
- **ResearchAgent** — autonomous, uses TavilySearchTool to research a company and return a CompanyBrief
- **InterviewerAgent** — stateful multi-turn conversation, uses MessageWindowChatMemory to maintain interview context
- **CoachAgent** — stateless evaluator, scores answers and identifies weak areas

**Package root:** `com.vipinsharma.interviewprep`
**Build:** Gradle 8.10, Groovy DSL
**Spring Boot:** 3.3.4
**Spring AI:** 1.0.0
**Java:** 21

## REST API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/research` | Research a company, returns CompanyBrief |
| POST | `/api/v1/sessions` | Start a mock interview session |
| POST | `/api/v1/sessions/{id}/chat` | Send a message, get interviewer reply |
| POST | `/api/v1/sessions/{id}/evaluate` | Trigger coaching feedback |
| GET  | `/api/v1/sessions/{id}` | Fetch session detail |
| GET  | `/api/v1/sessions` | List all sessions |

## DB Schema (4 tables)

- `sessions` — interview session metadata
- `messages` — per-session conversation turns
- `scores` — coaching scores per answer
- `weak_areas` — identified weak areas from coaching

## Key External Dependencies

| Dependency | Purpose | Notes |
|---|---|---|
| OpenAI API (gpt-4o-mini) | Primary LLM for all three agents | Swappable via Spring AI provider abstraction |
| Tavily Search API | Company research tool used by ResearchAgent | Autonomous web search |
| H2 | In-memory DB for local dev | Auto-configured by Spring Boot |
| PostgreSQL | Persistent DB for production | Hosted on Oracle Cloud Free Tier |
| Oracle Cloud Free Tier (Ampere A1 VM) | Production deployment target | ARM64 architecture |
| GitHub Actions | CI/CD pipeline | Build, test, deploy |
