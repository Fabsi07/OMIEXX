package dev.omniexx.discord.command;

import dev.omniexx.entity.Company;
import dev.omniexx.entity.PlayerEnergy;
import dev.omniexx.repository.CompanyRepository;
import dev.omniexx.repository.WorkSessionRepository;
import dev.omniexx.service.work.EnergyService;
import dev.omniexx.util.OmniexxEmbedBuilder;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnergyCommand {

    private final CompanyRepository   companyRepo;
    private final EnergyService       energyService;
    private final WorkSessionRepository sessionRepo;

    public void handle(SlashCommandInteractionEvent event) {
        Company company = companyRepo.findActiveByDiscordId(event.getUser().getId()).orElse(null);
        if (company == null) {
            event.reply("❌ Keine aktive Firma. Starte mit `/start`!").setEphemeral(true).queue();
            return;
        }

        PlayerEnergy energy = energyService.getOrCreate(company);
        long todaySessions  = sessionRepo.countTodayByCompanyId(company.getId());
        long jackpots       = sessionRepo.countJackpotsByCompanyId(company.getId());

        EmbedBuilder eb = OmniexxEmbedBuilder.base(
                "⚡ Energie — " + company.getName(), OmniexxEmbedBuilder.blue())
                .addField("Energie", energy.display() +
                    "  `" + energy.getCurrent() + "/" + energy.getMaxEnergy() + "`", false)
                .addField("Nächste Energie",
                    energy.isFull() ? "✅ Voll!" : energyService.nextRegenText(energy), true)
                .addField("Regen-Rate",
                    "1 Punkt alle " + EnergyService.regenMinutes() + " Min", true)
                .addField("Sessions heute", String.valueOf(todaySessions), true)
                .addField("Work-Streak", energy.getWorkStreak() + " Tage 🔥", true)
                .addField("Total Sessions", String.valueOf(energy.getTotalSessions()), true)
                .addField("Jackpots gesamt", String.valueOf(jackpots) + " 💎", true)
                .setFooter("Nutze /work um Energie zu verbrauchen");

        event.replyEmbeds(eb.build()).queue();
    }
}
