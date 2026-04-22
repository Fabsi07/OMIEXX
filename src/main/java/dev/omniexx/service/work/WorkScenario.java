package dev.omniexx.service.work;

import dev.omniexx.entity.Market;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 40 hardcoded Work-Szenarien — bunt gemischt pro Markt und global.
 * Jedes Szenario hat 2–3 Optionen mit unterschiedlichen Risikoprofilen.
 * Kein DB-Eintrag — alles in Java.
 */
@Getter
@Builder
public class WorkScenario {

    private final String  key;
    private final String  title;
    private final String  description;
    private final Market  market;           // null = global (gilt für alle Märkte)
    private final List<WorkOption> options;

    @Getter
    @Builder
    public static class WorkOption {
        private final String label;             // Button-Text
        private final String flavor;            // Was passiert
        // Outcome-Gewichte (0–100), Summe muss nicht 100 sein — wird normalisiert
        private final int weightNormal;
        private final int weightGood;
        private final int weightGreat;
        private final int weightJackpot;
        private final int weightBad;
        private final int weightCrisis;
    }

    // ── Alle Szenarien ──────────────────────────────────────────────────

    public static final List<WorkScenario> ALL = buildAll();

    private static List<WorkScenario> buildAll() {
        List<WorkScenario> list = new ArrayList<>();

        // ── GLOBALE SZENARIEN (alle Märkte) ─────────────────────────────

        list.add(WorkScenario.builder().key("ws_big_client_call")
            .title("📞 Wichtiger Kundencall")
            .description("Ein potenzieller Großkunde will einen Call. Du hast 10 Minuten Zeit dich vorzubereiten.")
            .market(null)
            .options(List.of(
                WorkOption.builder().label("💼 Vollständig vorbereiten")
                    .flavor("Du überzeugst ihn. Neuer Vertrag unterzeichnet!")
                    .weightNormal(20).weightGood(45).weightGreat(25).weightJackpot(8).weightBad(2).weightCrisis(0).build(),
                WorkOption.builder().label("🎲 Improvisiern und authentisch sein")
                    .flavor("Geht manchmal besser, manchmal schlechter als erwartet.")
                    .weightNormal(30).weightGood(30).weightGreat(20).weightJackpot(10).weightBad(8).weightCrisis(2).build()
            )).build());

        list.add(WorkScenario.builder().key("ws_team_conflict")
            .title("😤 Teamkonflikt")
            .description("Zwei deiner besten Mitarbeiter streiten sich heftig. Das Büro brodelt.")
            .market(null)
            .options(List.of(
                WorkOption.builder().label("⚖️ Vermitteln und Kompromiss finden")
                    .flavor("Beide beruhigen sich. Kurzer Produktivitätsverlust aber langfristig besser.")
                    .weightNormal(40).weightGood(35).weightGreat(15).weightJackpot(2).weightBad(7).weightCrisis(1).build(),
                WorkOption.builder().label("🔇 Ignorieren — löst sich von selbst")
                    .flavor("Manchmal geht es weg. Manchmal eskaliert es.")
                    .weightNormal(25).weightGood(15).weightGreat(5).weightJackpot(0).weightBad(30).weightCrisis(25).build(),
                WorkOption.builder().label("🚪 Einen der beiden verwarnen")
                    .flavor("Klare Linie. Morale leidet kurz, aber es hört auf.")
                    .weightNormal(35).weightGood(30).weightGreat(10).weightJackpot(1).weightBad(20).weightCrisis(4).build()
            )).build());

        list.add(WorkScenario.builder().key("ws_server_outage")
            .title("🔥 Server-Ausfall")
            .description("Eure Infrastruktur ist seit 20 Minuten down. Kunden beschweren sich.")
            .market(null)
            .options(List.of(
                WorkOption.builder().label("⚡ Alles stehen lassen — sofort fixen")
                    .flavor("Das Team zieht durch. Schnelle Lösung, Reputation gerettet.")
                    .weightNormal(25).weightGood(45).weightGreat(20).weightJackpot(5).weightBad(5).weightCrisis(0).build(),
                WorkOption.builder().label("📋 Erst Ursache analysieren dann fixen")
                    .flavor("Dauert länger, löst aber das Problem nachhaltig.")
                    .weightNormal(40).weightGood(35).weightGreat(15).weightJackpot(3).weightBad(7).weightCrisis(0).build()
            )).build());

        list.add(WorkScenario.builder().key("ws_viral_moment")
            .title("🚀 Viraler Moment")
            .description("Euer Produkt wird gerade auf Twitter/X viral — 50k Mentions in 2 Stunden.")
            .market(null)
            .options(List.of(
                WorkOption.builder().label("📣 Sofort drauf reagieren und pushen")
                    .flavor("Ihr nutzt den Hype perfekt aus. Massiver Traffic!")
                    .weightNormal(10).weightGood(30).weightGreat(35).weightJackpot(20).weightBad(4).weightCrisis(1).build(),
                WorkOption.builder().label("🤐 Ruhig bleiben — wirkt dann echter")
                    .flavor("Authentizität kommt gut an, aber ihr verpast den Peak-Hype.")
                    .weightNormal(45).weightGood(35).weightGreat(15).weightJackpot(5).weightBad(0).weightCrisis(0).build()
            )).build());

        list.add(WorkScenario.builder().key("ws_competitor_leaves")
            .title("🏃 Konkurrent gibt auf")
            .description("Ein direkter Konkurrent meldet Insolvenz an. Sein Kundenstamm ist frei.")
            .market(null)
            .options(List.of(
                WorkOption.builder().label("🏃 Sofort aggressiv akquirieren")
                    .flavor("Ihr holt schnell neue Kunden — aber auch Kosten durch Onboarding.")
                    .weightNormal(20).weightGood(40).weightGreat(30).weightJackpot(8).weightBad(2).weightCrisis(0).build(),
                WorkOption.builder().label("🎯 Nur Premium-Kunden herausfiltern")
                    .flavor("Weniger Volumen aber bessere Qualität.")
                    .weightNormal(35).weightGood(40).weightGreat(20).weightJackpot(5).weightBad(0).weightCrisis(0).build()
            )).build());

        list.add(WorkScenario.builder().key("ws_investor_interest")
            .title("💰 Unerwartetes Investor-Interesse")
            .description("Ein Angel-Investor hat dich auf LinkedIn angeschrieben. Unbekannter Name, aber interessantes Profil.")
            .market(null)
            .options(List.of(
                WorkOption.builder().label("☕ Erstgespräch annehmen")
                    .flavor("Könnte eine Goldmine sein — oder Zeitverschwendung.")
                    .weightNormal(25).weightGood(30).weightGreat(25).weightJackpot(15).weightBad(5).weightCrisis(0).build(),
                WorkOption.builder().label("🚫 Fokus behalten — kein Meeting")
                    .flavor("Du sparst Zeit. Kein Upside, kein Downside.")
                    .weightNormal(70).weightGood(20).weightGreat(10).weightJackpot(0).weightBad(0).weightCrisis(0).build()
            )).build());

        list.add(WorkScenario.builder().key("ws_press_scandal")
            .title("📰 Schlechte Presse")
            .description("Ein Journalist hat einen kritischen Artikel über dein Produkt veröffentlicht.")
            .market(null)
            .options(List.of(
                WorkOption.builder().label("🤝 Offen und transparent antworten")
                    .flavor("Ehrlichkeit kommt gut an. Krise abgewendet.")
                    .weightNormal(30).weightGood(40).weightGreat(20).weightJackpot(5).weightBad(4).weightCrisis(1).build(),
                WorkOption.builder().label("⚖️ Rechtliche Schritte androhen")
                    .flavor("Riskant. Kann eskalieren oder abschrecken.")
                    .weightNormal(15).weightGood(15).weightGreat(10).weightJackpot(5).weightBad(25).weightCrisis(30).build(),
                WorkOption.builder().label("🔇 Nichts tun und warten")
                    .flavor("Manchmal zieht es vorbei. Manchmal nicht.")
                    .weightNormal(35).weightGood(15).weightGreat(5).weightJackpot(0).weightBad(25).weightCrisis(20).build()
            )).build());

        list.add(WorkScenario.builder().key("ws_late_night_sprint")
            .title("🌙 Late-Night Sprint")
            .description("Das Team schlägt vor über Nacht zu arbeiten um ein Feature fertigzustellen.")
            .market(null)
            .options(List.of(
                WorkOption.builder().label("🔥 Alle machen mit — volle Power")
                    .flavor("Feature ist fertig. Aber das Team ist erschöpft.")
                    .weightNormal(20).weightGood(35).weightGreat(30).weightJackpot(10).weightBad(5).weightCrisis(0).build(),
                WorkOption.builder().label("🛌 Nein — gute Arbeit braucht Schlaf")
                    .flavor("Team ist ausgeruht. Feature kommt morgen, aber dafür bug-frei.")
                    .weightNormal(50).weightGood(35).weightGreat(10).weightJackpot(2).weightBad(3).weightCrisis(0).build()
            )).build());

        list.add(WorkScenario.builder().key("ws_partnership_offer")
            .title("🤝 Partnerschaftsangebot")
            .description("Ein etabliertes Unternehmen will offiziell mit dir partnern. Bedingung: 3-jährige Exklusivität.")
            .market(null)
            .options(List.of(
                WorkOption.builder().label("✅ Deal annehmen")
                    .flavor("Sofortiger Umsatz-Boost, aber du bist 3 Jahre gebunden.")
                    .weightNormal(15).weightGood(40).weightGreat(35).weightJackpot(8).weightBad(2).weightCrisis(0).build(),
                WorkOption.builder().label("🔄 Gegenvorschlag — ohne Exklusivität")
                    .flavor("Riskant — sie könnten ablehnen oder bessere Konditionen akzeptieren.")
                    .weightNormal(20).weightGood(25).weightGreat(20).weightJackpot(10).weightBad(20).weightCrisis(5).build(),
                WorkOption.builder().label("❌ Ablehnen — Unabhängigkeit halten")
                    .flavor("Kurzer Schmerz, langfristige Freiheit.")
                    .weightNormal(60).weightGood(25).weightGreat(10).weightJackpot(3).weightBad(2).weightCrisis(0).build()
            )).build());

        list.add(WorkScenario.builder().key("ws_data_breach")
            .title("🔓 Sicherheitslücke entdeckt")
            .description("Euer Sicherheitsteam hat eine kritische Lücke gefunden. Noch ist nichts passiert.")
            .market(null)
            .options(List.of(
                WorkOption.builder().label("🚨 Sofort patchen und Nutzer informieren")
                    .flavor("Transparenz zahlt sich aus. Reputation steigt langfristig.")
                    .weightNormal(25).weightGood(45).weightGreat(25).weightJackpot(3).weightBad(2).weightCrisis(0).build(),
                WorkOption.builder().label("🤫 Leise fixen ohne Kommunikation")
                    .flavor("Wenn's rauskommt: katastrophal. Wenn nicht: keine PR-Krise.")
                    .weightNormal(30).weightGood(25).weightGreat(10).weightJackpot(0).weightBad(15).weightCrisis(20).build()
            )).build());

        list.add(WorkScenario.builder().key("ws_key_hire_negotiation")
            .title("🌟 Traum-Kandidat verhandelt")
            .description("Der beste Dev den du je interviewt hast will 40% mehr Gehalt als geplant.")
            .market(null)
            .options(List.of(
                WorkOption.builder().label("💸 Bezahl es — Top-Talent wert")
                    .flavor("Er kommt. Höhere Burn Rate, aber enormer Skill-Boost.")
                    .weightNormal(20).weightGood(45).weightGreat(28).weightJackpot(5).weightBad(2).weightCrisis(0).build(),
                WorkOption.builder().label("🤜 Gegenhalten bei 20% mehr")
                    .flavor("Er akzeptiert vielleicht — oder geht zur Konkurrenz.")
                    .weightNormal(30).weightGood(30).weightGreat(15).weightJackpot(3).weightBad(15).weightCrisis(7).build(),
                WorkOption.builder().label("👋 Danke sagen und weitersuchen")
                    .flavor("Budget bleibt sicher. Du findest jemand Anderen.")
                    .weightNormal(60).weightGood(25).weightGreat(10).weightJackpot(0).weightBad(5).weightCrisis(0).build()
            )).build());

        list.add(WorkScenario.builder().key("ws_pivot_opportunity")
            .title("🔄 Pivot-Chance")
            .description("Eure Nutzerdaten zeigen: ein komplett anderes Feature wird am häufigsten genutzt als geplant.")
            .market(null)
            .options(List.of(
                WorkOption.builder().label("🚀 Voll auf dieses Feature pivoten")
                    .flavor("High risk, high reward. Kann der Durchbruch sein.")
                    .weightNormal(15).weightGood(25).weightGreat(30).weightJackpot(20).weightBad(8).weightCrisis(2).build(),
                WorkOption.builder().label("🎯 Beide Features gleichzeitig entwickeln")
                    .flavor("Langsamer aber sicherer. Team wird dünner ausgebreitet.")
                    .weightNormal(40).weightGood(35).weightGreat(18).weightJackpot(4).weightBad(3).weightCrisis(0).build()
            )).build());

        list.add(WorkScenario.builder().key("ws_acquisition_approach")
            .title("🏢 Übernahme-Anfrage")
            .description("Ein großes Unternehmen fragt unverbindlich an ob du Interesse an einem Acquisition-Talk hast.")
            .market(null)
            .options(List.of(
                WorkOption.builder().label("📊 Gespräch führen — Infos sammeln")
                    .flavor("Keine Verpflichtung. Du erfährst was sie zahlen würden.")
                    .weightNormal(20).weightGood(40).weightGreat(30).weightJackpot(8).weightBad(2).weightCrisis(0).build(),
                WorkOption.builder().label("🚫 Sofort ablehnen — zu früh")
                    .flavor("Fokus bleibt. Kein Downside außer verpasste Info.")
                    .weightNormal(70).weightGood(20).weightGreat(8).weightJackpot(2).weightBad(0).weightCrisis(0).build()
            )).build());

        list.add(WorkScenario.builder().key("ws_product_launch_timing")
            .title("🚀 Launch-Timing-Dilemma")
            .description("Das Produkt ist zu 85% fertig. Konkurrent plant ebenfalls einen Launch nächste Woche.")
            .market(null)
            .options(List.of(
                WorkOption.builder().label("⚡ Jetzt launchen — first mover")
                    .flavor("Du bist zuerst. Aber mit Bugs. Reaktion der Nutzer ungewiss.")
                    .weightNormal(15).weightGood(30).weightGreat(25).weightJackpot(15).weightBad(10).weightCrisis(5).build(),
                WorkOption.builder().label("🔧 Eine Woche warten — polish first")
                    .flavor("Besseres Produkt aber Konkurrent war zuerst.")
                    .weightNormal(35).weightGood(40).weightGreat(20).weightJackpot(3).weightBad(2).weightCrisis(0).build()
            )).build());

        list.add(WorkScenario.builder().key("ws_remote_work_policy")
            .title("🏠 Remote-Work-Debatte")
            .description("Das Team will 100% remote arbeiten. Du bist dir nicht sicher.")
            .market(null)
            .options(List.of(
                WorkOption.builder().label("✅ Vollständig remote erlauben")
                    .flavor("Team ist happy. Recruiting-Vorteil. Aber manchmal Koordinationsprobleme.")
                    .weightNormal(35).weightGood(40).weightGreat(18).weightJackpot(5).weightBad(2).weightCrisis(0).build(),
                WorkOption.builder().label("🏢 Hybrides Modell (3 Tage Office)")
                    .flavor("Kompromiss. Nicht alle sind happy aber es funktioniert.")
                    .weightNormal(45).weightGood(35).weightGreat(15).weightJackpot(3).weightBad(2).weightCrisis(0).build(),
                WorkOption.builder().label("🚫 Office-First — Zusammenarbeit zählt")
                    .flavor("Produktivitätsvorteil vor Ort, aber Recruiting wird schwerer.")
                    .weightNormal(30).weightGood(25).weightGreat(10).weightJackpot(2).weightBad(25).weightCrisis(8).build()
            )).build());

        list.add(WorkScenario.builder().key("ws_budget_crisis")
            .title("💸 Budget-Engpass")
            .description("Monatsende. Die Runway ist 2 Wochen kürzer als erwartet. Stresstest.")
            .market(null)
            .options(List.of(
                WorkOption.builder().label("✂️ Sofort Kosten senken")
                    .flavor("Schmerzhaft aber du überlebst den Monat sicher.")
                    .weightNormal(30).weightGood(40).weightGreat(20).weightJackpot(2).weightBad(8).weightCrisis(0).build(),
                WorkOption.builder().label("🎯 Emergency-Sales-Sprint starten")
                    .flavor("Riskant. Wenn du einen Deal landest: gerettet. Wenn nicht: gefährlich.")
                    .weightNormal(20).weightGood(25).weightGreat(25).weightJackpot(15).weightBad(10).weightCrisis(5).build()
            )).build());

        list.add(WorkScenario.builder().key("ws_feature_request_flood")
            .title("📬 Feature-Request-Flut")
            .description("Nach dem Launch kommen täglich 200+ Feature-Requests. Alle wollen etwas anderes.")
            .market(null)
            .options(List.of(
                WorkOption.builder().label("🗳️ Community abstimmen lassen")
                    .flavor("Nutzer fühlen sich einbezogen. Das Top-Feature bauen.")
                    .weightNormal(30).weightGood(40).weightGreat(22).weightJackpot(5).weightBad(3).weightCrisis(0).build(),
                WorkOption.builder().label("🎯 Eigene Vision durchziehen")
                    .flavor("Fokus bleibt. Nicht alle sind happy aber das Produkt bleibt kohärent.")
                    .weightNormal(35).weightGood(35).weightGreat(20).weightJackpot(8).weightBad(2).weightCrisis(0).build()
            )).build());

        list.add(WorkScenario.builder().key("ws_mentorship_offer")
            .title("🎓 Mentor-Angebot")
            .description("Ein erfahrener Serienunternehmer bietet dir monatliche Mentoring-Sessions an. Kostenlos.")
            .market(null)
            .options(List.of(
                WorkOption.builder().label("✅ Annehmen — immer")
                    .flavor("Wertvolle Insights. RP-Bonus und manchmal überraschende Kontakte.")
                    .weightNormal(20).weightGood(40).weightGreat(30).weightJackpot(8).weightBad(2).weightCrisis(0).build(),
                WorkOption.builder().label("⏰ Zu beschäftigt gerade")
                    .flavor("Du sparst 2 Stunden. Keine Belohnung, kein Verlust.")
                    .weightNormal(75).weightGood(20).weightGreat(5).weightJackpot(0).weightBad(0).weightCrisis(0).build()
            )).build());

        list.add(WorkScenario.builder().key("ws_board_presentation")
            .title("📊 Board-Präsentation")
            .description("Dein erster Advisory Board Meeting. Die Zahlen sehen solide aus, aber du bist nervös.")
            .market(null)
            .options(List.of(
                WorkOption.builder().label("📈 Nur die Wins präsentieren")
                    .flavor("Sie sind beeindruckt. Aber wenn Probleme auftauchen fehlt das Vertrauen.")
                    .weightNormal(25).weightGood(40).weightGreat(25).weightJackpot(5).weightBad(4).weightCrisis(1).build(),
                WorkOption.builder().label("🔍 Volle Transparenz: Wins und Probleme")
                    .flavor("Sie respektieren die Ehrlichkeit. Manchmal gibt es sogar Hilfe bei den Problemen.")
                    .weightNormal(20).weightGood(35).weightGreat(30).weightJackpot(12).weightBad(3).weightCrisis(0).build()
            )).build());

        list.add(WorkScenario.builder().key("ws_spontaneous_hackathon")
            .title("⚡ Spontaner Hackathon")
            .description("Das Team schlägt vor: dieses Wochenende 48h Hackathon. Alle freiwillig, alle excited.")
            .market(null)
            .options(List.of(
                WorkOption.builder().label("🔥 Machen! Energia is alles")
                    .flavor("Manchmal entsteht etwas Geniales. Manchmal einfach schöner Teamgeist.")
                    .weightNormal(20).weightGood(35).weightGreat(30).weightJackpot(12).weightBad(3).weightCrisis(0).build(),
                WorkOption.builder().label("😴 Nein — Wochenende ist Wochenende")
                    .flavor("Team erholt sich. Montag voller Energie.")
                    .weightNormal(55).weightGood(30).weightGreat(12).weightJackpot(1).weightBad(2).weightCrisis(0).build()
            )).build());

        // ── CYBERSEC SZENARIEN ────────────────────────────────────────────

        list.add(WorkScenario.builder().key("cs_ransomware_threat")
            .title("💀 Ransomware-Drohung")
            .description("Eine kriminelle Gruppe droht eure Systeme zu verschlüsseln wenn ihr nicht zahlt.")
            .market(Market.CYBERSECURITY)
            .options(List.of(
                WorkOption.builder().label("🛡️ Systeme härten + ignorieren")
                    .flavor("Ihr seid gut genug. Die Drohung war ein Bluff.")
                    .weightNormal(25).weightGood(40).weightGreat(25).weightJackpot(5).weightBad(5).weightCrisis(0).build(),
                WorkOption.builder().label("💸 Zahlen und weitermachen")
                    .flavor("Kurze Ruhe, aber ihr seid jetzt als zahlungswillig bekannt.")
                    .weightNormal(30).weightGood(15).weightGreat(5).weightJackpot(0).weightBad(30).weightCrisis(20).build(),
                WorkOption.builder().label("🚨 FBI und Behörden einschalten")
                    .flavor("Langsam, aber korrekt. Manchmal wird der Täter gefasst.")
                    .weightNormal(30).weightGood(35).weightGreat(20).weightJackpot(10).weightBad(5).weightCrisis(0).build()
            )).build());

        list.add(WorkScenario.builder().key("cs_zero_day_found")
            .title("🔎 Zero-Day entdeckt")
            .description("Euer Pen-Test-Team findet eine kritische Lücke in Software eines Fortune-500-Unternehmens.")
            .market(Market.CYBERSECURITY)
            .options(List.of(
                WorkOption.builder().label("📬 Responsible Disclosure")
                    .flavor("Ihr tut das Richtige. Reputation steigt. Manchmal zahlen sie eine Prämie.")
                    .weightNormal(20).weightGood(35).weightGreat(30).weightJackpot(12).weightBad(3).weightCrisis(0).build(),
                WorkOption.builder().label("🤫 Intern halten — für Forschung")
                    .flavor("Wertvolles Wissen. Aber legal riskant wenn es rauskommt.")
                    .weightNormal(35).weightGood(25).weightGreat(15).weightJackpot(5).weightBad(10).weightCrisis(10).build()
            )).build());

        list.add(WorkScenario.builder().key("cs_gov_contract_offer")
            .title("🏛️ Regierungsanfrage")
            .description("Das BSI fragt ob ihr an einem nationalen Sicherheitsprojekt teilnehmen wollt. NDA + Geheimhaltung.")
            .market(Market.CYBERSECURITY)
            .options(List.of(
                WorkOption.builder().label("✅ Mitmachen — Prestige und Revenue")
                    .flavor("Massive Glaubwürdigkeit und stabile Einnahmen. Aber eingeschränkte Flexibilität.")
                    .weightNormal(15).weightGood(35).weightGreat(35).weightJackpot(12).weightBad(3).weightCrisis(0).build(),
                WorkOption.builder().label("🚫 Ablehnen — wir bleiben unabhängig")
                    .flavor("Keine Einschränkungen. Kein Upside.")
                    .weightNormal(70).weightGood(20).weightGreat(8).weightJackpot(2).weightBad(0).weightCrisis(0).build()
            )).build());

        // ── FINTECH SZENARIEN ─────────────────────────────────────────────

        list.add(WorkScenario.builder().key("ft_regulator_visit")
            .title("🏦 BaFin-Inspektion")
            .description("Überraschungsbesuch vom Finanzregulator. Eure Compliance-Dokumentation ist... improvisiert.")
            .market(Market.FINTECH)
            .options(List.of(
                WorkOption.builder().label("😅 Offen zugeben und Lösung zeigen")
                    .flavor("Überraschend oft kommt das gut an wenn man einen Plan hat.")
                    .weightNormal(25).weightGood(40).weightGreat(25).weightJackpot(5).weightBad(5).weightCrisis(0).build(),
                WorkOption.builder().label("🎭 So tun als ob alles perfekt ist")
                    .flavor("Riskant. Wenn sie genauer schauen: problematisch.")
                    .weightNormal(20).weightGood(20).weightGreat(10).weightJackpot(0).weightBad(20).weightCrisis(30).build()
            )).build());

        list.add(WorkScenario.builder().key("ft_crypto_crash")
            .title("📉 Crypto-Crash")
            .description("Overnight crasht der Markt -40%. Euer Crypto-Feature macht 30% des Revenue aus.")
            .market(Market.FINTECH)
            .options(List.of(
                WorkOption.builder().label("🔄 Sofort diversifizieren")
                    .flavor("Ihr reduziert die Abhängigkeit. Kurzfristiger Aufwand, langfristig stabiler.")
                    .weightNormal(30).weightGood(40).weightGreat(22).weightJackpot(5).weightBad(3).weightCrisis(0).build(),
                WorkOption.builder().label("💎 Hodln und warten")
                    .flavor("Alles oder nichts. Märkte erholen sich — oder nicht.")
                    .weightNormal(20).weightGood(20).weightGreat(15).weightJackpot(20).weightBad(15).weightCrisis(10).build()
            )).build());

        // ── CONSUMER TECH SZENARIEN ──────────────────────────────────────

        list.add(WorkScenario.builder().key("ct_influencer_deal")
            .title("🌟 Influencer-Deal")
            .description("Ein Creator mit 2M Followern will euer Produkt gegen Beteiligung am Revenue promoten.")
            .market(Market.CONSUMER_TECH)
            .options(List.of(
                WorkOption.builder().label("🤝 Deal annehmen")
                    .flavor("Massiver Traffic möglich. Aber Revenue-Split läuft dauerhaft.")
                    .weightNormal(15).weightGood(30).weightGreat(30).weightJackpot(20).weightBad(4).weightCrisis(1).build(),
                WorkOption.builder().label("💵 Einmalige Bezahlung statt Beteiligung")
                    .flavor("Klarer Cost. Kein laufendes Commitment.")
                    .weightNormal(35).weightGood(40).weightGreat(20).weightJackpot(3).weightBad(2).weightCrisis(0).build(),
                WorkOption.builder().label("🚫 Kein Influencer-Marketing")
                    .flavor("Ihr bleibt organisch. Kein Risiko, kein Upside.")
                    .weightNormal(70).weightGood(20).weightGreat(8).weightJackpot(2).weightBad(0).weightCrisis(0).build()
            )).build());

        list.add(WorkScenario.builder().key("ct_app_store_rejection")
            .title("❌ App Store Ablehnung")
            .description("Apple hat eure App zum dritten Mal abgelehnt. Launch-Termin in 3 Tagen.")
            .market(Market.CONSUMER_TECH)
            .options(List.of(
                WorkOption.builder().label("🔧 Nochmal anpassen und einreichen")
                    .flavor("Aufwändig aber korrekt. Manchmal geht es durch, manchmal nicht.")
                    .weightNormal(30).weightGood(35).weightGreat(25).weightJackpot(5).weightBad(5).weightCrisis(0).build(),
                WorkOption.builder().label("🌐 Zuerst Web-Version launchen")
                    .flavor("Ihr gebt nicht auf. Web läuft parallel während App-Problem gelöst wird.")
                    .weightNormal(35).weightGood(40).weightGreat(20).weightJackpot(3).weightBad(2).weightCrisis(0).build()
            )).build());

        // ── E-COMMERCE SZENARIEN ─────────────────────────────────────────

        list.add(WorkScenario.builder().key("ec_supplier_problem")
            .title("📦 Lieferanten-Krise")
            .description("Euer Hauptlieferant kann nicht liefern. Black Friday in 2 Wochen.")
            .market(Market.E_COMMERCE)
            .options(List.of(
                WorkOption.builder().label("🆘 Notfall-Lieferant finden — jeden Preis")
                    .flavor("Teuer, aber ihr habt Ware. Black Friday gerettet.")
                    .weightNormal(20).weightGood(40).weightGreat(30).weightJackpot(5).weightBad(5).weightCrisis(0).build(),
                WorkOption.builder().label("😅 Kunden ehrlich informieren")
                    .flavor("Einige kündigen. Viele bleiben und respektieren die Transparenz.")
                    .weightNormal(35).weightGood(35).weightGreat(20).weightJackpot(5).weightBad(5).weightCrisis(0).build()
            )).build());

        list.add(WorkScenario.builder().key("ec_fake_reviews")
            .title("⭐ Fake-Review-Angebot")
            .description("Eine Agentur bietet 500 positive Reviews für $2.000 an. Garantiert unentdeckt.")
            .market(Market.E_COMMERCE)
            .options(List.of(
                WorkOption.builder().label("💸 Kaufen — alle machen das")
                    .flavor("Kurzfristiger Boost. Wenn Amazon es erkennt: Account gesperrt.")
                    .weightNormal(20).weightGood(20).weightGreat(15).weightJackpot(5).weightBad(20).weightCrisis(20).build(),
                WorkOption.builder().label("🚫 Nein — echte Reviews aufbauen")
                    .flavor("Langsamer aber sicher. Review-Request E-Mail-Kampagne starten.")
                    .weightNormal(40).weightGood(40).weightGreat(18).weightJackpot(2).weightBad(0).weightCrisis(0).build()
            )).build());

        // ── ENTERPRISE SAAS SZENARIEN ─────────────────────────────────────

        list.add(WorkScenario.builder().key("es_enterprise_churn")
            .title("📉 Enterprise-Churn")
            .description("Euer größter Kunde (30% des MRR) droht zu kündigen. Meeting in 1 Stunde.")
            .market(Market.ENTERPRISE_SAAS)
            .options(List.of(
                WorkOption.builder().label("💰 Massive Rabatt anbieten")
                    .flavor("Er bleibt — aber 40% günstiger. Revenue sinkt aber Churn verhindert.")
                    .weightNormal(35).weightGood(40).weightGreat(18).weightJackpot(3).weightBad(4).weightCrisis(0).build(),
                WorkOption.builder().label("🎯 Seinen Pain-Point lösen")
                    .flavor("Wenn du schnell genug sein kannst, beeindruckt das mehr als Rabatt.")
                    .weightNormal(15).weightGood(30).weightGreat(35).weightJackpot(15).weightBad(5).weightCrisis(0).build(),
                WorkOption.builder().label("👋 Ziehen lassen")
                    .flavor("Schmerzhaft aber manchmal ist ein toxischer Großkunde eine Befreiung.")
                    .weightNormal(25).weightGood(20).weightGreat(10).weightJackpot(5).weightBad(25).weightCrisis(15).build()
            )).build());

        list.add(WorkScenario.builder().key("es_competitor_copies_feature")
            .title("👀 Konkurrent kopiert Feature")
            .description("Ein größerer Wettbewerber hat exakt euer Killer-Feature 1:1 nachgebaut und promoted es aggressiv.")
            .market(Market.ENTERPRISE_SAAS)
            .options(List.of(
                WorkOption.builder().label("⚡ Sofort das nächste Feature bauen")
                    .flavor("Innovation-Speed ist euer Vorteil. Immer einen Schritt voraus.")
                    .weightNormal(20).weightGood(40).weightGreat(28).weightJackpot(10).weightBad(2).weightCrisis(0).build(),
                WorkOption.builder().label("📣 Marketing: Original ist besser")
                    .flavor("Kunden wissen dass ihr zuerst da wart. PR-Kampagne starten.")
                    .weightNormal(30).weightGood(35).weightGreat(22).weightJackpot(8).weightBad(5).weightCrisis(0).build()
            )).build());

        // Noch ein paar universelle für mehr Abwechslung
        list.add(WorkScenario.builder().key("ws_lucky_break")
            .title("🍀 Zufälliger Glücksmoment")
            .description("Ein zufälliger Tweet eines Tech-Celebrities erwähnt euer Produkt positiv — ohne dass ihr ihn kontaktiert habt.")
            .market(null)
            .options(List.of(
                WorkOption.builder().label("🚀 Hype sofort nutzen")
                    .flavor("Ihr seid bereit. Traffic konvertiert perfekt.")
                    .weightNormal(10).weightGood(25).weightGreat(35).weightJackpot(25).weightBad(4).weightCrisis(1).build(),
                WorkOption.builder().label("😐 Business as usual")
                    .flavor("Hype läuft ab. Kleiner Benefit trotzdem.")
                    .weightNormal(45).weightGood(35).weightGreat(15).weightJackpot(5).weightBad(0).weightCrisis(0).build()
            )).build());

        list.add(WorkScenario.builder().key("ws_office_space")
            .title("🏢 Büro-Entscheidung")
            .description("Euer Mietvertrag läuft ab. Premium-Location für das Doppelte oder günstig außerhalb?")
            .market(null)
            .options(List.of(
                WorkOption.builder().label("✨ Premium — Adresse ist Marketing")
                    .flavor("Recruits kommen leichter. Kunden sind beeindruckt. Aber teuer.")
                    .weightNormal(25).weightGood(40).weightGreat(25).weightJackpot(8).weightBad(2).weightCrisis(0).build(),
                WorkOption.builder().label("💡 Günstig + Budget in Gehälter")
                    .flavor("Team mag höhere Löhne mehr als fancy Office.")
                    .weightNormal(35).weightGood(40).weightGreat(20).weightJackpot(3).weightBad(2).weightCrisis(0).build()
            )).build());

        list.add(WorkScenario.builder().key("ws_technical_debt")
            .title("⚙️ Tech-Debt Moment der Wahrheit")
            .description("Das System bricht fast zusammen. Refactoring jetzt oder noch 3 Monate Feature-Bau?")
            .market(null)
            .options(List.of(
                WorkOption.builder().label("🔧 Refactoring jetzt durchziehen")
                    .flavor("2 Wochen kein Feature. Danach: 3x schneller. Langfristig die richtige Entscheidung.")
                    .weightNormal(30).weightGood(40).weightGreat(22).weightJackpot(5).weightBad(3).weightCrisis(0).build(),
                WorkOption.builder().label("🏃 Features first — Tech-Debt later")
                    .flavor("Kurzfristig mehr Output. Aber das System wird fragiler.")
                    .weightNormal(25).weightGood(30).weightGreat(20).weightJackpot(5).weightBad(12).weightCrisis(8).build()
            )).build());

        return list;
    }

    /** Gibt passende Szenarien für einen Markt zurück (global + marktspezifisch) */
    public static List<WorkScenario> forMarket(Market market) {
        return ALL.stream()
                .filter(s -> s.getMarket() == null || s.getMarket() == market)
                .toList();
    }

    /** Wählt zufälliges Szenario für Markt aus — niemals dasselbe wie zuletzt */
    public static WorkScenario random(Market market, String lastKey, Random rng) {
        List<WorkScenario> pool = forMarket(market);
        List<WorkScenario> candidates = pool.stream()
                .filter(s -> !s.getKey().equals(lastKey))
                .toList();
        if (candidates.isEmpty()) candidates = pool;
        return candidates.get(rng.nextInt(candidates.size()));
    }
}
