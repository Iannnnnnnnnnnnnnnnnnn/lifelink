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

CREATE INDEX IF NOT EXISTS idx_anniversaries_relationship_id ON anniversaries (relationship_id);
CREATE INDEX IF NOT EXISTS idx_anniversaries_date ON anniversaries (anniversary_date);
CREATE INDEX IF NOT EXISTS idx_anniversaries_status ON anniversaries (status);
CREATE INDEX IF NOT EXISTS idx_anniversaries_created_by ON anniversaries (created_by);
