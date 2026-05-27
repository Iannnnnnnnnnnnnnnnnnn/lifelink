CREATE TABLE IF NOT EXISTS cycle_care_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    default_lover_space_id BIGINT,
    cycle_length INT NOT NULL DEFAULT 28,
    period_length INT NOT NULL DEFAULT 5,
    last_period_start_date DATE,
    reminder_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    share_level VARCHAR(30) NOT NULL DEFAULT 'PRIVATE',
    timezone VARCHAR(50) NOT NULL DEFAULT 'Asia/Shanghai',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cycle_period_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    lover_space_id BIGINT,
    start_date DATE NOT NULL,
    end_date DATE,
    cycle_length_snapshot INT,
    period_length_snapshot INT,
    note TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cycle_daily_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    lover_space_id BIGINT,
    log_date DATE NOT NULL,
    flow_level VARCHAR(30) NOT NULL DEFAULT 'NONE',
    pain_level INT,
    mood VARCHAR(30),
    symptoms JSONB,
    temperature_feeling VARCHAR(30),
    appetite VARCHAR(30),
    note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, log_date)
);

CREATE TABLE IF NOT EXISTS cycle_reminder_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    period_before_days INT NOT NULL DEFAULT 2,
    late_after_days INT NOT NULL DEFAULT 7,
    enable_partner_care_reminder BOOLEAN NOT NULL DEFAULT TRUE,
    enable_warning BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cycle_warnings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    lover_space_id BIGINT,
    warning_type VARCHAR(50) NOT NULL,
    warning_date DATE NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_cycle_care_profiles_lover_space_id
ON cycle_care_profiles(default_lover_space_id);

CREATE INDEX IF NOT EXISTS idx_cycle_period_records_user_id
ON cycle_period_records(user_id);

CREATE INDEX IF NOT EXISTS idx_cycle_period_records_lover_space_id
ON cycle_period_records(lover_space_id);

CREATE INDEX IF NOT EXISTS idx_cycle_period_records_start_date
ON cycle_period_records(start_date);

CREATE INDEX IF NOT EXISTS idx_cycle_period_records_status
ON cycle_period_records(status);

CREATE INDEX IF NOT EXISTS idx_cycle_daily_logs_user_id
ON cycle_daily_logs(user_id);

CREATE INDEX IF NOT EXISTS idx_cycle_daily_logs_lover_space_id
ON cycle_daily_logs(lover_space_id);

CREATE INDEX IF NOT EXISTS idx_cycle_daily_logs_log_date
ON cycle_daily_logs(log_date);

CREATE INDEX IF NOT EXISTS idx_cycle_warnings_user_id
ON cycle_warnings(user_id);

CREATE INDEX IF NOT EXISTS idx_cycle_warnings_lover_space_id
ON cycle_warnings(lover_space_id);

CREATE INDEX IF NOT EXISTS idx_cycle_warnings_date
ON cycle_warnings(warning_date);

CREATE INDEX IF NOT EXISTS idx_cycle_warnings_status
ON cycle_warnings(status);

CREATE UNIQUE INDEX IF NOT EXISTS ux_cycle_warnings_active_once
ON cycle_warnings(user_id, warning_type, warning_date)
WHERE status = 'ACTIVE';
