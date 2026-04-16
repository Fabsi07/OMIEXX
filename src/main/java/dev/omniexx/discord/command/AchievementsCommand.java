package dev.omniexx.discord.command;

import dev.omniexx.repository.PlayerAchievementRepository;
import dev.omniexx.repository.PlayerRepository;
import dev.omniexx.service.achievement.AchievementType;
import dev.omniexx.util.OmniexxEmbedBuilder;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class AchievementsCommand {

    private final PlayerRepository            playerRepo;
    private final PlayerAchievementRepository achievementRepo;

    public void handle(SlashCommandInteractionEvent event) {
        var player = playerRepo.findByDiscordId(event.getUser().getId()).orElse(null);
        if (player == null) {
            event.reply("❌ Noch kein Account. Starte mit `/start`!").setEphemeral(true).queue();
            return;
        }

        Set<String> unlocked = achievementRepo.getUnlockedKeys(player.getId());
        int total = AchievementType.values().length;

        EmbedBuilder eb = OmniexxEmbedBuilder.base(
                "🎖️ Achievements — " + unlocked.size() + "/" + total, OmniexxEmbedBuilder.blue());

        StringBuilder unlockedSb = new StringBuilder();
        StringBuilder lockedSb   = new StringBuilder();

        for (AchievementType t : AchievementType.values()) {
            if (unlocked.contains(t.name())) {
                unlockedSb.append(t.getEmoji()).append(" **").append(t.getName()).append("**\n")
                          .append("   *").append(t.getDescription()).append("*\n");
            } else {
                lockedSb.append("🔒 ~~").append(t.getName()).append("~~\n")
                        .append("   *").append(t.getDescription()).append("*\n");
            }
        }

        if (unlockedSb.length() > 0) {
            eb.addField("✅ Freigeschaltet (" + unlocked.size() + ")", unlockedSb.toString(), false);
        }
        if (lockedSb.length() > 0) {
            // Discord Embed max 1024 Zeichen pro Field — kürzen
            String locked = lockedSb.toString();
            if (locked.length() > 900) locked = locked.substring(0, 900) + "\n*...und mehr*";
            eb.addField("🔒 Noch zu erreichen (" + (total - unlocked.size()) + ")", locked, false);
        }

        eb.setFooter("Achievements bleiben permanent — auch nach Prestige");
        event.replyEmbeds(eb.build()).setEphemeral(true).queue();
    }
}
