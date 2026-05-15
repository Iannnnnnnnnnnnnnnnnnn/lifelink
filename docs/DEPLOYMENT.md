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

## Start

From the repository root:

```bash
docker compose -f docker-compose.prod.yml --env-file .env up -d --build
```

Open:

```text
http://localhost
```

If `WEB_PORT` is changed, use that port instead.

## Database Initialization

`docker-compose.prod.yml` mounts `backend/docs/sql` into PostgreSQL `/docker-entrypoint-initdb.d`. SQL files run only when the PostgreSQL data volume is created for the first time.

If the database already exists, apply new SQL files manually, for example:

```bash
docker compose -f docker-compose.prod.yml exec -T postgres psql -U lifelink -d lifelink < backend/docs/sql/init_relationship_timeline_events.sql
```

## MinIO URL Note

The current backend stores uploaded file URLs using `lifelink.minio.endpoint`. If this is set to `http://minio:9000`, containers can reach MinIO but browsers outside Docker may not. For production, set `LIFELINK_MINIO_ENDPOINT` to a URL reachable by both backend and browser, such as a public MinIO domain.

## Health Checks

Backend health:

```bash
curl http://localhost/api/health
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
