package dev.ceosim.discord.command;

import dev.ceosim.entity.Company;
import dev.ceosim.repository.CompanyRepository;
import dev.ceosim.service.CooldownService;
import dev.ceosim.service.EventService;
import dev.ceosim.util.CeoEmbedBuilder;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Random;

@Component("investCommand")
@RequiredArgsConstructor
public class InvestCommand {

    private final CompanyRepository companyRepo;
    private final CooldownService   cooldownService;
    private final EventService      eventService;

    private static final Duration COOLDOWN = Duration.ofHours(4);

    public void handle(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();
        Duration cd = cooldownService.getRemaining(discordId, "invest");
        if (cd != null) {
            event.reply("⏳ Cooldown: noch **" + CooldownService.format(cd) + "**")
                    .setEphemeral(true).queue();
            return;
        }

        Company company = companyRepo.findActiveByDiscordId(discordId).orElse(null);
        if (company == null) { event.reply("❌ Keine aktive Firma!").setEphemeral(true).queue(); return; }

        long betrag = event.getOption("betrag").getAsLong() * 100; // $ → Cents
        if (betrag <= 0) { event.reply("❌ Betrag muss positiv sein.").setEphemeral(true).queue(); return; }
        if (company.getCapital() < betrag) {
            event.reply("❌ Zu wenig Kapital! Vorhanden: " + CeoEmbedBuilder.formatMoney(company.getCapital()))
                    .setEphemeral(true).queue(); return;
        }

        // 40% Morale, 30% Marktanteil, 30% Revenue
        company.setCapital(company.getCapital() - betrag);
        String effect;
        int roll = new Random().nextInt(3);
        if (roll == 0) {
            company.setMorale((short) Math.min(100, company.getMorale() + 15));
            effect = "Morale +15";
        } else if (roll == 1) {
            double newShare = Math.min(50.0, company.getMarketShare().doubleValue() + 2.0);
            company.setMarketShare(java.math.BigDecimal.valueOf(newShare));
            effect = "Marktanteil +2%";
        } else {
            company.setRevenuePerTick(company.getRevenuePerTick() + betrag / 10);
            effect = "Revenue +" + CeoEmbedBuilder.formatMoney(betrag / 10) + "/Tick";
        }
        companyRepo.save(company);
        cooldownService.set(discordId, "invest", COOLDOWN);
        eventService.log(company, "invest", "💸 Kapital-Investition", effect, Map.of("capital", -betrag));
        event.reply("💸 **" + CeoEmbedBuilder.formatMoney(betrag) + " investiert** → " + effect).queue();
    }
}
