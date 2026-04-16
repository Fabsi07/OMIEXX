package dev.ceosim.discord.command;

import dev.ceosim.entity.Company;
import dev.ceosim.repository.CompanyRepository;
import dev.ceosim.util.CeoEmbedBuilder;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HelpCommand {

    private final CompanyRepository companyRepo;

    public void handle(SlashCommandInteractionEvent event) {
        Company company = companyRepo
                .findActiveByDiscordId(event.getUser().getId())
                .orElse(null);

        boolean hasCompany = company != null;

        EmbedBuilder eb = CeoEmbedBuilder.base("📖 CEO Sim — Commands", CeoEmbedBuilder.blue())
                .setDescription("Alle verfügbaren Commands. Gesperrte Commands sind mit 🔒 markiert.");

        // ── Immer verfügbar ──────────────────────────────────────────────
        eb.addField("─── Kein Cooldown ───",
                """
                `/start` — Firma gründen (einmalig)
                `/report` — Company Dashboard
                `/team` — Dein Team anzeigen
                `/market` — Markt-Leaderboard
                `/log` — Letzte Events
                `/project list` — Verfügbare Projekte
                `/project status` — Aktives Projekt
                `/research tree` — Tech-Tree
                `/research status` — Research Points
                `/profile [firma]` — Firmenprofil
                `/legacy` — Prestige-History
                `/achievements` — Deine Abzeichen
                `/help` — Dieser Command
                `/notify [event]` — Benachrichtigungen
                """, false);

        // ── 3–4h ────────────────────────────────────────────────────────
        eb.addField("─── 3–4h Cooldown ───",
                """
                `/hire [rolle]` — Mitarbeiter einstellen
                `/fire [name]` — Mitarbeiter feuern
                `/invest [betrag]` — Kapital in KPI pumpen
                `/project boost` — Projekt beschleunigen
                `/loan [betrag]` — Kredit aufnehmen
                `/deal @user` — Handelsangebot schicken
                `/contract @user` — Liefervertrag schließen
                `/invest-in @firma` — Anteile kaufen
                """, false);

        // ── 6–8h ────────────────────────────────────────────────────────
        eb.addField("─── 6–8h Cooldown ───",
                "/pr [positiv|negativ] — PR-Kampagne\n"
                + "/acquire [firma] — NPC akquirieren" + lock(!hasCompany || company.getActiveEmployees().size() < 3, "3 Mitarbeiter nötig") + "\n"
                + "/sabotage @user [aktion] — Sabotage" + lock(!hasCompany || company.getValuation() < 5_000_000L, "$50k Valuation nötig") + "\n"
                + "/project start [id] — Projekt starten\n",
                false);

        // ── 24h ─────────────────────────────────────────────────────────
        eb.addField("─── 24h Cooldown ───",
                "/research pick [node] — Node freischalten\n"
                + "/expand [markt] — Markt betreten\n"
                + "/fundraise — VC-Pitch" + lock(!hasCompany || company.getTickCount() < 5, "ab Tick 5") + "\n"
                + "/alliance create [name] — Allianz gründen\n"
                + "/prestige — Prestige-Flow" + lock(!hasCompany || company.getValuation() < 100_000_000L, "$1M Valuation nötig") + "\n"
                + "/pause — Firma pausieren (2×/Monat)\n",
                false);

        eb.setFooter("CEO Sim • Tick alle 6h • Viel Erfolg, CEO!");

        event.replyEmbeds(eb.build()).setEphemeral(true).queue();
    }

    private String lock(boolean locked, String reason) {
        return locked ? "  🔒 *" + reason + "*" : "";
    }
}
