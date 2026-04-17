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

import java.awt.*;
import java.time.Duration;
import java.util.Map;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class FundraiseCommand {

    private final CompanyRepository companyRepo;
    private final CooldownService   cooldownService;
    private final EventService      eventService;

    private static final Duration COOLDOWN      = Duration.ofHours(24);
    private static final int      MIN_TICK      = 5;
    private static final Random   RANDOM        = new Random();

    public void handle(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();

        Duration cd = cooldownService.getRemaining(discordId, "fundraise");
        if (cd != null) {
            event.reply("⏳ `/fundraise` ist noch für **" + CooldownService.format(cd) + "** auf Cooldown.")
                    .setEphemeral(true).queue();
            return;
        }

        Company company = companyRepo.findActiveByDiscordId(discordId).orElse(null);
        if (company == null) {
            event.reply("❌ Keine aktive Firma. Starte mit `/start`!").setEphemeral(true).queue();
            return;
        }

        if (company.getTickCount() < MIN_TICK) {
            event.reply("🔒 `/fundraise` ist erst ab **Tick " + MIN_TICK + "** verfügbar.\n" +
                    "Aktuell: Tick " + company.getTickCount() +
                    " — VCs wollen erste Traktion sehen!")
                    .setEphemeral(true).queue();
            return;
        }

        FundraiseResult result = calculateOutcome(company);
        applyResult(company, result);
        cooldownService.set(discordId, "fundraise", COOLDOWN);

        EmbedBuilder eb = OmniexxEmbedBuilder.base(result.title, result.color)
                .setDescription(result.description)
                .addField("Bewertung deiner Firma", buildScorecard(company), false);

        if (result.capitalGained > 0) {
            eb.addField("💰 Kapital erhalten", OmniexxEmbedBuilder.formatMoney(result.capitalGained), true);
        }
        if (result.equityGiven > 0) {
            eb.addField("📉 Equity abgegeben", result.equityGiven + "%", true);
        }
        if (result.reputationDelta != 0) {
            String sign = result.reputationDelta > 0 ? "+" : "";
            eb.addField("⭐ Reputation", sign + result.reputationDelta, true);
        }

        eb.setFooter("Cooldown: 24h • Nächster Pitch in 24h möglich");
        event.replyEmbeds(eb.build()).queue();
    }

    private FundraiseResult calculateOutcome(Company company) {
        // Score basiert auf mehreren Faktoren (0–100)
        int score = 0;
        score += Math.min(30, company.getReputation() / 3);              // max 30 Punkte
        score += Math.min(20, (int)(company.getTickCount() * 1.5));      // max 20 (Tick 13+)
        score += Math.min(25, (int)(company.getRevenuePerTick() / 50_000L)); // max 25
        score += Math.min(15, company.getActiveEmployees().size() * 2);  // max 15 (7+ MA)
        score += RANDOM.nextInt(20);                                      // Zufalls-Faktor ±20

        long baseValuation = company.calculateValuation();

        if (score >= 70) {
            // Großer Erfolg: Tier-A VC
            long capital = (long)(baseValuation * 0.20); // 20% der Valuation
            return FundraiseResult.success(
                    "🎉 Tier-A VC Deal!",
                    "**Sequoia Capital** sieht das Potenzial. Sie investieren " +
                    OmniexxEmbedBuilder.formatMoney(capital) + " für 15% Equity.",
                    capital, 15, 5, OmniexxEmbedBuilder.green()
            );
        } else if (score >= 50) {
            // Normaler Erfolg: Angel/Seed
            long capital = (long)(baseValuation * 0.10);
            return FundraiseResult.success(
                    "✅ Seed-Runde erfolgreich",
                    "Ein Angel-Investor glaubt an deine Vision. " +
                    OmniexxEmbedBuilder.formatMoney(capital) + " für 20% Equity.",
                    capital, 20, 2, OmniexxEmbedBuilder.green()
            );
        } else if (score >= 35) {
            // Teilerfolg: Kleinere Finanzierung mit Auflagen
            long capital = (long)(baseValuation * 0.05);
            return FundraiseResult.conditional(
                    "⚠️ Bedingte Finanzierung",
                    "Ein Investor ist interessiert, aber stellt Bedingungen: " +
                    OmniexxEmbedBuilder.formatMoney(capital) + " — aber du musst in 3 Ticks " +
                    "profitabel sein, sonst folgt eine Nachverhandlung.",
                    capital, 25, 0
            );
        } else if (score >= 20) {
            // Ablehnung mit Feedback
            return FundraiseResult.rejected(
                    "❌ Abgelehnt",
                    "\"Gutes Produkt, aber zu früh.\" — Der VC gibt konstruktives Feedback: " +
                    "deine Reputation und dein Revenue/Tick brauchen noch Wachstum."
            );
        } else {
            // Schlechte Ablehnung — schadet Reputation
            return FundraiseResult.badRejected(
                    "💀 Ablehnung mit Reputationsschaden",
                    "Der Pitch lief katastrophal. Der VC postet auf Twitter über dein \"unrealistisches Pitch Deck\". " +
                    "Reputation −5."
            );
        }
    }

    @Transactional
    public void applyResult(Company company, FundraiseResult result) {
        if (result.capitalGained > 0) {
            company.setCapital(company.getCapital() + result.capitalGained);
        }
        if (result.reputationDelta != 0) {
            int newRep = Math.max(0, Math.min(100, company.getReputation() + result.reputationDelta));
            company.setReputation((short) newRep);
        }
        companyRepo.save(company);

        eventService.log(company, "fundraise_" + (result.capitalGained > 0 ? "success" : "failed"),
                result.title,
                result.description,
                Map.of("capital", (long) result.capitalGained,
                       "reputation", (long) result.reputationDelta));
    }

    private String buildScorecard(Company company) {
        return String.format("""
                Reputation:     %d/100
                Revenue/Tick:   %s
                Mitarbeiter:    %d
                Tick:           #%d
                Valuation:      %s
                """,
                company.getReputation(),
                OmniexxEmbedBuilder.formatMoney(company.getRevenuePerTick()),
                company.getActiveEmployees().size(),
                company.getTickCount(),
                OmniexxEmbedBuilder.formatMoney(company.calculateValuation())
        );
    }

    // ── Result-Klasse ──────────────────────────────────────────────────
    record FundraiseResult(String title, String description, long capitalGained,
                           int equityGiven, int reputationDelta, Color color) {

        static FundraiseResult success(String t, String d, long cap, int eq, int rep, Color c) {
            return new FundraiseResult(t, d, cap, eq, rep, c);
        }
        static FundraiseResult conditional(String t, String d, long cap, int eq, int rep) {
            return new FundraiseResult(t, d, cap, eq, rep, new Color(0xF1C40F));
        }
        static FundraiseResult rejected(String t, String d) {
            return new FundraiseResult(t, d, 0, 0, 0, new Color(0xE74C3C));
        }
        static FundraiseResult badRejected(String t, String d) {
            return new FundraiseResult(t, d, 0, 0, -5, new Color(0xC0392B));
        }
    }
}
