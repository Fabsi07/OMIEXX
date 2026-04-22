package dev.omniexx.service.work;

import dev.omniexx.entity.Company;
import dev.omniexx.repository.CompanyRepository;
import dev.omniexx.service.EventService;
import dev.omniexx.service.EventLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkService {

    private final CompanyRepository companyRepo;
    private final EventService      eventService;
    private final EventLogService   eventLogService;
    private static final Random     RNG = new Random();

    /** Berechnet Belohnungen basierend auf Tier und Firmen-State */
    @Transactional
    public WorkResult execute(Company company, WorkScenario scenario,
                              WorkScenario.Opt option) {

        WorkScenario.Tier tier = option.roll(RNG);

        // Streak-Bonus: +10% pro Streak-Tag, max +50%
        double streakMult = 1.0 + Math.min(0.5, company.getWorkStreak() * 0.05);

        long capitalBase  = switch (tier) {
            case JACKPOT  -> 8_000_00L + RNG.nextLong(12_000_00L);  // $8k–$20k
            case GREAT    -> 1_500_00L + RNG.nextLong(2_500_00L);   // $1.5k–$4k
            case GOOD     -> 300_00L   + RNG.nextLong(700_00L);     // $300–$1k
            case NORMAL   -> 100_00L   + RNG.nextLong(200_00L);     // $100–$300
            case BAD      -> -(200_00L + RNG.nextLong(300_00L));     // −$200–$500
            case CRITICAL -> -(500_00L + RNG.nextLong(500_00L));     // −$500–$1k
        };

        long capital = (long)(capitalBase * streakMult);

        int rpGained = switch (tier) {
            case JACKPOT  -> 8 + RNG.nextInt(8);  // 8–15
            case GREAT    -> 4 + RNG.nextInt(4);  // 4–7
            case GOOD     -> 2 + RNG.nextInt(2);  // 2–3
            case NORMAL   -> 1;
            case BAD, CRITICAL -> 0;
        };

        int moraleDelta = switch (tier) {
            case JACKPOT  -> +10;
            case GREAT    -> +5;
            case GOOD     -> +2;
            case NORMAL   -> 0;
            case BAD      -> -8;
            case CRITICAL -> -18;
        };

        // KPIs anwenden
        company.setCapital(Math.max(0, company.getCapital() + capital));
        company.setResearchPoints(company.getResearchPoints() + rpGained);
        company.setMorale((short) Math.max(0, Math.min(100, company.getMorale() + moraleDelta)));

        // Streak updaten
        LocalDate today = LocalDate.now();
        if (company.getWorkStreakDate() == null ||
                !company.getWorkStreakDate().equals(today)) {
            if (company.getWorkStreakDate() != null &&
                    company.getWorkStreakDate().plusDays(1).equals(today)) {
                company.setWorkStreak(company.getWorkStreak() + 1);
            } else {
                company.setWorkStreak(1); // Reset bei Lücke
            }
            company.setWorkStreakDate(today);
        }

        company.setTotalWorkCount(company.getTotalWorkCount() + 1);
        company.setLastWorkAt(java.time.OffsetDateTime.now());
        companyRepo.save(company);

        // Jackpot: Server-Post
        if (tier == WorkScenario.Tier.JACKPOT) {
            try { eventLogService.postJackpot(company, capital); } catch (Exception ignored) {}
        }

        // Event loggen
        String tierLabel = tierLabel(tier);
        eventService.log(company, "work_session",
                tierLabel + " — " + scenario.title,
                option.outcomeText,
                Map.of("capital", capital, "rp", (long) rpGained));

        return new WorkResult(tier, capital, rpGained, moraleDelta,
                company.getWorkStreak(), streakMult);
    }

    private String tierLabel(WorkScenario.Tier t) {
        return switch (t) {
            case JACKPOT  -> "🎰 JACKPOT";
            case GREAT    -> "🌟 Sehr gut";
            case GOOD     -> "✅ Gut";
            case NORMAL   -> "➡️ Normal";
            case BAD      -> "⚠️ Schwierig";
            case CRITICAL -> "💥 Kritisch";
        };
    }

    public record WorkResult(WorkScenario.Tier tier, long capitalDelta,
                              int rpGained, int moraleDelta,
                              int newStreak, double streakMult) {}
}
