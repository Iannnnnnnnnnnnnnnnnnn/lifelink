# Database Design

LifeLink currently uses PostgreSQL as the backend database.

Current connection:

```text
url: jdbc:postgresql://localhost:5432/lifelink
username: lifelink
driver: org.postgresql.Driver
```

## users

The `users` table stores account identity and authentication data. PostgreSQL reserved table name `user` is not used.

```sql
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(30) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

Initialization SQL:

```text
docs/sql/init_users.sql
```

## relationships

Stores relationship spaces.

```sql
CREATE TABLE IF NOT EXISTS relationships (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(30) NOT NULL,
    description VARCHAR(500),
    owner_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## relationship_members

Stores relationship membership and role.

```sql
CREATE TABLE IF NOT EXISTS relationship_members (
    id BIGSERIAL PRIMARY KEY,
    relationship_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    nickname VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (relationship_id, user_id)
);
```

`role` supports `OWNER`, `ADMIN`, and `MEMBER`.
`status` supports `ACTIVE`, `REMOVED`, and `LEFT`; member removal and leaving are soft state changes.

## relationship_invites

Stores invite codes for joining relationship spaces.

```sql
CREATE TABLE IF NOT EXISTS relationship_invites (
    id BIGSERIAL PRIMARY KEY,
    relationship_id BIGINT NOT NULL,
    invite_code VARCHAR(50) NOT NULL UNIQUE,
    inviter_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    expire_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

Relationship initialization SQL:

```text
docs/sql/init_relationships.sql
```

## daily_posts

Stores daily records (text with optional images).

## global search

Global search does not introduce a dedicated search index table in the first version.
It queries existing business tables with PostgreSQL `ILIKE` and filters by active relationship memberships.
Suggested future optimizations include PostgreSQL full-text search or `pg_trgm` indexes when data volume grows.

```sql
CREATE TABLE IF NOT EXISTS daily_posts (
    id BIGSERIAL PRIMARY KEY,
    relationship_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    mood VARCHAR(30),
    visibility VARCHAR(20) NOT NULL DEFAULT 'RELATIONSHIP',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

Daily post initialization SQL:

```text
docs/sql/init_daily_posts.sql
```

## file_resources

Stores uploaded file metadata.

```sql
CREATE TABLE IF NOT EXISTS file_resources (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    bucket VARCHAR(100) NOT NULL,
    object_key VARCHAR(500) NOT NULL,
    original_name VARCHAR(255),
    content_type VARCHAR(100),
    file_size BIGINT,
    file_url VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## daily_post_images

Stores relation between daily posts and uploaded images.

```sql
CREATE TABLE IF NOT EXISTS daily_post_images (
    id BIGSERIAL PRIMARY KEY,
    daily_post_id BIGINT NOT NULL,
    file_id BIGINT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

Daily image initialization SQL:

```text
docs/sql/init_daily_images.sql
```

File, accounting, and statistics tables will be added in later tasks.

## space_todos

Stores shared todo items in relationship spaces.

```sql
CREATE TABLE IF NOT EXISTS space_todos (
    id BIGSERIAL PRIMARY KEY,
    relationship_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT,
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    status VARCHAR(20) NOT NULL DEFAULT 'TODO',
    due_time TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    completed_by BIGINT,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

Todo initialization SQL:

```text
docs/sql/init_space_todos.sql
```

## daily_post_likes

Stores daily post likes. A user can like one post only once.

```sql
CREATE TABLE IF NOT EXISTS daily_post_likes (
    id BIGSERIAL PRIMARY KEY,
    daily_post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_daily_post_likes_post_user UNIQUE (daily_post_id, user_id)
);
```

## daily_post_comments

Stores daily post comments. Deletion is soft by setting `status` to `DELETED`.

```sql
CREATE TABLE IF NOT EXISTS daily_post_comments (
    id BIGSERIAL PRIMARY KEY,
    daily_post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

Daily post interaction initialization SQL:

```text
docs/sql/init_daily_post_interactions.sql
```

## account_books

Stores personal and relationship account books.

```sql
CREATE TABLE IF NOT EXISTS account_books (
    id BIGSERIAL PRIMARY KEY,
    relationship_id BIGINT,
    owner_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'PERSONAL',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## transaction_categories

Stores income and expense categories.

```sql
CREATE TABLE IF NOT EXISTS transaction_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL,
    icon VARCHAR(100),
    sort_order INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);
```

## transactions

Stores finance transactions.

```sql
CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    account_book_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    category_id BIGINT,
    title VARCHAR(100) NOT NULL,
    note TEXT,
    transaction_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

Accounting initialization SQL:

```text
docs/sql/init_accounting.sql
```

## anniversaries

Stores relationship-space anniversaries and countdown dates. Background images reuse `file_resources`.

```sql
CREATE TABLE IF NOT EXISTS anniversaries (
    id BIGSERIAL PRIMARY KEY,
    relationship_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    anniversary_date DATE NOT NULL,
    repeat_type VARCHAR(20) NOT NULL DEFAULT 'NONE',
    background_file_id BIGINT,
    background_url VARCHAR(1000),
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

Anniversary initialization SQL:

```text
docs/sql/init_anniversaries.sql
```

## space_activities

Stores timeline activities generated by relationship-space business events.

```sql
CREATE TABLE IF NOT EXISTS space_activities (
    id BIGSERIAL PRIMARY KEY,
    relationship_id BIGINT NOT NULL,
    actor_user_id BIGINT NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    target_type VARCHAR(50),
    target_id BIGINT,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    metadata JSONB,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

`metadata` stores extensible event payload data as PostgreSQL JSONB.

Activity initialization SQL:

```text
docs/sql/init_space_activities.sql
```
