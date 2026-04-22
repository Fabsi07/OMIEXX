package dev.omniexx.service.work;

import lombok.Builder;
import lombok.Getter;

import java.util.Random;

/**
 * Berechnet den konkreten Outcome einer Work-Session.
 * Balancing-Werte sind hier als Konstanten definiert — leicht anpassbar.
 */
@Getter
@Builder
public class WorkOutcome {

    public enum Tier {
        NORMAL, GOOD, GREAT, JACKPOT, BAD, CRISIS
    }

    private final Tier   tier;
    private final long   capitalGained;   // in Cents; negativ bei BAD/CRISIS
    private final int    rpGained;
    private final int    moraleDelta;
    private final String flavorText;
    private final String emoji;
    private final boolean bonusEnergy;   // GREAT+ manchmal +1 Energie

    // ── BALANCING-KONSTANTEN ─────────────────────────────────────────────
    // Alle Werte hier zentral — nach Playtest anpassen

    // Kapital-Ranges (in Cents) pro Tier
    private static final long CAP_NORMAL_MIN  =   10_000L;  // $100
    private static final long CAP_NORMAL_MAX  =   80_000L;  // $800
    private static final long CAP_GOOD_MIN    =   80_000L;  // $800
    private static final long CAP_GOOD_MAX    =  200_000L;  // $2.000
    private static final long CAP_GREAT_MIN   =  200_000L;  // $2.000
    private static final long CAP_GREAT_MAX   =  500_000L;  // $5.000
    private static final long CAP_JACKPOT_MIN =  500_000L;  // $5.000
    private static final long CAP_JACKPOT_MAX = 2_000_000L; // $20.000
    private static final long CAP_BAD_MIN     =    5_000L;  // $50 Kosten
    private static final long CAP_BAD_MAX     =   50_000L;  // $500 Kosten
    private static final long CAP_CRISIS_MIN  =   50_000L;  // $500 Kosten
    private static final long CAP_CRISIS_MAX  =  200_000L;  // $2.000 Kosten

    // RP-Gains
    private static final int RP_NORMAL   = 1;
    private static final int RP_GOOD     = 3;
    private static final int RP_GREAT    = 6;
    private static final int RP_JACKPOT  = 15;
    private static final int RP_BAD      = 0;
    private static final int RP_CRISIS   = 0;

    // Morale-Deltas
    private static final int MORALE_NORMAL   =  3;
    private static final int MORALE_GOOD     =  7;
    private static final int MORALE_GREAT    = 12;
    private static final int MORALE_JACKPOT  = 20;
    private static final int MORALE_BAD      = -12;
    private static final int MORALE_CRISIS   = -25;

    // Bonus-Energie Chancen
    private static final double BONUS_ENERGY_GREAT   = 0.15; // 15%
    private static final double BONUS_ENERGY_JACKPOT = 0.40; // 40%

    // ── BERECHNUNG ───────────────────────────────────────────────────────

    public static WorkOutcome calculate(
            WorkScenario scenario,
            WorkScenario.WorkOption option,
            String flavorOverride,
            Random rng,
            boolean isFirstEverWork  // Garantierter positiver Outcome beim ersten Mal
    ) {
        Tier tier = rollTier(option, rng, isFirstEverWork);
        return buildOutcome(tier, flavorOverride != null ? flavorOverride : option.getFlavor(), rng);
    }

    private static Tier rollTier(WorkScenario.WorkOption opt, Random rng, boolean forcePositive) {
        // Weights aus der Option
        int[] weights = {
            opt.getWeightNormal(),
            opt.getWeightGood(),
            opt.getWeightGreat(),
            opt.getWeightJackpot(),
            opt.getWeightBad(),
            opt.getWeightCrisis()
        };

        // Beim allerersten Work: BAD und CRISIS auf 0 setzen
        if (forcePositive) {
            weights[4] = 0; // BAD
            weights[5] = 0; // CRISIS
            // NORMAL auf GOOD Niveau anheben
            weights[0] = Math.min(weights[0], 20);
            weights[1] = Math.max(weights[1], 40);
        }

        int total = 0;
        for (int w : weights) total += w;
        if (total == 0) return Tier.NORMAL;

        int roll = rng.nextInt(total);
        int cumulative = 0;
        Tier[] tiers = Tier.values();
        for (int i = 0; i < weights.length; i++) {
            cumulative += weights[i];
            if (roll < cumulative) return tiers[i];
        }
        return Tier.NORMAL;
    }

    private static WorkOutcome buildOutcome(Tier tier, String flavor, Random rng) {
        return switch (tier) {
            case NORMAL -> WorkOutcome.builder()
                    .tier(tier)
                    .capitalGained(randRange(CAP_NORMAL_MIN, CAP_NORMAL_MAX, rng))
                    .rpGained(RP_NORMAL)
                    .moraleDelta(MORALE_NORMAL)
                    .flavorText(flavor)
                    .emoji("📋")
                    .bonusEnergy(false)
                    .build();
            case GOOD -> WorkOutcome.builder()
                    .tier(tier)
                    .capitalGained(randRange(CAP_GOOD_MIN, CAP_GOOD_MAX, rng))
                    .rpGained(RP_GOOD)
                    .moraleDelta(MORALE_GOOD)
                    .flavorText(flavor)
                    .emoji("✅")
                    .bonusEnergy(false)
                    .build();
            case GREAT -> WorkOutcome.builder()
                    .tier(tier)
                    .capitalGained(randRange(CAP_GREAT_MIN, CAP_GREAT_MAX, rng))
                    .rpGained(RP_GREAT)
                    .moraleDelta(MORALE_GREAT)
                    .flavorText(flavor)
                    .emoji("🌟")
                    .bonusEnergy(rng.nextDouble() < BONUS_ENERGY_GREAT)
                    .build();
            case JACKPOT -> WorkOutcome.builder()
                    .tier(tier)
                    .capitalGained(randRange(CAP_JACKPOT_MIN, CAP_JACKPOT_MAX, rng))
                    .rpGained(RP_JACKPOT)
                    .moraleDelta(MORALE_JACKPOT)
                    .flavorText(flavor)
                    .emoji("💎")
                    .bonusEnergy(rng.nextDouble() < BONUS_ENERGY_JACKPOT)
                    .build();
            case BAD -> WorkOutcome.builder()
                    .tier(tier)
                    .capitalGained(-randRange(CAP_BAD_MIN, CAP_BAD_MAX, rng))
                    .rpGained(RP_BAD)
                    .moraleDelta(MORALE_BAD)
                    .flavorText(flavor)
                    .emoji("⚠️")
                    .bonusEnergy(false)
                    .build();
            case CRISIS -> WorkOutcome.builder()
                    .tier(tier)
                    .capitalGained(-randRange(CAP_CRISIS_MIN, CAP_CRISIS_MAX, rng))
                    .rpGained(RP_CRISIS)
                    .moraleDelta(MORALE_CRISIS)
                    .flavorText(flavor)
                    .emoji("💥")
                    .bonusEnergy(false)
                    .build();
        };
    }

    private static long randRange(long min, long max, Random rng) {
        return min + (long)(rng.nextDouble() * (max - min));
    }

    /** Lesbarer Tier-Name */
    public String tierLabel() {
        return switch (tier) {
            case NORMAL  -> "Normal";
            case GOOD    -> "Gutes Ergebnis";
            case GREAT   -> "Sehr gutes Ergebnis!";
            case JACKPOT -> "💎 JACKPOT!";
            case BAD     -> "Rückschlag";
            case CRISIS  -> "Krise!";
        };
    }
}
