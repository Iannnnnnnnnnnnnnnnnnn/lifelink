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

CREATE TABLE IF NOT EXISTS daily_post_images (
    id BIGSERIAL PRIMARY KEY,
    daily_post_id BIGINT NOT NULL,
    file_id BIGINT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_file_resources_user_id ON file_resources (user_id);
CREATE INDEX IF NOT EXISTS idx_daily_post_images_post_id ON daily_post_images (daily_post_id);
CREATE INDEX IF NOT EXISTS idx_daily_post_images_file_id ON daily_post_images (file_id);
