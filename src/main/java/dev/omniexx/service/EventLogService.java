package dev.omniexx.service;

import dev.omniexx.entity.Company;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.awt.*;

/**
 * Postet wichtige Server-Ereignisse automatisch in den konfigurierten Event-Log Channel.
 */
@Slf4j
@Service
public class EventLogService {

    @Lazy
    private final JDA jda;

    @Value("${omniexx.channels.event-log:}")
    private String eventLogChannelId;

    public EventLogService(@Lazy JDA jda) {
        this.jda = jda;
    }

    public void postInsolvency(Company company) {
        post(new EmbedBuilder()
                .setColor(new Color(0xE74C3C))
                .setTitle("💀 Insolvenz — " + company.getName())
                .setDescription(company.getName() + " hat Insolvenz angemeldet und startet neu.")
                .addField("Letzter Tick", "#" + company.getTickCount(), true)
                .addField("Markt", company.getMarket().getDisplayName(), true)
                .build());
    }

    public void postPrestige(Company company, String type) {
        String label = switch (type) {
            case "hard_reset"       -> "🔄 Hard Reset";
            case "ipo"              -> "📊 IPO";
            case "market_dominance" -> "🌍 Markt-Dominanz";
            default -> type;
        };
        post(new EmbedBuilder()
                .setColor(new Color(0xF1C40F))
                .setTitle("🌟 Prestige — " + company.getName())
                .setDescription(company.getName() + " hat Prestige ausgelöst: **" + label + "**")
                .addField("Prestige Level", String.valueOf(company.getPrestigeLevel() + 1), true)
                .addField("Legacy Mult.", "×" + company.getLegacyMultiplier(), true)
                .build());
    }

    public void postAcquisition(Company buyer, String targetName) {
        post(new EmbedBuilder()
                .setColor(new Color(0x3498DB))
                .setTitle("🤝 Akquisition")
                .setDescription("**" + buyer.getName() + "** hat **" + targetName + "** übernommen.")
                .build());
    }

    public void postSabotage(Company attacker, Company target, String action, boolean backfire) {
        String title = backfire ? "💥 Sabotage-Backfire!" : "🗡️ Sabotage enthüllt";
        String desc = backfire
                ? "**" + attacker.getName() + "** sabotierte **" + target.getName() + "** — Backfire!"
                : "**" + attacker.getName() + "** sabotierte **" + target.getName() + "** (" + action + ")";
        post(new EmbedBuilder()
                .setColor(backfire ? new Color(0x9B59B6) : new Color(0xE74C3C))
                .setTitle(title)
                .setDescription(desc)
                .build());
    }

    private void post(net.dv8tion.jda.api.entities.MessageEmbed embed) {
        if (eventLogChannelId == null || eventLogChannelId.isBlank()) return;
        try {
            TextChannel channel = jda.getTextChannelById(eventLogChannelId);
            if (channel == null) {
                log.warn("Event-Log Channel {} nicht gefunden", eventLogChannelId);
                return;
            }
            channel.sendMessageEmbeds(embed).queue(
                    ok  -> {},
                    err -> log.warn("Event-Log Post fehlgeschlagen: {}", err.getMessage())
            );
        } catch (Exception e) {
            log.warn("EventLogService Fehler: {}", e.getMessage());
        }
    }
}
