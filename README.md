<div align="center">

# 🏢 OMIEXX

**Ein Discord Bot Wirtschaftssimulationsspiel**

Gründe eine Firma. Triff Entscheidungen. Sabotiere Konkurrenten. Dominiere den Markt.

[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-brightgreen?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=flat-square&logo=postgresql)](https://www.postgresql.org/)
[![JDA](https://img.shields.io/badge/JDA-5.0-7289DA?style=flat-square&logo=discord)](https://github.com/discord-jda/JDA)

</div>

---

## Was ist CEO Sim?

CEO Sim ist ein Discord-Bot-Spiel in dem Spieler Technologie-Startups gründen und gegeneinander antreten. Das Spiel läuft tickbasiert — alle 6 Stunden passiert etwas. Zwischen den Ticks trifft man Entscheidungen, stellt Mitarbeiter ein, startet Projekte und sabotiert die Konkurrenz.

Das Ziel: Als erstes $1M Valuation erreichen und prestige-resetten — immer stärker werden.

---

## Features

**Core Loop**
- Tick-System alle 6h mit zufälligen Events und Multiple-Choice-Entscheidungen
- 6 KPIs: Kapital, Umsatz/Tick, Burn Rate, Morale, Marktanteil, Reputation
- Insolvenz-Mechanik mit 2 Notbetriebs-Ticks bevor ein Reset folgt

**Märkte & NPCs**
- 5 Startmärkte: Consumer Tech, Enterprise SaaS, Fintech, E-Commerce, Cybersecurity
- 4 freischaltbare Märkte: AI/Deep Tech, Healthcare, Media & Gaming, Government
- NPC-Firmen mit Persönlichkeiten (aggressiv / konservativ / innovativ) die auto-wachsen

**Mitarbeiter**
- Hardcoded Name-Pools — keine API-Kosten
- 11 Rollen, einige markt- oder tech-tree-exklusiv
- Generationen-Mechanik: 1 Mitarbeiter überlebt einen Hard-Prestige-Reset

**Projekte**
- 30 hardcoded Projekte (6 pro Startmarkt)
- Risiko-Roll: Vollerfolg / Teilerfolg / Scheitern / Kritischer Fehler
- Projekte schalten Märkte, Rollen und Tech-Nodes frei

**Tech-Tree**
- 3 Säulen: Operations, R&D, People — je 5 Tiers
- Research Points passiv pro Tick, 1 Node pro 24h freischaltbar

**PvP**
- 4 Sabotage-Aktionen mit Backfire-Mechanik
- Deals und Lieferverträge zwischen Spielern
- Akquirierung von NPC-Firmen

**Prestige & Endgame**
- Hard Reset: +10% Legacy-Multiplikator dauerhaft, 1 Mitarbeiter kommt mit
- Soft Prestige: IPO oder Markt-Dominanz ohne Reset (einmalig pro Run)
- 35 permanente Achievements

---

## Commands

| Command | Beschreibung | Cooldown |
|---|---|---|
| `/start` | Firma gründen — Markt + Startertyp per Button wählen | Einmalig |
| `/report` | Company Dashboard mit allen KPIs | — |
| `/team` | Mitarbeiter-Übersicht mit Stats | — |
| `/market` | Valuation-Leaderboard (Spieler + NPCs) | — |
| `/log` | Letzte 10 Events mit KPI-Deltas | — |
| `/hire [rolle]` | Mitarbeiter einstellen | 3h |
| `/fire [name]` | Mitarbeiter feuern | 3h |
| `/invest [betrag]` | Kapital direkt in KPI pumpen | 4h |
| `/pr [positiv\|negativ]` | PR-Kampagne starten | 6h |
| `/project list` | Verfügbare Projekte des eigenen Markts | — |
| `/project start [id]` | Projekt starten | 24h |
| `/project status` | Aktives Projekt mit Fortschrittsbalken | — |
| `/project boost` | Projekt um 1 Tick beschleunigen (Morale -2) | 4h |
| `/project cancel` | Aktives Projekt abbrechen (50% Kosten verloren) | — |
| `/acquire [firma]` | NPC-Firma übernehmen | 8h |
| `/fundraise` | VC-Pitch (Outcome gewichtet nach KPIs) | 24h |
| `/expand [markt]` | Neuen Marktbereich betreten | 24h |
| `/research tree` | Tech-Tree anzeigen | — |
| `/research pick [node]` | Node freischalten | 24h |
| `/research status` | Research Points + nächste Nodes | — |
| `/sabotage @user [aktion]` | leak / hiring_war / fake_pr / infra | 6h |
| `/profile [firma]` | Öffentliches Firmenprofil | — |
| `/prestige` | Hard Reset oder Soft Prestige wählen | — |
| `/legacy` | Prestige-History und Achievements | — |
| `/achievements` | Alle Abzeichen anzeigen | — |
| `/pause` | Firma 48h einfrieren (kein Salary, keine Events) | 2× / Monat |
| `/notify [event]` | Bot-Pings an-/ausschalten | — |
| `/help` | Alle Commands mit Lock-Status | — |
| `/admin [aktion]` | tick_skip / reset / spawn / event | Admin only |

---

## Quickstart

### Voraussetzungen

- Java 21+
- Docker & Docker Compose
- Discord Bot Token

### 1. Repository klonen

```bash
git clone https://github.com/Fabsi07/OMIEXX.git
cd OMIEXX
```

### 2. Bot konfigurieren

```bash
cp .env.example .env
```

`.env` öffnen und mindestens diese Werte setzen:

```env
DISCORD_TOKEN=dein_bot_token
ADMIN_DISCORD_IDS=deine_discord_id
```

### 3. Starten

```bash
docker compose up -d
```

Flyway führt die Migrations automatisch durch. Nach wenigen Sekunden ist der Bot online und `/start` funktioniert.

### Lokal ohne Docker

```bash
# PostgreSQL starten
docker run --name ceosim-db \
  -e POSTGRES_DB=ceosim -e POSTGRES_USER=ceosim -e POSTGRES_PASSWORD=secret \
  -p 5432:5432 -d postgres:16

# Bot starten — Tick für Dev auf 1 Minute
export DISCORD_TOKEN=dein_token
export TICK_INTERVAL_MS=60000
./mvnw spring-boot:run
```

---

## Discord Bot erstellen

1. [Discord Developer Portal](https://discord.com/developers/applications) öffnen
2. **New Application** → Name vergeben → **Bot** → **Add Bot**
3. Token kopieren → in `.env` als `DISCORD_TOKEN` eintragen
4. **OAuth2 → URL Generator** → Scopes: `bot`, `applications.commands`
5. Bot Permissions: `Send Messages`, `Embed Links`, `Read Message History`
6. Generierten Link öffnen → Bot zum Server hinzufügen

---

## Projektstruktur

```
src/main/java/dev/ceosim/
├── config/
│   └── JdaConfig.java              JDA Bean + alle Slash Commands registriert
├── discord/
│   ├── command/                    23 Commands (einer pro Datei)
│   └── listener/
│       └── SlashCommandListener    Router für alle Commands
├── entity/                         12 JPA Entities
├── repository/                     Spring Data Repositories
├── service/
│   ├── TickService.java            @Scheduled Kern-Loop alle 6h
│   ├── CompanyService.java         Hire/Fire/Create Businesslogik
│   ├── NpcService.java             @PostConstruct Spawn + Tick-Wachstum
│   ├── PrestigeService.java        Hard Reset + Soft Prestige
│   ├── WeeklyScheduler.java        Sonntags Snapshots + Rangliste
│   ├── CooldownService.java        In-Memory Cooldowns
│   ├── EventService.java           Event-Log
│   ├── achievement/
│   │   ├── AchievementType.java    35 Achievements
│   │   └── AchievementService.java
│   ├── project/
│   │   ├── ProjectDefinition.java  30 Projekte hardcoded
│   │   └── ProjectService.java
│   └── techtree/
│       ├── TechNode.java           15 Nodes in 3 Säulen
│       └── TechTreeService.java
└── util/
    └── CeoEmbedBuilder.java        Embed-Factory, formatMoney(), progressBar()

src/main/resources/db/migration/
├── V1__init_core.sql               Players, Companies, KPIs, Ticks
├── V2__employees.sql               Employees, Generationen-Mechanik
├── V3__projects_techtree.sql       Projects, Tech Tree Nodes
├── V4__events_decisions.sql        Event Log, Tick Decisions
├── V5__pvp_alliances_market.sql    NPCs, Sabotage, Alliances, Loans
└── V6__achievements_prestige.sql   Achievements, Prestige, Notifications
```

---

## Umgebungsvariablen

| Variable | Beschreibung | Default |
|---|---|---|
| `DISCORD_TOKEN` | Bot Token aus dem Developer Portal | — |
| `DB_URL` | JDBC Connection String | `jdbc:postgresql://localhost:5432/ceosim` |
| `DB_USER` | Datenbank-User | `ceosim` |
| `DB_PASSWORD` | Datenbank-Passwort | `secret` |
| `ADMIN_DISCORD_IDS` | Komma-getrennte Discord-IDs | — |
| `TICK_INTERVAL_MS` | Tick-Intervall (6h = `21600000`) | `21600000` |
| `LEADERBOARD_CHANNEL_ID` | Channel für wöchentliche Rangliste | — |
| `EVENT_LOG_CHANNEL_ID` | Channel für server-weite Events | — |

---

## Architektur-Entscheidungen

**Hardcoded Spielinhalte**
Projektdefinitionen, Tech-Tree-Nodes, Event-Templates und Mitarbeiter-Namen sind Java-Enums — keine DB-Queries, keine API-Kosten, kein Admin-Interface nötig. Die DB speichert nur States und History.

**Geld in Cents**
Alle Geldwerte als `BIGINT` in Cents. Kein Floating-Point-Fehler, saubere Darstellung via `CeoEmbedBuilder.formatMoney()`.

**Soft-Delete für Companies**
Hard-Prestige-Resets setzen `deleted_at` statt Daten zu löschen. Alle historischen Daten bleiben für `/legacy` erhalten.

**In-Memory Cooldowns**
`CooldownService` nutzt eine `ConcurrentHashMap`. Reicht für Single-Instance. Bei Multi-Instance durch Redis ersetzen.

---

## Tech Stack

| | |
|---|---|
| Sprache | Java 21 |
| Framework | Spring Boot 3.3 |
| Discord | JDA 5 |
| Datenbank | PostgreSQL 16 |
| Migrations | Flyway |
| ORM | Spring Data JPA / Hibernate |
| Build | Maven |
| Container | Docker / Docker Compose |

---

## Roadmap

- [ ] Allianzen zwischen Spielern (`/alliance`)
- [ ] Aktienmarkt — Anteile an Spieler-Firmen kaufen
- [ ] Lieferverträge (`/contract`)
- [ ] Schwarzmarkt-Commands
- [ ] Persistente Cooldowns via Redis
- [ ] Event-Log Channel — automatische Posts bei Insolvenz, Prestige etc.
- [ ] Notification-System — DM-Pings für wichtige Events
- [ ] Balance-Tuning nach erstem Playtest

---

<div align="center">

Made with ☕ — Contributions welcome

</div>
