-- ============================================================
-- CEO SIM — V3: Projects & Tech Tree
-- ============================================================

-- -------------------------------------------------------
-- PROJECTS
-- The project catalogue is hardcoded in Java (ProjectDefinition).
-- This table tracks only active/completed runs per company.
-- -------------------------------------------------------
CREATE TABLE company_projects (
    id              BIGSERIAL PRIMARY KEY,
    company_id      BIGINT      NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    project_key     VARCHAR(64) NOT NULL,   -- matches Java enum / hardcoded key
    market          VARCHAR(32) NOT NULL,   -- which market this project belongs to

    -- Progress
    status          VARCHAR(16) NOT NULL DEFAULT 'active',
    -- active | completed_full | completed_partial | failed | critical_fail | cancelled

    ticks_total     SMALLINT    NOT NULL,
    ticks_remaining SMALLINT    NOT NULL,
    boosts_used     SMALLINT    NOT NULL DEFAULT 0,

    -- Financials
    cost_paid       BIGINT      NOT NULL DEFAULT 0,

    -- Timestamps
    started_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at    TIMESTAMPTZ,

    -- Only one active project per company at a time
    CONSTRAINT one_active_project UNIQUE (company_id, status) DEFERRABLE INITIALLY DEFERRED
);

-- Remove the unique constraint trick — handle in application layer instead.
-- (Postgres UNIQUE with partial index is cleaner)
ALTER TABLE company_projects DROP CONSTRAINT one_active_project;

CREATE UNIQUE INDEX idx_one_active_project
    ON company_projects(company_id)
    WHERE status = 'active';

CREATE INDEX idx_projects_company ON company_projects(company_id, started_at DESC);

-- -------------------------------------------------------
-- TECH TREE NODES UNLOCKED
-- The full tree definition lives in Java (TechNode enum).
-- This table only stores which nodes a company has unlocked.
-- -------------------------------------------------------
CREATE TABLE company_tech_nodes (
    id          BIGSERIAL PRIMARY KEY,
    company_id  BIGINT      NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    node_key    VARCHAR(64) NOT NULL,    -- matches Java TechNode key
    pillar      VARCHAR(16) NOT NULL,    -- operations | research | people
    cost_paid   BIGINT      NOT NULL,
    rp_paid     BIGINT      NOT NULL,
    unlocked_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (company_id, node_key)
);
