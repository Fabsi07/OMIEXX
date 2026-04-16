package dev.omniexx.util;

import dev.omniexx.entity.Company;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.time.OffsetDateTime;

/**
 * Zentrale Embed-Factory für einheitliches Look & Feel.
 * Embed-Farbe ändert sich je nach finanzieller Lage der Company.
 */
public class OmniexxEmbedBuilder {

    // Farben
    private static final Color GREEN  = new Color(0x2ECC71);
    private static final Color YELLOW = new Color(0xF1C40F);
    private static final Color RED    = new Color(0xE74C3C);
    private static final Color BLUE   = new Color(0x3498DB);
    private static final Color GREY   = new Color(0x95A5A6);

    private OmniexxEmbedBuilder() {}

    /** Erstellt ein Basis-Embed mit Timestamp und Footer */
    public static EmbedBuilder base(String title, Color color) {
        return new EmbedBuilder()
                .setTitle(title)
                .setColor(color)
                .setTimestamp(OffsetDateTime.now());
    }

    /** Farbe basierend auf Net Income der Company */
    public static Color statusColor(Company company) {
        long net = company.getNetIncomePerTick();
        if (company.getBankrupt()) return RED;
        if (net < 0)               return RED;
        if (net < 10_000)          return YELLOW;  // unter $100 Gewinn/Tick
        return GREEN;
    }

    /** Report Embed für /report */
    public static EmbedBuilder reportEmbed(Company company) {
        Color color = statusColor(company);
        int activeEmployees = company.getActiveEmployees().size();

        return base("🏢 " + company.getName(), color)
                .addField("💰 Kapital",        formatMoney(company.getCapital()), true)
                .addField("📈 Umsatz/Tick",    formatMoney(company.getRevenuePerTick()), true)
                .addField("🔥 Burn Rate",      formatMoney(company.getBurnRate()), true)
                .addField("😊 Morale",         progressBar(company.getMorale(), 100) + " " + company.getMorale() + "/100", true)
                .addField("🌍 Marktanteil",    company.getMarketShare() + "%", true)
                .addField("⭐ Reputation",     progressBar(company.getReputation(), 100) + " " + company.getReputation() + "/100", true)
                .addField("💎 Valuation",      formatMoney(company.calculateValuation()), true)
                .addField("👥 Team",           activeEmployees + " Mitarbeiter", true)
                .addField("🔬 Research",       company.getResearchPoints() + " RP  (+"+company.getRpPerTick()+"/Tick)", true)
                .addField("📊 Markt",          company.getMarket().getDisplayName(), true)
                .addField("🎯 Tick",           "#" + company.getTickCount(), true)
                .addField("🏆 Prestige",       "Level " + company.getPrestigeLevel() + "  (×" + company.getLegacyMultiplier() + ")", true)
                .setFooter("OMNIEXX • " + company.getStarterType().getDisplayName());
    }

    /** Konvertiert Cents zu lesbarem String: 1000000 → $10.000,00 */
    public static String formatMoney(long cents) {
        long dollars = cents / 100;
        long centsRest = Math.abs(cents % 100);
        if (dollars >= 1_000_000) {
            return String.format("$%.1fM", dollars / 1_000_000.0);
        }
        if (dollars >= 1_000) {
            return String.format("$%,d", dollars);
        }
        return String.format("$%d.%02d", dollars, centsRest);
    }

    /** Einfacher Fortschrittsbalken: ████░░░░ */
    public static String progressBar(int value, int max) {
        int filled = Math.round((value / (float) max) * 8);
        return "█".repeat(filled) + "░".repeat(8 - filled);
    }

    public static Color blue() { return BLUE; }
    public static Color grey() { return GREY; }
    public static Color red()  { return RED; }
    public static Color green(){ return GREEN; }
}
