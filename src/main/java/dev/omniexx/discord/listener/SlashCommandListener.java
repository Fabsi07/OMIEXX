package dev.omniexx.discord.listener;

import dev.omniexx.discord.command.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

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
    // Energie-System
    private final WorkCommand         workCommand;
    private final EnergyCommand       energyCommand;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String cmd = event.getName();
        log.debug("/{} von {}", cmd, event.getUser().getName());

        try {
            switch (cmd) {
                // ── Energie & Work ──
                case "work"         -> workCommand.handle(event);
                case "energy"       -> energyCommand.handle(event);
                // ── Phase 1 ──
                case "start"        -> startCommand.handle(event);
                case "report"       -> reportCommand.handle(event);
                case "help"         -> helpCommand.handle(event);
                // ── Phase 2 ──
                case "log"          -> logCommand.handle(event);
                case "market"       -> marketCommand.handle(event);
                case "notify"       -> notifyCommand.handle(event);
                case "pause"        -> pauseCommand.handle(event);
                case "invest"       -> investCommand.handle(event);
                case "pr"           -> prCommand.handle(event);
                // ── Phase 3 ──
                case "team"         -> teamCommand.handle(event);
                case "hire"         -> hireCommand.handle(event);
                case "fire"         -> fireCommand.handle(event);
                // ── Phase 4 ──
                case "project"      -> projectCommand.handle(event);
                // ── Phase 5 ──
                case "acquire"      -> acquireCommand.handle(event);
                case "fundraise"    -> fundraiseCommand.handle(event);
                case "expand"       -> expandCommand.handle(event);
                case "profile"      -> profileCommand.handle(event);
                // ── Phase 6 ──
                case "research"     -> researchCommand.handle(event);
                // ── Phase 7 ──
                case "sabotage"     -> sabotageCommand.handle(event);
                // ── Phase 8 ──
                case "prestige"     -> prestigeCommand.handle(event);
                case "legacy"       -> legacyCommand.handle(event);
                // ── Phase 9 ──
                case "achievements" -> achievementsCommand.handle(event);
                case "loanrepay"    -> loanRepayCommand.handle(event);
                case "admin"        -> adminCommand.handle(event);

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
