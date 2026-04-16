package dev.omniexx.service;

import dev.omniexx.entity.Company;
import dev.omniexx.entity.WeeklySnapshot;
import dev.omniexx.repository.CompanyRepository;
import dev.omniexx.repository.WeeklySnapshotRepository;
import dev.omniexx.util.OmniexxEmbedBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyScheduler {

    private final CompanyRepository       companyRepo;
    private final WeeklySnapshotRepository snapshotRepo;
    private final JDA                     jda;

    @Value("${omniexx.channels.leaderboard:}")
    private String leaderboardChannelId;

    // Jeden Sonntag 20:00 Uhr
    @Scheduled(cron = "0 0 20 * * SUN")
    @Transactional
    public void weeklyRun() {
        log.info("Wöchentlicher Snapshot + Rangliste wird erstellt");

        List<Company> companies = companyRepo.findAllActiveOrderByValuation();
        int week = LocalDate.now().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int year = LocalDate.now().getYear();

        // 1. Snapshots speichern
        for (Company c : companies) {
            // Vorherige Woche holen für Wachstums-Berechnung
            var prev = snapshotRepo.findByCompanyIdAndWeekAndYear(c.getId(), week - 1, year);

            WeeklySnapshot snap = WeeklySnapshot.builder()
                    .company(c)
                    .weekNumber(week)
                    .year((short) year)
                    .capital(c.getCapital())
                    .valuation(c.calculateValuation())
                    .marketShare(c.getMarketShare())
                    .reputation(c.getReputation())
                    .employeeCount((short) c.getActiveEmployees().size())
                    .tickCount(c.getTickCount())
                    .build();

            snapshotRepo.save(snap);
        }

        // 2. Rangliste nach Wachstum berechnen und posten
        if (!leaderboardChannelId.isBlank()) {
            postWeeklyLeaderboard(companies, week, year);
        }

        // 3. Monats-Reset für Pause-Zähler (grob: 1. jedes Monats)
        if (LocalDate.now().getDayOfMonth() <= 7) {
            resetMonthlyPauseCounters();
        }
    }

    private void postWeeklyLeaderboard(List<Company> companies, int week, int year) {
        try {
            TextChannel channel = jda.getTextChannelById(leaderboardChannelId);
            if (channel == null) return;

            // Wachstum berechnen
            record RankEntry(Company company, long valNow, long valPrev, double growthPct) {}

            List<RankEntry> ranked = companies.stream()
                    .map(c -> {
                        long valNow  = c.calculateValuation();
                        long valPrev = snapshotRepo
                                .findByCompanyIdAndWeekAndYear(c.getId(), week - 1, year)
                                .map(s -> s.getValuation())
                                .orElse(valNow);
                        double growth = valPrev == 0 ? 0 :
                                ((valNow - valPrev) / (double) valPrev) * 100;
                        return new RankEntry(c, valNow, valPrev, growth);
                    })
                    .sorted(Comparator.comparingDouble(RankEntry::growthPct).reversed())
                    .toList();

            StringBuilder sb = new StringBuilder();
            sb.append("```\n");
            sb.append(String.format("%-3s %-22s %-14s  Wachstum%n", "#", "Firma", "Valuation"));
            sb.append("─".repeat(52)).append("\n");

            for (int i = 0; i < Math.min(ranked.size(), 10); i++) {
                var r = ranked.get(i);
                String rank = i == 0 ? "🥇" : i == 1 ? "🥈" : i == 2 ? "🥉" : (i + 1) + ".";
                String growth = String.format("%+.1f%%", r.growthPct());
                sb.append(String.format("%-3s %-22s %-14s  %s%n",
                        rank,
                        truncate(r.company().getName(), 22),
                        OmniexxEmbedBuilder.formatMoney(r.valNow()),
                        growth));
            }
            sb.append("```");

            // Top 3 bekommen Buff — wird in TickService für nächste Woche berücksichtigt
            // (Vereinfacht: Event-Log Eintrag)
            var top3 = ranked.subList(0, Math.min(3, ranked.size()));
            top3.forEach(r -> {
                r.company().setRevenuePerTick(
                        (long)(r.company().getRevenuePerTick() * 1.05)); // +5% Revenue für Top 3
                companyRepo.save(r.company());
            });

            channel.sendMessage(
                    "## 📊 Wöchentliche Rangliste — KW " + week + "\n" + sb +
                    "\n🏆 **Top 3** erhalten +5% Revenue für die nächste Woche!"
            ).queue();

            log.info("Wöchentliche Rangliste für KW {} gepostet", week);

        } catch (Exception e) {
            log.error("Fehler beim Posten der Rangliste: {}", e.getMessage());
        }
    }

    @Transactional
    protected void resetMonthlyPauseCounters() {
        List<Company> companies = companyRepo.findAllActiveOrderByValuation();
        companies.forEach(c -> c.setPausesUsed((short) 0));
        companyRepo.saveAll(companies);
        log.info("Monatliche Pause-Zähler zurückgesetzt");
    }

    // Pause-Status prüfen (wird in TickService aufgerufen)
    @Transactional
    public void unpauseExpired() {
        List<Company> paused = companyRepo.findAllPausedAndExpired();
        paused.forEach(c -> {
            c.setPaused(false);
            c.setPauseUntil(null);
        });
        if (!paused.isEmpty()) {
            companyRepo.saveAll(paused);
            log.info("{} Firmen aus der Pause entlassen", paused.size());
        }
    }

    private String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}
