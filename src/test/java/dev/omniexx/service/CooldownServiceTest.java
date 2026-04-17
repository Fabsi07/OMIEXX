package dev.omniexx.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

class CooldownServiceTest {

    private CooldownService service;

    @BeforeEach
    void setUp() { service = new CooldownService(); }

    @Test
    void keinCooldownAnfangs() {
        assertThat(service.getRemaining("user1", "hire")).isNull();
    }

    @Test
    void cooldownNachSetzenAktiv() {
        service.set("user1", "hire", Duration.ofHours(3));
        assertThat(service.getRemaining("user1", "hire")).isNotNull();
    }

    @Test
    void checkAndSetSetztCooldownUndGibtNullZurueck() {
        Duration result = service.checkAndSet("user1", "hire", Duration.ofHours(3));
        assertThat(result).isNull();
    }

    @Test
    void checkAndSetGibtRestZeitBeiAktivemCooldown() {
        service.set("user1", "hire", Duration.ofHours(3));
        Duration remaining = service.checkAndSet("user1", "hire", Duration.ofHours(3));
        assertThat(remaining).isNotNull().isPositive();
    }

    @Test
    void verschiedeneUserHabenUnabhaengigeCooldowns() {
        service.set("user1", "hire", Duration.ofHours(3));
        assertThat(service.getRemaining("user2", "hire")).isNull();
    }

    @Test
    void formatGibtLesbareZeit() {
        assertThat(CooldownService.format(Duration.ofHours(2).plusMinutes(15)))
                .isEqualTo("2h 15m");
        assertThat(CooldownService.format(Duration.ofMinutes(45).plusSeconds(30)))
                .isEqualTo("45m 30s");
    }
}
