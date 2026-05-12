CREATE TABLE IF NOT EXISTS daily_post_likes (
    id BIGSERIAL PRIMARY KEY,
    daily_post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_daily_post_likes_post_user UNIQUE (daily_post_id, user_id)
);

CREATE TABLE IF NOT EXISTS daily_post_comments (
    id BIGSERIAL PRIMARY KEY,
    daily_post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_daily_post_likes_post_id ON daily_post_likes (daily_post_id);
CREATE INDEX IF NOT EXISTS idx_daily_post_likes_user_id ON daily_post_likes (user_id);
CREATE INDEX IF NOT EXISTS idx_daily_post_comments_post_id ON daily_post_comments (daily_post_id);
CREATE INDEX IF NOT EXISTS idx_daily_post_comments_user_id ON daily_post_comments (user_id);
CREATE INDEX IF NOT EXISTS idx_daily_post_comments_status ON daily_post_comments (status);
CREATE INDEX IF NOT EXISTS idx_daily_post_comments_created_at ON daily_post_comments (created_at);
