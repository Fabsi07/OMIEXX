package dev.ceosim.discord.command;

import dev.ceosim.entity.Company;
import dev.ceosim.entity.CompanyMarket;
import dev.ceosim.entity.Market;
import dev.ceosim.repository.CompanyRepository;
import dev.ceosim.service.CooldownService;
import dev.ceosim.service.EventService;
import dev.ceosim.util.CeoEmbedBuilder;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ExpandCommand {

    private final CompanyRepository companyRepo;
    private final CooldownService   cooldownService;
    private final EventService      eventService;

    private static final Duration COOLDOWN = Duration.ofHours(24);

    // Eintrittspreis pro Markt in Cents
    private static final Map<Market, Long> ENTRY_COST = Map.of(
        Market.CONSUMER_TECH,   500_000L,
        Market.ENTERPRISE_SAAS, 800_000L,
        Market.FINTECH,       1_500_000L,
        Market.E_COMMERCE,      600_000L,
        Market.CYBERSECURITY,   700_000L,
        Market.AI_DEEP_TECH,  2_000_000L,
        Market.HEALTHCARE,    2_500_000L,
        Market.MEDIA_GAMING,  1_000_000L,
        Market.GOVERNMENT,    3_000_000L
    );

    public void handle(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();

        Duration cd = cooldownService.getRemaining(discordId, "expand");
        if (cd != null) {
            event.reply("⏳ `/expand` ist noch für **" + CooldownService.format(cd) + "** auf Cooldown.")
                    .setEphemeral(true).queue();
            return;
        }

        Company company = companyRepo.findActiveByDiscordId(discordId).orElse(null);
        if (company == null) {
            event.reply("❌ Keine aktive Firma. Starte mit `/start`!").setEphemeral(true).queue();
            return;
        }

        // Markt parsen
        String marketInput = event.getOption("markt").getAsString().trim().toUpperCase()
                .replace(" ", "_").replace("-", "_").replace("&", "");
        Market target = parseMarket(marketInput);

        if (target == null) {
            String available = Arrays.stream(Market.values())
                    .map(m -> "`" + m.name().toLowerCase() + "` — " + m.getDisplayName())
                    .collect(Collectors.joining("\n"));
            event.reply("❌ Unbekannter Markt: **" + marketInput + "**\n\n**Verfügbare Märkte:**\n" + available)
                    .setEphemeral(true).queue();
            return;
        }

        // Bereits in dem Markt?
        boolean alreadyIn = company.getMarkets().stream()
                .anyMatch(cm -> cm.getMarket() == target);
        if (alreadyIn) {
            event.reply("❌ Du bist bereits im Markt **" + target.getDisplayName() + "**!")
                    .setEphemeral(true).queue();
            return;
        }

        long cost = ENTRY_COST.getOrDefault(target, 1_000_000L);
        if (company.getCapital() < cost) {
            event.reply("❌ Zu wenig Kapital!\n" +
                    "Eintrittspreis: **" + CeoEmbedBuilder.formatMoney(cost) + "**\n" +
                    "Dein Kapital: **" + CeoEmbedBuilder.formatMoney(company.getCapital()) + "**")
                    .setEphemeral(true).queue();
            return;
        }

        doExpand(company, target, cost);
        cooldownService.set(discordId, "expand", COOLDOWN);

        EmbedBuilder eb = CeoEmbedBuilder.base("🌍 Markteintritt: " + target.getDisplayName(), CeoEmbedBuilder.green())
                .addField("Markt",       target.getDisplayName(), true)
                .addField("Eintrittskosten", CeoEmbedBuilder.formatMoney(cost), true)
                .addField("Startanteil", "1%", true)
                .addField("Aktive Märkte",
                        company.getMarkets().stream()
                                .map(cm -> cm.getMarket().getDisplayName())
                                .collect(Collectors.joining(", ")), false)
                .setDescription("Deine Firma ist jetzt in **" + target.getDisplayName() +
                        "** aktiv! Nutze `/project list` für marktspezifische Projekte.")
                .setFooter("Cooldown: 24h");

        event.replyEmbeds(eb.build()).queue();
    }

    @Transactional
    protected void doExpand(Company company, Market target, long cost) {
        company.setCapital(company.getCapital() - cost);

        CompanyMarket cm = CompanyMarket.builder()
                .company(company)
                .market(target)
                .share(BigDecimal.ONE) // 1% Startanteil
                .build();
        company.getMarkets().add(cm);

        companyRepo.save(company);
        eventService.log(company, "expansion",
                "🌍 Markteintritt: " + target.getDisplayName(),
                "Eintrittspreis: " + CeoEmbedBuilder.formatMoney(cost),
                Map.of("capital", -cost));
    }

    private Market parseMarket(String input) {
        // Direkt
        try { return Market.valueOf(input); } catch (IllegalArgumentException ignored) {}
        // Teilweise — z.B. "AI" findet AI_DEEP_TECH
        return Arrays.stream(Market.values())
                .filter(m -> m.name().contains(input) || m.getDisplayName().toUpperCase().contains(input))
                .findFirst()
                .orElse(null);
    }
}
