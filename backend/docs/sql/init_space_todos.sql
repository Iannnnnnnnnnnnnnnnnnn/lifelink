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

CREATE INDEX IF NOT EXISTS idx_space_todos_relationship_id ON space_todos (relationship_id);
CREATE INDEX IF NOT EXISTS idx_space_todos_status ON space_todos (status);
CREATE INDEX IF NOT EXISTS idx_space_todos_due_time ON space_todos (due_time);
CREATE INDEX IF NOT EXISTS idx_space_todos_created_at ON space_todos (created_at);
