package dev.ceosim.discord.command;

import dev.ceosim.entity.Company;
import dev.ceosim.entity.Employee;
import dev.ceosim.repository.CompanyRepository;
import dev.ceosim.service.PrestigeService;
import dev.ceosim.util.CeoEmbedBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrestigeCommand extends ListenerAdapter {

    private final CompanyRepository companyRepo;
    private final PrestigeService   prestigeService;

    // Temporärer State: userId → gewählter Typ (hard_reset / ipo / market_dominance)
    private final Map<String, String> pendingType = new ConcurrentHashMap<>();

    public void handle(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();
        Company company = companyRepo.findActiveByDiscordId(discordId).orElse(null);

        if (company == null) {
            event.reply("❌ Keine aktive Firma. Starte mit `/start`!").setEphemeral(true).queue();
            return;
        }

        long valuation = company.calculateValuation();
        if (valuation < PrestigeService.getMinValuation()) {
            event.reply("🔒 Prestige erst ab **$1M Valuation** verfügbar.\n" +
                    "Aktuell: **" + CeoEmbedBuilder.formatMoney(valuation) + "**\n" +
                    "Noch " + CeoEmbedBuilder.formatMoney(PrestigeService.getMinValuation() - valuation) + " bis zum Ziel.")
                    .setEphemeral(true).queue();
            return;
        }

        EmbedBuilder eb = CeoEmbedBuilder.base("🌟 Prestige — Wähle deine Option", CeoEmbedBuilder.blue())
                .setDescription("Du hast **$1M Valuation** erreicht. Wähle wie du weitermachst:")
                .addField("🔄 Hard Reset",
                        "Alles auf 0. Dafür: **+10% Legacy-Multiplikator** dauerhaft und 1 Mitarbeiter kommt mit.\n" +
                        "Aktuell: ×" + company.getLegacyMultiplier() + " → ×" + (company.getLegacyMultiplier().doubleValue() + 0.10) + " nach Reset",
                        false)
                .addField("📊 IPO (Soft)",
                        "Kein Reset. **Einmaliger Börsengang** — 25% der Valuation als Kapital + Reputation +15.\n" +
                        (company.getSoftPrestigeUsed() ? "⛔ Bereits genutzt in diesem Run" : "Noch nicht genutzt ✅"),
                        false)
                .addField("🌍 Markt-Dominanz (Soft)",
                        "Kein Reset. **Alle Marktanteile +10%, Revenue +$5k/Tick**.\n" +
                        (company.getSoftPrestigeUsed() ? "⛔ Bereits genutzt in diesem Run" : "Noch nicht genutzt ✅"),
                        false)
                .addField("Deine Firma",
                        "Valuation: " + CeoEmbedBuilder.formatMoney(valuation) + "\n" +
                        "Prestige Level: " + company.getPrestigeLevel() + "\n" +
                        "Tick: #" + company.getTickCount(),
                        false);

        event.replyEmbeds(eb.build())
                .addActionRow(
                        Button.danger("prestige:hard_reset",        "🔄 Hard Reset"),
                        Button.success("prestige:ipo",              "📊 IPO"),
                        Button.primary("prestige:market_dominance", "🌍 Markt-Dominanz")
                )
                .setEphemeral(true)
                .queue();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId();
        if (!id.startsWith("prestige:")) return;

        String discordId = event.getUser().getId();
        String type = id.replace("prestige:", "");

        Company company = companyRepo.findActiveByDiscordId(discordId).orElse(null);
        if (company == null) {
            event.reply("❌ Keine aktive Firma.").setEphemeral(true).queue();
            return;
        }

        try {
            if ("hard_reset".equals(type)) {
                // Bei Hard Reset: Mitarbeiter-Auswahl anzeigen
                var employees = company.getActiveEmployees();
                if (employees.isEmpty()) {
                    doHardReset(event, company, null);
                    return;
                }

                // Buttons für Mitarbeiter-Wahl (max 5 Buttons)
                var buttons = employees.stream()
                        .limit(5)
                        .map(e -> Button.secondary("prestige:carry:" + e.getId(), e.getFullName()))
                        .toList();

                event.editMessage("👤 **Welchen Mitarbeiter nimmst du mit?**")
                        .setActionRow(buttons)
                        .queue();

            } else if (id.startsWith("prestige:carry:")) {
                // Mitarbeiter gewählt
                long empId = Long.parseLong(id.replace("prestige:carry:", ""));
                Employee carried = company.getActiveEmployees().stream()
                        .filter(e -> e.getId().equals(empId))
                        .findFirst()
                        .orElse(null);
                doHardReset(event, company, carried);

            } else {
                // Soft Prestige
                String result = prestigeService.softPrestige(company, type);
                event.editMessage("** **")
                        .setComponents()
                        .queue(hook -> hook.editOriginalEmbeds(
                                CeoEmbedBuilder.base("✅ Soft Prestige abgeschlossen!", CeoEmbedBuilder.green())
                                        .setDescription(result)
                                        .setFooter("Nutze /report für deinen neuen Status")
                                        .build()
                        ).queue());
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            event.reply("❌ " + e.getMessage()).setEphemeral(true).queue();
        }
    }

    private void doHardReset(ButtonInteractionEvent event, Company company, Employee carried) {
        Company newCompany = prestigeService.hardReset(company, carried);
        String carriedText = carried != null
                ? carried.getFullName() + " (" + carried.getRole() + ") kommt mit!"
                : "Kein Mitarbeiter ausgewählt";

        event.editMessage("** **")
                .setComponents()
                .queue(hook -> hook.editOriginalEmbeds(
                        CeoEmbedBuilder.base("🔄 Hard Reset abgeschlossen!", CeoEmbedBuilder.green())
                                .setDescription("Deine Firma wurde zurückgesetzt. Neuer Run beginnt!")
                                .addField("Prestige Level", String.valueOf(newCompany.getPrestigeLevel()), true)
                                .addField("Legacy Mult.",   "×" + newCompany.getLegacyMultiplier(), true)
                                .addField("Startkapital",  CeoEmbedBuilder.formatMoney(newCompany.getCapital()), true)
                                .addField("Mitnahme",      carriedText, false)
                                .setFooter("Tutorial übersprungen. Viel Erfolg im neuen Run!")
                                .build()
                ).queue());
    }
}
