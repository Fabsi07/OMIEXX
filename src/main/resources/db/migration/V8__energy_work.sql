-- ============================================================
-- V8: Energy System + Work Sessions
-- ============================================================

-- Energie pro Firma
CREATE TABLE player_energy (
    id              BIGSERIAL PRIMARY KEY,
    company_id      BIGINT      NOT NULL UNIQUE REFERENCES companies(id) ON DELETE CASCADE,
    current         SMALLINT    NOT NULL DEFAULT 5 CHECK (current >= 0),
    max_energy      SMALLINT    NOT NULL DEFAULT 5,
    last_regen      TIMESTAMPTZ NOT NULL DEFAULT now(),
    total_sessions  INT         NOT NULL DEFAULT 0,
    work_streak     INT         NOT NULL DEFAULT 0,      -- Tage in Folge mind. 1x /work
    last_work_date  DATE,                                -- für Streak-Check
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Log aller Work-Sessions (für Stats und Jackpot-History)
CREATE TABLE work_sessions (
    id              BIGSERIAL PRIMARY KEY,
    company_id      BIGINT      NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    scenario_key    VARCHAR(64) NOT NULL,
    chosen_option   SMALLINT    NOT NULL,                -- 0, 1, 2
    outcome_tier    VARCHAR(16) NOT NULL,                -- normal|good|great|jackpot|bad|crisis
    capital_gained  BIGINT      NOT NULL DEFAULT 0,
    rp_gained       SMALLINT    NOT NULL DEFAULT 0,
    morale_delta    SMALLINT    NOT NULL DEFAULT 0,
    played_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_work_sessions_company ON work_sessions(company_id, played_at DESC);

-- Alle bestehenden Companies bekommen Energie-Eintrag
INSERT INTO player_energy (company_id, current, max_energy, last_regen)
SELECT id, 5, 5, now()
FROM companies
WHERE deleted_at IS NULL
ON CONFLICT DO NOTHING;
