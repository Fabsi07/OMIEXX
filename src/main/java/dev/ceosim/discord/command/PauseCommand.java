package dev.ceosim.discord.command;

import dev.ceosim.entity.Company;
import dev.ceosim.repository.CompanyRepository;
import dev.ceosim.service.EventService;
import dev.ceosim.util.CeoEmbedBuilder;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class PauseCommand {

    private final CompanyRepository companyRepo;
    private final EventService      eventService;

    private static final int MAX_PAUSES_PER_MONTH = 2;
    private static final int PAUSE_HOURS          = 48;

    public void handle(SlashCommandInteractionEvent event) {
        Company company = companyRepo.findActiveByDiscordId(event.getUser().getId()).orElse(null);
        if (company == null) {
            event.reply("❌ Keine aktive Firma. Starte mit `/start`!").setEphemeral(true).queue();
            return;
        }

        // Bereits pausiert?
        if (company.getPaused() && company.getPauseUntil() != null
                && company.getPauseUntil().isAfter(OffsetDateTime.now())) {
            String until = company.getPauseUntil().format(DateTimeFormatter.ofPattern("dd.MM HH:mm"));
            event.reply("⏸️ Deine Firma ist bereits pausiert bis **" + until + "**.\n" +
                    "Tipp: Während der Pause laufen keine Salary-Abzüge und keine Events.")
                    .setEphemeral(true).queue();
            return;
        }

        // Monatliches Limit prüfen (vereinfacht: pausesUsed zählt im aktuellen Monat)
        if (company.getPausesUsed() >= MAX_PAUSES_PER_MONTH) {
            event.reply("🔒 Du hast `/pause` diesen Monat bereits **" + MAX_PAUSES_PER_MONTH +
                    "× genutzt** — Maximum erreicht.")
                    .setEphemeral(true).queue();
            return;
        }

        doPause(company);

        String until = company.getPauseUntil().format(DateTimeFormatter.ofPattern("dd.MM.yy HH:mm"));
        event.reply("⏸️ **Firma pausiert bis " + until + "**\n\n" +
                "Während der Pause:\n" +
                "• Kein Salary-Abzug\n" +
                "• Keine Tick-Events\n" +
                "• Kein Kredit-Zins\n\n" +
                "Genutzt: " + company.getPausesUsed() + "/" + MAX_PAUSES_PER_MONTH + " diesen Monat")
                .setEphemeral(true).queue();
    }

    @Transactional
    protected void doPause(Company company) {
        company.setPaused(true);
        company.setPauseUntil(OffsetDateTime.now().plusHours(PAUSE_HOURS));
        company.setPausesUsed((short) (company.getPausesUsed() + 1));
        companyRepo.save(company);
        eventService.log(company, "pause", "⏸️ Firma pausiert für 48h", null, null);
    }
}
