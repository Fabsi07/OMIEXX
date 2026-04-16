package dev.omniexx.entity;

public enum StarterType {

    BOOTSTRAPPER("Bootstrapper", "Solides Startkapital, langsames aber stabiles Wachstum.") {
        @Override public long startCapital() { return 2_000_000L; }   // $20.000 in Cents
        @Override public int  startRpPerTick() { return 1; }
        @Override public int  startFreeEmployees() { return 0; }
    },

    VISIONARY("Visionär", "Wenig Kapital, aber mehr Research Points pro Tick von Anfang an.") {
        @Override public long startCapital() { return 500_000L; }     // $5.000
        @Override public int  startRpPerTick() { return 3; }
        @Override public int  startFreeEmployees() { return 0; }
    },

    NETWORKER("Networker", "Mittleres Kapital und direkt 2 kostenlose Mitarbeiter beim Start.") {
        @Override public long startCapital() { return 1_000_000L; }   // $10.000
        @Override public int  startRpPerTick() { return 1; }
        @Override public int  startFreeEmployees() { return 2; }
    };

    private final String displayName;
    private final String description;

    StarterType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription()  { return description; }

    public abstract long startCapital();
    public abstract int  startRpPerTick();
    public abstract int  startFreeEmployees();
}
