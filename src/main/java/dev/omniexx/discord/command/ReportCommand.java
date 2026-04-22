package dev.omniexx.discord.command;

import dev.omniexx.entity.Company;
import dev.omniexx.repository.CompanyRepository;
import dev.omniexx.service.work.WorkService;
import dev.omniexx.util.OmniexxEmbedBuilder;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportCommand {

    private final CompanyRepository companyRepo;
    private final WorkService        workService;

    public void handle(SlashCommandInteractionEvent event) {
        Company company = companyRepo.findActiveByDiscordId(event.getUser().getId())
                .orElse(null);

        if (company == null) {
            event.reply("❌ Du hast noch keine Firma. Starte mit `/start`!")
                    .setEphemeral(true).queue();
            return;
        }

        // Valuation vor dem Anzeigen aktualisieren
        company.setValuation(company.calculateValuation());

        // Streak + Cooldown Info
        dev.omniexx.service.work.WorkStreak streak = workService.getStreak(company);
        var embed = OmniexxEmbedBuilder.reportEmbed(company);
        if (streak.getTotalSessions() > 0) {
            embed.addField("🔥 Work-Streak",
                    streak.getCurrentStreak() + " Tage  |  " + streak.getTotalSessions() + " Sessions gesamt",
                    true);
        } else {
            embed.addField("💼 Tipp", "Nutze `/work` um aktiv zu spielen und Belohnungen zu erhalten!", false);
        }
        // Valuation aktualisieren
        company.setValuation(company.calculateValuation());
        event.replyEmbeds(embed.build()).queue();
    }
}
