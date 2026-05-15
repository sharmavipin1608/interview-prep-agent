# Facts

_Format: [domain] fact — YYYY-MM-DD_
_Mark outdated entries with [stale] prefix — never delete._

[conventions] Java camelCase for vars/methods, PascalCase for classes, UPPER_SNAKE_CASE for constants — 2026-05-14
[conventions] API versioning is URL path versioning at /api/v1/ — 2026-05-14
[conventions] Google Java Format via spotless Gradle plugin, 100-char line limit — 2026-05-14
[api] All responses wrapped in ApiResponse<T> record: { data, error, meta: { timestamp } } — 2026-05-14
[api] ErrorCode enum: COMPANY_NOT_FOUND, SESSION_NOT_FOUND, SESSION_ALREADY_COMPLETED, RESEARCH_FAILED, INVALID_REQUEST — 2026-05-14
[testing] JaCoCo 80% minimum coverage, enforced via jacocoTestCoverageVerification in CI — 2026-05-14
[testing] Integration tests use @SpringBootTest + @Tag("integration") — 2026-05-14
[testing] Test naming convention: methodName_scenario_expectedBehavior() — 2026-05-14
[architecture] Constructor injection only, never @Autowired field injection — 2026-05-14
[architecture] @Transactional at service layer only, never on controllers — 2026-05-14
[docs] Springdoc OpenAPI, swagger-ui at /swagger-ui.html — 2026-05-14
[build] Spring AI 1.0.0 GA artifact is spring-ai-starter-model-openai (renamed from spring-ai-openai-spring-boot-starter) — 2026-05-14
[build] Spring AI 1.0.0 resolves from Maven Central only — no milestone or snapshot repos needed — 2026-05-14
[build] BOM managed via dependencyManagement block; do not add redundant implementation platform() alongside it — 2026-05-14
[config] spring.profiles.active must not be hardcoded in application.yml — drive via SPRING_PROFILES_ACTIVE env var — 2026-05-14
[config] spring.jpa.open-in-view must be explicitly set to false in all profiles — 2026-05-14
[testing] application-test.yml + @ActiveProfiles("test") makes integration tests self-contained — 2026-05-14
[conventions] jakarta.* imports must follow java.* imports (Google Java Format ordering) — 2026-05-14
[testing] @Autowired field injection permitted in @DataJpaTest/@WebMvcTest/@SpringBootTest test classes; constructor injection rule applies to production Spring beans only — 2026-05-14
[database] All entities use UUID primary keys generated via UUID.randomUUID() in @PrePersist — 2026-05-14
[database] companyBrief and feedback stored as opaque TEXT at entity layer — no JSON deserialisation — 2026-05-14
[conventions] Use Spring Data derived queries (e.g. findTop5ByOrderByFrequencyDesc) over JPQL LIMIT for portability — 2026-05-14
[dto] All 7 API DTOs are immutable Java records in com.vipinsharma.interviewprep.dto — 2026-05-14
[testing] Pure Java records (no logic) do not require unit tests — compiler guarantees behaviour — 2026-05-14
[build] RestClient.Builder must be injected via constructor — never use RestClient.create() directly — 2026-05-14
[testing] RETURNS_DEEP_STUBS does not work with Spring 6 RestClient; use explicit mocks per interface type — 2026-05-14
[security] Tool methods must validate input: null/blank/length checks before processing user-supplied strings — 2026-05-14
[agent] Spring AI @Tool methods require a descriptive description attribute on the annotation — 2026-05-14
[build] Spring AI 1.0.0: InMemoryChatMemory does not exist; use MessageWindowChatMemory.builder().chatMemoryRepository(new InMemoryChatMemoryRepository()).build() — 2026-05-14
[agent] ChatClient.Builder bean takes ChatModel parameter injected by Spring; never construct ChatModel manually — 2026-05-14
[agent] ResearchAgent validates all 3 inputs (companyName, jobTitle, jobDescription) via StringUtils.hasText() before LLM call — 2026-05-14
[agent] ResearchAgent uses private ParsedBrief record for Jackson deserialization; Jackson 2.17.2 handles Java records without @JsonProperty or ParameterNamesModule — 2026-05-14
[security] Prompt injection warning: ResearchAgent embeds user inputs verbatim in buildUserPrompt() — tracked as hardening backlog, non-blocking — 2026-05-14
[agent] InterviewerAgent uses MessageChatMemoryAdvisor.builder(chatMemory).conversationId(sessionId).build() per call for session-scoped memory — 2026-05-14
[agent] InterviewerAgent.chat() takes (sessionId, companyBrief, jobDescription, weakAreas, userMessage) — weakAreas may be null — 2026-05-14
[security] InMemoryChatMemoryRepository is unbounded; no TTL — memory exhaustion risk for long-running deployments; hardening backlog — 2026-05-14
[agent] CoachAgent.evaluate() takes (UUID sessionId, String transcript, List<String> weakAreaHistory); UUID parameter prevents injection — 2026-05-14
[agent] CoachAgent fallback on JSON parse failure: overallScore=0, empty lists, detailedFeedback="Evaluation unavailable" — 2026-05-14
[service] ResearchService.research() has no @Transactional — avoids holding DB connection during LLM call; Spring Data JPA save() is transactional by default — 2026-05-14
[service] ResearchService serializes CompanyBrief to JSON via ObjectMapper.writeValueAsString(); falls back to "{}" on JsonProcessingException — 2026-05-14
[service] InterviewSessionService.startSession() sets session status to "INTERVIEWING" unconditionally — no terminal-state guard yet (hardening backlog) — 2026-05-14
[service] InterviewSessionService.getWeakAreaTopics() returns global top-5 weak areas (no sessionId scoping — cross-session by design) — 2026-05-14
[service] ChatService.chat() persists two Message entities per turn: role="USER" (userMessage) and role="ASSISTANT" (agent reply) — 2026-05-14
[service] ChatService deserializes CompanyBrief from session.getCompanyBrief() JSON; falls back to companyName-only brief on parse failure — 2026-05-14
[service] ScoringService clamps overallScore to 1-5 range via Math.max(1, Math.min(5, ...)) before persisting — 2026-05-14
[service] ScoringService builds transcript as "ROLE: content\n" per message via Collectors.joining() — 2026-05-14
[service] SessionHistoryService.getSessions() uses N+1 pattern (findAll + per-session score query) — acceptable for current scale, track for optimization — 2026-05-14
[service] SessionHistoryService has no ownership/multi-tenancy filter — single-user design; revisit if multi-user support added — 2026-05-14
[api] ResearchService.research() returns ResearchResponse{sessionId, companyBrief} — not CompanyBrief — 2026-05-14
[controller] ResearchController POST /api/v1/research wraps result in ResponseEntity<ApiResponse<ResearchResponse>> — 2026-05-14
[controller] SessionController POST /api/v1/sessions returns @ResponseStatus(CREATED) ApiResponse<UUID> — 2026-05-14
[controller] GlobalExceptionHandler maps IllegalArgumentException → 400 ApiResponse with ErrorCode.INVALID_REQUEST — 2026-05-14
[controller] getSession uses getSessions().stream().filter() — no dedicated findById service method — 2026-05-14
[cicd] GitHub Actions deploy.yml triggers on push to master; uses ORACLE_SSH_KNOWN_HOSTS secret (not StrictHostKeyChecking=no) — 2026-05-14
[cicd] SSH key written via printf not echo to avoid log exposure; cleaned up with if: always() — 2026-05-14
[deploy] systemd unit EnvironmentFile=/opt/interview-prep-agent/.env — OPENAI_API_KEY, TAVILY_API_KEY, DB creds, SPRING_PROFILES_ACTIVE=prod must be in that file on VM — 2026-05-14
[deploy] Caddyfile domain placeholder is interview-prep.example.com — must be replaced before first deployment — 2026-05-14
[build] Full clean build produces 56MB fat JAR: build/libs/interview-prep-agent-0.0.1-SNAPSHOT.jar — 2026-05-14
[agent] Groq Llama models wrap JSON responses in ```json ... ``` markdown fences despite "return ONLY JSON" instructions — always strip fences before parsing with objectMapper — 2026-05-15
[agent] ChatClient.Builder is mutable in Spring AI 1.0.0 — defaultSystem() mutates in place; declaring it as a singleton @Bean causes all agents to share the last-initialized system prompt — must use @Scope("prototype") — 2026-05-15
[config] ddl-auto: validate fails on a fresh PostgreSQL database with no schema — use ddl-auto: update for first run; migrate to Flyway/Liquibase if schema stability becomes important — 2026-05-15
[build] springdoc-openapi-starter-webmvc-ui:2.6.0 must be added explicitly to build.gradle — it is not transitive from any other dependency — Swagger UI at /swagger-ui/index.html — 2026-05-15
[deploy] Docker image tarball transferred via scp gets corrupted — stream directly via docker save | ssh ... docker load instead — 2026-05-15

