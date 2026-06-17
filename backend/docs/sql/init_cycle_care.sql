CREATE TABLE IF NOT EXISTS cycle_care_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    default_lover_space_id BIGINT,
    cycle_length INT NOT NULL DEFAULT 28,
    period_length INT NOT NULL DEFAULT 5,
    last_period_start_date DATE,
    reminder_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    daily_advice_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    share_level VARCHAR(30) NOT NULL DEFAULT 'PRIVATE',
    timezone VARCHAR(50) NOT NULL DEFAULT 'Asia/Shanghai',
    privacy_note_visible_to_partner BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cycle_period_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    lover_space_id BIGINT,
    start_date DATE NOT NULL,
    end_date DATE,
    flow_summary VARCHAR(100),
    pain_summary VARCHAR(100),
    color_summary VARCHAR(100),
    cycle_length_snapshot INT,
    period_length_snapshot INT,
    note TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cycle_daily_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    lover_space_id BIGINT,
    log_date DATE NOT NULL,
    flow_level VARCHAR(30) NOT NULL DEFAULT 'NONE',
    blood_color VARCHAR(30),
    pain_level INT,
    mood VARCHAR(30),
    symptoms JSONB,
    temperature_feeling VARCHAR(30),
    appetite VARCHAR(30),
    sleep_hours NUMERIC(4, 1),
    water_cups INT,
    exercise_minutes INT,
    food_tags JSONB,
    medication_note TEXT,
    discharge_note TEXT,
    temperature NUMERIC(4, 1),
    weight NUMERIC(5, 2),
    note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, log_date)
);

CREATE TABLE IF NOT EXISTS cycle_custom_track_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    value_type VARCHAR(20) NOT NULL,
    options_json JSONB,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cycle_custom_track_values (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    log_date DATE NOT NULL,
    value_text TEXT,
    value_number NUMERIC(12, 2),
    value_boolean BOOLEAN,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, item_id, log_date)
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

CREATE TABLE IF NOT EXISTS cycle_daily_advice_reports (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    lover_space_id BIGINT,
    report_date DATE NOT NULL,
    phase VARCHAR(50),
    phase_label VARCHAR(100),
    is_predicted_phase BOOLEAN NOT NULL DEFAULT FALSE,
    summary TEXT,
    body_status_summary TEXT,
    flow_summary TEXT,
    pain_summary TEXT,
    mood_summary TEXT,
    symptom_summary TEXT,
    clothing_advice TEXT,
    food_advice TEXT,
    rest_advice TEXT,
    mood_advice TEXT,
    partner_advice TEXT,
    warning_summary TEXT,
    risk_level VARCHAR(20) NOT NULL DEFAULT 'NONE',
    warning_types JSONB,
    share_level_snapshot VARCHAR(30) NOT NULL DEFAULT 'PRIVATE',
    partner_visible_summary TEXT,
    source_type VARCHAR(20) NOT NULL DEFAULT 'RULE_BASED',
    ai_generated BOOLEAN NOT NULL DEFAULT FALSE,
    ai_model VARCHAR(100),
    prompt_version VARCHAR(50),
    raw_ai_response JSONB,
    status VARCHAR(20) NOT NULL DEFAULT 'GENERATED',
    error_message VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, report_date)
);

CREATE TABLE IF NOT EXISTS cycle_fertility_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    lover_space_id BIGINT,
    record_date DATE NOT NULL,
    basal_body_temperature NUMERIC(4, 1),
    ovulation_test_value VARCHAR(100),
    ovulation_test_result VARCHAR(50),
    intercourse_recorded BOOLEAN,
    discharge_type VARCHAR(50),
    note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE cycle_care_profiles
ADD COLUMN IF NOT EXISTS daily_advice_enabled BOOLEAN NOT NULL DEFAULT TRUE,
ADD COLUMN IF NOT EXISTS privacy_note_visible_to_partner BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE cycle_period_records
ADD COLUMN IF NOT EXISTS flow_summary VARCHAR(100),
ADD COLUMN IF NOT EXISTS pain_summary VARCHAR(100),
ADD COLUMN IF NOT EXISTS color_summary VARCHAR(100),
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

ALTER TABLE cycle_daily_logs
ADD COLUMN IF NOT EXISTS blood_color VARCHAR(30),
ADD COLUMN IF NOT EXISTS sleep_hours NUMERIC(4, 1),
ADD COLUMN IF NOT EXISTS water_cups INT,
ADD COLUMN IF NOT EXISTS exercise_minutes INT,
ADD COLUMN IF NOT EXISTS food_tags JSONB,
ADD COLUMN IF NOT EXISTS medication_note TEXT,
ADD COLUMN IF NOT EXISTS discharge_note TEXT,
ADD COLUMN IF NOT EXISTS temperature NUMERIC(4, 1),
ADD COLUMN IF NOT EXISTS weight NUMERIC(5, 2);

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

CREATE INDEX IF NOT EXISTS idx_cycle_custom_track_items_user_id
ON cycle_custom_track_items(user_id);

CREATE INDEX IF NOT EXISTS idx_cycle_custom_track_values_user_date
ON cycle_custom_track_values(user_id, log_date);

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

CREATE INDEX IF NOT EXISTS idx_cycle_daily_advice_reports_user_date
ON cycle_daily_advice_reports(user_id, report_date);

CREATE INDEX IF NOT EXISTS idx_cycle_daily_advice_reports_space_date
ON cycle_daily_advice_reports(lover_space_id, report_date);

CREATE INDEX IF NOT EXISTS idx_cycle_daily_advice_reports_status
ON cycle_daily_advice_reports(status);

CREATE INDEX IF NOT EXISTS idx_cycle_daily_advice_reports_created_at
ON cycle_daily_advice_reports(created_at);

CREATE INDEX IF NOT EXISTS idx_cycle_fertility_records_user_date
ON cycle_fertility_records(user_id, record_date);

CREATE INDEX IF NOT EXISTS idx_cycle_fertility_records_space_date
ON cycle_fertility_records(lover_space_id, record_date);
