-- ============================================================
-- CEO SIM — V4: Events & Decisions (the /log data)
-- ============================================================

-- -------------------------------------------------------
-- COMPANY EVENTS
-- Every meaningful thing that happens to a company.
-- Used for /log and end-of-run summary.
-- -------------------------------------------------------
CREATE TABLE company_events (
    id              BIGSERIAL PRIMARY KEY,
    company_id      BIGINT      NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    tick_number     INT         NOT NULL,
    event_type      VARCHAR(32) NOT NULL,
    -- tick_processed | decision_made | employee_hired | employee_resigned |
    -- employee_fired | project_started | project_completed | project_failed |
    -- sabotage_received | sabotage_sent | sabotage_backfire |
    -- pr_campaign | loan_taken | loan_repaid | acquisition | expansion |
    -- fundraise_success | fundraise_failed | insolvency_warning | prestige |
    -- tech_node_unlocked | contract_signed | contract_broken | market_event

    title           VARCHAR(128) NOT NULL,
    description     TEXT,
    -- JSON blob for delta KPIs shown in embed: {"capital": -5000, "morale": -10}
    kpi_delta       JSONB,
    occurred_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_events_company ON company_events(company_id, occurred_at DESC);

-- -------------------------------------------------------
-- DECISIONS
-- Tick events that require a player choice (buttons).
-- -------------------------------------------------------
CREATE TABLE decisions (
    id              BIGSERIAL PRIMARY KEY,
    company_id      BIGINT      NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    tick_number     INT         NOT NULL,
    event_key       VARCHAR(64) NOT NULL,   -- hardcoded event template key

    -- Discord message where buttons are shown
    discord_msg_id  VARCHAR(32),
    discord_ch_id   VARCHAR(32),

    -- Choices (stored as JSON array of option objects)
    options         JSONB       NOT NULL,   -- [{id, label, description}]
    chosen_option   VARCHAR(32),            -- NULL until answered
    is_expired      BOOLEAN     NOT NULL DEFAULT FALSE,

    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    answered_at     TIMESTAMPTZ
);

CREATE INDEX idx_decisions_company_open
    ON decisions(company_id)
    WHERE chosen_option IS NULL AND is_expired = FALSE;
