CREATE TABLE IF NOT EXISTS user_coin_accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    balance INT NOT NULL DEFAULT 0,
    total_earned INT NOT NULL DEFAULT 0,
    total_spent INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS coin_ledger (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    change_amount INT NOT NULL,
    balance_after INT NOT NULL,
    type VARCHAR(20) NOT NULL,
    source_type VARCHAR(40) NOT NULL,
    source_id BIGINT,
    title VARCHAR(120),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_coin_ledger_source UNIQUE (source_type, source_id, type)
);

CREATE TABLE IF NOT EXISTS rewards (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    description TEXT,
    cover_object_key VARCHAR(500),
    cover_url VARCHAR(1000),
    coin_cost INT NOT NULL,
    stock INT,
    redeemed_count INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    sort_order INT NOT NULL DEFAULT 0,
    created_by BIGINT,
    updated_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_rewards_coin_cost_positive CHECK (coin_cost > 0),
    CONSTRAINT ck_rewards_stock_non_negative CHECK (stock IS NULL OR stock >= 0)
);

CREATE TABLE IF NOT EXISTS reward_redemptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    reward_id BIGINT NOT NULL,
    coin_cost_snapshot INT NOT NULL,
    reward_title_snapshot VARCHAR(120) NOT NULL,
    reward_description_snapshot TEXT,
    reward_cover_url_snapshot VARCHAR(1000),
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_coin_ledger_user_created ON coin_ledger (user_id, created_at);
CREATE INDEX IF NOT EXISTS idx_rewards_status_sort ON rewards (status, sort_order, created_at);
CREATE INDEX IF NOT EXISTS idx_reward_redemptions_user_created ON reward_redemptions (user_id, created_at);
CREATE INDEX IF NOT EXISTS idx_reward_redemptions_reward_id ON reward_redemptions (reward_id);
