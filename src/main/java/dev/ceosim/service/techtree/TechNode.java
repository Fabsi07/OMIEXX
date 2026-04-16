package dev.ceosim.service.techtree;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * Alle Tech-Tree Nodes — 3 Säulen, je 5 Nodes.
 * Voraussetzungen sind andere Node-Keys.
 */
@Getter
public enum TechNode {

    // ════════════════════════════════════════════
    // SÄULE 1: OPERATIONS
    // Ziel: Burn Rate senken, Output/Tick steigern
    // ════════════════════════════════════════════

    OPS_PROCESS_AUTOMATION(
        "ops_process_automation",
        "Process Automation",
        "Wiederholende Aufgaben automatisieren. Burn Rate −$100/Tick.",
        Pillar.OPERATIONS,
        1,
        200_000L, 10,
        List.of(),
        NodeEffect.BURN_RATE_REDUCTION, -10_000L
    ),
    OPS_LEAN_OPERATIONS(
        "ops_lean_operations",
        "Lean Operations",
        "Schlanke Prozesse einführen. Burn Rate −$200/Tick, Output +5%.",
        Pillar.OPERATIONS,
        2,
        400_000L, 25,
        List.of("ops_process_automation"),
        NodeEffect.BURN_RATE_REDUCTION, -20_000L
    ),
    OPS_CLOUD_INFRASTRUCTURE(
        "ops_cloud_infrastructure",
        "Cloud Infrastructure",
        "Migration auf skalierbare Cloud-Architektur. Revenue/Tick +$500.",
        Pillar.OPERATIONS,
        3,
        800_000L, 50,
        List.of("ops_lean_operations"),
        NodeEffect.REVENUE_BONUS, 50_000L
    ),
    OPS_DEVOPS_PIPELINE(
        "ops_devops_pipeline",
        "DevOps Pipeline",
        "CI/CD + automatisiertes Testing. Projekt-Dauer −1 Tick (alle Projekte).",
        Pillar.OPERATIONS,
        4,
        1_200_000L, 80,
        List.of("ops_cloud_infrastructure"),
        NodeEffect.PROJECT_SPEED, -1L
    ),
    OPS_AI_OPERATIONS(
        "ops_ai_operations",
        "AI-Driven Operations",
        "KI übernimmt operative Entscheidungen. Burn Rate −$500/Tick, RP/Tick +2.",
        Pillar.OPERATIONS,
        5,
        2_500_000L, 150,
        List.of("ops_devops_pipeline"),
        NodeEffect.BURN_RATE_REDUCTION, -50_000L
    ),

    // ════════════════════════════════════════════
    // SÄULE 2: R&D
    // Ziel: Neue Märkte, Projekte, Produkte
    // ════════════════════════════════════════════

    RD_RESEARCH_LAB(
        "rd_research_lab",
        "Research Lab",
        "Dediziertes Forschungslabor. RP/Tick +2.",
        Pillar.RESEARCH,
        1,
        300_000L, 15,
        List.of(),
        NodeEffect.RP_PER_TICK, 2L
    ),
    RD_PATENT_PORTFOLIO(
        "rd_patent_portfolio",
        "Patent Portfolio",
        "Patente anmelden. Reputation +10, Sabotage-Resistenz +10%.",
        Pillar.RESEARCH,
        2,
        600_000L, 40,
        List.of("rd_research_lab"),
        NodeEffect.REPUTATION, 10L
    ),
    RD_MARKET_INTELLIGENCE(
        "rd_market_intelligence",
        "Market Intelligence",
        "Marktforschung und Datenanalyse. /expand Eintrittspreis −20%, /acquire Preis −10%.",
        Pillar.RESEARCH,
        3,
        900_000L, 70,
        List.of("rd_patent_portfolio"),
        NodeEffect.EXPAND_DISCOUNT, 20L
    ),
    RD_ADVANCED_RESEARCH(
        "rd_advanced_research",
        "Advanced Research",
        "Deep-Tech Forschung. Schaltet Markt AI/Deep Tech frei. RP/Tick +3.",
        Pillar.RESEARCH,
        4,
        1_500_000L, 100,
        List.of("rd_market_intelligence"),
        NodeEffect.RP_PER_TICK, 3L
    ),
    RD_BREAKTHROUGH_INNOVATION(
        "rd_breakthrough_innovation",
        "Breakthrough Innovation",
        "Revolutionäres Produkt. Revenue/Tick +$2.000, Marktanteil +5%.",
        Pillar.RESEARCH,
        5,
        3_000_000L, 200,
        List.of("rd_advanced_research"),
        NodeEffect.REVENUE_BONUS, 200_000L
    ),

