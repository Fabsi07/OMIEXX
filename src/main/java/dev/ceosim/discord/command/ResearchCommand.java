package dev.ceosim.discord.command;

import dev.ceosim.entity.Company;
import dev.ceosim.repository.CompanyRepository;
import dev.ceosim.service.CooldownService;
import dev.ceosim.service.techtree.TechNode;
import dev.ceosim.service.techtree.TechTreeService;
import dev.ceosim.util.CeoEmbedBuilder;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ResearchCommand {

    private final CompanyRepository companyRepo;
    private final TechTreeService   techTreeService;
    private final CooldownService   cooldownService;

    private static final Duration COOLDOWN = Duration.ofHours(24);

    public void handle(SlashCommandInteractionEvent event) {
        String aktion = event.getOption("aktion").getAsString().toLowerCase().trim();
        switch (aktion) {
            case "tree"   -> handleTree(event);
            case "pick"   -> handlePick(event);
            case "status" -> handleStatus(event);
            default -> event.reply("❌ Unbekannte Aktion. Nutze: `tree`, `pick`, `status`")
                    .setEphemeral(true).queue();
        }
    }

    // ── /research tree ──────────────────────────────────────────────────
    private void handleTree(SlashCommandInteractionEvent event) {
        Company company = getCompany(event);
        if (company == null) return;

        Set<String> unlocked = techTreeService.getUnlockedKeys(company);

        EmbedBuilder eb = CeoEmbedBuilder.base("🔬 Tech-Tree", CeoEmbedBuilder.blue())
                .setDescription("**✅** = freigeschaltet  |  **▶** = verfügbar  |  **🔒** = gesperrt\n" +
                        "Freischalten: `/research pick [key]`\n\u200b");

        for (TechNode.Pillar pillar : TechNode.Pillar.values()) {
            List<TechNode> nodes = TechNode.byPillar(pillar);
            StringBuilder sb = new StringBuilder();

            for (TechNode node : nodes) {
                boolean isUnlocked = unlocked.contains(node.getKey());
                boolean canUnlock  = !isUnlocked && node.getRequires().stream().allMatch(unlocked::contains);

                String icon = isUnlocked ? "✅" : canUnlock ? "▶️" : "🔒";
                String nameStr = isUnlocked ? "~~" + node.getName() + "~~" : "**" + node.getName() + "**";

                sb.append(icon).append(" ").append(nameStr).append("\n");
                sb.append(String.format("   `%s`  💰 %s  🔬 %d RP%n",
                        node.getKey(),
                        CeoEmbedBuilder.formatMoney(node.getCost()),
                        node.getRpCost()));
                if (!isUnlocked) {
                    sb.append("   *").append(node.getDescription()).append("*\n");
                }
                if (!node.getRequires().isEmpty() && !isUnlocked) {
                    sb.append("   🔗 Benötigt: ")
                      .append(String.join(", ", node.getRequires())).append("\n");
                }
                sb.append("\n");
            }
            eb.addField(pillar.getLabel(), sb.toString(), false);
        }

        eb.setFooter("RP: " + company.getResearchPoints() + " verfügbar  •  " +
                company.getRpPerTick() + " RP/Tick  •  1 Node pro 24h");
        event.replyEmbeds(eb.build()).queue();
    }

    // ── /research pick ──────────────────────────────────────────────────
    private void handlePick(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();

        Duration cd = cooldownService.getRemaining(discordId, "research_pick");
        if (cd != null) {
            event.reply("⏳ `/research pick` ist noch für **" + CooldownService.format(cd) + "** auf Cooldown.")
                    .setEphemeral(true).queue();
            return;
        }

        var nodeOpt = event.getOption("node");
        if (nodeOpt == null) {
            event.reply("❌ Bitte gib einen Node-Key an: `/research pick [key]`\nNutze `/research tree` für Übersicht.")
                    .setEphemeral(true).queue();
            return;
        }

        Company company = getCompany(event);
        if (company == null) return;

        String key = nodeOpt.getAsString().trim().toLowerCase();

        try {
            techTreeService.unlock(company, key);
            cooldownService.set(discordId, "research_pick", COOLDOWN);

            TechNode node = TechNode.byKey(key);
            EmbedBuilder eb = CeoEmbedBuilder.base("✅ Node freigeschaltet: " + node.getName(), CeoEmbedBuilder.green())
                    .addField("Säule",          node.getPillar().getLabel(), true)
                    .addField("Tier",           String.valueOf(node.getTier()), true)
                    .addField("Kosten bezahlt", CeoEmbedBuilder.formatMoney(node.getCost()) +
                              " + " + node.getRpCost() + " RP", true)
                    .addField("Effekt",         node.getDescription(), false)
                    .addField("Research Points", company.getResearchPoints() + " RP übrig", true)
                    .setFooter("Nächster Node in 24h möglich");

            event.replyEmbeds(eb.build()).queue();

        } catch (IllegalArgumentException | IllegalStateException e) {
            event.reply("❌ " + e.getMessage()).setEphemeral(true).queue();
        }
    }

    // ── /research status ────────────────────────────────────────────────
    private void handleStatus(SlashCommandInteractionEvent event) {
        Company company = getCompany(event);
        if (company == null) return;

        Set<String> unlocked = techTreeService.getUnlockedKeys(company);
        int total = TechNode.values().length;

        // Nächste verfügbare Nodes
        StringBuilder next = new StringBuilder();
        for (TechNode node : TechNode.values()) {
            if (!unlocked.contains(node.getKey()) &&
                    node.getRequires().stream().allMatch(unlocked::contains)) {
                next.append(String.format("▶ **%s** — %s + %d RP\n",
                        node.getName(),
                        CeoEmbedBuilder.formatMoney(node.getCost()),
                        node.getRpCost()));
            }
        }
        if (next.isEmpty()) next.append("*Alle verfügbaren Nodes freigeschaltet!*");

        EmbedBuilder eb = CeoEmbedBuilder.base("🔬 Research Status", CeoEmbedBuilder.blue())
                .addField("Research Points", company.getResearchPoints() + " RP", true)
                .addField("RP/Tick",         "+" + company.getRpPerTick(), true)
                .addField("Nodes",           unlocked.size() + " / " + total + " freigeschaltet", true)
                .addField("Jetzt verfügbar", next.toString(), false)
                .setFooter("Freischalten: /research pick [key]");

        event.replyEmbeds(eb.build()).queue();
    }

    private Company getCompany(SlashCommandInteractionEvent event) {
        Company c = companyRepo.findActiveByDiscordId(event.getUser().getId()).orElse(null);
        if (c == null) event.reply("❌ Keine aktive Firma. Starte mit `/start`!").setEphemeral(true).queue();
        return c;
    }
}
