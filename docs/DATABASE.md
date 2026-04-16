# CEO Sim — Database Schema

## Migrations (Flyway)

| File | Inhalt |
|------|--------|
| `V1__init_core.sql` | Players, Companies, KPIs, Company Markets, Ticks |
| `V2__employees.sql` | Employees, Generation Carry-over |
| `V3__projects_techtree.sql` | Company Projects, Tech Tree Nodes |
| `V4__events_decisions.sql` | Event Log, Tick Decisions |
| `V5__pvp_alliances_market.sql` | NPC Companies, Sabotage, Alliances, Stock Holdings, Contracts, Loans |
| `V6__achievements_prestige_notifications.sql` | Achievements, Prestige History, Notifications, Weekly Snapshots, Server Config |

---

## Kernprinzipien

### Geld immer in Cents (BIGINT)
Kein FLOAT, kein DECIMAL für Transaktionen. `100000` = $1.000,00.

### Soft-Delete für Companies
Beim Hard-Prestige-Reset wird die Company nicht gelöscht sondern `deleted_at` gesetzt. So bleiben alle historischen Daten für `/legacy` erhalten.

### Hardcoded Daten in Java
Folgendes existiert **nicht** in der DB — nur als Java-Enums/Records:
- Projekt-Definitionen (Kosten, Dauer, Reward, Risiko)
- Tech-Tree-Nodes (Voraussetzungen, Effekte)
- Event-Templates (Entscheidungs-Texte, KPI-Deltas)
- Mitarbeiter-Name-Pools
- Achievement-Definitionen

Die DB speichert nur **States und History** (was freigeschaltet wurde, was passiert ist).

### Markt-Repräsentation
```
companies.market         → Startmarkt (String-Key)
company_markets          → Alle aktiven Märkte inkl. Startmarkt (nach /expand)
npc_companies.market     → Markt des NPCs
```

---

## Wichtige Queries (Beispiele)

### Aktive Company eines Spielers
```sql
SELECT c.* FROM companies c
JOIN players p ON c.player_id = p.id
WHERE p.discord_id = '123456789'
  AND c.deleted_at IS NULL;
```

### Offene Entscheidungen
```sql
SELECT * FROM decisions
WHERE company_id = $1
  AND chosen_option IS NULL
  AND is_expired = FALSE;
```

### Letzte 10 Events für /log
```sql
SELECT * FROM company_events
WHERE company_id = $1
ORDER BY occurred_at DESC
LIMIT 10;
```

### Markt-Leaderboard
```sql
SELECT c.name, c.valuation, cm.market, cm.share
FROM companies c
JOIN company_markets cm ON c.id = cm.company_id
WHERE c.deleted_at IS NULL
  AND cm.market = $1
UNION ALL
SELECT n.name, n.valuation, n.market, n.market_share
FROM npc_companies n
WHERE n.market = $1 AND n.is_acquired = FALSE
ORDER BY valuation DESC;
```

### Wöchentliche Rangliste (Wachstum %)
```sql
SELECT 
    c.name,
    ws_now.valuation AS val_now,
    ws_prev.valuation AS val_prev,
    ROUND(((ws_now.valuation - ws_prev.valuation)::numeric / NULLIF(ws_prev.valuation, 0)) * 100, 2) AS growth_pct
FROM weekly_snapshots ws_now
JOIN weekly_snapshots ws_prev 
    ON ws_now.company_id = ws_prev.company_id
   AND ws_prev.week_number = ws_now.week_number - 1
   AND ws_prev.year = ws_now.year
JOIN companies c ON c.id = ws_now.company_id
WHERE ws_now.week_number = EXTRACT(WEEK FROM now())
  AND ws_now.year = EXTRACT(YEAR FROM now())
  AND c.deleted_at IS NULL
ORDER BY growth_pct DESC
LIMIT 10;
```
