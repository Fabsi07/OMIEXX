package dev.omniexx.discord.command;

import dev.omniexx.entity.Company;
import dev.omniexx.entity.PlayerEnergy;
import dev.omniexx.repository.CompanyRepository;
import dev.omniexx.service.work.EnergyService;
import dev.omniexx.service.work.WorkOutcome;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * /work — Kern-Command des Energie-Systems.
 * Zeigt ein zufälliges Szenario mit Buttons, wartet auf Antwort,
 * berechnet Outcome und zeigt Ergebnis.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkCommand extends ListenerAdapter {

    private final CompanyRepository companyRepo;
    private final EnergyService     energyService;
    private final WorkService       workService;

    private static final Random RANDOM = new Random();

    // ── /work ────────────────────────────────────────────────────────────
    public void handle(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();

        Company company = companyRepo.findActiveByDiscordId(discordId).orElse(null);
        if (company == null) {
            event.reply("❌ Keine aktive Firma. Starte mit `/start`!").setEphemeral(true).queue();
            return;
        }

        PlayerEnergy energy = energyService.getOrCreate(company);

        if (energy.isEmpty()) {
            long mins = energyService.minutesUntilNext(energy);
            event.replyEmbeds(
                new EmbedBuilder()
                    .setColor(new Color(0x4A5A72))
                    .setTitle("⚡ Keine Energie")
                    .setDescription("Dein Team braucht eine Pause.")
                    .addField("Nächste Energie",
                        "in **" + mins + " Minuten**\n" +
                        "(" + EnergyService.regenMinutes() + " Min pro Punkt)", true)
                    .addField("Energie",
                        energy.display() + "  `" + energy.getCurrent() + "/" + energy.getMaxEnergy() + "`", true)
                    .addField("Tipp",
                        "Nutze `/research` oder `/daily` während du wartest.", false)
                    .setFooter("Work-Streak: " + energy.getWorkStreak() + " Tage")
                    .build()
            ).setEphemeral(true).queue();
            return;
        }

        // Szenario wählen
        String lastKey = workService.getLastScenarioKey(company.getId());
        WorkScenario scenario = WorkScenario.random(company.getMarket(), lastKey, RANDOM);

        // Buttons für Optionen bauen
        List<Button> buttons = new ArrayList<>();
        List<WorkScenario.WorkOption> options = scenario.getOptions();
        for (int i = 0; i < Math.min(options.size(), 3); i++) {
            String btnId = "work:" + scenario.getKey() + ":" + i;
            String label = options.get(i).getLabel();
            // Kürzen falls nötig (Discord max 80 Zeichen)
            if (label.length() > 80) label = label.substring(0, 77) + "...";
            buttons.add(i == 0
                ? Button.primary(btnId, label)
                : i == 1
                    ? Button.secondary(btnId, label)
                    : Button.secondary(btnId, label)
            );
        }

        EmbedBuilder eb = new EmbedBuilder()
                .setColor(new Color(0x1c2535))
                .setTitle(scenario.getTitle())
                .setDescription(scenario.getDescription())
                .addField("⚡ Energie",
                    energy.display() + "  `" + energy.getCurrent() + "/" + energy.getMaxEnergy() + "`", true)
                .addField("Sessions heute",
                    String.valueOf(workService.isFirstEverWork(company.getId()) ? 0
                        : 0), true) // vereinfacht
                .setFooter("Wähle innerhalb von 60 Sekunden");

        event.replyEmbeds(eb.build())
                .addActionRow(buttons)
                .queue();
    }

    // ── Button-Handler ────────────────────────────────────────────────────
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId();
        if (!id.startsWith("work:")) return;

        String discordId = event.getUser().getId();

        // work:{scenarioKey}:{optionIndex}
        String[] parts = id.split(":");
        if (parts.length < 3) return;

        // scenarioKey kann Doppelpunkte enthalten — alles zwischen erstem und letztem ":"
        int lastColon    = id.lastIndexOf(":");
        String scenarioKey = id.substring("work:".length(), lastColon);
        int optionIndex;
        try {
            optionIndex = Integer.parseInt(id.substring(lastColon + 1));
        } catch (NumberFormatException e) {
            return;
        }

        Company company = companyRepo.findActiveByDiscordId(discordId).orElse(null);
        if (company == null) {
            event.reply("❌ Keine aktive Firma.").setEphemeral(true).queue();
            return;
        }

        PlayerEnergy energy = energyService.getOrCreate(company);
        if (energy.isEmpty()) {
            event.reply("❌ Keine Energie mehr! Warte bis deine Energie regeneriert.")
                    .setEphemeral(true).queue();
            return;
        }

        // Prüfen ob Nutzer der Firmeneigentümer ist
        if (!company.getPlayer().getDiscordId().equals(discordId)) {
            event.reply("❌ Das ist nicht deine Firma!").setEphemeral(true).queue();
            return;
        }

        try {
            boolean isFirst = workService.isFirstEverWork(company.getId());
            WorkOutcome outcome = workService.executeWork(
                    company, energy, scenarioKey, optionIndex, isFirst);

            // Ergebnis-Embed
            Color color = switch (outcome.getTier()) {
                case JACKPOT -> new Color(0xF1C40F);
                case GREAT   -> new Color(0x2ECC71);
                case GOOD    -> new Color(0x27AE60);
                case NORMAL  -> new Color(0x3498DB);
                case BAD     -> new Color(0xE67E22);
                case CRISIS  -> new Color(0xE74C3C);
            };

            // Energie nach dem Verbrauch neu laden
            energy = energyService.getOrCreate(company);

            EmbedBuilder result = new EmbedBuilder()
                    .setColor(color)
                    .setTitle(outcome.getEmoji() + " " + outcome.tierLabel())
                    .setDescription("*" + outcome.getFlavorText() + "*");

            // Gains/Losses anzeigen
            if (outcome.getCapitalGained() >= 0) {
                result.addField("💰 Kapital",
                    "+" + OmniexxEmbedBuilder.formatMoney(outcome.getCapitalGained()), true);
            } else {
                result.addField("💸 Kosten",
                    "-" + OmniexxEmbedBuilder.formatMoney(-outcome.getCapitalGained()), true);
            }

            if (outcome.getRpGained() > 0) {
                result.addField("🔬 Research", "+" + outcome.getRpGained() + " RP", true);
            }

            String moraleText = outcome.getMoraleDelta() >= 0
                    ? "+" + outcome.getMoraleDelta()
                    : String.valueOf(outcome.getMoraleDelta());
            result.addField("😊 Morale", moraleText, true);

            if (outcome.isBonusEnergy()) {
                result.addField("⚡ Bonus!", "+1 Energie erhalten!", false);
            }

            // Energie-Stand
            result.addField("⚡ Energie",
                energy.display() + "  `" + energy.getCurrent() + "/" + energy.getMaxEnergy() + "`\n" +
                (energy.isEmpty() ? "Nächste " + energyService.nextRegenText(energy) : ""),
                false);

            // Jackpot: extra Nachricht
            if (outcome.getTier() == WorkOutcome.Tier.JACKPOT) {
                result.addField("🎊 JACKPOT!", "Außergewöhnlicher Moment! " +
                    OmniexxEmbedBuilder.formatMoney(outcome.getCapitalGained()) +
                    " in einer einzigen Session!", false);
            }

            result.setFooter("Work-Streak: " + energy.getWorkStreak() + " Tage  •  " +
                            "Total Sessions: " + energy.getTotalSessions());

            event.editMessageEmbeds(result.build())
                    .setComponents() // Buttons entfernen nach Auswahl
                    .queue();

        } catch (IllegalStateException e) {
            event.reply("❌ " + e.getMessage()).setEphemeral(true).queue();
        }
    }
}
