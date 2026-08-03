CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS memory_vector (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    space_id BIGINT NOT NULL,
    source_type VARCHAR(50) NOT NULL,
    source_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    embedding vector(1024) NOT NULL,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_memory_vector_user_space ON memory_vector (user_id, space_id);
CREATE INDEX IF NOT EXISTS idx_memory_vector_embedding_l2 ON memory_vector USING hnsw (embedding vector_l2_ops);
