# LifeLink Production Deployment

This document describes the Docker-based production deployment files added for LifeLink.

## Files

- `backend/Dockerfile`: builds the Spring Boot backend with Java 17 and runs the packaged jar.
- `web/Dockerfile`: builds the Vite frontend and serves it with Nginx.
- `backend/src/main/resources/application-prod.yml`: production Spring profile using environment variables.
- `docker-compose.prod.yml`: production Compose stack for PostgreSQL, Redis, MinIO, backend, and web.
- `web/nginx.conf.template`: Nginx template used by the frontend container.
- `docker/nginx/lifelink.conf.template`: optional external reverse-proxy template.
- `.env.example`: example production environment variables.

## Prerequisites

- Docker Engine or Rancher Desktop with Docker compatibility.
- Docker Compose v2.
- A production-grade random JWT secret.
- A public MinIO endpoint strategy if uploaded images must be reachable outside Docker.

## Configure Environment

Copy the example environment file:

```bash
cp .env.example .env
```

Update at least these values:

- `POSTGRES_PASSWORD`
- `MINIO_ROOT_PASSWORD`
- `LIFELINK_JWT_SECRET`
- `LIFELINK_MINIO_ENDPOINT`

`VITE_API_BASE_URL` defaults to an empty value. In that mode, the frontend calls `/api/...` on the same origin and Nginx proxies requests to the backend container. For a separate API domain, set it to that origin, for example:

```env
VITE_API_BASE_URL=https://api.example.com
```

`LIFELINK_CORS_ALLOWED_ORIGINS` can stay empty when the frontend and API are on the same origin. If they are on different domains, set it to the HTTPS frontend origin list:

```env
LIFELINK_CORS_ALLOWED_ORIGINS=https://example.com,https://www.example.com
```

## Start

From the repository root:

```bash
docker compose -f docker-compose.prod.yml --env-file .env up -d --build
```

Open:

```text
https://example.com
```

If `WEB_PORT` is changed, use that port instead.

## Database Initialization

`docker-compose.prod.yml` mounts `backend/docs/sql` into PostgreSQL `/docker-entrypoint-initdb.d`. SQL files run only when the PostgreSQL data volume is created for the first time.

If the database already exists, apply new SQL files manually, for example:

```bash
docker compose -f docker-compose.prod.yml exec -T postgres psql -U lifelink -d lifelink < backend/docs/sql/init_relationship_timeline_events.sql
```

## MinIO URL Note

The backend connects to MinIO using `lifelink.minio.endpoint`, but stores uploaded file URLs with the fixed public host `http://47.97.202.182`. This keeps uploaded image URLs browser-reachable even when the backend talks to MinIO through Docker's internal service name.

For Docker production, keep the internal endpoint as the Compose service name:

```env
LIFELINK_MINIO_ENDPOINT=http://minio:9000
```

The frontend Nginx template proxies `/lifelink/` to MinIO, so URLs like `http://47.97.202.182/lifelink/daily/2026/05/xxx.jpg` can be served from the same public host.

New uploads will use the public URL. Existing rows in `file_resources.file_url`, `anniversaries.background_url`, and `relationship_timeline_events.cover_url` must be updated separately if they already contain `http://minio:9000`.
The current backend stores uploaded file URLs using `lifelink.minio.endpoint`. If this is set to `http://minio:9000`, containers can reach MinIO but HTTPS pages in browsers may not be able to load those files. For production, set `LIFELINK_MINIO_ENDPOINT` to an HTTPS URL reachable by both backend and browser, such as `https://files.example.com`.

## Health Checks

Backend health:

```bash
curl https://example.com/api/health
```

Container status:

```bash
docker compose -f docker-compose.prod.yml ps
```

Logs:

```bash
docker compose -f docker-compose.prod.yml logs -f backend
docker compose -f docker-compose.prod.yml logs -f web
```

## Stop

```bash
docker compose -f docker-compose.prod.yml down
```

To remove data volumes as well:

```bash
docker compose -f docker-compose.prod.yml down -v
```

Use `down -v` only when intentionally deleting PostgreSQL, Redis, and MinIO data.
