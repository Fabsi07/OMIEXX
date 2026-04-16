package dev.omniexx.discord.command;

import dev.omniexx.entity.Company;
import dev.omniexx.entity.NpcCompany;
import dev.omniexx.repository.CompanyRepository;
import dev.omniexx.repository.NpcCompanyRepository;
import dev.omniexx.service.CooldownService;
import dev.omniexx.service.EventService;
import dev.omniexx.util.OmniexxEmbedBuilder;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AcquireCommand {

    private final CompanyRepository    companyRepo;
    private final NpcCompanyRepository npcRepo;
    private final CooldownService      cooldownService;
    private final EventService         eventService;

    private static final Duration COOLDOWN      = Duration.ofHours(8);
    private static final int      MIN_EMPLOYEES = 3;
    private static final int      KARTELL_LIMIT = 4; // ab hier Kartellbehörde möglich

    public void handle(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();

        Duration cd = cooldownService.getRemaining(discordId, "acquire");
        if (cd != null) {
            event.reply("⏳ `/acquire` ist noch für **" + CooldownService.format(cd) + "** auf Cooldown.")
                    .setEphemeral(true).queue();
            return;
        }

        Company company = companyRepo.findActiveByDiscordId(discordId).orElse(null);
        if (company == null) {
            event.reply("❌ Keine aktive Firma. Starte mit `/start`!").setEphemeral(true).queue();
            return;
        }

        // Mindest-Mitarbeiter
        if (company.getActiveEmployees().size() < MIN_EMPLOYEES) {
            event.reply("🔒 Du brauchst mindestens **" + MIN_EMPLOYEES +
                    " Mitarbeiter** um Firmen zu akquirieren. Aktuell: " +
                    company.getActiveEmployees().size())
                    .setEphemeral(true).queue();
            return;
        }

        String targetName = event.getOption("firma").getAsString().trim();
        NpcCompany npc = npcRepo.findByNameIgnoreCaseAndAcquiredFalse(targetName).orElse(null);

        if (npc == null) {
            // Ähnliche Treffer anzeigen
            String suggestions = npcRepo.findByAcquiredFalse().stream()
                    .filter(n -> n.getMarket() == company.getMarket())
                    .map(NpcCompany::getName)
                    .limit(5)
                    .reduce((a, b) -> a + "\n• " + b)
                    .map(s -> "\n\n**Verfügbare Firmen in deinem Markt:**\n• " + s)
                    .orElse("");
            event.reply("❌ NPC-Firma **\"" + targetName + "\"** nicht gefunden oder bereits akquiriert." + suggestions)
                    .setEphemeral(true).queue();
            return;
        }

        // Kaufpreis = 150% der Valuation (Kontrollprämie)
        long price = (long) (npc.getValuation() * 1.5);
        if (company.getCapital() < price) {
            event.reply("❌ Zu wenig Kapital!\n" +
                    "Kaufpreis: **" + OmniexxEmbedBuilder.formatMoney(price) + "** (150% der Valuation)\n" +
                    "Dein Kapital: **" + OmniexxEmbedBuilder.formatMoney(company.getCapital()) + "**")
                    .setEphemeral(true).queue();
            return;
        }

        doAcquire(company, npc, price);
        cooldownService.set(discordId, "acquire", COOLDOWN);

        // Kartellbehörde check
        long acquisitions = company.getProjects().stream()
                .filter(p -> "acquisition".equals(p.getProjectKey()))
                .count();

        // Wir zählen acquisitions direkt über Events
        long acqCount = company.getProjects().size(); // vereinfacht — richtiger Count via Events

        EmbedBuilder eb = OmniexxEmbedBuilder.base("🏢 Akquisition erfolgreich!", OmniexxEmbedBuilder.green())
                .addField("Übernommen",  npc.getName(), true)
                .addField("Markt",       npc.getMarket().getDisplayName(), true)
                .addField("Kaufpreis",   OmniexxEmbedBuilder.formatMoney(price), true)
                .addField("Marktanteil", "+" + npc.getMarketShare() + "% übernommen", true)
                .addField("Persönlichkeit", personalityLabel(npc.getPersonality()), true)
                .addField("Valuation",   OmniexxEmbedBuilder.formatMoney(npc.getValuation()), true)
                .setFooter("Cooldown: 8h • Ab 4 Akquisitionen: Kartellbehörde möglich!");

        event.replyEmbeds(eb.build()).queue();
    }

    @Transactional
    protected void doAcquire(Company company, NpcCompany npc, long price) {
        company.setCapital(company.getCapital() - price);

        // Marktanteil übertragen
        BigDecimal newShare = company.getMarketShare().add(npc.getMarketShare());
        company.setMarketShare(newShare);

        // Revenue-Boost durch Übernahme
        long revenueBoost = npc.getValuation() / 100; // 1% der NPC-Valuation als Revenue/Tick
        company.setRevenuePerTick(company.getRevenuePerTick() + revenueBoost);

        // NPC als acquired markieren
        npc.setAcquired(true);
        npc.setAcquiredBy(company);
        npc.setAcquiredAt(OffsetDateTime.now());

        companyRepo.save(company);
        npcRepo.save(npc);

        eventService.log(company, "acquisition",
                "🏢 " + npc.getName() + " akquiriert",
                "Kaufpreis: " + OmniexxEmbedBuilder.formatMoney(price) +
                " | Revenue +: " + OmniexxEmbedBuilder.formatMoney(revenueBoost) + "/Tick",
                Map.of("capital", -price, "revenue_per_tick", revenueBoost));
    }

    private String personalityLabel(String p) {
        return switch (p) {
            case "aggressive"   -> "⚔️ Aggressiv";
            case "conservative" -> "🛡️ Konservativ";
            case "innovative"   -> "💡 Innovativ";
            default             -> p;
        };
    }
}
