# CEO Sim

Ein Discord-Bot Wirtschaftssimulationsspiel. Spieler gründen Firmen, treffen Entscheidungen, sabotieren Konkurrenten und versuchen den Markt zu dominieren.

## Tech Stack
- **Backend:** Java + Spring Boot
- **Discord:** JDA / Discord4J
- **DB:** PostgreSQL + Flyway
- **Build:** Maven / Gradle

## Docs
- [Datenbankschema](docs/DATABASE.md)

## Setup

```bash
# PostgreSQL starten
docker run --name ceosim-db -e POSTGRES_DB=ceosim -e POSTGRES_USER=ceosim -e POSTGRES_PASSWORD=secret -p 5432:5432 -d postgres:16

# Flyway Migrations laufen automatisch beim Start
./mvnw spring-boot:run
```

## Projektstruktur

```
src/main/resources/db/migration/
  V1__init_core.sql                          # Players, Companies, KPIs
  V2__employees.sql                          # Mitarbeiter
  V3__projects_techtree.sql                  # Projekte & Tech-Tree
  V4__events_decisions.sql                   # Event-Log & Entscheidungen
  V5__pvp_alliances_market.sql               # PvP, Allianzen, Börse, Verträge
  V6__achievements_prestige_notifications.sql # Achievements, Prestige, Config
```
