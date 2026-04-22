package dev.omniexx.service.work;

import lombok.Builder;
import lombok.Getter;

/**
 * Ergebnis einer Work-Session.
 */
@Getter
@Builder
public class WorkOutcome {

    public enum Tier {
        JACKPOT, GREAT, GOOD, NORMAL, BAD, CRITICAL;

        public String emoji() {
            return switch (this) {
                case JACKPOT   -> "🎰";
                case GREAT     -> "🌟";
                case GOOD      -> "✅";
                case NORMAL    -> "📋";
                case BAD       -> "⚠️";
                case CRITICAL  -> "💥";
            };
        }

        public String label() {
            return switch (this) {
                case JACKPOT  -> "JACKPOT!";
                case GREAT    -> "Exzellent";
                case GOOD     -> "Gut";
                case NORMAL   -> "Normal";
                case BAD      -> "Rückschlag";
                case CRITICAL -> "Krise!";
            };
        }

        public boolean isPositive() {
            return this == JACKPOT || this == GREAT || this == GOOD || this == NORMAL;
        }
    }

    private final Tier   tier;
    private final long   capitalDelta;   // Cents
    private final short  moraleDelta;
    private final short  rpDelta;
    private final boolean rareCardDrop;
    private final String  flavourText;    // Erklärender Satz zum Ergebnis
    private final boolean serverAnnounce; // Jackpots werden im Event-Log gepostet
}
