package dev.omniexx.discord.command;

import dev.omniexx.entity.Company;
import dev.omniexx.repository.CompanyRepository;
import dev.omniexx.service.CooldownService;
import dev.omniexx.service.work.*;
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
    private final CooldownService   cooldownService;
    private final WorkService       workService;

    // Cooldowns
    private static final Duration WORK_CD   = Duration.ofMinutes(90);
    private static final Duration CRUNCH_CD = Duration.ofMinutes(60);

    // Pending: userId → (scenarioKey, optionIndex)
    private final Map<String, String> pendingScenario = new ConcurrentHashMap<>();

    private static final Random RNG = new Random();

    // ── /work ────────────────────────────────────────────────────────────
    public void handle(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();

        Duration cd = cooldownService.getRemaining(discordId, "work");
        if (cd != null) {
            event.reply("⏳ Nächste Work-Session in **" + CooldownService.format(cd) + "**.\n"
                      + "Nutze die Zeit für `/research`, `/market` oder `/report`!")
                    .setEphemeral(true).queue();
            return;
        }

        Company company = companyRepo.findActiveByDiscordId(discordId).orElse(null);
        if (company == null) {
            event.reply("❌ Keine aktive Firma. Starte mit `/start`!").setEphemeral(true).queue();
            return;
        }

        // Ist das der allererste /work des Spielers? → garantiert GOOD/GREAT
        boolean isFirstWork = workService.getStreak(company).getTotalSessions() == 0;

        WorkScenario scenario = isFirstWork
                ? WorkScenario.byKey("ws_startup_idea")  // freundliches Starter-Szenario
                : WorkScenario.random(RNG);

        showScenario(event, company, scenario, discordId, false);
    }

    // ── /crunch ──────────────────────────────────────────────────────────
    public void handleCrunch(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();

        Duration cd = cooldownService.getRemaining(discordId, "crunch");
        if (cd != null) {
            event.reply("⏳ /crunch in **" + CooldownService.format(cd) + "** wieder verfügbar.")
                    .setEphemeral(true).queue();
            return;
        }

        Duration workCd = cooldownService.getRemaining(discordId, "work");
        if (workCd != null) {
            event.reply("⏳ Erst wenn /work wieder verfügbar ist kannst du /crunch nutzen.")
                    .setEphemeral(true).queue();
            return;
        }

        Company company = companyRepo.findActiveByDiscordId(discordId).orElse(null);
        if (company == null) {
            event.reply("❌ Keine aktive Firma!").setEphemeral(true).queue();
            return;
        }

        WorkScenario scenario = WorkScenario.random(RNG);
        showScenario(event, company, scenario, discordId, true);
    }

    // ── Szenario anzeigen ────────────────────────────────────────────────
    private void showScenario(SlashCommandInteractionEvent event, Company company,
                              WorkScenario scenario, String discordId, boolean isCrunch) {

        pendingScenario.put(discordId, scenario.getKey() + ":" + (isCrunch ? "1" : "0"));

        List<WorkScenario.Option> options = scenario.getOptions();

        EmbedBuilder eb = OmniexxEmbedBuilder.base(
                        (isCrunch ? "🔥 CRUNCH — " : "💼 Work — ") + scenario.getTitle(),
                        isCrunch ? new Color(0xE74C3C) : OmniexxEmbedBuilder.blue())
                .setDescription(scenario.getDescription())
                .addField("📊 Aktueller Status",
                        "Kapital: " + OmniexxEmbedBuilder.formatMoney(company.getCapital())
                        + "  |  Morale: " + company.getMorale() + "/100"
                        + "  |  Streak: " + workService.getStreak(company).getCurrentStreak() + " Tage",
                        false);

        if (isCrunch) {
            eb.addField("⚠️ Crunch-Modus",
                    "Doppeltes Risiko, doppelte Belohnung. Team-Morale −5 egal was passiert.", false);
        }

        // Buttons für jede Option
        List<Button> buttons = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            WorkScenario.Option opt = options.get(i);
            String btnId = "work:opt:" + scenario.getKey() + ":" + i + ":" + (isCrunch ? "1" : "0");
            buttons.add(Button.secondary(btnId, opt.getLabel()));
        }

        event.replyEmbeds(eb.build())
                .addActionRow(buttons)
                .queue();
    }

    // ── Button-Handler ───────────────────────────────────────────────────
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId();
        if (!id.startsWith("work:opt:")) return;

        String[] parts = id.split(":");
        // work:opt:scenarioKey:optionIndex:isCrunch
        if (parts.length < 5) return;

        String scenarioKey = parts[2];
        int    optionIndex = Integer.parseInt(parts[3]);
        boolean isCrunch   = "1".equals(parts[4]);
        String discordId   = event.getUser().getId();

        Company company = companyRepo.findActiveByDiscordId(discordId).orElse(null);
        if (company == null) {
            event.reply("❌ Keine aktive Firma.").setEphemeral(true).queue();
            return;
        }

        WorkScenario scenario = WorkScenario.byKey(scenarioKey);
        if (scenario == null || optionIndex >= scenario.getOptions().size()) {
            event.reply("❌ Szenario nicht mehr gültig.").setEphemeral(true).queue();
            return;
        }

        WorkScenario.Option option = scenario.getOptions().get(optionIndex);

        // Outcome berechnen — beim ersten Mal geprimtes positives Ergebnis
        boolean isFirstWork = workService.getStreak(company).getTotalSessions() == 0;
        WorkOutcome outcome;

        if (isFirstWork) {
            // Erstes Mal: immer GOOD oder GREAT
            outcome = WorkOutcome.builder()
                    .tier(RNG.nextBoolean() ? WorkOutcome.Tier.GOOD : WorkOutcome.Tier.GREAT)
                    .capitalDelta(200_000L + RNG.nextLong(300_000L))
                    .moraleDelta((short)(5 + RNG.nextInt(10)))
                    .rpDelta((short) 5)
                    .rareCardDrop(false)
                    .flavourText("✅ Guter Start! " + option.getLabel() + " war die richtige Wahl.")
                    .serverAnnounce(false)
                    .build();
        } else {
            outcome = workService.calculateOutcome(scenario, option, company);
            // Crunch: alles ×1.8 aber Morale immer −5 extra
            if (isCrunch) {
                outcome = WorkOutcome.builder()
                        .tier(outcome.getTier())
                        .capitalDelta((long)(outcome.getCapitalDelta() * 1.8))
                        .moraleDelta((short)(outcome.getMoraleDelta() - 5))
                        .rpDelta((short)(outcome.getRpDelta() * 2))
                        .rareCardDrop(outcome.isRareCardDrop())
                        .flavourText("🔥 CRUNCH: " + outcome.getFlavourText())
                        .serverAnnounce(outcome.isServerAnnounce())
                        .build();
            }
        }

        workService.applyOutcome(company, scenario, option, outcome);

        // Cooldown setzen
        Duration cd = isCrunch ? CRUNCH_CD : WORK_CD;
        cooldownService.set(discordId, "work", cd);
        if (isCrunch) cooldownService.set(discordId, "crunch", CRUNCH_CD);

        // Ergebnis-Embed
        Color color = switch (outcome.getTier()) {
            case JACKPOT  -> new Color(0xF1C40F);
            case GREAT    -> OmniexxEmbedBuilder.green();
            case GOOD     -> OmniexxEmbedBuilder.green();
            case NORMAL   -> OmniexxEmbedBuilder.blue();
            case BAD      -> new Color(0xE67E22);
            case CRITICAL -> OmniexxEmbedBuilder.red();
        };

        String deltaStr = outcome.getCapitalDelta() >= 0
                ? "+" + OmniexxEmbedBuilder.formatMoney(outcome.getCapitalDelta())
                : OmniexxEmbedBuilder.formatMoney(outcome.getCapitalDelta());

        String nextInfo = "Nächste Work-Session in **" + CooldownService.format(cd) + "**";

        EmbedBuilder result = OmniexxEmbedBuilder.base(
                        outcome.getTier().emoji() + " " + outcome.getTier().label(), color)
                .setDescription(outcome.getFlavourText())
                .addField("💰 Kapital", deltaStr, true)
                .addField("😊 Morale", (outcome.getMoraleDelta() >= 0 ? "+" : "") + outcome.getMoraleDelta(), true)
                .addField("🔬 RP", "+" + outcome.getRpDelta(), true)
                .addField("📊 Firma", OmniexxEmbedBuilder.formatMoney(company.getCapital()), true)
                .addField("🔥 Streak", workService.getStreak(company).getCurrentStreak() + " Tage", true);

        if (outcome.isRareCardDrop()) {
            result.addField("🃏 Rare Drop!", "Du hast eine seltene Mitarbeiter-Karte erhalten! `/cards` zum Anzeigen.", false);
        }

        if (outcome.getTier() == WorkOutcome.Tier.JACKPOT) {
            result.addField("🎰 JACKPOT!", "Dieser Moment wird im Server-Log festgehalten!", false);
        }

        result.setFooter(nextInfo);

        event.editMessageEmbeds(result.build())
                .setComponents()
                .queue();
    }
}
