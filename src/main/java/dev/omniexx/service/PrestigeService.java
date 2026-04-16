package dev.omniexx.service;

import dev.omniexx.entity.*;
import dev.omniexx.repository.*;
import dev.omniexx.service.achievement.AchievementService;
import dev.omniexx.service.achievement.AchievementType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrestigeService {

    private final CompanyRepository        companyRepo;
    private final PlayerRepository         playerRepo;
    private final PrestigeHistoryRepository historyRepo;
    private final AchievementService       achievementService;

    private static final long   PRESTIGE_MIN_VALUATION = 100_000_000L; // $1M in Cents
    private static final double LEGACY_BONUS           = 0.10;          // +10% pro Hard Reset

    // ── Hard Reset ───────────────────────────────────────────────────────
    /**
     * Setzt Firma auf 0 zurück.
     * Gibt Legacy-Multiplikator +10%, 1 Mitarbeiter bleibt.
     */
    @Transactional
    public Company hardReset(Company company, Employee carriedEmployee) {
        if (company.calculateValuation() < PRESTIGE_MIN_VALUATION) {
            throw new IllegalStateException("Prestige erst ab **$1M Valuation** möglich.");
        }

        Player player = company.getPlayer();

        // History speichern
        BigDecimal newMult = company.getLegacyMultiplier()
                .add(BigDecimal.valueOf(LEGACY_BONUS));

        historyRepo.save(PrestigeHistory.builder()
                .player(player)
                .company(company)
                .prestigeType("hard_reset")
                .valuationAt(company.calculateValuation())
                .tickCount(company.getTickCount())
                .legacyMultAfter(newMult)
                .carriedEmployee(carriedEmployee)
                .build());

        // Alte Firma soft-deleten
        company.setDeletedAt(OffsetDateTime.now());
        companyRepo.save(company);

        // Neue Firma anlegen
        Company newCompany = Company.builder()
                .player(player)
                .name(company.getName())                      // Name behalten
                .market(company.getMarket())                  // Startmarkt behalten
                .starterType(company.getStarterType())
                .capital(company.getStarterType().startCapital())
                .rpPerTick((short) company.getStarterType().startRpPerTick())
                .prestigeLevel((short) (company.getPrestigeLevel() + 1))
                .legacyMultiplier(newMult)
                .tutorialDone(true)                           // Tutorial überspringen
                .build();

        // Startmarkt eintragen
        CompanyMarket cm = CompanyMarket.builder()
                .company(newCompany)
                .market(newCompany.getMarket())
                .build();
        newCompany.getMarkets().add(cm);

        companyRepo.save(newCompany);

        // Mitarbeiter migrieren (Generationen-Mechanik)
        if (carriedEmployee != null) {
            Employee carried = Employee.builder()
                    .company(newCompany)
                    .firstName(carriedEmployee.getFirstName())
                    .lastName(carriedEmployee.getLastName())
                    .role(carriedEmployee.getRole())
                    .skill(carriedEmployee.getSkill())
                    .loyalty(carriedEmployee.getLoyalty())
                    .salaryPerTick(carriedEmployee.getSalaryPerTick())
                    .fireReason("generation")
                    .build();
            newCompany.getEmployees().add(carried);
            newCompany.setBurnRate(carriedEmployee.getSalaryPerTick());
            companyRepo.save(newCompany);
        }

        // Achievements
        achievementService.unlock(player, AchievementType.FIRST_PRESTIGE);
        if (newCompany.getPrestigeLevel() >= 3) {
            achievementService.unlock(player, AchievementType.TRIPLE_PRESTIGE);
        }

        log.info("Hard Prestige: {} → Level {}", player.getDiscordName(), newCompany.getPrestigeLevel());
        return newCompany;
    }

    // ── Soft Prestige ────────────────────────────────────────────────────
    /**
     * Einmalige Mega-Aktion ohne Reset.
     * Optionen: ipo | hostile_takeover | market_dominance
     */
    @Transactional
    public String softPrestige(Company company, String type) {
        if (company.calculateValuation() < PRESTIGE_MIN_VALUATION) {
            throw new IllegalStateException("Soft Prestige erst ab **$1M Valuation** möglich.");
        }
        if (company.getSoftPrestigeUsed()) {
            throw new IllegalStateException("Soft Prestige wurde in diesem Run bereits genutzt!");
        }

        company.setSoftPrestigeUsed(true);

        historyRepo.save(PrestigeHistory.builder()
                .player(company.getPlayer())
                .company(company)
                .prestigeType(type)
                .valuationAt(company.calculateValuation())
                .tickCount(company.getTickCount())
                .legacyMultAfter(company.getLegacyMultiplier())
                .build());

        String resultMessage = switch (type) {
            case "ipo" -> {
                // IPO: Kapital-Injection basierend auf Valuation
                long capital = (long)(company.calculateValuation() * 0.25);
                company.setCapital(company.getCapital() + capital);
                company.setReputation((short) Math.min(100, company.getReputation() + 15));
                yield "🎉 **IPO erfolgreich!** " + OmniexxEmbedBuilderRef.fmt(capital) +
                      " eingenommen. Reputation +15.";
            }
            case "market_dominance" -> {
                // Marktdominanz: Alle Marktanteile +10%
                company.getMarkets().forEach(m -> {
                    double newShare = Math.min(50.0, m.getShare().doubleValue() + 10.0);
                    m.setShare(BigDecimal.valueOf(newShare));
                });
                company.setRevenuePerTick(company.getRevenuePerTick() + 500_000L);
                yield "🌍 **Markt-Dominanz aktiviert!** Alle Marktanteile +10%, Revenue +$5.000/Tick.";
            }
            default -> throw new IllegalArgumentException("Unbekannter Typ: " + type);
        };

        companyRepo.save(company);
        achievementService.unlock(company.getPlayer(), AchievementType.SOFT_PRESTIGE);
        return resultMessage;
    }

    public static long getMinValuation() { return PRESTIGE_MIN_VALUATION; }
}

// Mini-Hilfsklasse um OmniexxEmbedBuilder nicht direkt zu importieren (kein JDA in Service)
class OmniexxEmbedBuilderRef {
    static String fmt(long cents) {
        long d = cents / 100;
        if (d >= 1_000_000) return String.format("$%.1fM", d / 1_000_000.0);
        if (d >= 1_000)     return String.format("$%,d", d);
        return "$" + d;
    }
}
