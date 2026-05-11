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

CREATE INDEX IF NOT EXISTS idx_daily_posts_relationship_id ON daily_posts (relationship_id);
CREATE INDEX IF NOT EXISTS idx_daily_posts_user_id ON daily_posts (user_id);
CREATE INDEX IF NOT EXISTS idx_daily_posts_created_at ON daily_posts (created_at);
