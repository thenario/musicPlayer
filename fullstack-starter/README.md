# Fullstack Starter

Vue 3 + TypeScript + Spring Boot 3 的中性全栈脚手架。业务代码应分别放在 `web/src/features/` 与 `backend/src/main/java/com/example/starter/features/`。

## Run locally

```bash
cd backend && ./mvnw spring-boot:run
cd web && pnpm install && pnpm dev
```

Copy `.env.example` to `.env` before `docker compose up --build`.

## Included conventions

- `/api/**` API namespace and `{ code, message, data }` response envelope
- Validation and centralized error responses
- Flyway migrations; never edit an applied migration
- Environment-only secrets and Docker Compose checks for required values
- Vue API boundary in `src/api/http.ts`; feature code must not instantiate Axios directly
