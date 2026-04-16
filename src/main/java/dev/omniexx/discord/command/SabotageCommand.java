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
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SabotageCommand {

    private final CompanyRepository companyRepo;
    private final CooldownService   cooldownService;
    private final EventService      eventService;

    private static final Duration COOLDOWN      = Duration.ofHours(6);
    private static final long     MIN_VALUATION = 5_000_000L; // $50k
    private static final Random   RANDOM        = new Random();
    private static final Set<String> VALID_ACTIONS = Set.of("leak", "hiring_war", "fake_pr", "infra");

    public void handle(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();

        Duration cd = cooldownService.getRemaining(discordId, "sabotage");
        if (cd != null) {
            event.reply("⏳ `/sabotage` ist noch für **" + CooldownService.format(cd) + "** auf Cooldown.")
                    .setEphemeral(true).queue();
            return;
        }

        Company attacker = companyRepo.findActiveByDiscordId(discordId).orElse(null);
        if (attacker == null) {
            event.reply("❌ Keine aktive Firma. Starte mit `/start`!").setEphemeral(true).queue();
            return;
        }

        if (attacker.calculateValuation() < MIN_VALUATION) {
            event.reply("🔒 `/sabotage` erst ab **$50k Valuation** verfügbar.\nAktuell: " +
                    OmniexxEmbedBuilder.formatMoney(attacker.calculateValuation()))
                    .setEphemeral(true).queue();
            return;
        }

        var targetUser = event.getOption("ziel");
        var actionOpt  = event.getOption("aktion");
        if (targetUser == null || actionOpt == null) {
            event.reply("❌ Nutze: `/sabotage @user [leak|hiring_war|fake_pr|infra]`")
                    .setEphemeral(true).queue();
            return;
        }

        String action = actionOpt.getAsString().toLowerCase().trim();
        if (!VALID_ACTIONS.contains(action)) {
            event.reply("❌ Ungültige Aktion. Nutze: `leak`, `hiring_war`, `fake_pr`, `infra`")
                    .setEphemeral(true).queue();
            return;
        }

        String targetDiscordId = targetUser.getAsUser().getId();
        if (targetDiscordId.equals(discordId)) {
            event.reply("❌ Du kannst dich nicht selbst sabotieren.").setEphemeral(true).queue();
            return;
        }

        Company target = companyRepo.findActiveByDiscordId(targetDiscordId).orElse(null);
        if (target == null) {
            event.reply("❌ Das Ziel hat keine aktive Firma.").setEphemeral(true).queue();
            return;
        }

        // Kosten abziehen
        long cost = actionCost(action);
        if (attacker.getCapital() < cost) {
            event.reply("❌ Zu wenig Kapital! Aktion **" + action + "** kostet " +
                    OmniexxEmbedBuilder.formatMoney(cost) + ".")
                    .setEphemeral(true).queue();
            return;
        }

        // Backfire-Chance berechnen
        int backfireChance = calculateBackfireChance(target);
        boolean backfire = RANDOM.nextInt(100) < backfireChance;

        executeAction(attacker, target, action, cost, backfire);
        cooldownService.set(discordId, "sabotage", COOLDOWN);

        EmbedBuilder eb;
        if (backfire) {
            eb = OmniexxEmbedBuilder.base("💥 Backfire! Aktion schlug fehl", new Color(0xE74C3C))
                    .setDescription("Die Sabotage wurde abgewehrt und trifft **dich** stattdessen!")
                    .addField("Ziel hatte",    "Reputation " + target.getReputation() + "/100 — zu stark verteidigt", false)
                    .addField("Backfire-Schaden", buildEffectDescription(action, true), false)
                    .addField("Kosten",        OmniexxEmbedBuilder.formatMoney(cost) + " trotzdem verloren", true);
        } else {
            eb = OmniexxEmbedBuilder.base("🗡️ Sabotage erfolgreich: " + actionLabel(action), new Color(0x8E44AD))
                    .addField("Ziel",          target.getName(), true)
                    .addField("Aktion",        actionLabel(action), true)
                    .addField("Kosten",        OmniexxEmbedBuilder.formatMoney(cost), true)
                    .addField("Effekt",        buildEffectDescription(action, false), false)
                    .setFooter("Cooldown: 6h");
        }

        event.replyEmbeds(eb.build()).queue();
    }

    @Transactional
    protected void executeAction(Company attacker, Company target,
                                 String action, long cost, boolean backfire) {
        // Kosten immer abziehen
        attacker.setCapital(attacker.getCapital() - cost);

        Company victim = backfire ? attacker : target;

        switch (action) {
            case "leak" -> {
                // Morale −15
                victim.setMorale((short) Math.max(0, victim.getMorale() - 15));
                eventService.log(victim, "sabotage_received",
                        backfire ? "💥 Backfire: Leak aufgeflogen" : "🔓 Internes Leak aufgetaucht",
                        "Morale −15 durch interne Dokumente die publik wurden.",
                        Map.of("morale", -15L));
            }
            case "hiring_war" -> {
                // Burn Rate + $200/Tick für 3 Ticks (vereinfacht: direkt +600)
                victim.setBurnRate(victim.getBurnRate() + 20_000L);
                eventService.log(victim, "sabotage_received",
                        backfire ? "💥 Backfire: Talent-Abwerbung" : "⚔️ Hiring War gestartet",
                        "Konkurrenz wirbt deine Mitarbeiter ab — Burn Rate +$200/Tick.",
                        Map.of("burn_rate", 20_000L));
            }
            case "fake_pr" -> {
                // Marktanteil −2%, Reputation −5
                double newShare = Math.max(0, victim.getMarketShare().doubleValue() - 2.0);
                victim.setMarketShare(java.math.BigDecimal.valueOf(newShare));
                victim.setReputation((short) Math.max(0, victim.getReputation() - 5));
                eventService.log(victim, "sabotage_received",
                        backfire ? "💥 Backfire: Fake-PR" : "📰 Negative Presse",
                        "Gefälschte Berichte beschädigen Marktanteil (−2%) und Reputation (−5).",
                        Map.of("market_share", -2L, "reputation", -5L));
            }
            case "infra" -> {
                // Burn Rate +$300/Tick für 2 Ticks
                victim.setBurnRate(victim.getBurnRate() + 30_000L);
                eventService.log(victim, "sabotage_received",
                        backfire ? "💥 Backfire: Infra-Angriff" : "⚡ Infrastruktur-Angriff",
                        "DDoS/Sabotage erhöht Burn Rate +$300/Tick für 2 Ticks.",
                        Map.of("burn_rate", 30_000L));
            }
        }

        companyRepo.save(attacker);
        companyRepo.save(victim);

        if (!backfire) {
            eventService.log(attacker, "sabotage_sent",
                    "🗡️ Sabotage gesendet an " + target.getName(),
                    "Aktion: " + action + " | Kosten: " + OmniexxEmbedBuilder.formatMoney(cost),
                    Map.of("capital", -cost));
        }
    }

    private int calculateBackfireChance(Company target) {
        int chance = 10; // Basis 10%
        // Hohe Reputation schützt
        if (target.getReputation() >= 80) chance += 20;
        else if (target.getReputation() >= 60) chance += 10;
        // Zero Trust Projekt (Cybersec) — check via Events vereinfacht
        if (target.getMarket().name().equals("CYBERSECURITY")) chance += 15;
        return Math.min(60, chance);
    }

    private long actionCost(String action) {
        return switch (action) {
            case "leak"        -> 200_000L;  // $2k
            case "hiring_war"  -> 350_000L;  // $3.5k
            case "fake_pr"     -> 150_000L;  // $1.5k
            case "infra"       -> 500_000L;  // $5k
            default            -> 200_000L;
        };
    }

    private String actionLabel(String action) {
        return switch (action) {
            case "leak"        -> "🔓 Internes Leak";
            case "hiring_war"  -> "⚔️ Hiring War";
            case "fake_pr"     -> "📰 Fake PR";
            case "infra"       -> "⚡ Infra-Angriff";
            default            -> action;
        };
    }

    private String buildEffectDescription(String action, boolean backfire) {
        String who = backfire ? "Du erleidest" : "Ziel erleidet";
        return switch (action) {
            case "leak"        -> who + ": Morale −15";
            case "hiring_war"  -> who + ": Burn Rate +$200/Tick";
            case "fake_pr"     -> who + ": Marktanteil −2%, Reputation −5";
            case "infra"       -> who + ": Burn Rate +$300/Tick für 2 Ticks";
            default            -> who + ": Unbekannter Effekt";
        };
    }
}
