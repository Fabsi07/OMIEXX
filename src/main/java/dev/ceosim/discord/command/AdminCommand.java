package dev.ceosim.discord.command;

import dev.ceosim.entity.Company;
import dev.ceosim.entity.Market;
import dev.ceosim.repository.CompanyRepository;
import dev.ceosim.repository.PlayerRepository;
import dev.ceosim.service.NpcService;
import dev.ceosim.service.TickService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminCommand {

    private final TickService       tickService;
    private final CompanyRepository companyRepo;
    private final PlayerRepository  playerRepo;
    private final NpcService        npcService;

    // Discord IDs die Admin-Rechte haben (aus application.yml oder Env-Var)
    @Value("${ceosim.admin.discord-ids:}")
    private String adminIds;

    public void handle(SlashCommandInteractionEvent event) {
        if (!isAdmin(event.getUser().getId())) {
            event.reply("🔒 Keine Admin-Rechte.").setEphemeral(true).queue();
            return;
        }

        String aktion = event.getOption("aktion").getAsString().toLowerCase().trim();

        switch (aktion) {
            case "tick_skip" -> {
                event.deferReply().setEphemeral(true).queue();
                List<Company> companies = companyRepo.findAllActiveAndNotPaused();
                companies.forEach(tickService::processTickForCompany);
                event.getHook().editOriginal("⏭️ Tick übersprungen — " + companies.size() + " Firmen verarbeitet.").queue();
                log.warn("Admin tick_skip von {}", event.getUser().getName());
            }
            case "reset" -> {
                var targetOpt = event.getOption("spieler");
                if (targetOpt == null) { event.reply("❌ Spieler angeben!").setEphemeral(true).queue(); return; }
                String targetId = targetOpt.getAsUser().getId();
                Company c = companyRepo.findActiveByDiscordId(targetId).orElse(null);
                if (c == null) { event.reply("❌ Keine aktive Firma für diesen Spieler.").setEphemeral(true).queue(); return; }
                c.setDeletedAt(OffsetDateTime.now());
                companyRepo.save(c);
                event.reply("🗑️ Firma **" + c.getName() + "** wurde zurückgesetzt.").setEphemeral(true).queue();
                log.warn("Admin reset von {} für {}", event.getUser().getName(), targetId);
            }
            case "spawn" -> {
                var wertOpt = event.getOption("wert");
                if (wertOpt == null) { event.reply("❌ Format: `/admin spawn [firma:markt]`").setEphemeral(true).queue(); return; }
                String[] parts = wertOpt.getAsString().split(":");
                if (parts.length < 2) { event.reply("❌ Format: `firmaname:markt_key`").setEphemeral(true).queue(); return; }
                try {
                    Market market = Market.valueOf(parts[1].toUpperCase().trim());
                    npcService.spawnNpc(market, parts[0].trim());
                    event.reply("✅ NPC **" + parts[0] + "** in **" + market.getDisplayName() + "** gespawnt.").setEphemeral(true).queue();
                } catch (IllegalArgumentException e) {
                    event.reply("❌ Unbekannter Markt: " + parts[1]).setEphemeral(true).queue();
                }
            }
            case "event" -> {
                var wertOpt = event.getOption("wert");
                String eventType = wertOpt != null ? wertOpt.getAsString() : "boom";
                // TODO: Server-weite Events triggern (Phase 9 Saisonalevents)
                event.reply("⚡ Global Event **" + eventType + "** getriggert (noch nicht vollständig implementiert).")
                        .setEphemeral(true).queue();
            }
            default -> event.reply("❌ Unbekannte Aktion. Nutze: `tick_skip`, `reset`, `spawn`, `event`")
                    .setEphemeral(true).queue();
        }
    }

    private boolean isAdmin(String discordId) {
        if (adminIds == null || adminIds.isBlank()) return false;
        return java.util.Arrays.asList(adminIds.split(",")).contains(discordId.trim());
    }
}
