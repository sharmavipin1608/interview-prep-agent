# Scratchpad

## Status
All IMPL tasks (001-017) complete. 110 tests passing. Project is feature-complete.

## Remaining before first production deploy
1. Replace Caddyfile domain placeholder (interview-prep.example.com)
2. Set up GitHub Actions secrets: ORACLE_SSH_PRIVATE_KEY, ORACLE_SSH_KNOWN_HOSTS, ORACLE_HOST, ORACLE_USER, ORACLE_DEPLOY_PATH
3. Create /opt/interview-prep-agent/.env on Oracle VM with: SPRING_PROFILES_ACTIVE=prod, OPENAI_API_KEY, TAVILY_API_KEY, DB_HOST, DB_USER, DB_PASSWORD
4. Install Java 21, Caddy, PostgreSQL on Oracle VM
5. Copy deploy/interview-prep-agent.service to /etc/systemd/system/ and enable it
