# OMNIEXX — Self-Hosting Guide

> **Hinweis:** OMNIEXX wird offiziell als gehosteter Bot betrieben.  
> Diese Anleitung ist für Entwickler die den Bot selbst betreiben möchten.

---

## Voraussetzungen

- Java 21+
- Docker & Docker Compose
- Discord Bot Token ([Anleitung](#discord-bot-erstellen))
- PostgreSQL 16

---

## Setup

### 1. Repository klonen

```bash
git clone https://github.com/Fabsi07/OMIEXX.git
cd OMIEXX
```

### 2. Konfigurieren

```bash
cp .env.example .env
```

`.env` bearbeiten:

```env
DISCORD_TOKEN=dein_bot_token
ADMIN_DISCORD_IDS=deine_discord_id

# Optional
LEADERBOARD_CHANNEL_ID=
EVENT_LOG_CHANNEL_ID=
```

### 3. Starten

```bash
docker compose up -d
```

Flyway führt die Migrations automatisch durch. Bot ist nach ~10 Sekunden online.

---

## Discord Bot erstellen

1. [Discord Developer Portal](https://discord.com/developers/applications) öffnen
2. **New Application** → Name → **Bot** → **Add Bot**
3. Token kopieren → in `.env` als `DISCORD_TOKEN`
4. **OAuth2 → URL Generator** → Scopes: `bot`, `applications.commands`
5. Permissions: `Send Messages`, `Embed Links`, `Read Message History`
6. URL öffnen → Bot zu Server hinzufügen

---

## Lokal ohne Docker

```bash
# PostgreSQL
docker run --name omniexx-db \
  -e POSTGRES_DB=omniexx -e POSTGRES_USER=omniexx -e POSTGRES_PASSWORD=secret \
  -p 5432:5432 -d postgres:16

# Dev-Mode: Tick jede Minute
export DISCORD_TOKEN=dein_token
export TICK_INTERVAL_MS=60000
mvn spring-boot:run
```

---

## Umgebungsvariablen

| Variable | Beschreibung | Default |
|---|---|---|
| `DISCORD_TOKEN` | Bot Token | — |
| `DB_URL` | JDBC URL | `jdbc:postgresql://localhost:5432/omniexx` |
| `DB_USER` | DB-User | `omniexx` |
| `DB_PASSWORD` | DB-Passwort | `secret` |
| `ADMIN_DISCORD_IDS` | Komma-getrennte Discord-IDs | — |
| `TICK_INTERVAL_MS` | Tick-Intervall in ms | `21600000` (6h) |
| `LEADERBOARD_CHANNEL_ID` | Channel für wöchentl. Ranking | — |
| `EVENT_LOG_CHANNEL_ID` | Channel für Server-Events | — |

---

## Datenbankschema

Vollständige Dokumentation: [docs/DATABASE.md](docs/DATABASE.md)

Migrations laufen automatisch via Flyway beim Start.
