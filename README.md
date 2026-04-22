<div align="center">

# 🏢 OMNIEXX

**Discord Wirtschaftssimulation**

Gründe ein Startup. Triff Entscheidungen. Sabotiere Konkurrenten. Dominiere den Markt.

[![Discord](https://img.shields.io/badge/Discord-Add_to_Server-5865F2?style=for-the-badge&logo=discord&logoColor=white)](https://discord.com/api/oauth2/authorize?client_id=DEINE_CLIENT_ID&permissions=277025392640&scope=bot%20applications.commands)

[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-brightgreen?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=flat-square&logo=postgresql)](https://www.postgresql.org/)
[![JDA](https://img.shields.io/badge/JDA-5.0-7289DA?style=flat-square&logo=discord)](https://github.com/discord-jda/JDA)

[**→ Bot zum Server hinzufügen**](https://discord.com/api/oauth2/authorize?client_id=DEINE_CLIENT_ID&permissions=277025392640&scope=bot%20applications.commands) · [Website](https://fabsi07.github.io/OMIEXX/) · [Support Server](#)

</div>

---

## Was ist OMNIEXX?

OMNIEXX ist ein tickbasiertes Wirtschaftssimulationsspiel direkt in Discord. Alle 6 Stunden läuft ein Tick — Events passieren, Entscheidungen müssen getroffen werden, Konkurrenten sabotieren sich gegenseitig.

Baue ein Startup auf, stelle ein Team ein, forsche im Tech-Tree, akquiriere NPC-Firmen und prestige-resette für immer stärkere Runs. Alles per Slash Commands direkt im Server.

---

## Features

**Work-System**
Jede Entscheidung zählt. Mit `/work` bekommst du ein zufälliges Business-Szenario mit 2–3 Optionen. Das Ergebnis ist variabel — manchmal ein Jackpot, manchmal ein Rückschlag. Aktives Spielen macht deine Firma deutlich stärker als passives Warten. 90-Minuten-Cooldown.

**Startup-Simulation**
Gründe eine Firma in einem von 5 Märkten, wähle deinen Startertyp und manage 6 KPIs: Kapital, Umsatz/Tick, Burn Rate, Morale, Marktanteil und Reputation.

**Echte Entscheidungen**
Alle 6 Stunden passiert ein Tick mit zufälligen Events. Investiere, starte PR-Kampagnen, reagiere auf Krisen. Jede Entscheidung hat messbare Konsequenzen.

**Team aufbauen**
11 Rollen mit einzigartigen Namen, gewürfelten Stats und festem Gehalt pro Tick. Mitarbeiter können kündigen wenn die Morale zu niedrig ist.

**30 Projekte**
6 pro Markt, hardcoded mit echten Kosten, Dauer und Risiko-Roll. Projekte schalten Märkte, Rollen und Tech-Nodes frei.

**Tech-Tree**
3 Säulen (Operations, R&D, People) mit je 5 Tiers. Research Points passiv generiert, 1 Node pro 24h freischaltbar.

**NPC-Welt**
Firmen mit 3 Persönlichkeiten wachsen automatisch, reagieren auf den Markt und können akquiriert werden.

**PvP & Sabotage**
4 Sabotage-Aktionen (Leak, Hiring War, Fake PR, Infra-Angriff) mit Backfire-Mechanik.

**Prestige-System**
Hard Reset mit dauerhaftem Legacy-Multiplikator (+10% pro Run) oder Soft Prestige für eine einmalige Mega-Aktion wie einen IPO.

---

## Commands

| Command | Was es macht |
|---|---|
| `/start` | Firma gründen — Markt und Startertyp per Button wählen |
| `/report` | Company Dashboard mit allen KPIs und Trends |
| `/team` | Mitarbeiter-Übersicht mit Stats und Gehalt |
| `/hire [rolle]` | Mitarbeiter einstellen |
| `/fire [name]` | Mitarbeiter feuern |
| `/project list` | Verfügbare Projekte für deinen Markt |
| `/project start [id]` | Projekt starten |
| `/project status` | Aktives Projekt mit Fortschrittsbalken |
| `/project boost` | Projekt beschleunigen (Morale −2) |
| `/research tree` | Tech-Tree anzeigen |
| `/research pick [node]` | Node freischalten |
| `/acquire [firma]` | NPC-Firma übernehmen |
| `/fundraise` | VC-Pitch starten |
| `/expand [markt]` | Neuen Markt betreten |
| `/sabotage @user [aktion]` | Konkurrenten sabotieren |
| `/pr [positiv\|negativ]` | PR-Kampagne starten |
| `/invest [betrag]` | Kapital in KPI pumpen |
| `/prestige` | Prestige-Flow starten (ab $1M Valuation) |
| `/legacy` | Prestige-History und Achievements |
| `/achievements` | Alle Abzeichen anzeigen |
| `/market` | Server-Leaderboard nach Valuation |
| `/profile [firma]` | Öffentliches Firmenprofil |
| `/work` | Work-Session starten — Szenario mit 2–3 Entscheidungen, variable Belohnung (90min CD) |
| `/crunch` | Intensiv arbeiten — doppeltes Risiko, doppelte Belohnung (60min CD) |
| `/log` | Letzte Events deiner Firma |
| `/pause` | Firma 48h einfrieren (2× / Monat) |
| `/help` | Alle Commands mit Status |

---

## Bot zum Server hinzufügen

**[→ Hier klicken um OMNIEXX einzuladen](https://discord.com/api/oauth2/authorize?client_id=DEINE_CLIENT_ID&permissions=277025392640&scope=bot%20applications.commands)**

Der Bot benötigt folgende Berechtigungen:
- `Send Messages` — Antworten in Channels
- `Embed Links` — Discord Embeds für Dashboards
- `Read Message History` — Slash Command Context
- `Use Application Commands` — Slash Commands

Nach dem Hinzufügen einfach `/start` in einem Channel eingeben — der Bot führt durch den Onboarding-Flow.

---

## Benötigte Channel-Konfiguration (optional)

Für den vollen Funktionsumfang können zwei dedizierte Channels eingerichtet werden:

| Channel | Zweck | Einrichten |
|---|---|---|
| `#omniexx-events` | Automatischer Event-Log (Insolvenzen, Prestige, Sabotage) | Channel-ID per `/admin eventlog [channel]` setzen |
| `#omniexx-ranking` | Wöchentliches Leaderboard (automatisch jeden Sonntag) | Channel-ID per `/admin leaderboard [channel]` setzen |

Beide sind optional — der Bot funktioniert auch ohne sie.

---

## Märkte

| Startmarkt | Beschreibung |
|---|---|
| 📱 Consumer Tech | Mobile Apps, Subscription-Produkte, B2C |
| 💼 Enterprise SaaS | B2B Software, SOC 2, Enterprise Sales |
| 💳 Fintech | Banking, Payments, Crypto, Compliance |
| 🛒 E-Commerce | Fulfillment, Marketplace, Logistik |
| 🔐 Cybersecurity | Pen Testing, SOC, Zero Trust, Gov-Zertifizierung |

Freischaltbar durch Projekte und `/expand`:
`🤖 AI/Deep Tech` · `🏥 Healthcare` · `🎮 Media & Gaming` · `🏛️ Government`

---

## Startertypen

| Typ | Startkapital | Besonderheit |
|---|---|---|
| 💰 Bootstrapper | $20.000 | Solides Startkapital, stabiles Wachstum |
| 🔬 Visionär | $5.000 | Mehr Research Points/Tick von Anfang an |
| 🤝 Networker | $10.000 | Direkt 2 kostenlose Mitarbeiter beim Start |

---

<div align="center">

Made with ☕ · [Website](https://fabsi07.github.io/OMIEXX/) · [Issues melden](https://github.com/Fabsi07/OMIEXX/issues)

</div>
