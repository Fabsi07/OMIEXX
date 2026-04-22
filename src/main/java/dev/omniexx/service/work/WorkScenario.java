package dev.omniexx.service.work;

import dev.omniexx.entity.Market;
import java.util.*;

public class WorkScenario {

    public final String key;
    public final String title;
    public final String description;
    public final Market market; // null = alle Märkte
    public final List<Opt> options;

    public WorkScenario(String key, String title, String description, Market market, List<Opt> options) {
        this.key = key; this.title = title; this.description = description;
        this.market = market; this.options = options;
    }

    public enum Tier { JACKPOT, GREAT, GOOD, NORMAL, BAD, CRITICAL }

    public static class Opt {
        public final String id, label, outcomeText;
        public final int wJackpot, wGreat, wGood, wNormal, wBad, wCritical;
        public Opt(String id, String label, int wj, int wgr, int wgo, int wn, int wb, int wc, String ot) {
            this.id=id; this.label=label; this.outcomeText=ot;
            this.wJackpot=wj; this.wGreat=wgr; this.wGood=wgo;
            this.wNormal=wn; this.wBad=wb; this.wCritical=wc;
        }
        public Tier roll(Random rng) {
            int n = rng.nextInt(100);
            if (n < wJackpot)                           return Tier.JACKPOT;
            if (n < wJackpot+wGreat)                   return Tier.GREAT;
            if (n < wJackpot+wGreat+wGood)             return Tier.GOOD;
            if (n < wJackpot+wGreat+wGood+wNormal)     return Tier.NORMAL;
            if (n < wJackpot+wGreat+wGood+wNormal+wBad) return Tier.BAD;
            return Tier.CRITICAL;
        }
    }

    private static WorkScenario s(String key, String title, String desc, Market m, Opt... opts) {
        return new WorkScenario(key, title, desc, m, List.of(opts));
    }
    private static Opt o(String id, String label, int j, int gr, int go, int n, int b, int c, String ot) {
        return new Opt(id, label, j, gr, go, n, b, c, ot);
    }

