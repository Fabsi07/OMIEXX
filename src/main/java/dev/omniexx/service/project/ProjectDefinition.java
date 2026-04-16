package dev.omniexx.service.project;

import dev.omniexx.entity.Market;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Hardcoded Projektdefinitionen pro Markt.
 * Keine DB — alles in Java.
 */
@Getter
@Builder
public class ProjectDefinition {

    private final String  key;
    private final String  name;
    private final String  description;
    private final Market  market;
    private final int     durationTicks;     // Anzahl Ticks bis Abschluss
    private final long    cost;              // in Cents
    private final String  reward;            // Lesbare Beschreibung des Rewards
    private final long    revenueBonus;      // +X Revenue/Tick bei Vollerfolg
    private final int     reputationBonus;   // +X Reputation bei Vollerfolg
    private final String  unlocks;           // Was wird freigeschaltet? (lesbar)
    private final List<String> requirements; // Bedingungen (lesbar)

    // ─────────────────────────────────────────────────────────────
    // Alle Projekte — nach Markt gruppiert
    // ─────────────────────────────────────────────────────────────

    public static final Map<Market, List<ProjectDefinition>> ALL = Map.of(

        // ── CONSUMER TECH ────────────────────────────────────────────
        Market.CONSUMER_TECH, List.of(
            ProjectDefinition.builder()
                .key("ct_mvp_launch")
                .name("MVP Launch")
                .description("Baut und launcht ein minimales Produkt um erste Nutzer zu gewinnen.")
                .market(Market.CONSUMER_TECH)
                .durationTicks(2).cost(300_000L)
                .reward("+$500/Tick Revenue, Reputation +5")
                .revenueBonus(50_000L).reputationBonus(5)
                .unlocks("Zugang zu /fundraise")
                .requirements(List.of("Mindestens 1 Dev"))
                .build(),

            ProjectDefinition.builder()
                .key("ct_mobile_app")
                .name("Mobile App")
                .description("Native iOS/Android App entwickeln. Mehr Reichweite, mehr Revenue.")
                .market(Market.CONSUMER_TECH)
                .durationTicks(4).cost(800_000L)
                .reward("+$1.200/Tick Revenue, Marktanteil +2%")
                .revenueBonus(120_000L).reputationBonus(3)
                .unlocks("Markt: Media & Gaming")
                .requirements(List.of("Mindestens 2 Devs", "MVP Launch abgeschlossen"))
                .build(),

            ProjectDefinition.builder()
                .key("ct_viral_marketing")
                .name("Viral Marketing Kampagne")
                .description("Social-Media-Kampagne mit Influencern für explosives Nutzerwachstum.")
                .market(Market.CONSUMER_TECH)
                .durationTicks(3).cost(500_000L)
                .reward("Marktanteil +5%, Reputation +8")
                .revenueBonus(30_000L).reputationBonus(8)
                .unlocks("-")
                .requirements(List.of("Marketing-Mitarbeiter vorhanden"))
                .build(),

            ProjectDefinition.builder()
                .key("ct_subscription_model")
                .name("Subscription Umbau")
                .description("Produkt von einmaligem Kauf auf monatliches Abo umstellen.")
                .market(Market.CONSUMER_TECH)
                .durationTicks(3).cost(400_000L)
                .reward("+$800/Tick stabiler Revenue, weniger Volatilität")
                .revenueBonus(80_000L).reputationBonus(2)
                .unlocks("Fundraising-Chancen verbessert")
                .requirements(List.of("MVP Launch abgeschlossen"))
                .build(),

            ProjectDefinition.builder()
                .key("ct_ai_features")
                .name("KI-Features integrieren")
                .description("ML-Modelle in das Produkt einbauen für personalisierte UX.")
                .market(Market.CONSUMER_TECH)
                .durationTicks(5).cost(1_500_000L)
                .reward("+$2.000/Tick, Markt AI/Deep Tech freischalten")
                .revenueBonus(200_000L).reputationBonus(10)
                .unlocks("Markt: AI / Deep Tech")
                .requirements(List.of("CTO vorhanden", "Mindestens 3 Devs"))
                .build(),

            ProjectDefinition.builder()
                .key("ct_data_privacy")
                .name("DSGVO-Compliance")
                .description("Datenschutz-Infrastruktur aufbauen. Pflicht für EU-Expansion.")
                .market(Market.CONSUMER_TECH)
                .durationTicks(2).cost(250_000L)
                .reward("Reputation +6, Sabotage-Resistenz +10%")
                .revenueBonus(0L).reputationBonus(6)
                .unlocks("EU-Märkte erreichbar")
                .requirements(List.of())
                .build()
        ),

        // ── ENTERPRISE SAAS ──────────────────────────────────────────
        Market.ENTERPRISE_SAAS, List.of(
            ProjectDefinition.builder()
                .key("es_soc2_certification")
                .name("SOC 2 Zertifizierung")
                .description("Sicherheits- und Verfügbarkeits-Audit durch externe Prüfer.")
                .market(Market.ENTERPRISE_SAAS)
                .durationTicks(4).cost(700_000L)
                .reward("Reputation +12, Enterprise-Deals +30% Chancen")
                .revenueBonus(50_000L).reputationBonus(12)
                .unlocks("Markt: Government Contracts")
                .requirements(List.of("CFO vorhanden"))
                .build(),

            ProjectDefinition.builder()
                .key("es_enterprise_sales")
                .name("Enterprise Sales Team")
                .description("Dediziertes B2B-Sales-Team für Fortune-500-Deals aufbauen.")
                .market(Market.ENTERPRISE_SAAS)
                .durationTicks(3).cost(1_200_000L)
                .reward("+$3.000/Tick Revenue, Marktanteil +3%")
                .revenueBonus(300_000L).reputationBonus(5)
                .unlocks("Rolle: Compliance Officer")
                .requirements(List.of("Sales-Mitarbeiter vorhanden", "Mindestens $50k Kapital"))
                .build(),

            ProjectDefinition.builder()
                .key("es_api_marketplace")
                .name("API Marketplace")
                .description("Öffentliche API mit Drittanbieter-Ökosystem lancieren.")
                .market(Market.ENTERPRISE_SAAS)
                .durationTicks(5).cost(1_000_000L)
                .reward("+$1.500/Tick, Research Points/Tick +2")
                .revenueBonus(150_000L).reputationBonus(8)
                .unlocks("RP/Tick +2")
                .requirements(List.of("CTO vorhanden", "Mindestens 2 Devs"))
                .build(),

            ProjectDefinition.builder()
                .key("es_multi_tenant")
                .name("Multi-Tenant Architektur")
                .description("Produktarchitektur für parallele Enterprise-Kunden optimieren.")
                .market(Market.ENTERPRISE_SAAS)
                .durationTicks(4).cost(900_000L)
                .reward("Burn Rate −$200/Tick, Revenue +$1.000/Tick")
                .revenueBonus(100_000L).reputationBonus(3)
                .unlocks("Burn Rate −$200/Tick")
                .requirements(List.of("CTO vorhanden", "SOC 2 abgeschlossen"))
                .build(),

            ProjectDefinition.builder()
                .key("es_customer_success")
                .name("Customer Success Program")
                .description("Churn reduzieren durch proaktives Onboarding und Support.")
                .market(Market.ENTERPRISE_SAAS)
                .durationTicks(2).cost(400_000L)
                .reward("Revenue-Stabilität, Churn −30%, Reputation +4")
                .revenueBonus(40_000L).reputationBonus(4)
                .unlocks("-")
                .requirements(List.of())
                .build()
        ),

        // ── FINTECH ──────────────────────────────────────────────────
        Market.FINTECH, List.of(
            ProjectDefinition.builder()
                .key("ft_banking_license")
                .name("Banklizenz beantragen")
                .description("Regulatorische Zulassung für Einlagen- und Kreditgeschäft.")
                .market(Market.FINTECH)
                .durationTicks(8).cost(3_000_000L)
                .reward("+$5.000/Tick, Markt Healthcare zugänglich")
                .revenueBonus(500_000L).reputationBonus(15)
                .unlocks("Markt: Healthcare")
                .requirements(List.of("CFO vorhanden", "Reputation ≥ 60"))
                .build(),

            ProjectDefinition.builder()
                .key("ft_fraud_detection")
                .name("KI Fraud Detection")
                .description("ML-basiertes Betrugserkennungssystem für Transaktionen.")
                .market(Market.FINTECH)
                .durationTicks(4).cost(800_000L)
                .reward("Reputation +10, Sabotage-Resistenz +15%")
                .revenueBonus(60_000L).reputationBonus(10)
                .unlocks("Sabotage-Resistenz +15%")
                .requirements(List.of("CTO vorhanden", "Mindestens 2 Devs"))
                .build(),

            ProjectDefinition.builder()
                .key("ft_open_banking")
                .name("Open Banking API")
                .description("PSD2-konforme API für Drittanbieter-Integration.")
                .market(Market.FINTECH)
                .durationTicks(3).cost(600_000L)
                .reward("+$1.200/Tick, Partnerschaften möglich")
                .revenueBonus(120_000L).reputationBonus(6)
                .unlocks("Liefervertrag-Bonus mit Banken")
                .requirements(List.of("CTO vorhanden"))
                .build(),

            ProjectDefinition.builder()
                .key("ft_crypto_integration")
                .name("Crypto & Blockchain Integration")
                .description("DeFi-Features und Wallet-Support einbauen.")
                .market(Market.FINTECH)
                .durationTicks(4).cost(1_000_000L)
                .reward("+$800/Tick, hohes Risiko (Regulierung möglich)")
                .revenueBonus(80_000L).reputationBonus(0)
                .unlocks("Markt: AI / Deep Tech")
                .requirements(List.of("CTO vorhanden", "Mindestens 2 Devs"))
                .build(),

            ProjectDefinition.builder()
                .key("ft_compliance_framework")
                .name("Compliance Framework")
                .description("BAFIN/SEC-konformes Reporting und Audit-Trail aufbauen.")
                .market(Market.FINTECH)
                .durationTicks(3).cost(500_000L)
                .reward("Reputation +8, Regulierungs-Events −50%")
                .revenueBonus(0L).reputationBonus(8)
                .unlocks("Regulierungs-Events reduziert")
                .requirements(List.of("CFO vorhanden"))
                .build()
        ),

        // ── E-COMMERCE ───────────────────────────────────────────────
        Market.E_COMMERCE, List.of(
            ProjectDefinition.builder()
                .key("ec_fulfillment_center")
                .name("Fulfillment Center")
                .description("Eigenes Lager und Logistik aufbauen für schnellere Lieferung.")
                .market(Market.E_COMMERCE)
                .durationTicks(5).cost(2_000_000L)
                .reward("+$2.000/Tick, Marktanteil +4%")
                .revenueBonus(200_000L).reputationBonus(6)
                .unlocks("Same-Day Delivery möglich")
                .requirements(List.of("Mindestens $100k Kapital"))
                .build(),

            ProjectDefinition.builder()
                .key("ec_loyalty_program")
                .name("Loyalty Programm")
                .description("Punktesystem und Prämien für Stammkunden einführen.")
                .market(Market.E_COMMERCE)
                .durationTicks(2).cost(350_000L)
                .reward("Churn −20%, Revenue +$500/Tick")
                .revenueBonus(50_000L).reputationBonus(5)
                .unlocks("-")
                .requirements(List.of("Marketing-Mitarbeiter vorhanden"))
                .build(),

            ProjectDefinition.builder()
                .key("ec_same_day_delivery")
                .name("Same-Day Delivery")
                .description("Express-Lieferung innerhalb von 24h in Ballungsräumen.")
                .market(Market.E_COMMERCE)
                .durationTicks(4).cost(1_500_000L)
                .reward("Marktanteil +6%, Reputation +8")
                .revenueBonus(100_000L).reputationBonus(8)
                .unlocks("-")
                .requirements(List.of("Fulfillment Center abgeschlossen"))
                .build(),

            ProjectDefinition.builder()
                .key("ec_marketplace_expansion")
                .name("Marketplace Expansion")
                .description("Drittanbieter auf die Plattform lassen — Amazon-Style.")
                .market(Market.E_COMMERCE)
                .durationTicks(6).cost(2_500_000L)
                .reward("+$4.000/Tick, Markt Media & Gaming zugänglich")
                .revenueBonus(400_000L).reputationBonus(5)
                .unlocks("Markt: Media & Gaming")
                .requirements(List.of("Loyalty Programm abgeschlossen", "CTO vorhanden"))
                .build(),

            ProjectDefinition.builder()
                .key("ec_sustainability")
                .name("Nachhaltigkeits-Initiative")
                .description("CO2-neutraler Versand und Recycling-Programm.")
                .market(Market.E_COMMERCE)
                .durationTicks(2).cost(300_000L)
                .reward("Reputation +10, PR-Kampagnen günstiger")
                .revenueBonus(20_000L).reputationBonus(10)
                .unlocks("PR-Kosten −20%")
                .requirements(List.of())
                .build()
        ),

        // ── CYBERSECURITY ─────────────────────────────────────────────
        Market.CYBERSECURITY, List.of(
            ProjectDefinition.builder()
                .key("cs_zero_trust")
                .name("Zero Trust Architecture")
                .description("Interne und externe Netzwerke nach Zero-Trust-Prinzip absichern.")
                .market(Market.CYBERSECURITY)
                .durationTicks(4).cost(800_000L)
                .reward("Sabotage-Resistenz +25%, Reputation +8")
                .revenueBonus(40_000L).reputationBonus(8)
                .unlocks("Sabotage-Resistenz +25%")
                .requirements(List.of("CTO vorhanden", "Mindestens 1 Pen Tester"))
                .build(),

            ProjectDefinition.builder()
                .key("cs_soc_buildup")
                .name("SOC Aufbau")
                .description("Security Operations Center mit 24/7 Monitoring einrichten.")
                .market(Market.CYBERSECURITY)
                .durationTicks(5).cost(1_500_000L)
                .reward("+$3.000/Tick, SOC Analyst Rolle freischalten")
                .revenueBonus(300_000L).reputationBonus(12)
                .unlocks("Rolle: SOC Analyst")
                .requirements(List.of("Mindestens 2 Pen Tester oder Malware Analysten"))
                .build(),

            ProjectDefinition.builder()
                .key("cs_red_team_framework")
                .name("Red Team Framework")
                .description("Internes Angriffssimulations-Team aufbauen für proaktive Sicherheit.")
                .market(Market.CYBERSECURITY)
                .durationTicks(4).cost(1_000_000L)
                .reward("+$2.000/Tick, kann Konkurrenten-Infrastruktur scannen")
                .revenueBonus(200_000L).reputationBonus(6)
                .unlocks("Infra-Sabotage Kosten −30%")
                .requirements(List.of("SOC Aufbau abgeschlossen", "Mindestens 1 Malware Analyst"))
                .build(),

            ProjectDefinition.builder()
                .key("cs_gov_certification")
                .name("Regierungszertifizierung")
                .description("BSI/NSA-Zertifizierung für staatliche Aufträge erwerben.")
                .market(Market.CYBERSECURITY)
                .durationTicks(6).cost(2_000_000L)
                .reward("+$5.000/Tick, Markt Government Contracts freischalten")
                .revenueBonus(500_000L).reputationBonus(20)
                .unlocks("Markt: Government Contracts")
                .requirements(List.of("Zero Trust abgeschlossen", "SOC Aufbau abgeschlossen", "Reputation ≥ 70"))
                .build(),

            ProjectDefinition.builder()
                .key("cs_threat_intel")
                .name("Threat Intelligence Platform")
                .description("Eigene Datenbank mit Bedrohungsinformationen aufbauen und vermarkten.")
                .market(Market.CYBERSECURITY)
                .durationTicks(5).cost(1_200_000L)
                .reward("+$2.500/Tick, RP/Tick +3")
                .revenueBonus(250_000L).reputationBonus(8)
                .unlocks("RP/Tick +3")
                .requirements(List.of("Mindestens 1 Malware Analyst", "CTO vorhanden"))
                .build(),

            ProjectDefinition.builder()
                .key("cs_bug_bounty")
                .name("Bug Bounty Programm")
                .description("Community-basierte Sicherheitsforschung durch Prämien incentivieren.")
                .market(Market.CYBERSECURITY)
                .durationTicks(2).cost(200_000L)
                .reward("Reputation +7, Sabotage-Resistenz +10%")
                .revenueBonus(10_000L).reputationBonus(7)
                .unlocks("-")
                .requirements(List.of())
                .build()
        )
    );

    /** Gibt alle Projekte eines Markts zurück */
    public static List<ProjectDefinition> forMarket(Market market) {
        return ALL.getOrDefault(market, List.of());
    }

    /** Gibt ein einzelnes Projekt per Key zurück */
    public static ProjectDefinition byKey(String key) {
        return ALL.values().stream()
                .flatMap(List::stream)
                .filter(p -> p.getKey().equals(key))
                .findFirst()
                .orElse(null);
    }
}
