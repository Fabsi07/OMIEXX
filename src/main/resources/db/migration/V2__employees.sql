-- ============================================================
-- CEO SIM — V2: Employees
-- ============================================================

CREATE TABLE employees (
    id              BIGSERIAL PRIMARY KEY,
    company_id      BIGINT      NOT NULL REFERENCES companies(id) ON DELETE CASCADE,

    -- Identity (hardcoded name pools in Java)
    first_name      VARCHAR(32) NOT NULL,
    last_name       VARCHAR(32) NOT NULL,

    -- Role
    role            VARCHAR(32) NOT NULL,
    -- cto | cfo | dev | designer | marketing | sales | hr |
    -- compliance_officer | pen_tester | malware_analyst | soc_analyst

    -- Stats (rolled on spawn)
    skill           SMALLINT    NOT NULL CHECK (skill BETWEEN 1 AND 10),
    loyalty         SMALLINT    NOT NULL CHECK (loyalty BETWEEN 1 AND 10),
    salary_per_tick BIGINT      NOT NULL,   -- in cents

    -- State
    is_active       BOOLEAN     NOT NULL DEFAULT TRUE,
    hired_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    fired_at        TIMESTAMPTZ,            -- set when fired or resigned
    fire_reason     VARCHAR(16)             -- fired | resigned | generation (kept through prestige)

);

CREATE INDEX idx_employees_company_active ON employees(company_id) WHERE is_active = TRUE;

-- -------------------------------------------------------
-- GENERATION CARRY-OVER
-- When a hard prestige happens, one employee can be kept.
-- We store their original employee_id for /legacy display.
-- -------------------------------------------------------
CREATE TABLE generation_employees (
    id                  BIGSERIAL PRIMARY KEY,
    original_company_id BIGINT      NOT NULL REFERENCES companies(id),
    new_company_id      BIGINT      NOT NULL REFERENCES companies(id),
    employee_id         BIGINT      NOT NULL REFERENCES employees(id),
    carried_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
