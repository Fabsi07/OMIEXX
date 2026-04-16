package dev.ceosim.discord.command;

import dev.ceosim.entity.Company;
import dev.ceosim.repository.CompanyRepository;
import dev.ceosim.service.CooldownService;
import dev.ceosim.service.EventService;
import dev.ceosim.util.CeoEmbedBuilder;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class PrCommand {

    private final CompanyRepository companyRepo;
    private final CooldownService   cooldownService;
    private final EventService      eventService;

    private static final Duration COOLDOWN = Duration.ofHours(6);
    private static final long     COST_POS = 300_000L;
    private static final long     COST_NEG = 150_000L;

    public void handle(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();
        Duration cd = cooldownService.getRemaining(discordId, "pr");
        if (cd != null) { event.reply("⏳ Cooldown: **" + CooldownService.format(cd) + "**").setEphemeral(true).queue(); return; }

        String typ = event.getOption("typ").getAsString().toLowerCase().trim();
        Company company = companyRepo.findActiveByDiscordId(discordId).orElse(null);
        if (company == null) { event.reply("❌ Keine aktive Firma!").setEphemeral(true).queue(); return; }

        boolean positive = typ.startsWith("pos");
        long cost = positive ? COST_POS : COST_NEG;

        if (company.getCapital() < cost) {
            event.reply("❌ Zu wenig Kapital! PR kostet " + CeoEmbedBuilder.formatMoney(cost))
                    .setEphemeral(true).queue(); return;
        }

        company.setCapital(company.getCapital() - cost);
        String result;

        if (positive) {
            company.setReputation((short) Math.min(100, company.getReputation() + 8));
            double newShare = Math.min(50.0, company.getMarketShare().doubleValue() + 1.5);
            company.setMarketShare(java.math.BigDecimal.valueOf(newShare));
            result = "✅ **Positive PR-Kampagne** — Reputation +8, Marktanteil +1.5%";
        } else {
            // Negative PR gegen Konkurrent — Backfire möglich
            boolean backfire = new Random().nextInt(100) < 25;
            if (backfire) {
                company.setReputation((short) Math.max(0, company.getReputation() - 5));
                result = "💥 **Backfire!** Negative Kampagne fiel auf dich zurück — Reputation −5";
            } else {
                result = "🗞️ **Negative PR gestartet** — Konkurrent unter Druck gesetzt (Effekt nächster Tick)";
            }
        }

        companyRepo.save(company);
        cooldownService.set(discordId, "pr", COOLDOWN);
        eventService.log(company, "pr_campaign", result, null, Map.of("capital", -cost));
        event.reply(result + "\nKosten: " + CeoEmbedBuilder.formatMoney(cost)).queue();
    }
}
