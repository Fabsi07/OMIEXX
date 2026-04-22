package dev.omniexx.service.work;

import dev.omniexx.entity.Company;
import dev.omniexx.entity.PlayerEnergy;
import dev.omniexx.repository.PlayerEnergyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnergyService {

    private final PlayerEnergyRepository energyRepo;

    // ── Balancing-Konstanten ─────────────────────────────────────────────
    /** Minuten bis 1 Energie-Punkt regeneriert */
    private static final long REGEN_MINUTES = 90;
    /** Maximal-Energie (Default) */
    public static final short MAX_ENERGY    = 5;

    // ── Energie laden + Regen berechnen ─────────────────────────────────

    /**
     * Lädt oder erstellt PlayerEnergy, berechnet Regen seit letztem Aufruf.
     * Immer aufrufen bevor Energie gelesen oder verbraucht wird.
     */
    @Transactional
    public PlayerEnergy getOrCreate(Company company) {
        PlayerEnergy energy = energyRepo.findByCompanyId(company.getId())
                .orElseGet(() -> energyRepo.save(
                        PlayerEnergy.builder()
                                .company(company)
                                .current(MAX_ENERGY)
                                .maxEnergy(MAX_ENERGY)
                                .lastRegen(OffsetDateTime.now())
                                .build()
                ));

        applyRegen(energy);
        return energy;
    }

    private void applyRegen(PlayerEnergy energy) {
        if (energy.isFull()) {
            energy.setLastRegen(OffsetDateTime.now());
            return;
        }

        Duration elapsed  = Duration.between(energy.getLastRegen(), OffsetDateTime.now());
        long regenPoints  = elapsed.toMinutes() / REGEN_MINUTES;

        if (regenPoints > 0) {
            short newCurrent = (short) Math.min(energy.getMaxEnergy(),
                                                energy.getCurrent() + regenPoints);
            energy.setCurrent(newCurrent);

            // lastRegen nur um verbrauchte Punkte verschieben (Rest bleibt erhalten)
            long usedMinutes = regenPoints * REGEN_MINUTES;
            energy.setLastRegen(energy.getLastRegen().plusMinutes(usedMinutes));
            energyRepo.save(energy);
        }
    }

    // ── Energie verbrauchen ──────────────────────────────────────────────

    /**
     * Verbraucht amount Energie. Gibt false zurück wenn nicht genug da.
     */
    @Transactional
    public boolean consume(PlayerEnergy energy, int amount) {
        if (energy.getCurrent() < amount) return false;
        energy.setCurrent((short)(energy.getCurrent() - amount));
        if (energy.getCurrent() == 0) {
            energy.setLastRegen(OffsetDateTime.now()); // Regen-Timer starten
        }
        energyRepo.save(energy);
        return true;
    }

    /** Gibt 1 Bonus-Energie (z.B. nach Jackpot) */
    @Transactional
    public void addBonus(PlayerEnergy energy) {
        if (!energy.isFull()) {
            energy.setCurrent((short) Math.min(energy.getMaxEnergy(), energy.getCurrent() + 1));
            energyRepo.save(energy);
        }
    }

    /** Streak aktualisieren nach /work */
    @Transactional
    public void recordWorkSession(PlayerEnergy energy) {
        LocalDate today = LocalDate.now();
        energy.setTotalSessions(energy.getTotalSessions() + 1);

        if (energy.getLastWorkDate() == null) {
            energy.setWorkStreak(1);
        } else if (energy.getLastWorkDate().equals(today)) {
            // Heute schon gespielt — Streak nicht erhöhen
        } else if (energy.getLastWorkDate().equals(today.minusDays(1))) {
            energy.setWorkStreak(energy.getWorkStreak() + 1);
        } else {
            // Streak gerissen
            energy.setWorkStreak(1);
        }
        energy.setLastWorkDate(today);
        energyRepo.save(energy);
    }

    // ── Zeitanzeigen ─────────────────────────────────────────────────────

    /** Minuten bis zur nächsten Energie */
    public long minutesUntilNext(PlayerEnergy energy) {
        if (energy.isFull()) return 0;
        Duration elapsed  = Duration.between(energy.getLastRegen(), OffsetDateTime.now());
        long minutesDone  = elapsed.toMinutes() % REGEN_MINUTES;
        return REGEN_MINUTES - minutesDone;
    }

    /** Lesbare Anzeige: "nächste Energie in 47 Min" */
    public String nextRegenText(PlayerEnergy energy) {
        if (energy.isFull()) return "Energie voll!";
        long mins = minutesUntilNext(energy);
        if (mins <= 1) return "Gleich!";
        return "in " + mins + " Min";
    }

    public static long regenMinutes() { return REGEN_MINUTES; }
}
