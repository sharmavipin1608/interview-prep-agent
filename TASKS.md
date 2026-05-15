# Task Queue

Tasks are processed top-to-bottom. Each task goes through the full agent pipeline.

## Format

```
### [TASK-ID] Task Title
**Status:** pending | in_progress | done | blocked | failed
**Priority:** high | medium | low
**Agent:** researcher | coder | tester | etc.
**Tags:** [domain] tags matching facts.md
**Depends on:** TASK-ID (if any)

Task description — what needs to be done and why.
```

## Tasks

### [TASK-001] Review and fill in CONVENTIONS.md
**Status:** done
**Priority:** high
**Agent:** writer
**Tags:** [conventions]

Review the generated CONVENTIONS.md and fill in all `# TODO` markers with project-specific decisions. This file is a living document — the reviewer and memory agents will actively maintain it throughout the project lifecycle.

### [TASK-002] Populate memory/core.md
**Status:** done
**Priority:** high
**Agent:** writer
**Tags:** [core]

Verify that bootstrap.sh correctly populated memory/core.md with project identity. Add any missing details: key team members, external dependencies, architectural constraints, third-party services.

### [IMPL-001] Gradle project setup and application bootstrap
**Status:** done
**Priority:** high
**Agent:** coder
**Tags:** [build] [config]

Set up Gradle 8.10 project with Spring Boot 3.3.4, Spring AI 1.0.0, Java 21 toolchain. Configure application.yml profiles (dev, test, prod stubs). Write smoke tests confirming context loads, web server binds, and random port assignment.

### [IMPL-002] JPA entities and repositories
**Status:** done
**Priority:** high
**Agent:** coder
**Tags:** [database] [jpa]
**Depends on:** IMPL-001

Session, Message, Score, WeakArea JPA entities + Spring Data JPA repositories. SessionRepositoryTest.

### [IMPL-003] DTOs as Java records
**Status:** done
**Priority:** high
**Agent:** coder
**Tags:** [dto] [api]
**Depends on:** IMPL-002

ResearchRequest, CompanyBrief, StartSessionRequest, ChatRequest, ChatResponse, SessionFeedback, SessionSummary — all Java records.

### [IMPL-004] TavilySearchTool
**Status:** done
**Priority:** high
**Agent:** coder
**Tags:** [agent] [search]
**Depends on:** IMPL-003

Spring AI @Tool-annotated component that calls the Tavily Search API.

### [IMPL-005] AI configuration (AiConfig)
**Status:** done
**Priority:** high
**Agent:** coder
**Tags:** [agent] [config]
**Depends on:** IMPL-004

ChatClient beans and MessageWindowChatMemory configuration for InterviewerAgent.

### [IMPL-006] ResearchAgent
**Status:** done
**Priority:** high
**Agent:** coder
**Tags:** [agent]
**Depends on:** IMPL-005

Autonomous multi-step researcher using TavilySearchTool. Produces CompanyBrief. ResearchAgentTest.

### [IMPL-007] InterviewerAgent
**Status:** done
**Priority:** high
**Agent:** coder
**Tags:** [agent]
**Depends on:** IMPL-006

Stateful multi-turn interviewer with MessageWindowChatMemory. Loaded with CompanyBrief + job description + weak areas. InterviewerAgentTest.

### [IMPL-008] CoachAgent
**Status:** done
**Priority:** high
**Agent:** coder
**Tags:** [agent]
**Depends on:** IMPL-007

Stateless evaluator. Receives full transcript + weak area history. Returns SessionFeedback. CoachAgentTest.

### [IMPL-009] ResearchService
**Status:** done
**Priority:** high
**Agent:** coder
**Tags:** [service]
**Depends on:** IMPL-008

Calls ResearchAgent, persists CompanyBrief to DB. ResearchServiceTest.

### [IMPL-010] InterviewSessionService
**Status:** done
**Priority:** high
**Agent:** coder
**Tags:** [service]
**Depends on:** IMPL-009

Creates sessions, loads CompanyBrief, initialises InterviewerAgent context with weak areas. Test included.

### [IMPL-011] ChatService
**Status:** done
**Priority:** high
**Agent:** coder
**Tags:** [service]
**Depends on:** IMPL-010

Per-turn conversation handling, persists messages, calls InterviewerAgent. ChatServiceTest.

### [IMPL-012] ScoringService
**Status:** done
**Priority:** high
**Agent:** coder
**Tags:** [service]
**Depends on:** IMPL-011

Loads transcript + weak area history, calls CoachAgent, persists scores. ScoringServiceTest.

### [IMPL-013] SessionHistoryService
**Status:** done
**Priority:** high
**Agent:** coder
**Tags:** [service]
**Depends on:** IMPL-012

Reads past sessions, aggregates weak areas for cross-session memory. Test included.

### [IMPL-014] REST controllers
**Status:** done
**Priority:** high
**Agent:** coder
**Tags:** [api]
**Depends on:** IMPL-013

ResearchController and SessionController wiring all 6 endpoints. Controller tests.

### [IMPL-015] GitHub Actions CI/CD
**Status:** done
**Priority:** high
**Agent:** coder
**Tags:** [infra] [cicd]
**Depends on:** IMPL-014

.github/workflows/deploy.yml — test, bootJar, scp to Oracle VM, systemctl restart.

### [IMPL-016] Deployment configuration
**Status:** done
**Priority:** high
**Agent:** coder
**Tags:** [infra] [deploy]
**Depends on:** IMPL-015

deploy/interview-prep-agent.service (systemd unit) and deploy/Caddyfile (reverse proxy).

### [IMPL-017] Full test run and final verification
**Status:** done
**Priority:** high
**Agent:** tester
**Tags:** [testing]
**Depends on:** IMPL-016

./gradlew test — all tests pass. Coverage check. Local smoke run of the app.
