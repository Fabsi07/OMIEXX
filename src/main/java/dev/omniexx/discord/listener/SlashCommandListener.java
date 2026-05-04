package dev.omniexx.discord.listener;

import dev.omniexx.discord.command.*;
import dev.omniexx.service.CooldownService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlashCommandListener extends ListenerAdapter {

    private final StartCommand        startCommand;
    private final ReportCommand       reportCommand;
    private final HelpCommand         helpCommand;
    private final TeamCommand         teamCommand;
    private final MarketCommand       marketCommand;
    private final HireCommand         hireCommand;
    private final FireCommand         fireCommand;
    private final ProjectCommand      projectCommand;
    private final LogCommand          logCommand;
    private final AcquireCommand      acquireCommand;
    private final FundraiseCommand    fundraiseCommand;
    private final ExpandCommand       expandCommand;
    private final ResearchCommand     researchCommand;
    private final SabotageCommand     sabotageCommand;
    private final ProfileCommand      profileCommand;
    private final PrestigeCommand     prestigeCommand;
    private final LegacyCommand       legacyCommand;
    private final AchievementsCommand achievementsCommand;
    private final InvestCommand       investCommand;
    private final PrCommand           prCommand;
    private final PauseCommand        pauseCommand;
    private final NotifyCommand       notifyCommand;
    private final AdminCommand        adminCommand;
    private final LoanRepayCommand    loanRepayCommand;
    private final CooldownService     cooldownService;

    /**
     * Global Slash-Cooldowns (pro User + Command-Key).
     * Detail-Commands wie /project start oder /research pick haben weiterhin eigene Cooldowns in ihren Command-Klassen.
     */
    private static final Map<String, Duration> SLASH_COOLDOWNS = Map.of(
            "report", Duration.ofSeconds(20),
            "team", Duration.ofSeconds(20),
            "market", Duration.ofSeconds(30),
            "profile", Duration.ofSeconds(30),
            "log", Duration.ofSeconds(15),
            "help", Duration.ofSeconds(10),
            "legacy", Duration.ofSeconds(60),
            "achievements", Duration.ofSeconds(60)
    );

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String cmd = event.getName();
        log.debug("/{} von {}", cmd, event.getUser().getName());

        try {
            Duration cooldown = SLASH_COOLDOWNS.get(cmd);
            if (cooldown != null) {
                String key = "slash_" + cmd;
                Duration remaining = cooldownService.checkAndSet(event.getUser().getId(), key, cooldown);
                if (remaining != null) {
                    event.reply("⏳ **/" + cmd + "** ist noch für **" + CooldownService.format(remaining) + "** auf Cooldown.")
                            .setEphemeral(true)
                            .queue();
                    return;
                }
            }

            switch (cmd) {
                // Phase 1 — Foundation
                case "start"        -> startCommand.handle(event);
                case "report"       -> reportCommand.handle(event);
                case "help"         -> helpCommand.handle(event);
                // Phase 2 — Core Loop
                case "log"          -> logCommand.handle(event);
                case "market"       -> marketCommand.handle(event);
                case "notify"       -> notifyCommand.handle(event);
                case "pause"        -> pauseCommand.handle(event);
                case "invest"       -> investCommand.handle(event);
                case "pr"           -> prCommand.handle(event);
                // Phase 3 — Mitarbeiter
                case "team"         -> teamCommand.handle(event);
                case "hire"         -> hireCommand.handle(event);
                case "fire"         -> fireCommand.handle(event);
                // Phase 4 — Projekte
                case "project"      -> projectCommand.handle(event);
                // Phase 5 — NPC-Welt
                case "acquire"      -> acquireCommand.handle(event);
                case "fundraise"    -> fundraiseCommand.handle(event);
                case "expand"       -> expandCommand.handle(event);
                case "profile"      -> profileCommand.handle(event);
                // Phase 6 — Tech-Tree
                case "research"     -> researchCommand.handle(event);
                // Phase 7 — PvP
                case "sabotage"     -> sabotageCommand.handle(event);
                // Phase 8 — Prestige
                case "prestige"     -> prestigeCommand.handle(event);
                case "legacy"       -> legacyCommand.handle(event);
                // Phase 9 — Polish
                case "achievements" -> achievementsCommand.handle(event);
                case "admin"        -> adminCommand.handle(event);
                case "loanrepay"    -> loanRepayCommand.handle(event);

                default -> event.reply("⚙️ **/" + cmd + "** ist noch nicht implementiert.")
                        .setEphemeral(true).queue();
            }
        } catch (Exception e) {
            log.error("Fehler bei /{} von {}: {}", cmd, event.getUser().getId(), e.getMessage(), e);
            event.reply("❌ Interner Fehler. Bitte versuch es erneut.")
                    .setEphemeral(true).queue();
        }
    }
}
