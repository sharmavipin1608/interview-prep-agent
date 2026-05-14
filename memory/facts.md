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

