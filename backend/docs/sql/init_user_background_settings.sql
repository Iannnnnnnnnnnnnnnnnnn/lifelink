CREATE TABLE IF NOT EXISTS user_background_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT false,
    object_key VARCHAR(500),
    image_url VARCHAR(1000),
    scale DOUBLE PRECISION NOT NULL DEFAULT 1,
    position_x INT NOT NULL DEFAULT 50,
    position_y INT NOT NULL DEFAULT 50,
    preset_position VARCHAR(30) NOT NULL DEFAULT 'CENTER',
    opacity DOUBLE PRECISION NOT NULL DEFAULT 0.22,
    blur INT NOT NULL DEFAULT 0,
    overlay_opacity DOUBLE PRECISION NOT NULL DEFAULT 0.35,
    scope VARCHAR(30) NOT NULL DEFAULT 'GLOBAL',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_background_settings_user_scope UNIQUE (user_id, scope)
);

CREATE INDEX IF NOT EXISTS idx_user_background_settings_user_id ON user_background_settings (user_id);
