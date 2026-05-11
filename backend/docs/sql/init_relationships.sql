CREATE TABLE IF NOT EXISTS relationships (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(30) NOT NULL,
    description VARCHAR(500),
    owner_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS relationship_members (
    id BIGSERIAL PRIMARY KEY,
    relationship_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    nickname VARCHAR(50),
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (relationship_id, user_id)
);

CREATE TABLE IF NOT EXISTS relationship_invites (
    id BIGSERIAL PRIMARY KEY,
    relationship_id BIGINT NOT NULL,
    invite_code VARCHAR(50) NOT NULL UNIQUE,
    inviter_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    expire_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_relationships_owner_id ON relationships (owner_id);
CREATE INDEX IF NOT EXISTS idx_relationship_members_user_id ON relationship_members (user_id);
CREATE INDEX IF NOT EXISTS idx_relationship_members_relationship_id ON relationship_members (relationship_id);
CREATE INDEX IF NOT EXISTS idx_relationship_invites_code ON relationship_invites (invite_code);
