package dev.omniexx.discord.command;

import dev.omniexx.entity.Company;
import dev.omniexx.entity.Employee;
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
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FireCommand {

    private final CompanyRepository companyRepo;
    private final CompanyService    companyService;
    private final CooldownService   cooldownService;

    private static final Duration COOLDOWN = Duration.ofMinutes(3);

    public void handle(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();
        String name = event.getOption("name").getAsString().trim();

        Duration remaining = cooldownService.getRemaining(discordId, "fire");
        if (remaining != null) {
            event.reply("⏳ `/fire` ist noch für **" + CooldownService.format(remaining) + "** auf Cooldown.")
                    .setEphemeral(true).queue();
            return;
        }

        Company company = companyRepo.findActiveByDiscordId(discordId).orElse(null);
        if (company == null) {
            event.reply("❌ Keine aktive Firma. Starte mit `/start`!").setEphemeral(true).queue();
            return;
        }

        // Fuzzy-Match: case-insensitiv, auch nur Vorname möglich
        Employee match = findEmployee(company, name);
        if (match == null) {
            String available = company.getActiveEmployees().stream()
                    .map(Employee::getFullName)
                    .collect(Collectors.joining("\n• ", "• ", ""));
            event.reply("❌ Mitarbeiter **\"" + name + "\"** nicht gefunden.\n\n**Dein Team:**\n" + available)
                    .setEphemeral(true).queue();
            return;
        }

        try {
            companyService.fireEmployee(company, match.getFullName());
            cooldownService.set(discordId, "fire", COOLDOWN);

            EmbedBuilder eb = OmniexxEmbedBuilder.base("🚪 Mitarbeiter entlassen", OmniexxEmbedBuilder.red())
                    .addField("Name",      match.getFullName(), true)
                    .addField("Rolle",     capitalize(match.getRole()), true)
                    .addField("Morale",    "−8 (Team reagiert unruhig)", true)
                    .addField("Ersparnis", OmniexxEmbedBuilder.formatMoney(match.getSalaryPerTick()) + "/Tick weniger Burnrate", false)
                    .setFooter("Cooldown: 3h • Häufige Entlassungen schaden der Reputation");

            event.replyEmbeds(eb.build()).queue();

        } catch (IllegalArgumentException e) {
            event.reply("❌ " + e.getMessage()).setEphemeral(true).queue();
        }
    }

    private Employee findEmployee(Company company, String input) {
        String lower = input.toLowerCase();
        // Exakter Match zuerst
        return company.getActiveEmployees().stream()
                .filter(e -> e.getFullName().equalsIgnoreCase(input)
                          || e.getFirstName().equalsIgnoreCase(input)
                          || e.getFullName().toLowerCase().contains(lower))
                .findFirst()
                .orElse(null);
    }

    private String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).replace("_", " ");
    }
}
