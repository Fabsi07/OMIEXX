package dev.ceosim.discord.command;

import dev.ceosim.entity.NotificationPreference;
import dev.ceosim.entity.Player;
import dev.ceosim.repository.NotificationPreferenceRepository;
import dev.ceosim.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class NotifyCommand {

    private final PlayerRepository                playerRepo;
    private final NotificationPreferenceRepository notifRepo;

    private static final Set<String> VALID_EVENTS = Set.of(
            "tick_ready", "project_done", "sabotaged", "employee_quit",
            "loan_due", "insolvency_warning"
    );

    public void handle(SlashCommandInteractionEvent event) {
        String eventType = event.getOption("event").getAsString().toLowerCase().trim();

        if (!VALID_EVENTS.contains(eventType)) {
            event.reply("❌ Unbekannter Event-Typ.\n\nVerfügbar:\n" +
                    VALID_EVENTS.stream().map(e -> "• `" + e + "`")
                                .reduce((a, b) -> a + "\n" + b).orElse(""))
                    .setEphemeral(true).queue();
            return;
        }

        Player player = playerRepo.findByDiscordId(event.getUser().getId()).orElse(null);
        if (player == null) {
            event.reply("❌ Noch kein Account. Starte mit `/start`!").setEphemeral(true).queue();
            return;
        }

        // Toggle: existiert der Eintrag? → toggeln. Sonst: neu anlegen (aktiviert)
        var existing = notifRepo.findByPlayerIdAndEventType(player.getId(), eventType);

        if (existing.isPresent()) {
            var pref = existing.get();
            pref.setEnabled(!pref.isEnabled());
            notifRepo.save(pref);
            event.reply((pref.isEnabled() ? "🔔" : "🔕") + " Benachrichtigung `" + eventType + "` ist jetzt **" +
                    (pref.isEnabled() ? "aktiviert" : "deaktiviert") + "**.")
                    .setEphemeral(true).queue();
        } else {
            notifRepo.save(NotificationPreference.builder()
                    .player(player)
                    .eventType(eventType)
                    .enabled(true)
                    .build());
            event.reply("🔔 Benachrichtigung `" + eventType + "` ist jetzt **aktiviert**.")
                    .setEphemeral(true).queue();
        }
    }
}
