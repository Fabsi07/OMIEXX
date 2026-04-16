-- ============================================================
-- CEO SIM — V5: PvP, Alliances, Stock Market, Contracts
-- ============================================================

-- -------------------------------------------------------
-- NPC COMPANIES
-- Spawned at server startup. Grow automatically each tick.
-- -------------------------------------------------------
CREATE TABLE npc_companies (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(64) NOT NULL UNIQUE,
    market          VARCHAR(32) NOT NULL,
    personality     VARCHAR(16) NOT NULL,   -- aggressive | conservative | innovative
    capital         BIGINT      NOT NULL DEFAULT 0,
    market_share    NUMERIC(5,2) NOT NULL DEFAULT 0.00,
    valuation       BIGINT      NOT NULL DEFAULT 0,
    is_acquired     BOOLEAN     NOT NULL DEFAULT FALSE,
    acquired_by     BIGINT      REFERENCES companies(id),
    acquired_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- -------------------------------------------------------
-- SABOTAGE LOG
-- -------------------------------------------------------
CREATE TABLE sabotage_actions (
    id              BIGSERIAL PRIMARY KEY,
    attacker_id     BIGINT      NOT NULL REFERENCES companies(id),
    target_id       BIGINT      NOT NULL REFERENCES companies(id),
    action_type     VARCHAR(16) NOT NULL,   -- leak | hiring_war | fake_pr | infra
    was_backfire    BOOLEAN     NOT NULL DEFAULT FALSE,
    cost_paid       BIGINT      NOT NULL,
    executed_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_sabotage_target ON sabotage_actions(target_id, executed_at DESC);

-- -------------------------------------------------------
-- ALLIANCES
-- -------------------------------------------------------
CREATE TABLE alliances (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(64) NOT NULL UNIQUE,
    created_by  BIGINT      NOT NULL REFERENCES companies(id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    disbanded_at TIMESTAMPTZ
);

CREATE TABLE alliance_members (
    id          BIGSERIAL PRIMARY KEY,
    alliance_id BIGINT      NOT NULL REFERENCES alliances(id) ON DELETE CASCADE,
    company_id  BIGINT      NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    joined_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    left_at     TIMESTAMPTZ,
    UNIQUE (alliance_id, company_id)
);

-- Max 3 active members enforced in application layer.

-- -------------------------------------------------------
-- STOCK MARKET
-- Players can buy shares in other player companies.
-- -------------------------------------------------------
CREATE TABLE stock_holdings (
    id              BIGSERIAL PRIMARY KEY,
    owner_id        BIGINT      NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    target_id       BIGINT      NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    shares          INT         NOT NULL DEFAULT 0,     -- number of shares
    total_paid      BIGINT      NOT NULL DEFAULT 0,     -- in cents
    bought_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (owner_id, target_id)
);

-- -------------------------------------------------------
-- CONTRACTS (Lieferverträge)
-- -------------------------------------------------------
CREATE TABLE contracts (
    id              BIGSERIAL PRIMARY KEY,
    initiator_id    BIGINT      NOT NULL REFERENCES companies(id),
    counterpart_id  BIGINT      NOT NULL REFERENCES companies(id),
    description     TEXT        NOT NULL,
    duration_ticks  SMALLINT    NOT NULL,
    ticks_remaining SMALLINT    NOT NULL,
    status          VARCHAR(16) NOT NULL DEFAULT 'pending',
    -- pending | active | completed | broken | cancelled

    -- Penalty for breaking
    penalty_amount  BIGINT      NOT NULL DEFAULT 0,

    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    accepted_at     TIMESTAMPTZ,
    ended_at        TIMESTAMPTZ,
    broken_by       BIGINT      REFERENCES companies(id)
);

-- -------------------------------------------------------
-- LOANS
-- -------------------------------------------------------
CREATE TABLE loans (
    id              BIGSERIAL PRIMARY KEY,
    company_id      BIGINT      NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    principal       BIGINT      NOT NULL,
    interest_rate   NUMERIC(5,4) NOT NULL DEFAULT 0.0200,
    balance         BIGINT      NOT NULL,
    taken_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    repaid_at       TIMESTAMPTZ
);
