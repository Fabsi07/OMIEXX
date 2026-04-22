package dev.omniexx.service.work;

import dev.omniexx.entity.*;
import dev.omniexx.repository.*;
import dev.omniexx.service.EventService;
import dev.omniexx.util.OmniexxEmbedBuilder;
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

    private final CompanyRepository    companyRepo;
    private final WorkSessionRepository sessionRepo;
    private final WorkStreakRepository  streakRepo;
    private final EventService          eventService;

    private static final Random RNG = new Random();

    // Basis-Belohnungen pro Tier (in Cents)
    private static final long BASE_JACKPOT  = 1_000_000L;  // $10k Basis, multiplied by level
    private static final long BASE_GREAT    =   300_000L;
    private static final long BASE_GOOD     =   100_000L;
    private static final long BASE_NORMAL   =    40_000L;
    private static final long BASE_BAD      =   -20_000L;
    private static final long BASE_CRITICAL =  -100_000L;

    // ── Outcome berechnen ────────────────────────────────────────────────

    public WorkOutcome calculateOutcome(WorkScenario scenario,
                                        WorkScenario.Option option,
                                        Company company) {
        WorkOutcome.Tier tier = rollTier(option);

        // Prestige-Level skaliert Belohnungen
        double prestigeScale = 1.0 + (company.getPrestigeLevel() * 0.15);
        double optionMult    = option.getCapitalMultiplier();

        long capital = switch (tier) {
            case JACKPOT  -> (long)((BASE_JACKPOT  + RNG.nextLong(2_000_000L)) * prestigeScale * optionMult);
            case GREAT    -> (long)((BASE_GREAT    + RNG.nextLong(500_000L))   * prestigeScale * optionMult);
            case GOOD     -> (long)((BASE_GOOD     + RNG.nextLong(200_000L))   * prestigeScale * optionMult);
            case NORMAL   -> (long)((BASE_NORMAL   + RNG.nextLong(60_000L))    * prestigeScale * optionMult);
            case BAD      -> (long)((BASE_BAD      - RNG.nextLong(30_000L))    * optionMult);
            case CRITICAL -> (long)((BASE_CRITICAL - RNG.nextLong(100_000L))   * optionMult);
        };

        short morale = (short)(switch (tier) {
            case JACKPOT  -> 15 + RNG.nextInt(10);
            case GREAT    -> 8  + RNG.nextInt(7);
            case GOOD     -> 3  + RNG.nextInt(5);
            case NORMAL   -> 0;
            case BAD      -> -15 - RNG.nextInt(10);
            case CRITICAL -> -25 - RNG.nextInt(15);
        } + option.getMoraleBonus());

        short rp = (short)(switch (tier) {
            case JACKPOT  -> 20 + RNG.nextInt(15);
            case GREAT    -> 10 + RNG.nextInt(10);
            case GOOD     -> 5  + RNG.nextInt(5);
            case NORMAL   -> 2;
            case BAD      -> 0;
            case CRITICAL -> 0;
        } + option.getRpBonus());

        // Rare-Card: Jackpot immer, sonst geringe Chance
        boolean rareCard = tier == WorkOutcome.Tier.JACKPOT
                || (tier == WorkOutcome.Tier.GREAT && RNG.nextInt(100) < 15)
                || (tier == WorkOutcome.Tier.GOOD  && RNG.nextInt(100) < 3);

        return WorkOutcome.builder()
                .tier(tier)
                .capitalDelta(capital)
                .moraleDelta(morale)
                .rpDelta(rp)
                .rareCardDrop(rareCard)
                .flavourText(buildFlavour(tier, scenario, option))
                .serverAnnounce(tier == WorkOutcome.Tier.JACKPOT)
                .build();
    }

    // ── Outcome anwenden ─────────────────────────────────────────────────

    @Transactional
    public void applyOutcome(Company company, WorkScenario scenario,
                             WorkScenario.Option option, WorkOutcome outcome) {

        // Kapital
        company.setCapital(Math.max(0, company.getCapital() + outcome.getCapitalDelta()));

        // Morale (0–100)
        short newMorale = (short) Math.max(0, Math.min(100,
                company.getMorale() + outcome.getMoraleDelta()));
        company.setMorale(newMorale);

        // Research Points
        company.setResearchPoints(company.getResearchPoints() + outcome.getRpDelta());

        // Valuation neu berechnen
        company.setValuation(company.calculateValuation());
        companyRepo.save(company);

        // Work-Session loggen
        sessionRepo.save(WorkSession.builder()
                .company(company)
                .scenarioKey(scenario.getKey())
                .chosenOption(option.getLabel().substring(0, Math.min(4, option.getLabel().length())))
                .outcomeTier(outcome.getTier().name().toLowerCase())
                .capitalDelta(outcome.getCapitalDelta())
                .moraleDelta(outcome.getMoraleDelta())
                .rpDelta(outcome.getRpDelta())
                .rareCardDrop(outcome.isRareCardDrop())
                .build());

        // Streak aktualisieren
        updateStreak(company);

        // Event-Log
        eventService.log(company, "work_session",
                outcome.getTier().emoji() + " Work: " + outcome.getTier().label(),
                outcome.getFlavourText(),
                Map.of("capital", outcome.getCapitalDelta(),
                       "morale", (long) outcome.getMoraleDelta()));
    }

    // ── Streak ───────────────────────────────────────────────────────────

    @Transactional
    public void updateStreak(Company company) {
        WorkStreak streak = streakRepo.findByCompanyId(company.getId())
                .orElseGet(() -> WorkStreak.builder().company(company).build());

        LocalDate today = LocalDate.now();
        if (streak.getLastWorkDate() == null) {
            streak.setCurrentStreak(1);
        } else if (streak.getLastWorkDate().equals(today)) {
            // Heute schon gespielt — nur Sessions hochzählen
        } else if (streak.getLastWorkDate().equals(today.minusDays(1))) {
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
        } else {
            // Streak gerissen
            streak.setCurrentStreak(1);
        }

        streak.setLastWorkDate(today);
        streak.setTotalSessions(streak.getTotalSessions() + 1);
        if (streak.getCurrentStreak() > streak.getLongestStreak()) {
            streak.setLongestStreak(streak.getCurrentStreak());
        }
        streakRepo.save(streak);
    }

    public WorkStreak getStreak(Company company) {
        return streakRepo.findByCompanyId(company.getId())
                .orElseGet(() -> WorkStreak.builder()
                        .company(company)
                        .currentStreak(0)
                        .longestStreak(0)
                        .totalSessions(0)
                        .build());
    }

    // ── Tier-Roll ────────────────────────────────────────────────────────

    private WorkOutcome.Tier rollTier(WorkScenario.Option option) {
        int total = option.getJackpotWeight() + option.getGreatWeight()
                + option.getGoodWeight()   + option.getNormalWeight()
                + option.getBadWeight()    + option.getCriticalWeight();

        int roll = RNG.nextInt(Math.max(total, 1));
        int cum  = 0;

        if ((cum += option.getJackpotWeight())  > roll) return WorkOutcome.Tier.JACKPOT;
        if ((cum += option.getGreatWeight())    > roll) return WorkOutcome.Tier.GREAT;
        if ((cum += option.getGoodWeight())     > roll) return WorkOutcome.Tier.GOOD;
        if ((cum += option.getNormalWeight())   > roll) return WorkOutcome.Tier.NORMAL;
        if ((cum += option.getBadWeight())      > roll) return WorkOutcome.Tier.BAD;
        return WorkOutcome.Tier.CRITICAL;
    }

    // ── Flavour-Texte ────────────────────────────────────────────────────

    private String buildFlavour(WorkOutcome.Tier tier, WorkScenario scenario, WorkScenario.Option option) {
        return switch (tier) {
            case JACKPOT  -> "🎰 Unglaublich — " + option.getLabel() + " war die perfekte Entscheidung. Das Team ist euphorisch!";
            case GREAT    -> "🌟 Ausgezeichnet. " + option.getLabel() + " hat mehr gebracht als erwartet.";
            case GOOD     -> "✅ " + option.getLabel() + " — solide Entscheidung. Firma wächst.";
            case NORMAL   -> "📋 " + option.getLabel() + ". Nichts Spektakuläres, aber der Betrieb läuft.";
            case BAD      -> "⚠️ " + option.getLabel() + " hat nicht funktioniert. Rückschlag, aber überwindbar.";
            case CRITICAL -> "💥 Schlechte Entscheidung. " + option.getLabel() + " hat eine Krise ausgelöst!";
        };
    }
}
