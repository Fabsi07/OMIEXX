package dev.omniexx.service;

import java.util.Random;

/**
 * Hardcoded Name-Pools pro Rolle.
 * Keine Datenbank, keine API — läuft komplett lokal.
 */
public final class NamePool {

    private static final Random RANDOM = new Random();

    // First names
    private static final String[] FIRST_NAMES = {
        "Alex", "Jordan", "Taylor", "Morgan", "Casey", "Riley", "Quinn",
        "Avery", "Peyton", "Blake", "Jamie", "Skyler", "Drew", "Reese",
        "Lena", "Max", "Nina", "Finn", "Zoe", "Leon", "Maya", "Lars",
        "Emma", "Tom", "Sara", "Erik", "Lisa", "Ben", "Mia", "Tim",
        "Hannah", "Noah", "Sophie", "Lukas", "Julia", "Felix", "Anna",
        "David", "Laura", "Jan", "Katharina", "Markus", "Sandra", "Paul"
    };

    // Last names — tech/startup feel
    private static final String[] LAST_NAMES = {
        "Chen", "Park", "Walker", "Singh", "Müller", "Nakamura", "Okafor",
        "Torres", "Weber", "Patel", "Kim", "Bauer", "Andersen", "Kovacs",
        "Fischer", "Johansson", "Nguyen", "Schmidt", "Ali", "Rodriguez",
        "Martinez", "Thompson", "Clarke", "Evans", "Hughes", "Lewis",
        "Turner", "Shah", "Petrov", "Mayer", "Hoffman", "Braun", "Wolf"
    };

    private NamePool() {}

    /** Gibt [firstName, lastName] zurück */
    public static String[] randomName(String role) {
        // Alle Rollen bekommen zufällige Namen aus denselben Pools.
        // Kann später pro Rolle spezialisiert werden.
        String first = FIRST_NAMES[RANDOM.nextInt(FIRST_NAMES.length)];
        String last  = LAST_NAMES[RANDOM.nextInt(LAST_NAMES.length)];
        return new String[]{first, last};
    }
}
