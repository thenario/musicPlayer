# Fullstack Starter

Vue 3 + TypeScript + Spring Boot 3 的中性全栈脚手架。业务代码应分别放在 `web/src/features/` 与 `backend/src/main/java/com/example/starter/features/`。

## Run locally

```bash
# API (requires Java 21; Maven is provided by the wrapper)
cd backend
./mvnw spring-boot:run

# Web app (requires pnpm)
cd ../web
pnpm install
pnpm dev
```

The Vite development server proxies `/api` to `http://localhost:8080`.

## Run with Docker Compose

```bash
cp .env.example .env
# Set unique MYSQL_PASSWORD and JWT_SECRET values in .env.
docker compose up --build
```

Compose persists MySQL data in the `starter-db-data` named volume, waits for MySQL before starting the API, and exposes the API at `http://localhost:8080`. Its readiness endpoint is `GET /api/health`.

## Included conventions

- `/api/**` API namespace and `{ code, message, data }` response envelope
- Validation and centralized error responses with server-side error logs
- Flyway migrations; never edit an applied migration
- Environment-only secrets and Docker Compose checks for required values
- Vue API boundary in `src/api/http.ts`; feature code must not instantiate Axios directly