-- ============================================================
-- V7: Persistente Cooldowns
-- ============================================================
CREATE TABLE cooldowns (
    id          BIGSERIAL PRIMARY KEY,
    discord_id  VARCHAR(32)  NOT NULL,
    command     VARCHAR(32)  NOT NULL,
    expires_at  TIMESTAMPTZ  NOT NULL,
    UNIQUE (discord_id, command)
);

CREATE INDEX idx_cooldowns_lookup ON cooldowns(discord_id, command);
