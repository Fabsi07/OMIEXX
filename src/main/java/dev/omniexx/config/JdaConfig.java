package dev.omniexx.config;

import dev.omniexx.discord.listener.SlashCommandListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JdaConfig {

    @Value("${omniexx.discord.token}")
    private String token;

    @Bean
    public JDA jda(SlashCommandListener slashCommandListener,
                   StartCommand startCommand,
                   PrestigeCommand prestigeCommand,
                   WorkCommand workCommand) throws InterruptedException {
        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT
                )
                .addEventListeners(slashCommandListener, startCommand, prestigeCommand, workCommand)
                .build()
                .awaitReady();

        // Slash Commands global registrieren
        jda.updateCommands().addCommands(

                // Phase 1 — Foundation
                Commands.slash("work", "Arbeite an deiner Firma — Entscheidungs-Szenario (20s Cooldown)"),
                Commands.slash("start", "Gründe deine Firma und starte ins Spiel"),
                Commands.slash("report", "Zeigt dein aktuelles Company Dashboard"),
                Commands.slash("help", "Alle verfügbaren Commands mit Erklärung"),

                // Work-System
                Commands.slash("work",   "Work-Session starten — treffe Entscheidungen und verdiene Belohnungen"),
                Commands.slash("crunch", "Intensiv arbeiten — doppeltes Risiko, doppelte Belohnung (60min CD)"),

                // Phase 2 — Core Loop
                Commands.slash("log", "Letzte Events deiner Firma"),
                Commands.slash("notify", "Benachrichtigungen ein-/ausschalten")
                        .addOption(OptionType.STRING, "event",
                                "tick_ready | project_done | sabotaged | employee_quit", true),
                Commands.slash("pause", "Firma für 48h pausieren (max. 2x/Monat)"),
                Commands.slash("invest", "Kapital direkt in einen KPI investieren")
                        .addOption(OptionType.INTEGER, "betrag", "Betrag in $ (wird in Cents umgerechnet)", true),
                Commands.slash("pr", "PR-Kampagne starten")
                        .addOption(OptionType.STRING, "typ", "positiv oder negativ", true),

                // Phase 3 — Mitarbeiter
                Commands.slash("hire", "Mitarbeiter einstellen")
                        .addOption(OptionType.STRING, "rolle",
                                "cto | cfo | dev | designer | marketing | sales", true),
                Commands.slash("fire", "Mitarbeiter feuern")
                        .addOption(OptionType.STRING, "name", "Name des Mitarbeiters", true),
                Commands.slash("team", "Dein aktuelles Team anzeigen"),

                // Phase 4 — Projekte
                Commands.slash("project", "Projekt-Commands")
                        .addOption(OptionType.STRING, "aktion",
                                "list | start | status | boost | cancel", true)
                        .addOption(OptionType.STRING, "id", "Projekt-ID (für start)", false),

                // Phase 5 — Markt
                Commands.slash("market", "Markt-Leaderboard anzeigen"),
                Commands.slash("acquire", "NPC-Firma akquirieren")
                        .addOption(OptionType.STRING, "firma", "Name der NPC-Firma", true),
                Commands.slash("expand", "Neuen Marktbereich betreten")
                        .addOption(OptionType.STRING, "markt", "Name des Markts", true),
                Commands.slash("fundraise", "Bei NPC-VCs pitchen"),
                Commands.slash("profile", "Firmenprofil anzeigen")
                        .addOption(OptionType.STRING, "firma", "Firmenname", false),

                // Phase 6 — Tech Tree
                Commands.slash("research", "Tech-Tree Commands")
                        .addOption(OptionType.STRING, "aktion", "tree | pick | status", true)
                        .addOption(OptionType.STRING, "node", "Node-Key (für pick)", false),

                // Phase 7 — PvP
                Commands.slash("sabotage", "Gegner sabotieren")
                        .addOption(OptionType.USER, "ziel", "Ziel-Spieler", true)
                        .addOption(OptionType.STRING, "aktion",
                                "leak | hiring_war | fake_pr | infra", true),
                // TODO feat/deal-command: Commands.slash("deal", ...) — noch nicht implementiert
                // TODO feat/alliances: Commands.slash("alliance", ...) — noch nicht implementiert

                // Phase 8 — Prestige
                Commands.slash("prestige", "Prestige-Flow starten (ab $1M Valuation)"),
                Commands.slash("legacy", "Deine Prestige-History und Achievements"),

                // Phase 9 — Polish
                Commands.slash("achievements", "Deine freigeschalteten Achievements"),

                // Admin
                Commands.slash("admin", "Admin-Commands")
                        .addOption(OptionType.STRING, "aktion",
                                "tick_skip | reset | spawn | event", true)
                        .addOption(OptionType.USER, "spieler", "Ziel-Spieler", false)
                        .addOption(OptionType.STRING, "wert", "Wert (firma, markt, event-typ)", false)

        ).queue();

        return jda;
    }
}
