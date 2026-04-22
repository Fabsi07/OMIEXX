-- ============================================================
-- V8: Work-System & Work-Session Tracking
-- ============================================================

CREATE TABLE work_sessions (
    id              BIGSERIAL PRIMARY KEY,
    company_id      BIGINT       NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    scenario_key    VARCHAR(64)  NOT NULL,
    option_chosen   VARCHAR(4)   NOT NULL,   -- a / b / c
    outcome_tier    VARCHAR(16)  NOT NULL,   -- jackpot / great / good / normal / bad / critical
    capital_gained  BIGINT       NOT NULL DEFAULT 0,
    rp_gained       SMALLINT     NOT NULL DEFAULT 0,
    morale_delta    SMALLINT     NOT NULL DEFAULT 0,
    streak_count    INT          NOT NULL DEFAULT 0,   -- Aktuelle Streak zum Zeitpunkt
    executed_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_work_company ON work_sessions(company_id, executed_at DESC);

-- Work-Streak pro Spieler (tagesbasiert)
ALTER TABLE companies
    ADD COLUMN IF NOT EXISTS work_streak       INT     NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS work_streak_date  DATE,
    ADD COLUMN IF NOT EXISTS total_work_count  INT     NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_work_at      TIMESTAMPTZ;
