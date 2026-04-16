package dev.omniexx.discord.command;

import dev.omniexx.entity.Company;
import dev.omniexx.entity.Employee;
import dev.omniexx.repository.CompanyRepository;
import dev.omniexx.util.OmniexxEmbedBuilder;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TeamCommand {

    private final CompanyRepository companyRepo;

    public void handle(SlashCommandInteractionEvent event) {
        Company company = companyRepo.findActiveByDiscordId(event.getUser().getId())
                .orElse(null);

        if (company == null) {
            event.reply("❌ Du hast noch keine Firma. Starte mit `/start`!")
                    .setEphemeral(true).queue();
            return;
        }

        List<Employee> employees = company.getActiveEmployees();

        EmbedBuilder eb = OmniexxEmbedBuilder.base("👥 Team — " + company.getName(), OmniexxEmbedBuilder.blue());

        if (employees.isEmpty()) {
            eb.setDescription("Du hast noch keine Mitarbeiter.\nNutze `/hire [rolle]` um jemanden einzustellen.");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("```\n");
            sb.append(String.format("%-20s %-14s  Skill  Loyalty  Gehalt/Tick%n", "Name", "Rolle"));
            sb.append("─".repeat(65)).append("\n");

            long totalSalary = 0;
            for (Employee e : employees) {
                sb.append(String.format("%-20s %-14s    %2d      %2d     %s%n",
                        e.getFullName(),
                        roleEmoji(e.getRole()) + " " + capitalize(e.getRole()),
                        e.getSkill(),
                        e.getLoyalty(),
                        OmniexxEmbedBuilder.formatMoney(e.getSalaryPerTick())
                ));
                totalSalary += e.getSalaryPerTick();
            }
            sb.append("─".repeat(65)).append("\n");
            sb.append(String.format("%-38s Gesamt: %s/Tick%n", "", OmniexxEmbedBuilder.formatMoney(totalSalary)));
            sb.append("```");

            eb.setDescription(sb.toString());
        }

        eb.setFooter(employees.size() + " Mitarbeiter  •  /hire um jemanden einzustellen");
        event.replyEmbeds(eb.build()).queue();
    }

    private String roleEmoji(String role) {
        return switch (role.toLowerCase()) {
            case "cto"              -> "⚙️";
            case "cfo"              -> "💰";
            case "dev"              -> "💻";
            case "designer"         -> "🎨";
            case "marketing"        -> "📣";
            case "sales"            -> "🤝";
            case "hr"               -> "👔";
            case "compliance_officer" -> "📋";
            case "pen_tester"       -> "🔓";
            case "malware_analyst"  -> "🦠";
            case "soc_analyst"      -> "🛡️";
            default                 -> "👤";
        };
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).replace("_", " ");
    }
}
