-- ============================================================
-- CEO SIM — V1: Core Tables
-- Players, Companies, KPIs, Ticks
-- ============================================================

-- -------------------------------------------------------
-- PLAYERS
-- -------------------------------------------------------
CREATE TABLE players (
    id              BIGSERIAL PRIMARY KEY,
    discord_id      VARCHAR(32)  NOT NULL UNIQUE,
    discord_name    VARCHAR(64)  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- -------------------------------------------------------
-- COMPANIES
-- A player has exactly one active company at a time.
-- On hard prestige the company is soft-deleted (deleted_at set)
-- and a new one is created.
-- -------------------------------------------------------
CREATE TABLE companies (
    id              BIGSERIAL PRIMARY KEY,
    player_id       BIGINT       NOT NULL REFERENCES players(id),
    name            VARCHAR(64)  NOT NULL,

    -- Starter choice
    market          VARCHAR(32)  NOT NULL,   -- consumer_tech | enterprise_saas | fintech | e_commerce | cybersecurity
    starter_type    VARCHAR(16)  NOT NULL,   -- bootstrapper | visionary | networker

    -- Core KPIs
    capital         BIGINT       NOT NULL DEFAULT 0,        -- in cents to avoid floats
    revenue_per_tick BIGINT      NOT NULL DEFAULT 0,
    burn_rate       BIGINT       NOT NULL DEFAULT 0,
    morale          SMALLINT     NOT NULL DEFAULT 70,        -- 0–100
    market_share    NUMERIC(5,2) NOT NULL DEFAULT 0.00,      -- 0.00–100.00 %
    reputation      SMALLINT     NOT NULL DEFAULT 50,        -- 0–100
    valuation       BIGINT       NOT NULL DEFAULT 0,

    -- Research
    research_points BIGINT       NOT NULL DEFAULT 0,
    rp_per_tick     SMALLINT     NOT NULL DEFAULT 1,

    -- Loan
    loan_balance    BIGINT       NOT NULL DEFAULT 0,
    loan_interest   NUMERIC(5,4) NOT NULL DEFAULT 0.0200,    -- 2 % per tick default

    -- State
    is_bankrupt     BOOLEAN      NOT NULL DEFAULT FALSE,
    bankrupt_ticks  SMALLINT     NOT NULL DEFAULT 0,         -- counts up to 2, then reset
    is_paused       BOOLEAN      NOT NULL DEFAULT FALSE,
    pause_until     TIMESTAMPTZ,
    pauses_used     SMALLINT     NOT NULL DEFAULT 0,

    -- Prestige
    prestige_level      SMALLINT NOT NULL DEFAULT 0,
    legacy_multiplier   NUMERIC(5,4) NOT NULL DEFAULT 1.0000, -- stacks +0.10 per hard reset
    soft_prestige_used  BOOLEAN  NOT NULL DEFAULT FALSE,      -- once per run

    -- Lifecycle
    tick_count      INT          NOT NULL DEFAULT 0,
    tutorial_done   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ                               -- soft-delete on hard prestige
);

CREATE INDEX idx_companies_player_active ON companies(player_id) WHERE deleted_at IS NULL;

-- -------------------------------------------------------
-- COMPANY MARKETS
-- A company can be present in multiple markets (via /expand).
-- The starting market is added on company creation.
-- -------------------------------------------------------
CREATE TABLE company_markets (
    id          BIGSERIAL PRIMARY KEY,
    company_id  BIGINT      NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    market      VARCHAR(32) NOT NULL,
    share       NUMERIC(5,2) NOT NULL DEFAULT 0.00,
    joined_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (company_id, market)
);

-- -------------------------------------------------------
-- TICKS
-- One row per processed tick per company.
-- Useful for history, /log, weekly snapshot.
-- -------------------------------------------------------
CREATE TABLE ticks (
    id              BIGSERIAL PRIMARY KEY,
    company_id      BIGINT      NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    tick_number     INT         NOT NULL,
    capital_before  BIGINT      NOT NULL,
    capital_after   BIGINT      NOT NULL,
    revenue         BIGINT      NOT NULL,
    burn            BIGINT      NOT NULL,
    interest_paid   BIGINT      NOT NULL DEFAULT 0,
    rp_gained       SMALLINT    NOT NULL DEFAULT 0,
    processed_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_ticks_company ON ticks(company_id, tick_number DESC);
