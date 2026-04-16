-- ============================================================
-- CEO SIM — V6: Achievements, Prestige History, Notifications
-- ============================================================

-- -------------------------------------------------------
-- ACHIEVEMENTS
-- Unlocked per player (persist across all runs / prestige resets).
-- -------------------------------------------------------
CREATE TABLE player_achievements (
    id              BIGSERIAL PRIMARY KEY,
    player_id       BIGINT      NOT NULL REFERENCES players(id) ON DELETE CASCADE,
    achievement_key VARCHAR(64) NOT NULL,   -- matches Java AchievementType enum
    unlocked_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (player_id, achievement_key)
);

-- -------------------------------------------------------
-- PRESTIGE HISTORY
-- One row per prestige event for /legacy display.
-- -------------------------------------------------------
CREATE TABLE prestige_history (
    id              BIGSERIAL PRIMARY KEY,
    player_id       BIGINT      NOT NULL REFERENCES players(id),
    company_id      BIGINT      NOT NULL REFERENCES companies(id),    -- the company that was prestiged
    prestige_type   VARCHAR(16) NOT NULL,   -- hard_reset | ipo | hostile_takeover | market_dominance
    valuation_at    BIGINT      NOT NULL,
    tick_count      INT         NOT NULL,
    legacy_mult_after NUMERIC(5,4) NOT NULL,
    carried_employee_id BIGINT  REFERENCES employees(id),             -- only for hard_reset
    prestiged_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- -------------------------------------------------------
-- NOTIFICATION PREFERENCES
-- -------------------------------------------------------
CREATE TABLE notification_preferences (
    id          BIGSERIAL PRIMARY KEY,
    player_id   BIGINT      NOT NULL REFERENCES players(id) ON DELETE CASCADE,
    event_type  VARCHAR(32) NOT NULL,
    -- tick_ready | project_done | sabotaged | employee_quit | loan_due | insolvency_warning
    is_enabled  BOOLEAN     NOT NULL DEFAULT TRUE,
    UNIQUE (player_id, event_type)
);

-- -------------------------------------------------------
-- WEEKLY SNAPSHOTS
-- Posted every Sunday automatically.
-- -------------------------------------------------------
CREATE TABLE weekly_snapshots (
    id              BIGSERIAL PRIMARY KEY,
    company_id      BIGINT      NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    week_number     INT         NOT NULL,   -- ISO week
    year            SMALLINT    NOT NULL,
    capital         BIGINT      NOT NULL,
    valuation       BIGINT      NOT NULL,
    market_share    NUMERIC(5,2) NOT NULL,
    reputation      SMALLINT    NOT NULL,
    employee_count  SMALLINT    NOT NULL,
    tick_count      INT         NOT NULL,
    snapshot_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (company_id, week_number, year)
);

-- -------------------------------------------------------
-- SERVER CONFIG
-- Global settings + server-wide event state.
-- -------------------------------------------------------
CREATE TABLE server_config (
    id              BIGSERIAL PRIMARY KEY,
    guild_id        VARCHAR(32) NOT NULL UNIQUE,
    event_log_ch_id VARCHAR(32),            -- channel for server-wide event log
    leaderboard_ch_id VARCHAR(32),          -- channel for auto-updated leaderboard
    snapshot_ch_id  VARCHAR(32),            -- channel for weekly snapshots
    last_tick_at    TIMESTAMPTZ,
    next_tick_at    TIMESTAMPTZ,
    seasonal_event_key VARCHAR(64),         -- currently active seasonal event (nullable)
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
