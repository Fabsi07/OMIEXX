package dev.omniexx.service;

import dev.omniexx.entity.Cooldown;
import dev.omniexx.repository.CooldownRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Persistente Cooldown-Verwaltung via PostgreSQL.
 * Cooldowns überleben Bot-Neustarts.
 * In-Memory Cache für schnelle Lookups.
 */
@Service
@RequiredArgsConstructor
public class CooldownService {

    private final CooldownRepository cooldownRepo;

    // In-Memory Cache: verhindert DB-Hit bei jedem Command
    private final Map<String, OffsetDateTime> cache = new ConcurrentHashMap<>();

    public Duration getRemaining(String discordId, String command) {
        String key = key(discordId, command);
        OffsetDateTime expires = cache.get(key);

        if (expires == null) {
            // Cache-Miss: aus DB laden
            Optional<Cooldown> cd = cooldownRepo.findByDiscordIdAndCommand(discordId, command);
            if (cd.isEmpty()) return null;
            expires = cd.get().getExpiresAt();
            cache.put(key, expires);
        }

        Duration remaining = Duration.between(OffsetDateTime.now(), expires);
        if (remaining.isNegative()) {
            cache.remove(key);
            return null;
        }
        return remaining;
    }

    @Transactional
    public void set(String discordId, String command, Duration duration) {
        OffsetDateTime expires = OffsetDateTime.now().plus(duration);
        cache.put(key(discordId, command), expires);

        // Upsert in DB
        cooldownRepo.findByDiscordIdAndCommand(discordId, command).ifPresentOrElse(
            cd -> { cd.setExpiresAt(expires); cooldownRepo.save(cd); },
            ()  -> cooldownRepo.save(Cooldown.builder()
                        .discordId(discordId)
                        .command(command)
                        .expiresAt(expires)
                        .build())
        );
    }

    public Duration checkAndSet(String discordId, String command, Duration duration) {
        Duration remaining = getRemaining(discordId, command);
        if (remaining != null) return remaining;
        set(discordId, command, duration);
        return null;
    }

    /** Abgelaufene Cooldowns täglich aufräumen */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanup() {
        int deleted = cooldownRepo.deleteExpired(OffsetDateTime.now());
        if (deleted > 0) cache.clear(); // Cache nach Cleanup leeren
    }

    public static String format(Duration d) {
        long hours = d.toHours();
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