    public static final List<WorkScenario> ALL = List.of(
        s("morning_standup","☀️ Morning Standup","Das Team ist versammelt. Wie startest du den Tag?",null,
            o("a","💪 Motivationsrede",0,15,35,40,8,2,"Team fokussiert und produktiv."),
            o("b","📋 Tasks priorisieren",0,10,30,50,8,2,"Strukturiert, ohne besonderen Schwung."),
            o("c","🎯 Stretch Goals setzen",2,20,28,35,12,3,"Hohes Risiko, hohes Potential.")),
        s("investor_email","📧 E-Mail vom Investor","Ein Investor fragt nach einem Update.",null,
            o("a","📊 Ehrlicher Bericht",0,20,40,35,5,0,"Vertrauen aufgebaut."),
            o("b","🌟 Optimistisch formulieren",1,15,25,40,15,4,"Gut — bis die Zahlen enttäuschen."),
            o("c","🤐 Nächsten Monat antworten",0,5,15,30,35,15,"Investor ist unzufrieden.")),
        s("new_competitor","⚠️ Neuer Konkurrent","Ein gut-finanziertes Startup betritt deinen Markt.",null,
            o("a","🚀 Tempo erhöhen",1,18,32,35,12,2,"First-mover Vorteil gesichert."),
            o("b","🔍 Analysieren",0,12,38,40,8,2,"Wertvolle Intel gesammelt."),
            o("c","😤 Ignorieren",0,8,20,42,22,8,"Konkurrent gewinnt Boden.")),
        s("team_conflict","😤 Teamkonflikt","CTO und Marketing streiten. Du musst entscheiden.",null,
            o("a","🤝 Mediation",0,10,35,45,8,2,"Konflikt gelöst. Morale ok."),
            o("b","⚡ Schnelle Entscheidung",1,20,30,30,15,4,"Einer happy, einer nicht."),
            o("c","🙈 Selbst klären lassen",0,5,15,35,30,15,"Eskaliert. Kostet Produktivität.")),
        s("viral_moment","🔥 Viraler Moment","Ein Tweet geht gerade viral. 50k Impressions.",null,
            o("a","📣 PR-Push starten",3,25,35,30,6,1,"Welle erfolgreich geritten!"),
            o("b","💰 Preise erhöhen",1,12,25,40,17,5,"Teils negativ, aber Umsatz steigt."),
            o("c","😎 Organisch lassen",0,15,35,40,8,2,"Ruhig und nachhaltig.")),
        s("late_night_bug","🐛 Production-Bug um Mitternacht","Kritischer Fehler ist live.",null,
            o("a","🚨 Sofort alles fixen",0,15,35,40,9,1,"Professionell. Kunden merken es kaum."),
            o("b","🔄 Rollback",0,20,40,30,8,2,"Smart. Wenig Downtime."),
            o("c","😴 Morgen früh",0,2,8,25,40,25,"Kunden beschweren sich massiv.")),
        s("partnership_offer","🤝 Partnerschaftsangebot","Größeres Unternehmen bietet Kooperation an.",null,
            o("a","✍️ Deal annehmen",1,20,35,35,8,1,"Neue Einnahmequelle erschlossen!"),
            o("b","🔍 Due Diligence",0,15,40,38,6,1,"Solider Ansatz. Partner zufrieden."),
            o("c","❌ Ablehnen",0,5,15,50,25,5,"Fokus — aber Chance verpasst.")),
        s("board_meeting","📊 Board Meeting","Quartalspräsentation. Wie präsentierst du?",null,
            o("a","🎯 Vision und Wachstum",1,22,38,30,8,1,"Board beeindruckt."),
            o("b","💰 Unit Economics",0,12,35,42,9,2,"Solide, konservativ."),
            o("c","🎲 Neues Pivot-Konzept",3,18,20,25,25,9,"Polarisierend.")),
        s("key_employee_sick","🤒 CTO krankgemeldet","Wichtiges Deployment steht an.",null,
            o("a","⏳ Deployment verschieben",0,8,25,52,13,2,"Sicher, aber Verzögerung."),
            o("b","💪 Team selbst",1,15,30,35,16,3,"Team zeigt was sie können."),
            o("c","💸 Freelancer",0,10,28,40,18,4,"Teuer aber professionell.")),
        s("recruitment_event","👨‍💼 Job-Messe","Uni-Karrieremesse morgen.",null,
            o("a","🎪 Großer Stand",0,12,28,42,15,3,"Sichtbar aber teuer."),
            o("b","🪑 Kleiner Stand, persönlich",1,18,38,35,7,1,"Authentisch und effektiv!"),
            o("c","❌ Nicht hingehen",0,3,10,55,25,7,"Konkurrenten scouten Talente.")),
        s("product_roadmap","🗺️ Roadmap-Entscheidung","Feature A (Kunden) vs Feature B (Strategie).",null,
            o("a","🎯 Feature A — Kundenwunsch",0,10,30,45,13,2,"Kunden happy, Churn sinkt."),
            o("b","♟️ Feature B — Strategisch",1,18,30,38,11,2,"Langfristig stärker."),
            o("c","⚡ Beides, doppeltes Tempo",2,20,25,28,20,5,"Team am Limit.")),
        s("keynote_invite","🎤 Keynote-Einladung","Große Konferenz lädt dich als Speaker ein.",null,
            o("a","✅ Annehmen",1,22,35,33,8,1,"Riesige Sichtbarkeit!"),
            o("b","❌ Ablehnen",0,5,15,55,20,5,"Fokussiert geblieben."),
            o("c","📝 Nur Remote",0,10,28,45,15,2,"Kompromiss akzeptiert.")),
        s("acquisition_rumor","💬 Übernahme-Gerücht","Presse spekuliert über BigTech-Übernahme.",null,
            o("a","🤐 Kein Kommentar",0,10,30,45,13,2,"Professionell. Kurs gehalten."),
            o("b","📣 Öffentlich dementieren",0,8,25,42,18,7,"Gerücht stirbt."),
            o("c","🎲 Treiben lassen",2,20,25,30,18,5,"Medienpräsenz explodiert.")),
        s("weekend_deploy","🗓️ Freitagabend-Deploy","Kritisches Feature ist fertig. Live heute?",null,
            o("a","🚀 Deployen!",2,22,30,30,12,4,"Manchmal klappt es, manchmal brennt es."),
            o("b","📅 Montag früh",0,8,25,55,10,2,"Smooth deployment."),
            o("c","🌙 Nachts deployen",0,12,28,42,14,4,"Niemand merkt es. Oder den Bug.")),
        s("pricing_change","💰 Preisanpassung","Kosten 15% gestiegen. Preise erhöhen?",null,
            o("a","📈 12% Preiserhöhung",0,10,28,42,17,3,"Margin gerettet. Etwas Churn."),
            o("b","😰 Preise einfrieren",0,5,18,50,22,5,"Kunden happy, Margin leidet."),
            o("c","🎯 Premium-Tier",1,18,30,38,11,2,"Neues Revenue-Segment!")),
        s("ai_tool_adoption","🤖 KI-Tools im Team","Team will GPT für Code nutzen.",null,
            o("a","✅ Volles Commitment",1,20,35,35,8,1,"Produktivität steigt deutlich!"),
            o("b","⚠️ Pilot mit 2 Personen",0,15,38,38,8,1,"Funktioniert gut."),
            o("c","❌ Qualitätsrisiken",0,5,15,48,25,7,"Konkurrenten nutzen es.")),
        s("data_breach_rumor","🔐 Datenleck-Gerücht","Nutzer melden verdächtige Logins.",null,
            o("a","🚨 Sofort Passwort-Reset",0,10,35,42,12,1,"Richtige Reaktion. Vertrauen bleibt."),
            o("b","🔍 Intern untersuchen",1,18,35,35,10,1,"Solide forensische Arbeit."),
            o("c","🤞 Abwarten",0,1,5,20,40,34,"Tatsächlich ein Leak. Katastrophe.")),
        s("competitor_hire","🎯 Talent beim Konkurrenten","Star-Dev beim Konkurrenten ist unzufrieden.",null,
            o("a","💸 Aggressives Angebot",1,22,33,33,10,1,"Touchdown! Talent gewonnen."),
            o("b","☕ Erst informell treffen",0,15,35,40,9,1,"Strategisch klug."),
            o("c","🤝 Fair Offer",0,10,30,45,13,2,"Meist akzeptiert.")),
        s("pivot_discussion","🔄 Pivot-Diskussion","Advisor schlägt fundamentalen Schwenk vor.",null,
            o("a","🚀 Pivot wagen",3,20,22,28,22,5,"High Risk, High Reward!"),
            o("b","🔬 Kleine Experimente",0,12,35,40,11,2,"Daten sammeln."),
            o("c","♟️ Kurs halten",0,8,25,50,14,3,"Stabil. Advisor enttäuscht.")),
        s("mentorship_request","🎓 Mentorship-Anfrage","Startup-Accelerator bittet um Gastvorlesung.",null,
            o("a","✅ Machen",1,18,35,36,9,1,"Netzwerk wächst!"),
            o("b","📧 Video-Session",0,12,30,45,11,2,"Nett aber weniger Wirkung."),
            o("c","❌ Keine Zeit",0,3,10,55,25,7,"Verpasste Gelegenheit.")),
        // Marktspezifische
        s("app_store_review","⭐ 1-Stern-Review viral","Tweet mit 50k Impressions.",Market.CONSUMER_TECH,
            o("a","🙏 Öffentlich antworten",1,20,35,35,8,1,"Community sieht dein Engagement."),
            o("b","🔧 Sofort patchen",1,25,35,30,8,1,"Schnelligkeit beeindruckt."),
            o("c","🤐 Ignorieren",0,2,8,25,40,25,"Weitere negative Reviews folgen.")),
        s("influencer_deal","📱 Influencer-Anfrage","Tech-YouTube (800k Subs) will reviewen.",Market.CONSUMER_TECH,
            o("a","🎁 Gratis-Access + Provision",2,25,35,30,7,1,"Großer Traffic-Spike!"),
            o("b","💰 Bezahltes Sponsored Video",0,10,25,40,20,5,"Audience riecht Sponsorship."),
            o("c","❌ Nur organische Reviews",0,8,22,45,20,5,"Prinzipientreu — Chance verpasst.")),
        s("enterprise_demo","🏢 Enterprise Demo","Fortune-500 will live Demo. CTO krank.",Market.ENTERPRISE_SAAS,
            o("a","🎯 Demo selbst halten",2,22,32,32,10,2,"CEO überzeugt!"),
            o("b","👥 Lead-Dev schicken",0,12,30,40,15,3,"Kompetent, kein Executive-Flair."),
            o("c","📅 Verschieben",0,5,15,40,30,10,"Kunde wirkt enttäuscht.")),
        s("regulatory_inquiry","⚖️ BaFin-Anfrage","Regulierungsbehörde fragt nach Compliance.",Market.FINTECH,
            o("a","📁 Sofort vollständig liefern",0,10,35,45,8,2,"Verfahren eingestellt."),
            o("b","👨‍💼 Anwalt einschalten",0,12,30,40,14,4,"Teuer aber juristisch sicher."),
            o("c","🤞 Minimale Info",0,3,10,25,40,22,"Behörde eskaliert.")),
        s("flash_sale","⚡ Flash Sale?","Lager-Überbestand. 50%-Sale?",Market.E_COMMERCE,
            o("a","🔥 Flash Sale 24h",1,22,35,32,8,2,"Ausverkauft! Cashflow-Boost."),
            o("b","📦 Langsam abverkaufen",0,8,25,52,12,3,"Stable Margin."),
            o("c","🗑️ Überschuss wegwerfen",0,3,8,25,35,29,"Massiver Verlust.")),
        s("zero_day","💀 Zero-Day Exploit","Sicherheitsforscher meldet kritische Lücke.",Market.CYBERSECURITY,
            o("a","🚨 Patchen + kommunizieren",1,20,35,35,8,1,"Reputation steigt!"),
            o("b","🔒 Patch still ausrollen",0,12,30,45,12,1,"Sicher gepatcht."),
            o("c","📅 Nächste Woche",0,2,5,20,40,33,"Wird ausgenutzt. Katastrophe."))
    );

    public static WorkScenario getRandom(Market market, Random rng, String lastKey) {
        List<WorkScenario> pool = new ArrayList<>();
        for (WorkScenario s : ALL) {
            if ((s.market == null || s.market == market) && !s.key.equals(lastKey)) {
                pool.add(s);
            }
        }
        if (pool.isEmpty()) pool = new ArrayList<>(ALL);
        return pool.get(rng.nextInt(pool.size()));
    }
}
