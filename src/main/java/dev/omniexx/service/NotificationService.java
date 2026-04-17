package dev.omniexx.service;

import dev.omniexx.entity.Company;
import dev.omniexx.entity.Player;
import dev.omniexx.repository.NotificationPreferenceRepository;
import dev.omniexx.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final PlayerRepository                playerRepo;
    private final NotificationPreferenceRepository prefRepo;
    @Lazy
    private final JDA                             jda;

    /**
     * Sendet eine DM an den Spieler falls er den Event-Typ aktiviert hat.
     *
     * @param company   Betroffene Firma
     * @param eventType z.B. "tick_ready", "project_done", "sabotaged", "employee_quit"
     * @param message   Nachrichtentext
     */
    public void sendIfEnabled(Company company, String eventType, String message) {
        String discordId = company.getPlayer().getDiscordId();

        boolean enabled = prefRepo
                .findByPlayerIdAndEventType(company.getPlayer().getId(), eventType)
                .map(p -> p.isEnabled())
                .orElse(false);

        if (!enabled) return;

        try {
            jda.retrieveUserById(discordId).queue(user -> {
                if (user == null) return;
                user.openPrivateChannel().queue(channel -> {
                    channel.sendMessage("🔔 **OMNIEXX** — " + company.getName() + "\n" + message).queue(
                            success -> log.debug("DM gesendet an {}", discordId),
                            error   -> log.warn("DM fehlgeschlagen für {}: {}", discordId, error.getMessage())
                    );
                }, error -> log.warn("Private Channel konnte nicht geöffnet werden: {}", error.getMessage()));
            }, error -> log.warn("User {} nicht gefunden: {}", discordId, error.getMessage()));
        } catch (Exception e) {
            log.warn("Notification fehlgeschlagen für {}: {}", discordId, e.getMessage());
        }
    }

    public void tickReady(Company company) {
        sendIfEnabled(company, "tick_ready",
                "⏱️ Tick #" + company.getTickCount() + " wurde verarbeitet. Nutze `/report` für deinen Status.");
    }

    public void projectDone(Company company, String projectName, String outcome) {
        String emoji = outcome.startsWith("completed") ? "✅" : "❌";
        sendIfEnabled(company, "project_done",
                emoji + " Projekt **" + projectName + "** abgeschlossen: " + outcome);
    }

    public void sabotaged(Company company, String action, String attackerName) {
        sendIfEnabled(company, "sabotaged",
                "⚠️ **" + attackerName + "** hat deine Firma sabotiert! Aktion: " + action);
    }

    public void employeeQuit(Company company, String employeeName) {
        sendIfEnabled(company, "employee_quit",
                "😤 **" + employeeName + "** hat gekündigt. Team-Morale gesunken.");
    }
}
