package dev.omniexx.discord.command;

import dev.omniexx.entity.Company;
import dev.omniexx.repository.CompanyRepository;
import dev.omniexx.util.OmniexxEmbedBuilder;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportCommand {

    private final CompanyRepository companyRepo;

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

        event.replyEmbeds(OmniexxEmbedBuilder.reportEmbed(company).build()).queue();
    }
}
