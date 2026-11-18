# Backend Deployment (Module 08)

AnchorGuard backend ships as a Spring Boot API (`anchorguard-api`) with PostgreSQL and Nginx reverse proxy via Docker Compose.

## Prerequisites
- Docker Engine + Docker Compose plugin
- TLS certificate/key for `api.anchorguard.com` (or target host) mounted into `backend/deploy/certs/`
- Environment values/secrets provided via `.env` or deployment secret manager (never commit real secrets)

## Files
- `backend/Dockerfile` — multi-stage image build (Gradle build stage, JRE runtime, non-root user)
- `backend/docker-compose.yml` — `db` (Postgres 16), `api`, `nginx`
- `backend/deploy/nginx/conf.d/anchorguard.conf` — HTTP->HTTPS redirect and reverse proxy to `api:8080`
- `backend/deploy/certs/` — mount point for `fullchain.pem` and `privkey.pem` (gitignored patterns apply to cert/key material)
- `backend/deploy/certbot/www/` — ACME challenge webroot mount
- `backend/.env.example` — template for DB/JWT/CORS/API base URL values

## Configure
1. Copy and edit environment file:
   ```bash
   cd backend
   cp .env.example .env
   ```
2. Set strong secrets:
   - `ANCHORGUARD_JWT_SECRET` (required, >=32 bytes)
   - `POSTGRES_PASSWORD` / `ANCHORGUARD_DB_PASSWORD`
   - `ANCHORGUARD_CORS_ORIGINS` (web console origin)
   - `ANCHORGUARD_API_BASE_URL` (client-facing API base URL, e.g. `https://api.anchorguard.com/v1`)
3. Place TLS materials:
   - `backend/deploy/certs/fullchain.pem`
   - `backend/deploy/certs/privkey.pem`
   (For Let's Encrypt automation, use certbot to populate/mount compatible cert paths and `deploy/certbot/www`.)

## Run
```bash
cd backend
docker compose up --build -d
docker compose ps
docker compose logs -f api
```
- Postgres data persisted in `anchorguard_pgdata` volume.
- Flyway migrations run on API startup (Spring Boot/Flyway configuration in backend app).
- Nginx exposes ports `80`/`443`; API container is internal (`expose: 8080`).

## Health and docs
- Health: `/health`, `/actuator/health` (as configured/exposed through proxy)
- API docs: `/v1/docs/**` (Swagger/OpenAPI paths permitted by security config)

## Build notes
- `backend/Dockerfile` uses `gradle:8.11.1-jdk21` build stage and does not require a committed Gradle wrapper JAR. If you prefer wrapper-based builds, add `backend/gradle/wrapper/gradle-wrapper.jar` and update Dockerfile/CI accordingly.
- CI workflow: `.github/workflows/backend-ci.yml` runs tests and builds Docker image.

## Security notes
- Never commit `.env`, JWT secrets, DB passwords, keystores, or private keys.
- Rotate secrets on compromise; restrict DB/network access to internal compose network where possible.
- Review CORS origins and TLS settings before production exposure.
