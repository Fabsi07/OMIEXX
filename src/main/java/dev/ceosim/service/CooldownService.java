package dev.ceosim.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-Memory Cooldown-System.
 * Key: "discordUserId:commandName"
 * Value: Zeitpunkt wann der Cooldown abläuft.
 *
 * Reicht für den Start — bei Multi-Instance-Deployment
 * später durch Redis ersetzen.
 */
@Service
public class CooldownService {

    private final ConcurrentHashMap<String, Instant> cooldowns = new ConcurrentHashMap<>();

    /**
     * Prüft ob ein Cooldown aktiv ist.
     * @return verbleibende Zeit, oder null wenn kein Cooldown
     */
    public Duration getRemaining(String discordId, String command) {
        Instant expires = cooldowns.get(key(discordId, command));
        if (expires == null) return null;

        Duration remaining = Duration.between(Instant.now(), expires);
        if (remaining.isNegative()) {
            cooldowns.remove(key(discordId, command));
            return null;
        }
        return remaining;
    }

    /**
     * Setzt einen Cooldown.
     */
    public void set(String discordId, String command, Duration duration) {
        cooldowns.put(key(discordId, command), Instant.now().plus(duration));
    }

    /**
     * Prüft und setzt in einem Schritt.
     * @return null wenn OK, sonst verbleibende Duration
     */
    public Duration checkAndSet(String discordId, String command, Duration duration) {
        Duration remaining = getRemaining(discordId, command);
        if (remaining != null) return remaining;
        set(discordId, command, duration);
        return null;
    }

    /** Formatiert eine Duration lesbar: "2h 15m" oder "45m 30s" */
    public static String format(Duration d) {
        long hours   = d.toHours();
        long minutes = d.toMinutesPart();
        long seconds = d.toSecondsPart();

        if (hours > 0)   return hours + "h " + minutes + "m";
        if (minutes > 0) return minutes + "m " + seconds + "s";
        return seconds + "s";
    }

    private String key(String discordId, String command) {
        return discordId + ":" + command;
    }
}
