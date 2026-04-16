package dev.ceosim.entity;

public enum Market {
    // Startmärkte
    CONSUMER_TECH("Consumer Tech"),
    ENTERPRISE_SAAS("Enterprise SaaS"),
    FINTECH("Fintech"),
    E_COMMERCE("E-Commerce"),
    CYBERSECURITY("Cybersecurity"),

    // Freischaltbare Märkte
    AI_DEEP_TECH("AI / Deep Tech"),
    HEALTHCARE("Healthcare"),
    MEDIA_GAMING("Media & Gaming"),
    GOVERNMENT("Government Contracts");

    private final String displayName;

    Market(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isStartMarket() {
        return this.ordinal() < 5;
    }
}
