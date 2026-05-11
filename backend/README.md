# LifeLink Backend

LifeLink is a relationship-oriented daily record and life ledger system. This backend currently implements the base skeleton plus user registration, login, current-user lookup, and JWT authentication.

## Tech Stack

- JDK 17
- Spring Boot 3.3.5
- Maven
- PostgreSQL
- Redis
- MyBatis-Plus
- Spring Security 6
- Springdoc OpenAPI
- JWT
- MinIO Java SDK

## Required Docker Services

When using Windows + Rancher Desktop, start the required services in PowerShell:

```powershell
docker run --name lifelink-postgres -e POSTGRES_DB=lifelink -e POSTGRES_USER=lifelink -e POSTGRES_PASSWORD=lifelink123456 -p 5432:5432 -d postgres:15
docker run --name lifelink-redis -p 6379:6379 -d redis:7
docker run --name lifelink-minio -p 9000:9000 -p 9001:9001 -e MINIO_ROOT_USER=admin -e MINIO_ROOT_PASSWORD=admin123456 -d minio/minio server /data --console-address ":9001"
```

Create the MinIO bucket after MinIO starts:

```text
bucket: lifelink
console: http://localhost:9001
accessKey: admin
secretKey: admin123456
```

## Database Init

Run the SQL files before testing registration and relationship spaces:

```text
docs/sql/init_users.sql
docs/sql/init_relationships.sql
docs/sql/init_daily_posts.sql
docs/sql/init_space_todos.sql
```

PostgreSQL connection:

```text
url: jdbc:postgresql://localhost:5432/lifelink
username: lifelink
password: lifelink123456
driver: org.postgresql.Driver
```

## Start On Windows

Open PowerShell in the backend directory:

```powershell
cd D:\vb\lifelink\backend
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn -s settings.xml spring-boot:run
```

The backend runs on:

```text
http://localhost:8081
```

## API Documentation

After startup, open:

```text
http://localhost:8081/swagger-ui.html
```

## Auth Test Flow

1. Start PostgreSQL, Redis, and MinIO.
2. Execute `docs/sql/init_users.sql` in the `lifelink` database.
3. Start the backend.
4. Start the frontend at `D:\vb\lifelink\web`.
5. Open `http://localhost:5173/register`, create an account, then log in.
6. Go to `http://localhost:5173/relationships`, create a relationship, generate an invite code, then use another account to join.
7. Go to `http://localhost:5173/daily`, publish a text daily post, view detail, filter by relationship, and delete your own post.
8. Open a relationship detail page, click `View Todos`, then create, edit, complete, uncomplete, and delete shared todos.

Health check:

```text
GET http://localhost:8081/api/health
```
