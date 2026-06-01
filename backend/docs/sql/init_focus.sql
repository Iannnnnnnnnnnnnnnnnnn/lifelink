CREATE TABLE IF NOT EXISTS focus_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    focus_minutes INT NOT NULL DEFAULT 25,
    short_break_minutes INT NOT NULL DEFAULT 5,
    long_break_minutes INT NOT NULL DEFAULT 15,
    long_break_interval INT NOT NULL DEFAULT 4,
    auto_start_break BOOLEAN NOT NULL DEFAULT FALSE,
    auto_start_next_focus BOOLEAN NOT NULL DEFAULT FALSE,
    sound_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    notification_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    strict_mode_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS focus_rooms (
    id BIGSERIAL PRIMARY KEY,
    creator_user_id BIGINT NOT NULL,
    space_id BIGINT NOT NULL,
    title VARCHAR(120),
    planned_minutes INT NOT NULL DEFAULT 25,
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    started_at TIMESTAMP,
    ended_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS focus_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    space_id BIGINT,
    todo_id BIGINT,
    room_id BIGINT,
    session_type VARCHAR(20) NOT NULL DEFAULT 'PERSONAL',
    phase VARCHAR(20) NOT NULL DEFAULT 'FOCUS',
    planned_minutes INT NOT NULL,
    actual_minutes INT NOT NULL DEFAULT 0,
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP,
    paused_seconds INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'RUNNING',
    source VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS focus_session_events (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    event_type VARCHAR(20) NOT NULL,
    event_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS focus_room_members (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    member_status VARCHAR(20) NOT NULL DEFAULT 'INVITED',
    joined_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_focus_room_members_room_user UNIQUE (room_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_focus_sessions_user_started ON focus_sessions (user_id, started_at);
CREATE INDEX IF NOT EXISTS idx_focus_sessions_space_started ON focus_sessions (space_id, started_at);
CREATE INDEX IF NOT EXISTS idx_focus_sessions_todo_id ON focus_sessions (todo_id);
CREATE INDEX IF NOT EXISTS idx_focus_sessions_status ON focus_sessions (status);
CREATE INDEX IF NOT EXISTS idx_focus_sessions_room_id ON focus_sessions (room_id);
CREATE INDEX IF NOT EXISTS idx_focus_session_events_session_id ON focus_session_events (session_id);
CREATE INDEX IF NOT EXISTS idx_focus_session_events_user_id ON focus_session_events (user_id);
CREATE INDEX IF NOT EXISTS idx_focus_rooms_space_id ON focus_rooms (space_id);
CREATE INDEX IF NOT EXISTS idx_focus_rooms_status ON focus_rooms (status);
CREATE INDEX IF NOT EXISTS idx_focus_room_members_user_id ON focus_room_members (user_id);
