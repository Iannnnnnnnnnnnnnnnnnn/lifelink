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

CREATE INDEX IF NOT EXISTS idx_space_activities_relationship_id ON space_activities (relationship_id);
CREATE INDEX IF NOT EXISTS idx_space_activities_actor_user_id ON space_activities (actor_user_id);
CREATE INDEX IF NOT EXISTS idx_space_activities_activity_type ON space_activities (activity_type);
CREATE INDEX IF NOT EXISTS idx_space_activities_created_at ON space_activities (created_at);
