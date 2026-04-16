package dev.ceosim.service.achievement;

import lombok.Getter;

/**
 * Alle Achievement-Definitionen.
 * Check-Logik läuft im AchievementService nach jedem relevanten Event.
 */
@Getter
public enum AchievementType {

    // ── Meilensteine ──────────────────────────────────────────────
    FIRST_STEPS(       "🚀", "Erste Schritte",        "Erste Firma gegründet"),
    FIRST_HIRE(        "👤", "Erster Mitarbeiter",     "Ersten Mitarbeiter eingestellt"),
    TEAM_OF_5(         "👥", "Kleines Team",           "5 aktive Mitarbeiter gleichzeitig"),
    TEAM_OF_10(        "🏢", "Ernstzunehmendes Team",  "10 aktive Mitarbeiter gleichzeitig"),

    // ── Finanzen ──────────────────────────────────────────────────
    FIRST_100K(        "💵", "Erste $100k",            "$100k Kapital erreicht"),
    FIRST_1M(          "💰", "Millionär",              "$1M Kapital erreicht"),
    VALUATION_50K(     "📈", "Auf dem Weg",            "$50k Valuation erreicht"),
    VALUATION_500K(    "🚀", "Halbe Million",          "$500k Valuation erreicht"),
    VALUATION_1M(      "💎", "Der erste Meilenstein",  "$1M Valuation — Prestige verfügbar"),
    VALUATION_10M(     "🏆", "Unicorn in sight",       "$10M Valuation erreicht"),

    // ── Projekte ──────────────────────────────────────────────────
    FIRST_PROJECT(     "🔨", "Erstes Projekt",         "Erstes Projekt abgeschlossen"),
    PROJECT_STREAK_3(  "⚡", "Projekt-Enthusiast",     "3 Projekte ohne Fehlschlag"),
    PROJECT_FAIL(      "💥", "Lehrreiche Niederlage",  "Erstes Projekt gescheitert"),
    CRITICAL_FAIL(     "😱", "Murphy's Law",           "Kritischer Fehler bei einem Projekt"),

    // ── Akquisitionen ─────────────────────────────────────────────
    FIRST_ACQUIRE(     "🤝", "Erste Übernahme",        "Erste NPC-Firma akquiriert"),
    ACQUIRE_5(         "🌐", "Marktkonsolidierung",    "5 NPC-Firmen akquiriert"),
    KARTELL(           "⚖️", "Kartellverdacht",        "4+ Akquisitionen — Kartellbehörde aufmerksam"),

    // ── Fundraising ───────────────────────────────────────────────
    FIRST_VC(          "💼", "VC-Deal",                "Ersten VC-Deal abgeschlossen"),
    TIER_A_VC(         "🎯", "Tier-A Investor",        "Tier-A VC Deal abgeschlossen"),
    PITCH_REJECTED(    "📭", "Klassische Ablehnung",   "Ersten VC-Pitch abgelehnt bekommen"),

    // ── PvP ───────────────────────────────────────────────────────
    FIRST_SABOTAGE(    "🗡️", "Erste Sabotage",         "Ersten Sabotage-Angriff gestartet"),
    BACKFIRE(          "💥", "Backfire!",              "Sabotage ist auf einen selbst zurückgefallen"),
    SURVIVED_SABOTAGE( "🛡️", "Standhaft",              "Sabotage-Angriff überlebt"),

    // ── Insolvenz ─────────────────────────────────────────────────
    NEAR_DEATH(        "😰", "Brinkmanship",           "Aus dem Notbetrieb gerettet"),
    BANKRUPT(          "💀", "Aus der Asche",          "Insolvent — und neugestartet"),

    // ── Tech-Tree ─────────────────────────────────────────────────
    FIRST_NODE(        "🔬", "Forscher",               "Ersten Tech-Node freigeschaltet"),
    PILLAR_COMPLETE(   "🏛️", "Eine Säule komplett",    "Alle 5 Nodes einer Säule freigeschaltet"),
    ALL_NODES(         "🧠", "Tech-Visionär",          "Alle 15 Tech-Nodes freigeschaltet"),

    // ── Prestige ──────────────────────────────────────────────────
    FIRST_PRESTIGE(    "🌟", "Erste Legende",          "Erstes Prestige durchgeführt"),
    TRIPLE_PRESTIGE(   "✨", "Dreifach-Legende",       "3x Prestige durchgeführt"),
    SOFT_PRESTIGE(     "📊", "Börsengang!",            "Soft-Prestige (IPO/Takeover) abgeschlossen"),

    // ── Sonstige ──────────────────────────────────────────────────
    MARKET_EXPAND(     "🗺️", "Expansion",              "Ersten neuen Markt betreten"),
    MULTI_MARKET(      "🌍", "Weltkonzern",            "In 3+ Märkten gleichzeitig aktiv"),
    VETERAN(           "⭐", "Veteran",                "100 Ticks überlebt");

    private final String emoji;
    private final String name;
    private final String description;

    AchievementType(String emoji, String name, String description) {
        this.emoji = emoji;
        this.name = name;
        this.description = description;
    }
}
