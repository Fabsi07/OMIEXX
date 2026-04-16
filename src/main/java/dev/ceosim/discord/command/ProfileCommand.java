package dev.ceosim.discord.command;

import dev.ceosim.entity.Company;
import dev.ceosim.repository.CompanyRepository;
import dev.ceosim.service.EventService;
import dev.ceosim.util.CeoEmbedBuilder;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProfileCommand {

    private final CompanyRepository companyRepo;
    private final EventService      eventService;

    public void handle(SlashCommandInteractionEvent event) {
        var firmaOpt = event.getOption("firma");

        Company company;
        if (firmaOpt == null) {
            // Eigenes Profil
            company = companyRepo.findActiveByDiscordId(event.getUser().getId()).orElse(null);
            if (company == null) {
                event.reply("❌ Keine aktive Firma. Starte mit `/start`!").setEphemeral(true).queue();
                return;
            }
        } else {
            String name = firmaOpt.getAsString().trim();
            company = companyRepo.findAllActiveOrderByValuation().stream()
                    .filter(c -> c.getName().equalsIgnoreCase(name) ||
                                 c.getName().toLowerCase().contains(name.toLowerCase()))
                    .findFirst()
                    .orElse(null);
            if (company == null) {
                event.reply("❌ Firma **\"" + name + "\"** nicht gefunden.").setEphemeral(true).queue();
                return;
            }
        }

        String markets = company.getMarkets().stream()
                .map(cm -> cm.getMarket().getDisplayName() + " (" + cm.getShare() + "%)")
                .collect(Collectors.joining("\n"));

        String recentActivity = eventService.getRecentEvents(company.getId(), 3).stream()
                .map(e -> "• " + e.getTitle())
                .collect(Collectors.joining("\n"));
        if (recentActivity.isBlank()) recentActivity = "*Noch keine Events*";

        EmbedBuilder eb = CeoEmbedBuilder.base("🏢 " + company.getName(), CeoEmbedBuilder.statusColor(company))
                .addField("💎 Valuation",    CeoEmbedBuilder.formatMoney(company.calculateValuation()), true)
                .addField("📈 Revenue/Tick", CeoEmbedBuilder.formatMoney(company.getRevenuePerTick()), true)
                .addField("⭐ Reputation",   company.getReputation() + "/100", true)
                .addField("👥 Team",         company.getActiveEmployees().size() + " Mitarbeiter", true)
                .addField("🏆 Prestige",     "Level " + company.getPrestigeLevel(), true)
                .addField("🎯 Tick",         "#" + company.getTickCount(), true)
                .addField("🌍 Aktive Märkte", markets.isBlank() ? "*keine*" : markets, false)
                .addField("📜 Letzte Aktivität", recentActivity, false)
                .setFooter("Markt: " + company.getMarket().getDisplayName() +
                           "  •  Startertyp: " + company.getStarterType().getDisplayName());

        event.replyEmbeds(eb.build()).queue();
    }
}
