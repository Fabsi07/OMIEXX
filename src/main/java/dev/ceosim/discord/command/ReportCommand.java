package dev.ceosim.discord.command;

import dev.ceosim.entity.Company;
import dev.ceosim.repository.CompanyRepository;
import dev.ceosim.util.CeoEmbedBuilder;
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

        event.replyEmbeds(CeoEmbedBuilder.reportEmbed(company).build()).queue();
    }
}
