# CONVENTIONS.md

Living document — actively maintained by the reviewer and memory agents.
Last reviewed: 2026-05-14

---

## Code Style

### Naming
- Variables and methods: camelCase (e.g., `sessionId`, `getInterviewSession()`)
- Classes and interfaces: PascalCase (e.g., `InterviewSession`, `SessionRepository`)
- Constants: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_COUNT`)
- Packages: all lowercase, dot-separated (e.g., `com.vipinsharma.interviewprep.service`)
- Spring beans: camelCase, no `Impl` suffix unless interface and implementation coexist in the same package
- Test methods: `methodName_scenario_expectedBehavior()` (e.g., `getSession_whenNotFound_throwsException()`)

### Formatting
- Indentation: 4 spaces (no tabs)
- Max line length: 100 characters
- Blank lines between top-level definitions: 2
- Formatter: Google Java Format (enforced via `spotless` Gradle plugin) or standard IntelliJ Java formatting — both produce equivalent output at these settings
- Gradle DSL: Groovy (not Kotlin); build files use `build.gradle`, not `build.gradle.kts`

### Imports
- Import ordering (IntelliJ / Google Java Format standard):
  1. `static` imports (alphabetical)
  2. `java.*`
  3. `jakarta.*` (Jakarta EE / Spring Framework 6+ replacements for javax.*)
  4. `org.*`
  5. `com.*` (third-party, then project-local)
- No wildcard imports (e.g., never `import java.util.*`)
- Remove all unused imports before committing

---

## Architecture

### Folder Structure
- Source layout under `src/main/java/com/vipinsharma/interviewprep/`:
  - `api/`        — REST controllers (`@RestController`)
  - `service/`    — business logic (`@Service`)
  - `agent/`      — Spring AI agents and their `tools/` sub-package
  - `model/`      — JPA entities (`@Entity`)
  - `dto/`        — Java records used as request/response DTOs
  - `config/`     — Spring configuration classes (`@Configuration`)
  - `repository/` — Spring Data JPA repositories (`@Repository`)
- Resources: `src/main/resources/` (application.yml, SQL migrations, etc.)
- Tests: `src/test/java/` — mirrors the source package tree exactly
- Deployment: `deploy/` — systemd unit files and Caddy configuration
- CI/CD: `.github/workflows/`

### Patterns
- Prefer composition over inheritance
- One responsibility per module/file
- Interfaces before implementations
- **Repository pattern:** all database access goes through Spring Data JPA repositories; no raw SQL in service classes
- **Service layer orchestration:** `@Service` classes orchestrate calls to repositories and Spring AI agents; they own transaction boundaries (`@Transactional` at the service method level, never on controllers)
- **Agent isolation:** Spring AI agents in `agent/` never call each other directly — data flows through the service layer
- **DTOs are Java records:** request and response objects are immutable `record` types in `dto/`; JPA entities are never exposed directly in API responses
- **Constructor injection only:** use constructor injection for all Spring beans (enables `final` fields and simplifies testing); never use `@Autowired` field injection
- **API response envelope:** every REST response is wrapped in a generic `ApiResponse<T>` record: `{ "data": T, "error": null, "meta": { "timestamp": "..." } }`

### What to Avoid
- God objects / monolithic files
- Tight coupling between layers
- Global mutable state
- `@Autowired` field injection — use constructor injection instead
- `@Transactional` on `@RestController` classes — transaction boundaries belong in the service layer
- `Optional.get()` without a prior `isPresent()` check — use `orElseThrow()` or `orElse()` instead
- Exposing JPA entities directly as API responses — always map to a DTO record first
- Agents calling other agents directly — route through service classes to keep agents decoupled

---

## Testing

### Unit Tests
- Every public function has a unit test
- Tests are co-located in `tests/` mirroring source structure
- Test naming: `methodName_scenario_expectedBehavior()` (e.g., `getSession_whenNotFound_throwsException()`)
- One assertion per test (prefer this over multi-assertion tests)
- TDD: write failing test first, then implement

### Integration Tests
- Cover cross-layer flows (API → service → database)
- Use real database/external services in integration tests — no mocks
- Annotate with `@SpringBootTest` and `@Tag("integration")`
- Use H2 in-memory database for local integration tests; PostgreSQL dialect used in CI

### Coverage
- Unit test coverage target: 80% minimum
- No coverage exceptions without a comment explaining why
- 80% minimum line coverage, enforced by the Gradle JaCoCo plugin (`jacocoTestCoverageVerification` task fails the build if coverage drops below threshold)
- Coverage report generated on every `./gradlew test` run; CI blocks merge if the task fails

### What Not to Test
- Framework internals
- Third-party library behavior
- Trivial getters/setters with no logic

---

## Git

### Branch Naming
- Features: `feat/<short-description>`
- Bug fixes: `fix/<short-description>`
- Chores: `chore/<short-description>`
- Docs: `docs/<short-description>`

### Commit Style
- Format: `<type>: <short description>` (50 chars max for subject)
- Types: feat, fix, chore, docs, test, refactor, perf
- Body: wrap at 72 chars, explain WHY not WHAT
- No "WIP" commits on main — squash before merge

### PR Size
- Aim for < 400 lines changed per PR
- One logical change per PR
- Link to TASK-ID in PR description

### What Not to Do
- Never force push to main
- Never commit secrets, tokens, or credentials
- Never skip pre-commit hooks (`--no-verify`)

---

## API / Interface Design

### Request / Response Structure
- All API responses use the generic `ApiResponse<T>` Java record:
  ```json
  { "data": <payload or null>, "error": null, "meta": { "timestamp": "2026-05-14T10:00:00Z" } }
  ```
- Error responses set `data` to `null` and populate `error`:
  ```json
  { "data": null, "error": { "code": "SESSION_NOT_FOUND", "message": "Session 42 does not exist" }, "meta": { "timestamp": "..." } }
  ```
- HTTP status codes follow REST semantics (200, 201, 400, 404, 422, 500); the envelope error field is supplementary

### Error Codes
- Error codes are defined in the `ErrorCode` enum (UPPER_SNAKE_CASE, not snake_case):
  - `COMPANY_NOT_FOUND`
  - `SESSION_NOT_FOUND`
  - `SESSION_ALREADY_COMPLETED`
  - `RESEARCH_FAILED`
  - `INVALID_REQUEST`
- Every error response includes both the `ErrorCode` value and a human-readable `message` string
- New codes must be added to the `ErrorCode` enum — do not use raw strings for error codes

### Versioning
- URL path versioning: all endpoints are prefixed with `/api/v1/` (e.g., `/api/v1/sessions`, `/api/v1/companies`)
- A version bump to `/api/v2/` is required for any breaking change to request/response contracts

---

## Agent Rules

### Must Do
- Memory agent runs after every completed pipeline — no exceptions
- Security agent is a hard gate — pipeline stops on BLOCKERS
- Coder agent writes unit tests before implementation (TDD)
- Reviewer agent must output `PASS` or `FIX_REQUIRED` — nothing else

### Must Not Do
- Sub-agents do not inherit full conversation history
- Sub-agents do not load all skill files — only the one relevant to their task
- Orchestrator does not write code directly
- No agent skips the memory step after a significant decision

### Convention Promotion
When reviewer or memory agent identifies a repeated pattern, flag it to the orchestrator as:
```
CONVENTION_CANDIDATE: [section] description of the convention
```
Orchestrator reviews and adds to this file if appropriate.

---

## Documentation

### What to Document
- Public APIs and their contracts
- Non-obvious design decisions (the WHY, not the WHAT)
- Setup and onboarding steps
- External service integrations

### What Not to Document
- Code that reads clearly on its own
- Trivial implementation details
- Temporary workarounds (fix them instead)

### Style
- Short sentences
- Active voice
- Examples over descriptions
- API documentation: Springdoc OpenAPI (dependency `springdoc-openapi-starter-webmvc-ui`)
- Swagger UI available at `/swagger-ui.html` in all non-production profiles
- Annotate controllers with `@Operation` and `@ApiResponse` from `io.swagger.v3.oas.annotations` for accurate spec generation
