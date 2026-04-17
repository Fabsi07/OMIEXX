package dev.omniexx.discord.command;

import dev.omniexx.entity.*;
import dev.omniexx.repository.CompanyRepository;
import dev.omniexx.repository.PlayerRepository;
import dev.omniexx.service.CompanyService;
import dev.omniexx.util.OmniexxEmbedBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * /start — Onboarding-Flow via Discord Buttons.
 *
 * Flow:
 *   1. Spieler tippt /start
 *   2. Bot fragt: Welchen Markt willst du?  (5 Buttons)
 *   3. Spieler wählt Markt → Bot fragt: Welchen Startertyp? (3 Buttons)
 *   4. Spieler wählt Startertyp → Firma wird angelegt, Tutorial-Embed gezeigt
 *
 * Der Firmenname wird aus dem Discord-Displayname generiert (anpassbar später).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartCommand extends ListenerAdapter {

    private final PlayerRepository  playerRepo;
    private final CompanyRepository companyRepo;
    private final CompanyService    companyService;

    // Temporärer State während des Onboarding-Flows: userId -> gewählter Markt
    // (In-Memory reicht hier, Flow dauert max. wenige Minuten)
    private final Map<String, Market> pendingMarket = new ConcurrentHashMap<>();

    public void handle(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();

        // Bereits eine aktive Firma?
        if (companyRepo.existsByPlayerDiscordIdAndDeletedAtIsNull(discordId)) {
            event.reply("❌ Du hast bereits eine aktive Firma! Nutze `/report` um sie anzuzeigen.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Markt-Auswahl anzeigen
        event.reply("## 🏢 Willkommen bei OMNIEXX!\n\nWähle deinen **Startmarkt**:")
                .addActionRow(
                        Button.primary("start:market:CONSUMER_TECH",    "📱 Consumer Tech"),
                        Button.primary("start:market:ENTERPRISE_SAAS",  "💼 Enterprise SaaS"),
                        Button.primary("start:market:FINTECH",          "💳 Fintech")
                )
                .addActionRow(
                        Button.primary("start:market:E_COMMERCE",       "🛒 E-Commerce"),
                        Button.primary("start:market:CYBERSECURITY",    "🔐 Cybersecurity")
                )
                .setEphemeral(true)
                .queue();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId();
        String userId = event.getUser().getId();

        // --- Schritt 2: Markt gewählt ---
        if (id.startsWith("start:market:")) {
            Market market = Market.valueOf(id.replace("start:market:", ""));
            pendingMarket.put(userId, market);

            event.editMessage("## 🏢 Markt: **" + market.getDisplayName() + "** ✅\n\nWähle deinen **Startertyp**:")
                    .setActionRow(
                            Button.success("start:type:BOOTSTRAPPER", "💰 Bootstrapper"),
                            Button.success("start:type:VISIONARY",    "🔬 Visionär"),
                            Button.success("start:type:NETWORKER",    "🤝 Networker")
                    )
                    .queue();
            return;
        }

        // --- Schritt 3: Startertyp gewählt → Firma anlegen ---
        if (id.startsWith("start:type:")) {
            Market market = pendingMarket.remove(userId);
            if (market == null) {
                event.reply("❌ Session abgelaufen. Bitte starte `/start` erneut.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            StarterType type = StarterType.valueOf(id.replace("start:type:", ""));
            String discordName = event.getUser().getEffectiveName();
            // Name auf 60 Zeichen begrenzen (DB: VARCHAR(64))
            String companyName = (discordName.length() > 56 ? discordName.substring(0, 56) : discordName) + " Corp";

            // Player anlegen oder laden
            Player player = playerRepo.findByDiscordId(userId).orElseGet(() ->
                    playerRepo.save(Player.builder()
                            .discordId(userId)
                            .discordName(discordName)
                            .build())
            );

            Company company = companyService.createCompany(player, companyName, market, type);

            event.editMessage("** **")   // leere Nachricht, dann Embed
                    .setComponents()
                    .queue(hook ->
                            hook.editOriginalEmbeds(
                                    OmniexxEmbedBuilder.base("🎉 " + companyName + " wurde gegründet!", OmniexxEmbedBuilder.green())
                                            .setDescription("Deine Firma ist live! Hier sind deine Startdaten:")
                                            .addField("🏢 Firma",         companyName, true)
                                            .addField("🌍 Markt",         market.getDisplayName(), true)
                                            .addField("🎯 Startertyp",    type.getDisplayName(), true)
                                            .addField("💰 Startkapital",  OmniexxEmbedBuilder.formatMoney(company.getCapital()), true)
                                            .addField("🔬 RP/Tick",       String.valueOf(company.getRpPerTick()), true)
                                            .addField("👥 Team",          company.getActiveEmployees().size() + " Mitarbeiter", true)
                                            .addField("📖 Nächster Schritt",
                                                    "Nutze `/report` für dein Dashboard.\nDein erster Tick startet in **6 Stunden**!", false)
                                            .setFooter("OMNIEXX • " + type.getDescription())
                                            .build()
                            ).queue()
                    );
        }
    }
}
