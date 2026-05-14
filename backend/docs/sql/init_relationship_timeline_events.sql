CREATE TABLE IF NOT EXISTS relationship_timeline_events (
    id BIGSERIAL PRIMARY KEY,
    relationship_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    actor_user_id BIGINT,
    target_type VARCHAR(50),
    target_id BIGINT,
    cover_file_id BIGINT,
    cover_url VARCHAR(1000),
    event_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    importance VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    source VARCHAR(30) NOT NULL DEFAULT 'AUTO',
    metadata JSONB,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_relationship_timeline_relationship_id ON relationship_timeline_events (relationship_id);
CREATE INDEX IF NOT EXISTS idx_relationship_timeline_event_type ON relationship_timeline_events (event_type);
CREATE INDEX IF NOT EXISTS idx_relationship_timeline_target ON relationship_timeline_events (target_type, target_id);
CREATE INDEX IF NOT EXISTS idx_relationship_timeline_status ON relationship_timeline_events (status);
CREATE INDEX IF NOT EXISTS idx_relationship_timeline_event_date ON relationship_timeline_events (event_date);
