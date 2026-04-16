package dev.ceosim.discord.listener;

import dev.ceosim.discord.command.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlashCommandListener extends ListenerAdapter {

    private final StartCommand    startCommand;
    private final ReportCommand   reportCommand;
    private final HelpCommand     helpCommand;
    private final TeamCommand     teamCommand;
    private final MarketCommand   marketCommand;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String cmd = event.getName();
        log.debug("Command /{} von {} ({})", cmd, event.getUser().getName(), event.getUser().getId());

        try {
            switch (cmd) {
                case "start"    -> startCommand.handle(event);
                case "report"   -> reportCommand.handle(event);
                case "help"     -> helpCommand.handle(event);
                case "team"     -> teamCommand.handle(event);
                case "market"   -> marketCommand.handle(event);

                // Noch nicht implementiert — saubere Antwort statt Exception
                default -> event.reply("⚙️ **/" + cmd + "** wird in einer der nächsten Phasen implementiert.")
                        .setEphemeral(true)
                        .queue();
            }
        } catch (Exception e) {
            log.error("Fehler bei /{} von {}: {}", cmd, event.getUser().getId(), e.getMessage(), e);
            event.reply("❌ Ein interner Fehler ist aufgetreten. Bitte versuch es erneut.")
                    .setEphemeral(true)
                    .queue();
        }
    }
}
