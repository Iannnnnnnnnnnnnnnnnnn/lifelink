CREATE TABLE IF NOT EXISTS dating_records (
    id BIGSERIAL PRIMARY KEY,
    relationship_id BIGINT NOT NULL,
    dating_date DATE NOT NULL,
    activities JSONB NOT NULL,
    location VARCHAR(200),
    note TEXT,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_dating_records_relationship_id ON dating_records (relationship_id);
CREATE INDEX IF NOT EXISTS idx_dating_records_date ON dating_records (dating_date DESC);
CREATE INDEX IF NOT EXISTS idx_dating_records_status ON dating_records (status);
