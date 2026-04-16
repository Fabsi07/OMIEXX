package dev.ceosim.discord.command;

import dev.ceosim.entity.Company;
import dev.ceosim.repository.CompanyRepository;
import dev.ceosim.util.CeoEmbedBuilder;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MarketCommand {

    private final CompanyRepository companyRepo;

    public void handle(SlashCommandInteractionEvent event) {
        List<Company> companies = companyRepo.findAllActiveOrderByValuation();

        EmbedBuilder eb = CeoEmbedBuilder.base("📊 Markt-Leaderboard", CeoEmbedBuilder.blue())
                .setDescription("Top Firmen nach Valuation — Stand jetzt");

        if (companies.isEmpty()) {
            eb.setDescription("Noch keine Firmen vorhanden. Sei der erste mit `/start`!");
        } else {
            StringBuilder sb = new StringBuilder("```\n");
            sb.append(String.format("%-3s %-22s %-16s  Marktanteil  Tick%n", "#", "Firma", "Valuation"));
            sb.append("─".repeat(62)).append("\n");

            String callerId = event.getUser().getId();

            for (int i = 0; i < Math.min(companies.size(), 15); i++) {
                Company c = companies.get(i);
                boolean isMe = c.getPlayer().getDiscordId().equals(callerId);
                String marker = isMe ? " ◄" : "";

                sb.append(String.format("%-3s %-22s %-16s     %5s%%   %s%s%n",
                        rankEmoji(i),
                        truncate(c.getName(), 22),
                        CeoEmbedBuilder.formatMoney(c.calculateValuation()),
                        c.getMarketShare(),
                        "#" + c.getTickCount(),
                        marker
                ));
            }
            sb.append("```");
            eb.setDescription(sb.toString());
        }

        eb.setFooter("CEO Sim • " + companies.size() + " aktive Firmen • Aktualisiert jetzt");
        event.replyEmbeds(eb.build()).queue();
    }

    private String rankEmoji(int index) {
        return switch (index) {
            case 0 -> "🥇";
            case 1 -> "🥈";
            case 2 -> "🥉";
            default -> String.valueOf(index + 1) + ".";
        };
    }

    private String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}
