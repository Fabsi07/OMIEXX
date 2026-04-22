package dev.omniexx.discord.command;

import dev.omniexx.entity.Company;
import dev.omniexx.repository.CompanyRepository;
import dev.omniexx.service.CooldownService;
import dev.omniexx.service.work.WorkScenario;
import dev.omniexx.service.work.WorkService;
import dev.omniexx.util.OmniexxEmbedBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkCommand extends ListenerAdapter {

    private final CompanyRepository companyRepo;
    private final WorkService       workService;
    private final CooldownService   cooldownService;

    // Kurze Cooldowns — das ist der Suchtkern
    private static final Duration WORK_CD    = Duration.ofSeconds(20);  // 20 Sekunden!
    private static final Duration CRUNCH_CD  = Duration.ofSeconds(45);
    private static final Duration NETWORK_CD = Duration.ofMinutes(2);

    // Pending Scenario: userId → aktives Szenario
    private final Map<String, WorkScenario> pendingScenario = new ConcurrentHashMap<>();
    private final Map<String, String>       lastScenarioKey  = new ConcurrentHashMap<>();
    private static final Random RNG = new Random();

    // ── /work ────────────────────────────────────────────────────────────
    public void handle(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();

        Duration cd = cooldownService.getRemaining(discordId, "work");
        if (cd != null) {
            long secs = cd.toSeconds();
            String timeStr = secs < 60 ? secs + "s" : CooldownService.format(cd);
            event.reply("⏳ **" + timeStr + "** — dann wieder bereit.")
                    .setEphemeral(true).queue();
            return;
        }

        Company company = companyRepo.findActiveByDiscordId(discordId).orElse(null);
        if (company == null) {
            event.reply("❌ Keine aktive Firma. Starte mit `/start`!").setEphemeral(true).queue();
            return;
        }

        String lastKey = lastScenarioKey.getOrDefault(discordId, "");
        WorkScenario scenario = WorkScenario.getRandom(company.getMarket(), RNG, lastKey);
        pendingScenario.put(discordId, scenario);

        List<Button> buttons = new ArrayList<>();
        for (WorkScenario.Opt opt : scenario.options) {
            buttons.add(Button.primary("work:choose:" + opt.id + ":" + discordId, opt.label));
        }

        // Streak-Info im Footer
        String streakInfo = company.getWorkStreak() > 0
                ? "🔥 Streak: " + company.getWorkStreak() + " Tage | "
                : "";

        EmbedBuilder eb = OmniexxEmbedBuilder.base(scenario.title, new Color(0x3498DB))
                .setDescription(scenario.description)
                .setFooter(streakInfo + "💰 " + OmniexxEmbedBuilder.formatMoney(company.getCapital())
                        + "  ⚡ " + company.getMorale() + "/100 Morale  •  /work alle 20s");

        event.replyEmbeds(eb.build())
                .addActionRow(buttons)
                .queue();
    }

    // ── Button-Handler ───────────────────────────────────────────────────
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId();
        if (!id.startsWith("work:choose:")) return;

        String[] parts   = id.split(":");
        String optId     = parts[2];
        String ownerId   = parts[3];

        // Nur der eigene Spieler darf klicken
        if (!event.getUser().getId().equals(ownerId)) {
            event.reply("❌ Das ist nicht dein Work-Szenario!").setEphemeral(true).queue();
            return;
        }

        WorkScenario scenario = pendingScenario.remove(ownerId);
        if (scenario == null) {
            event.reply("⏰ Zu spät — dieses Szenario ist abgelaufen.").setEphemeral(true).queue();
            return;
        }

        WorkScenario.Opt chosen = scenario.options.stream()
                .filter(o -> o.id.equals(optId)).findFirst().orElse(null);
        if (chosen == null) return;

        Company company = companyRepo.findActiveByDiscordId(ownerId).orElse(null);
        if (company == null) return;

        WorkService.WorkResult result = workService.execute(company, scenario, chosen);

        // Cooldown setzen NACH Ausführung
        cooldownService.set(ownerId, "work", WORK_CD);
        lastScenarioKey.put(ownerId, scenario.key);

        // Ergebnis-Embed
        Color color = switch (result.tier()) {
            case JACKPOT  -> new Color(0xF1C40F);  // Gold
            case GREAT    -> new Color(0x2ECC71);  // Grün
            case GOOD     -> new Color(0x27AE60);  // Dunkelgrün
            case NORMAL   -> new Color(0x3498DB);  // Blau
            case BAD      -> new Color(0xE67E22);  // Orange
            case CRITICAL -> new Color(0xE74C3C);  // Rot
        };

        String tierEmoji = switch (result.tier()) {
            case JACKPOT  -> "🎰 **JACKPOT!**";
            case GREAT    -> "🌟 **Sehr gut!**";
            case GOOD     -> "✅ **Gut!**";
            case NORMAL   -> "➡️ Normal";
            case BAD      -> "⚠️ Schwierig";
            case CRITICAL -> "💥 Kritisch!";
        };

        String capitalStr = result.capitalDelta() >= 0
                ? "+" + OmniexxEmbedBuilder.formatMoney(result.capitalDelta())
                : "−" + OmniexxEmbedBuilder.formatMoney(Math.abs(result.capitalDelta()));

        EmbedBuilder eb = OmniexxEmbedBuilder.base(tierEmoji + " — " + chosen.outcomeText, color)
                .addField("💰 Kapital",  capitalStr, true)
                .addField("🔬 RP",       "+" + result.rpGained(), true)
                .addField("😊 Morale",   (result.moraleDelta() >= 0 ? "+" : "") + result.moraleDelta(), true);

        // Streak-Info
        if (result.newStreak() >= 2) {
            eb.addField("🔥 Streak",
                    result.newStreak() + " Tage in Folge  (+" + (int)((result.streakMult()-1)*100) + "% Bonus)",
                    false);
        }

        // Jackpot: Server-Post triggern (über EventLogService)
        if (result.tier() == WorkScenario.Tier.JACKPOT) {
            eb.addField("🎉 Jackpot!", "Glückwunsch! Das ist selten.", false);
        }

        eb.setFooter("Nächste /work in 20s  •  Streak-Bonus: ×" +
                String.format("%.2f", result.streakMult()));

        event.editMessageEmbeds(eb.build())
                .setComponents()
                .queue();
    }
}
