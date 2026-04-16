package dev.omniexx.discord.command;

import dev.omniexx.entity.Company;
import dev.omniexx.entity.CompanyEvent;
import dev.omniexx.repository.CompanyRepository;
import dev.omniexx.service.EventService;
import dev.omniexx.util.OmniexxEmbedBuilder;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LogCommand {

    private final CompanyRepository companyRepo;
    private final EventService      eventService;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd.MM HH:mm");

    public void handle(SlashCommandInteractionEvent event) {
        Company company = companyRepo.findActiveByDiscordId(event.getUser().getId()).orElse(null);
        if (company == null) {
            event.reply("❌ Keine aktive Firma. Starte mit `/start`!").setEphemeral(true).queue();
            return;
        }

        List<CompanyEvent> events = eventService.getRecentEvents(company.getId(), 10);

        EmbedBuilder eb = OmniexxEmbedBuilder.base(
                "📜 Event Log — " + company.getName(), OmniexxEmbedBuilder.blue());

        if (events.isEmpty()) {
            eb.setDescription("Noch keine Events. Dein erster Tick passiert bald!");
        } else {
            StringBuilder sb = new StringBuilder();
            for (CompanyEvent e : events) {
                String time = e.getOccurredAt().format(FMT);
                sb.append(String.format("`%s` **%s**%n", time, e.getTitle()));
                if (e.getDescription() != null && !e.getDescription().isBlank()) {
                    sb.append("└ ").append(e.getDescription()).append("\n");
                }
                if (e.getKpiDelta() != null && !e.getKpiDelta().isEmpty()) {
                    String deltas = e.getKpiDelta().entrySet().stream()
                            .map(en -> en.getKey() + " " + (en.getValue() >= 0 ? "+" : "") + en.getValue())
                            .reduce((a, b) -> a + "  •  " + b)
                            .orElse("");
                    sb.append("└ *").append(deltas).append("*\n");
                }
                sb.append("\n");
            }
            eb.setDescription(sb.toString());
        }

        eb.setFooter("Tick #" + company.getTickCount() + "  •  Letzte 10 Events");
        event.replyEmbeds(eb.build()).queue();
    }
}
