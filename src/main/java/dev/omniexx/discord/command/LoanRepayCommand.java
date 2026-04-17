package dev.omniexx.discord.command;

import dev.omniexx.entity.Company;
import dev.omniexx.repository.CompanyRepository;
import dev.omniexx.service.CooldownService;
import dev.omniexx.service.EventService;
import dev.omniexx.util.OmniexxEmbedBuilder;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LoanRepayCommand {

    private final CompanyRepository companyRepo;
    private final CooldownService   cooldownService;
    private final EventService      eventService;

    private static final Duration COOLDOWN = Duration.ofHours(2);

    public void handle(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();

        Duration cd = cooldownService.getRemaining(discordId, "loan_repay");
        if (cd != null) {
            event.reply("⏳ Cooldown: noch **" + CooldownService.format(cd) + "**")
                    .setEphemeral(true).queue();
            return;
        }

        Company company = companyRepo.findActiveByDiscordId(discordId).orElse(null);
        if (company == null) {
            event.reply("❌ Keine aktive Firma. Starte mit `/start`!").setEphemeral(true).queue();
            return;
        }

        if (company.getLoanBalance() <= 0) {
            event.reply("✅ Du hast keine offenen Kredite!").setEphemeral(true).queue();
            return;
        }

        long betrag = event.getOption("betrag") != null
                ? event.getOption("betrag").getAsLong() * 100L
                : 0L;

        if (betrag <= 0) {
            event.reply("❌ Betrag muss positiv sein.").setEphemeral(true).queue();
            return;
        }

        if (company.getCapital() < betrag) {
            event.reply("❌ Zu wenig Kapital! Vorhanden: **" +
                    OmniexxEmbedBuilder.formatMoney(company.getCapital()) + "**")
                    .setEphemeral(true).queue();
            return;
        }

        // Maximal den offenen Betrag zurückzahlen
        long tatsaechlich = Math.min(betrag, company.getLoanBalance());
        repay(company, tatsaechlich);
        cooldownService.set(discordId, "loan_repay", COOLDOWN);

        EmbedBuilder eb = OmniexxEmbedBuilder.base("💳 Kredit zurückgezahlt", OmniexxEmbedBuilder.green())
                .addField("Zurückgezahlt",   OmniexxEmbedBuilder.formatMoney(tatsaechlich), true)
                .addField("Verbleibende Schulden", OmniexxEmbedBuilder.formatMoney(company.getLoanBalance()), true)
                .addField("Kapital danach",  OmniexxEmbedBuilder.formatMoney(company.getCapital()), true)
                .setFooter(company.getLoanBalance() == 0 ? "🎉 Schuldenfrei!" : "Cooldown: 2h");

        event.replyEmbeds(eb.build()).queue();
    }

    @Transactional
    public void repay(Company company, long betrag) {
        company.setCapital(company.getCapital() - betrag);
        company.setLoanBalance(Math.max(0, company.getLoanBalance() - betrag));
        companyRepo.save(company);

        eventService.log(company, "loan_repaid",
                "💳 Kredit zurückgezahlt: " + OmniexxEmbedBuilder.formatMoney(betrag),
                "Verbleibend: " + OmniexxEmbedBuilder.formatMoney(company.getLoanBalance()),
                Map.of("capital", -betrag, "loan_balance", -betrag));
    }
}
