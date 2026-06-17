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
- `LIFELINK_MINIO_PUBLIC_ENDPOINT`
- `LIFELINK_PHILOSOPHY_ALLOWED_PHONES`

`VITE_API_BASE_URL` defaults to an empty value. In that mode, the frontend calls `/api/...` on the same origin and Nginx proxies requests to the backend container. For a separate API domain, set it to that origin, for example:

```env
VITE_API_BASE_URL=https://api.example.com
```

`LIFELINK_CORS_ALLOWED_ORIGINS` can stay empty when the frontend and API are on the same origin. If they are on different domains, set it to the HTTPS frontend origin list:

```env
LIFELINK_CORS_ALLOWED_ORIGINS=https://example.com,https://www.example.com
```

`LIFELINK_PHILOSOPHY_ALLOWED_PHONES` controls access to the Philosophy Dialogues module. Leave it empty to disable the module for all users, or set a comma-separated phone allowlist in production.

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


The backend connects to MinIO using `lifelink.minio.endpoint`, but stores uploaded file URLs with `lifelink.minio.public-endpoint`. This keeps uploaded image URLs browser-reachable over HTTPS even when the backend talks to MinIO through Docker's internal service name.

For Docker production, keep the internal endpoint as the Compose service name:


```env
LIFELINK_MINIO_ENDPOINT=http://minio:9000
```

Uploaded file URLs are generated from `LIFELINK_MINIO_PUBLIC_ENDPOINT`. If it is empty, the backend returns same-origin URLs like `/lifelink/daily/...`, which work when the Nginx templates proxy `/lifelink/` to MinIO. For miniapps or a separate file domain, set it to an HTTPS URL reachable by browsers:

```env
LIFELINK_MINIO_PUBLIC_ENDPOINT=https://example.com
```

The Nginx templates proxy `/lifelink/` to MinIO, so uploaded images will look like `https://example.com/lifelink/daily/2026/05/xxx.jpg`.

Set the public endpoint to the HTTPS domain that serves the frontend:

```env
LIFELINK_MINIO_PUBLIC_ENDPOINT=https://memoryspace.online
```

The frontend Nginx template proxies `/lifelink/` to MinIO, so URLs like `https://memoryspace.online/lifelink/daily/2026/05/xxx.jpg` can be served from the same public host.

New uploads will use the public URL. Existing rows in `file_resources.file_url`, `anniversaries.background_url`, and `relationship_timeline_events.cover_url` must be updated separately if they already contain an old host such as `http://47.97.202.182` or `http://minio:9000`.
>>>>>>> 6529f625afb39156fe842b5f2632498ae2ddf6af

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
