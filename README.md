\# LifeLink



LifeLink 是一个关系型日常记录与生活管理项目。



\## 技术栈



\- Backend: Java 17 + Spring Boot

\- Frontend: React + Vite + TypeScript

\- Database: PostgreSQL

\- Cache: Redis

\- File Storage: MinIO

\- Container: Rancher Desktop / Docker Compose

## Production Deployment

Production deployment support is provided through Docker and Docker Compose:

- `docker-compose.prod.yml`
- `.env.example`
- `backend/Dockerfile`
- `web/Dockerfile`
- `web/nginx.conf.template`
- `docker/nginx/lifelink.conf.template`
- `backend/src/main/resources/application-prod.yml`

See `docs/DEPLOYMENT.md` for the deployment steps and environment variable notes.
