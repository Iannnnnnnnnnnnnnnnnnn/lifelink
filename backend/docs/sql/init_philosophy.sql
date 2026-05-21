CREATE TABLE IF NOT EXISTS philosophers (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name_zh VARCHAR(100) NOT NULL,
    name_en VARCHAR(100) NOT NULL,
    era_zh VARCHAR(100),
    era_en VARCHAR(100),
    description_zh TEXT,
    description_en TEXT,
    avatar_url VARCHAR(500),
    tags JSONB,
    sort_order INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS philosophy_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    question TEXT NOT NULL,
    language VARCHAR(20) NOT NULL DEFAULT 'zh-CN',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS philosophy_responses (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL,
    philosopher_code VARCHAR(50) NOT NULL,
    philosopher_name VARCHAR(100) NOT NULL,
    viewpoint TEXT,
    question_back TEXT,
    objection TEXT,
    summary TEXT,
    raw_response TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_philosophers_code ON philosophers(code);
CREATE INDEX IF NOT EXISTS idx_philosophy_sessions_user_id ON philosophy_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_philosophy_sessions_created_at ON philosophy_sessions(created_at);
CREATE INDEX IF NOT EXISTS idx_philosophy_responses_session_id ON philosophy_responses(session_id);
CREATE INDEX IF NOT EXISTS idx_philosophy_responses_philosopher_code ON philosophy_responses(philosopher_code);

INSERT INTO philosophers (
    code, name_zh, name_en, era_zh, era_en, description_zh, description_en, tags, sort_order, status, updated_at
) VALUES
('SOCRATES', '苏格拉底', 'Socrates', '古希腊', 'Ancient Greece', '以追问推动自我审视，关注德性、定义与未经省察的生活。', 'Uses questioning to invite self-examination, focusing on virtue, definition, and the examined life.', '["追问","德性","定义","反思"]'::jsonb, 1, 'ACTIVE', CURRENT_TIMESTAMP),
('PLATO', '柏拉图', 'Plato', '古希腊', 'Ancient Greece', '以理念论、灵魂秩序与正义城邦思考现实背后的更高形式。', 'Explores higher forms behind reality through the theory of forms, the soul, and justice.', '["理念论","灵魂","正义","理想国"]'::jsonb, 2, 'ACTIVE', CURRENT_TIMESTAMP),
('ARISTOTLE', '亚里士多德', 'Aristotle', '古希腊', 'Ancient Greece', '重视经验观察、目的、德性实践与适度的中道。', 'Values empirical observation, purpose, practiced virtue, and the balanced mean.', '["目的论","德性伦理","中道","经验"]'::jsonb, 3, 'ACTIVE', CURRENT_TIMESTAMP),
('KANT', '康德', 'Kant', '启蒙时代', 'Enlightenment', '强调理性、自律、义务与可普遍化的道德法则。', 'Emphasizes reason, autonomy, duty, and moral laws that can be universalized.', '["理性","义务","自由","道德律"]'::jsonb, 4, 'ACTIVE', CURRENT_TIMESTAMP),
('NIETZSCHE', '尼采', 'Nietzsche', '19世纪', '19th Century', '关注生命力、价值重估、权力意志与自我超越。', 'Focuses on vitality, revaluation of values, will to power, and self-overcoming.', '["权力意志","超人","价值重估","生命力"]'::jsonb, 5, 'ACTIVE', CURRENT_TIMESTAMP),
('SCHOPENHAUER', '叔本华', 'Schopenhauer', '19世纪', '19th Century', '从意志、欲望与痛苦出发，重视审美和慈悲的暂时解脱。', 'Starts from will, desire, and suffering, valuing aesthetic and compassionate release.', '["意志","痛苦","悲观主义","审美"]'::jsonb, 6, 'ACTIVE', CURRENT_TIMESTAMP),
('CONFUCIUS', '孔子', 'Confucius', '中国春秋时期', 'Spring and Autumn Period of China', '强调仁、礼、修身与在关系中成为君子。', 'Emphasizes ren, ritual propriety, self-cultivation, and becoming noble in relationships.', '["仁","礼","君子","修身"]'::jsonb, 7, 'ACTIVE', CURRENT_TIMESTAMP),
('ZHUANGZI', '庄子', 'Zhuangzi', '中国战国时期', 'Warring States Period of China', '以逍遥、齐物、自然和无为松动固执的分别。', 'Uses freedom, equality of things, naturalness, and non-forcing to loosen rigid distinctions.', '["逍遥","齐物","自然","无为"]'::jsonb, 8, 'ACTIVE', CURRENT_TIMESTAMP)
ON CONFLICT (code) DO UPDATE SET
    name_zh = EXCLUDED.name_zh,
    name_en = EXCLUDED.name_en,
    era_zh = EXCLUDED.era_zh,
    era_en = EXCLUDED.era_en,
    description_zh = EXCLUDED.description_zh,
    description_en = EXCLUDED.description_en,
    tags = EXCLUDED.tags,
    sort_order = EXCLUDED.sort_order,
    status = EXCLUDED.status,
    updated_at = CURRENT_TIMESTAMP;
