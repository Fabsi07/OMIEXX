package dev.omniexx.discord.command;

import dev.omniexx.entity.Company;
import dev.omniexx.entity.CompanyProject;
import dev.omniexx.repository.CompanyRepository;
import dev.omniexx.service.CooldownService;
import dev.omniexx.service.project.ProjectDefinition;
import dev.omniexx.service.project.ProjectService;
import dev.omniexx.util.OmniexxEmbedBuilder;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProjectCommand {

    private final CompanyRepository companyRepo;
    private final ProjectService    projectService;
    private final CooldownService   cooldownService;

    private static final Duration START_COOLDOWN  = Duration.ofHours(24);
    private static final Duration BOOST_COOLDOWN  = Duration.ofHours(4);

    public void handle(SlashCommandInteractionEvent event) {
        String aktion = event.getOption("aktion").getAsString().toLowerCase().trim();

        switch (aktion) {
            case "list"   -> handleList(event);
            case "start"  -> handleStart(event);
            case "status" -> handleStatus(event);
            case "boost"  -> handleBoost(event);
            case "cancel" -> handleCancel(event);
            default -> event.reply("❌ Unbekannte Aktion. Nutze: `list`, `start`, `status`, `boost`, `cancel`")
                    .setEphemeral(true).queue();
        }
    }

    // ── /project list ────────────────────────────────────────────────────
    private void handleList(SlashCommandInteractionEvent event) {
        Company company = getCompany(event);
        if (company == null) return;

        List<ProjectDefinition> projects = ProjectDefinition.forMarket(company.getMarket());
        CompanyProject active = company.getActiveProject();

        EmbedBuilder eb = OmniexxEmbedBuilder.base(
                "📋 Projekte — " + company.getMarket().getDisplayName(), OmniexxEmbedBuilder.blue());

        StringBuilder sb = new StringBuilder();
        for (ProjectDefinition def : projects) {
            boolean isActive = active != null && active.getProjectKey().equals(def.getKey());
            boolean done = company.getProjects().stream()
                    .anyMatch(p -> p.getProjectKey().equals(def.getKey())
                                && p.getStatus().startsWith("completed"));

            String status = isActive ? " 🔄 *aktiv*" : done ? " ✅ *abgeschlossen*" : "";
            String lock   = done ? "~~" : "";

            sb.append(String.format("**`%s`** %s— %s%s%s%n", def.getKey(), lock, def.getName(), lock, status));
            sb.append(String.format("└ ⏱ %d Ticks  •  💰 %s  •  🎁 %s%n",
                    def.getDurationTicks(),
                    OmniexxEmbedBuilder.formatMoney(def.getCost()),
                    def.getReward()));
            if (!def.getRequirements().isEmpty()) {
                sb.append("└ 🔒 *").append(String.join(", ", def.getRequirements())).append("*\n");
            }
            sb.append("\n");
        }

        eb.setDescription(sb.toString());
        eb.setFooter("Starten: /project start [key] • Nur 1 Projekt gleichzeitig aktiv");
        event.replyEmbeds(eb.build()).queue();
    }

    // ── /project start ───────────────────────────────────────────────────
    private void handleStart(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();

        Duration cd = cooldownService.getRemaining(discordId, "project_start");
        if (cd != null) {
            event.reply("⏳ `/project start` ist noch für **" + CooldownService.format(cd) + "** auf Cooldown.")
                    .setEphemeral(true).queue();
            return;
        }

        var idOption = event.getOption("id");
        if (idOption == null) {
            event.reply("❌ Bitte gib eine Projekt-ID an: `/project start [id]`\nNutze `/project list` für eine Übersicht.")
                    .setEphemeral(true).queue();
            return;
        }

        Company company = getCompany(event);
        if (company == null) return;

        String key = idOption.getAsString().trim().toLowerCase();
        ProjectDefinition def = ProjectDefinition.byKey(key);

        if (def == null) {
            event.reply("❌ Unbekannte Projekt-ID: `" + key + "`\nNutze `/project list` für alle verfügbaren Projekte.")
                    .setEphemeral(true).queue();
            return;
        }

        if (def.getMarket() != company.getMarket()) {
            event.reply("❌ Dieses Projekt gehört zum Markt **" + def.getMarket().getDisplayName() +
                    "**, du bist aber in **" + company.getMarket().getDisplayName() + "**.")
                    .setEphemeral(true).queue();
            return;
        }

        try {
            CompanyProject project = projectService.startProject(company, key);
            cooldownService.set(discordId, "project_start", START_COOLDOWN);

            EmbedBuilder eb = OmniexxEmbedBuilder.base("🚀 Projekt gestartet!", OmniexxEmbedBuilder.green())
                    .addField("Projekt",   def.getName(), true)
                    .addField("Dauer",     def.getDurationTicks() + " Ticks (~" + (def.getDurationTicks() * 6) + "h)", true)
                    .addField("Kosten",    OmniexxEmbedBuilder.formatMoney(def.getCost()), true)
                    .addField("Reward",    def.getReward(), false)
                    .addField("Freischaltet", def.getUnlocks(), false)
                    .setDescription(def.getDescription())
                    .setFooter("Status: /project status • Beschleunigen: /project boost");

            event.replyEmbeds(eb.build()).queue();

        } catch (IllegalStateException | IllegalArgumentException e) {
            event.reply("❌ " + e.getMessage()).setEphemeral(true).queue();
        }
    }

    // ── /project status ──────────────────────────────────────────────────
    private void handleStatus(SlashCommandInteractionEvent event) {
        Company company = getCompany(event);
        if (company == null) return;

        CompanyProject active = company.getActiveProject();
        if (active == null) {
            event.reply("📋 Kein aktives Projekt.\nNutze `/project list` um eines zu starten.")
                    .setEphemeral(true).queue();
            return;
        }

        ProjectDefinition def = ProjectDefinition.byKey(active.getProjectKey());
        int done    = active.getTicksTotal() - active.getTicksRemaining();
        int total   = active.getTicksTotal();
        int pct     = (int) ((done / (float) total) * 100);

        String bar = progressBar(done, total);

        EmbedBuilder eb = OmniexxEmbedBuilder.base("🔄 Aktives Projekt", OmniexxEmbedBuilder.blue())
                .addField("Projekt",     def != null ? def.getName() : active.getProjectKey(), false)
                .addField("Fortschritt", bar + "  " + pct + "%", false)
                .addField("Ticks",       done + " / " + total + " abgeschlossen", true)
                .addField("Verbleibend", active.getTicksRemaining() + " Ticks (~" + (active.getTicksRemaining() * 6) + "h)", true)
                .addField("Boosts",      active.getBoostsUsed() + " / 2 genutzt", true)
                .setFooter("Beschleunigen: /project boost • Abbrechen: /project cancel (50% Verlust)");

        event.replyEmbeds(eb.build()).queue();
    }

    // ── /project boost ───────────────────────────────────────────────────
    private void handleBoost(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();

        Duration cd = cooldownService.getRemaining(discordId, "project_boost");
        if (cd != null) {
            event.reply("⏳ `/project boost` ist noch für **" + CooldownService.format(cd) + "** auf Cooldown.")
                    .setEphemeral(true).queue();
            return;
        }

        Company company = getCompany(event);
        if (company == null) return;

        try {
            projectService.boostProject(company);
            cooldownService.set(discordId, "project_boost", BOOST_COOLDOWN);

            CompanyProject proj = company.getActiveProject();
            event.reply("⚡ **Boost aktiviert!**\n" +
                    "Ticks verbleibend: **" + proj.getTicksRemaining() + "**\n" +
                    "Morale: −2 (Team arbeitet auf Hochtouren)\n" +
                    "Boosts genutzt: " + proj.getBoostsUsed() + " / 2").queue();

        } catch (IllegalStateException e) {
            event.reply("❌ " + e.getMessage()).setEphemeral(true).queue();
        }
    }

    // ── /project cancel ──────────────────────────────────────────────────
    private void handleCancel(SlashCommandInteractionEvent event) {
        Company company = getCompany(event);
        if (company == null) return;

        CompanyProject active = company.getActiveProject();
        if (active == null) {
            event.reply("❌ Kein aktives Projekt zum Abbrechen.").setEphemeral(true).queue();
            return;
        }

        try {
            projectService.cancelProject(company);
            event.reply("❌ **Projekt abgebrochen.**\n" +
                    "50% der Kosten wurden erstattet: " +
                    OmniexxEmbedBuilder.formatMoney(active.getCostPaid() / 2) +
                    "\nKein Cooldown auf den nächsten `/project start`.").queue();
        } catch (IllegalStateException e) {
            event.reply("❌ " + e.getMessage()).setEphemeral(true).queue();
        }
    }

    // ── Hilfsmethoden ────────────────────────────────────────────────────
    private Company getCompany(SlashCommandInteractionEvent event) {
        Company c = companyRepo.findActiveByDiscordId(event.getUser().getId()).orElse(null);
        if (c == null) {
            event.reply("❌ Keine aktive Firma. Starte mit `/start`!").setEphemeral(true).queue();
        }
        return c;
    }

    private String progressBar(int done, int total) {
        int filled = total == 0 ? 0 : (int) Math.round((done / (float) total) * 10);
        return "█".repeat(filled) + "░".repeat(10 - filled);
    }
}
