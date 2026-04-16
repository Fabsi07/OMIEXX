package dev.ceosim.discord.command;

import dev.ceosim.entity.Company;
import dev.ceosim.entity.PrestigeHistory;
import dev.ceosim.repository.CompanyRepository;
import dev.ceosim.repository.PlayerRepository;
import dev.ceosim.repository.PrestigeHistoryRepository;
import dev.ceosim.service.achievement.AchievementType;
import dev.ceosim.repository.PlayerAchievementRepository;
import dev.ceosim.util.CeoEmbedBuilder;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class LegacyCommand {

    private final CompanyRepository        companyRepo;
    private final PlayerRepository         playerRepo;
    private final PrestigeHistoryRepository historyRepo;
    private final PlayerAchievementRepository achievementRepo;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yy");

    public void handle(SlashCommandInteractionEvent event) {
        var player = playerRepo.findByDiscordId(event.getUser().getId()).orElse(null);
        if (player == null) {
            event.reply("❌ Noch kein Account. Starte mit `/start`!").setEphemeral(true).queue();
            return;
        }

        List<PrestigeHistory> history = historyRepo.findByPlayerIdOrderByPrestigedAtDesc(player.getId());
        Set<String> achievements = achievementRepo.getUnlockedKeys(player.getId());
        var activeCompany = companyRepo.findActiveByDiscordId(player.getDiscordId()).orElse(null);

        EmbedBuilder eb = CeoEmbedBuilder.base("📜 Legacy — " + player.getDiscordName(), CeoEmbedBuilder.blue());

        // Aktueller Stand
        if (activeCompany != null) {
            eb.addField("Aktueller Run",
                    "Prestige Level " + activeCompany.getPrestigeLevel() +
                    "  •  Legacy ×" + activeCompany.getLegacyMultiplier() +
                    "  •  Tick #" + activeCompany.getTickCount(),
                    false);
        }

        // Prestige-History
        if (history.isEmpty()) {
            eb.addField("Prestige-History", "*Noch kein Prestige durchgeführt*", false);
        } else {
            StringBuilder sb = new StringBuilder();
            for (PrestigeHistory h : history.subList(0, Math.min(5, history.size()))) {
                String date = h.getPrestigedAt().format(FMT);
                sb.append(String.format("`%s` **%s** — %s  •  Tick #%d%n",
                        date,
                        prestigeTypeLabel(h.getPrestigeType()),
                        CeoEmbedBuilder.formatMoney(h.getValuationAt()),
                        h.getTickCount()));
                if (h.getCarriedEmployee() != null) {
                    sb.append("   👤 Mitgenommen: ").append(h.getCarriedEmployee().getFullName()).append("\n");
                }
            }
            eb.addField("🏆 Prestige-History (" + history.size() + " gesamt)", sb.toString(), false);
        }

        // Achievements
        if (!achievements.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String key : achievements) {
                try {
                    AchievementType t = AchievementType.valueOf(key);
                    sb.append(t.getEmoji()).append(" ").append(t.getName()).append("  ");
                } catch (IllegalArgumentException ignored) {}
            }
            eb.addField("🎖️ Achievements (" + achievements.size() + "/" + AchievementType.values().length + ")",
                    sb.toString(), false);
        }

        eb.setFooter("CEO Sim • " + achievements.size() + " Achievements freigeschaltet");
        event.replyEmbeds(eb.build()).queue();
    }

    private String prestigeTypeLabel(String type) {
        return switch (type) {
            case "hard_reset"        -> "🔄 Hard Reset";
            case "ipo"               -> "📊 IPO";
            case "market_dominance"  -> "🌍 Markt-Dominanz";
            case "hostile_takeover"  -> "⚔️ Hostile Takeover";
            default                  -> type;
        };
    }
}
