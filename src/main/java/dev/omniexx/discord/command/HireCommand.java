package dev.omniexx.discord.command;

import dev.omniexx.entity.Company;
import dev.omniexx.entity.Employee;
import dev.omniexx.entity.Market;
import dev.omniexx.repository.CompanyRepository;
import dev.omniexx.service.CompanyService;
import dev.omniexx.service.CooldownService;
import dev.omniexx.util.OmniexxEmbedBuilder;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class HireCommand {

    private final CompanyRepository companyRepo;
    private final CompanyService    companyService;
    private final CooldownService   cooldownService;

    private static final Duration COOLDOWN = Duration.ofHours(3);
    private static final int MAX_EMPLOYEES = 20;

    // Basis-Rollen — immer verfügbar
    private static final Set<String> BASE_ROLES = Set.of(
            "cto", "cfo", "dev", "designer", "marketing", "sales"
    );
    // Freischaltbar via Tech-Tree
    private static final Set<String> TECH_TREE_ROLES = Set.of(
            "hr", "compliance_officer"
    );
    // Nur Cybersec-Markt
    private static final Set<String> CYBERSEC_ROLES = Set.of(
            "pen_tester", "malware_analyst", "soc_analyst"
    );

    public void handle(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();
        String role = event.getOption("rolle").getAsString().toLowerCase().trim();

        // Cooldown check
        Duration remaining = cooldownService.getRemaining(discordId, "hire");
        if (remaining != null) {
            event.reply("⏳ `/hire` ist noch für **" + CooldownService.format(remaining) + "** auf Cooldown.")
                    .setEphemeral(true).queue();
            return;
        }

        Company company = companyRepo.findActiveByDiscordId(discordId).orElse(null);
        if (company == null) {
            event.reply("❌ Keine aktive Firma. Starte mit `/start`!").setEphemeral(true).queue();
            return;
        }

        // Max-Mitarbeiter check
        if (company.getActiveEmployees().size() >= MAX_EMPLOYEES) {
            event.reply("❌ Maximale Mitarbeiterzahl (" + MAX_EMPLOYEES + ") erreicht!")
                    .setEphemeral(true).queue();
            return;
        }

        // Rollen-Validierung
        if (!isRoleAllowed(role, company)) {
            event.reply(buildRoleError(role, company)).setEphemeral(true).queue();
            return;
        }

        // Gehalt check — kann man sich das leisten?
        long salary = salaryForRole(role);
        if (company.getCapital() < salary * 3) { // 3 Ticks Puffer
            event.reply("❌ Zu wenig Kapital für diesen Mitarbeiter! Brauche mindestens **" +
                    OmniexxEmbedBuilder.formatMoney(salary * 3) + "** als Puffer.")
                    .setEphemeral(true).queue();
            return;
        }

        Employee emp = companyService.hireEmployee(company, role);
        cooldownService.set(discordId, "hire", COOLDOWN);

        EmbedBuilder eb = OmniexxEmbedBuilder.base("👤 Neuer Mitarbeiter eingestellt!", OmniexxEmbedBuilder.green())
                .addField("Name",       emp.getFullName(), true)
                .addField("Rolle",      roleEmoji(role) + " " + capitalize(role), true)
                .addField("Skill",      skillBar(emp.getSkill()), true)
                .addField("Loyalty",    skillBar(emp.getLoyalty()), true)
                .addField("Gehalt",     OmniexxEmbedBuilder.formatMoney(emp.getSalaryPerTick()) + "/Tick", true)
                .addField("Team",       company.getActiveEmployees().size() + " Mitarbeiter", true)
                .setFooter("Cooldown: 3h • /team für Übersicht");

        event.replyEmbeds(eb.build()).queue();
    }

    private boolean isRoleAllowed(String role, Company company) {
        if (BASE_ROLES.contains(role)) return true;
        if (CYBERSEC_ROLES.contains(role)) return company.getMarket() == Market.CYBERSECURITY;
        if (TECH_TREE_ROLES.contains(role)) {
            // TODO: Tech-Tree Node check (Phase 6)
            // Vorerst gesperrt
            return false;
        }
        return false;
    }

    private String buildRoleError(String role, Company company) {
        if (CYBERSEC_ROLES.contains(role)) {
            return "🔒 **" + capitalize(role) + "** ist nur im Cybersecurity-Markt verfügbar!\n" +
                   "Dein Markt: " + company.getMarket().getDisplayName();
        }
        if (TECH_TREE_ROLES.contains(role)) {
            return "🔒 **" + capitalize(role) + "** muss erst im Tech-Tree freigeschaltet werden!";
        }
        return "❌ Unbekannte Rolle: **" + role + "**\n\n" +
               "Verfügbare Rollen: `cto`, `cfo`, `dev`, `designer`, `marketing`, `sales`\n" +
               (company.getMarket() == Market.CYBERSECURITY
                       ? "Cybersec-exklusiv: `pen_tester`, `malware_analyst`, `soc_analyst`" : "");
    }

    private long salaryForRole(String role) {
        return switch (role) {
            case "cto", "cfo"                                    -> 150_000L;
            case "dev"                                           -> 80_000L;
            case "designer", "marketing", "sales"               -> 60_000L;
            case "hr", "compliance_officer"                     -> 70_000L;
            case "pen_tester", "malware_analyst", "soc_analyst" -> 120_000L;
            default                                             -> 50_000L;
        };
    }

    private String skillBar(int value) {
        return "⭐".repeat(value / 2) + (value % 2 == 1 ? "½" : "") + "  " + value + "/10";
    }

    private String roleEmoji(String role) {
        return switch (role) {
            case "cto"               -> "⚙️";
            case "cfo"               -> "💰";
            case "dev"               -> "💻";
            case "designer"          -> "🎨";
            case "marketing"         -> "📣";
            case "sales"             -> "🤝";
            case "hr"                -> "👔";
            case "compliance_officer"-> "📋";
            case "pen_tester"        -> "🔓";
            case "malware_analyst"   -> "🦠";
            case "soc_analyst"       -> "🛡️";
            default                  -> "👤";
        };
    }

    private String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).replace("_", " ");
    }
}