    // ════════════════════════════════════════════
    // SÄULE 3: PEOPLE
    // Ziel: Bessere Mitarbeiter, weniger Kündigung
    // ════════════════════════════════════════════

    PEOPLE_TALENT_ACQUISITION(
        "people_talent_acquisition",
        "Talent Acquisition",
        "Bessere Recruiting-Prozesse. Neue Mitarbeiter: Skill +1 beim Spawn.",
        Pillar.PEOPLE,
        1,
        250_000L, 10,
        List.of(),
        NodeEffect.HIRE_SKILL_BONUS, 1L
    ),
    PEOPLE_HR_DEPARTMENT(
        "people_hr_department",
        "HR Department",
        "Dedizierte HR-Abteilung. Schaltet HR-Rolle frei. Loyalität sinkt langsamer.",
        Pillar.PEOPLE,
        2,
        500_000L, 30,
        List.of("people_talent_acquisition"),
        NodeEffect.UNLOCK_ROLE, 0L // Rolle: HR
    ),
    PEOPLE_TRAINING_PROGRAM(
        "people_training_program",
        "Training Program",
        "Interne Weiterbildung. Alle Mitarbeiter: Skill +1 (einmalig).",
        Pillar.PEOPLE,
        3,
        800_000L, 60,
        List.of("people_hr_department"),
        NodeEffect.BOOST_ALL_SKILLS, 1L
    ),
    PEOPLE_EQUITY_PROGRAM(
        "people_equity_program",
        "Employee Equity Program",
        "Mitarbeiterbeteiligung. Loyalty aller aktiven MAs +2, Kündigungsrate −50%.",
        Pillar.PEOPLE,
        4,
        1_200_000L, 90,
        List.of("people_training_program"),
        NodeEffect.LOYALTY_BOOST, 2L
    ),
    PEOPLE_DREAM_TEAM(
        "people_dream_team",
        "Dream Team Culture",
        "Weltklasse-Unternehmenskultur. Morale-Cap auf 100, Morale-Regen +3/Tick.",
        Pillar.PEOPLE,
        5,
        2_000_000L, 130,
        List.of("people_equity_program"),
        NodeEffect.MORALE_CAP, 100L
    );

    // ── Felder ─────────────────────────────────────────────────────────

    private final String key;
    private final String name;
    private final String description;
    private final Pillar pillar;
    private final int    tier;           // 1–5
    private final long   cost;           // Cents
    private final int    rpCost;         // Research Points
    private final List<String> requires; // Keys von Voraussetzungen
    private final NodeEffect effect;
    private final long   effectValue;

    TechNode(String key, String name, String description, Pillar pillar, int tier,
             long cost, int rpCost, List<String> requires,
             NodeEffect effect, long effectValue) {
        this.key = key;
        this.name = name;
        this.description = description;
        this.pillar = pillar;
        this.tier = tier;
        this.cost = cost;
        this.rpCost = rpCost;
        this.requires = requires;
        this.effect = effect;
        this.effectValue = effectValue;
    }

    public enum Pillar {
        OPERATIONS("⚙️ Operations"),
        RESEARCH("🔬 R&D"),
        PEOPLE("👥 People");

        private final String label;
        Pillar(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    public enum NodeEffect {
        BURN_RATE_REDUCTION,
        REVENUE_BONUS,
        RP_PER_TICK,
        PROJECT_SPEED,
        REPUTATION,
        EXPAND_DISCOUNT,
        UNLOCK_ROLE,
        HIRE_SKILL_BONUS,
        BOOST_ALL_SKILLS,
        LOYALTY_BOOST,
        MORALE_CAP
    }

    public static TechNode byKey(String key) {
        return Arrays.stream(values())
                .filter(n -> n.key.equals(key))
                .findFirst()
                .orElse(null);
    }

    public static List<TechNode> byPillar(Pillar pillar) {
        return Arrays.stream(values())
                .filter(n -> n.pillar == pillar)
                .sorted(java.util.Comparator.comparingInt(TechNode::getTier))
                .toList();
    }
}
