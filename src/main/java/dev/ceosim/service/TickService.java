package dev.ceosim.service;

import dev.ceosim.entity.Company;
import dev.ceosim.entity.Employee;
import dev.ceosim.repository.CompanyRepository;
import dev.ceosim.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * TickService — verarbeitet alle aktiven Firmen alle 6 Stunden.
 *
 * Pro Tick passiert für jede Firma:
 *  1. Salary aller Mitarbeiter abziehen
 *  2. Kredit-Zinsen abziehen
 *  3. Revenue gutschreiben
 *  4. Research Points generieren
 *  5. Valuation neu berechnen
 *  6. Zufälliges Event triggern
 *  7. Insolvenz-Check
 *  8. Tick-Counter +1
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TickService {

    private final CompanyRepository  companyRepo;
    private final EmployeeRepository employeeRepo;
    private final EventService       eventService;
    private final NpcService         npcService;
    private final dev.ceosim.service.achievement.AchievementService achievementService;

    private static final Random RANDOM = new Random();

    // ── Scheduler ───────────────────────────────────────────────────────
    // fixedRate = 6 Stunden in Millisekunden
    // initialDelay = 30s damit die DB-Migrations sicher durch sind beim Start

    @Scheduled(fixedRateString = "${ceosim.tick.interval-ms:21600000}",
               initialDelay = 30_000)
    public void runTick() {
        List<Company> companies = companyRepo.findAllActiveAndNotPaused();
        log.info("Tick gestartet — {} aktive Firmen", companies.size());

        // NPCs wachsen lassen
        npcService.tickAllNpcs();

        for (Company company : companies) {
            try {
                processTick(company);
            } catch (Exception e) {
                log.error("Tick-Fehler bei Firma {}: {}", company.getName(), e.getMessage(), e);
            }
        }
        log.info("Tick abgeschlossen");
    }

    // ── Manuell auslösen (für /admin tick skip) ──────────────────────────
    @Transactional
    public void processTickForCompany(Company company) {
        processTick(company);
    }

    // ── Kern-Logik ───────────────────────────────────────────────────────
    @Transactional
    protected void processTick(Company company) {
        long capital = company.getCapital();

        // 1. Salary abziehen
        long totalSalary = company.getActiveEmployees().stream()
                .mapToLong(Employee::getSalaryPerTick)
                .sum();
        capital -= totalSalary;

        // 2. Kredit-Zinsen abziehen
        if (company.getLoanBalance() > 0) {
            long interest = (long) (company.getLoanBalance() *
                    company.getLoanInterest().doubleValue());
            capital -= interest;
            eventService.log(company, "tick_processed",
                    "💳 Zinsen gezahlt",
                    CeoEmbedBuilder_format(interest) + " Zinsen auf Kredit",
                    Map.of("capital", -interest));
        }

        // 3. Revenue gutschreiben (mit Legacy-Multiplikator)
        long revenue = (long) (company.getRevenuePerTick() *
                company.getLegacyMultiplier().doubleValue());
        capital += revenue;

        // 4. Research Points
        company.setResearchPoints(
                company.getResearchPoints() + company.getRpPerTick());

        // 5. KPIs aktualisieren
        company.setCapital(capital);
        company.setValuation(company.calculateValuation());
        company.setTickCount(company.getTickCount() + 1);

        // 6. Morale langsam regenerieren (max. 85 ohne HR)
        boolean hasHr = company.getActiveEmployees().stream()
                .anyMatch(e -> "hr".equals(e.getRole()));
        int moraleMax = hasHr ? 95 : 85;
        if (company.getMorale() < moraleMax) {
            company.setMorale((short) Math.min(moraleMax, company.getMorale() + 2));
        }

        // 7. Zufälliges Event (30% Chance)
        if (RANDOM.nextInt(100) < 30) {
            triggerRandomEvent(company);
        }

        // 8. Insolvenz-Check
        if (capital < 0) {
            handleInsolvency(company);
        }

        // 9. Aktives Projekt voranschreiten
        if (company.getActiveProject() != null) {
            advanceProject(company);
        }

        companyRepo.save(company);

        // Achievement-Check
        achievementService.checkAndUnlock(company);

        // Tick-Event loggen
        eventService.log(company, "tick_processed",
                "⏱️ Tick #" + company.getTickCount() + " verarbeitet",
                String.format("Revenue +%s | Salary -%s | Kapital: %s",
                        CeoEmbedBuilder_format(revenue),
                        CeoEmbedBuilder_format(totalSalary),
                        CeoEmbedBuilder_format(capital)),
                Map.of("capital", revenue - totalSalary));
    }

    // ── Projekt voranbringen ─────────────────────────────────────────────
    private void advanceProject(Company company) {
        var project = company.getActiveProject();
        short remaining = (short) (project.getTicksRemaining() - 1);
        project.setTicksRemaining(remaining);

        if (remaining <= 0) {
            // Projekt abschließen — Risiko-Roll
            int roll = RANDOM.nextInt(100);
            String outcome;
            if (roll < 60)       outcome = "completed_full";
            else if (roll < 80)  outcome = "completed_partial";
            else if (roll < 95)  outcome = "failed";
            else                 outcome = "critical_fail";

            project.setStatus(outcome);
            project.setCompletedAt(java.time.OffsetDateTime.now());

            applyProjectOutcome(company, project.getProjectKey(), outcome);

            eventService.log(company, "project_completed",
                    projectOutcomeTitle(outcome) + " — " + project.getProjectKey(),
                    null, null);
        }
    }

    private void applyProjectOutcome(Company company, String key, String outcome) {
        switch (outcome) {
            case "completed_full" -> {
                company.setRevenuePerTick(company.getRevenuePerTick() + 50_000L);
                company.setReputation((short) Math.min(100, company.getReputation() + 5));
            }
            case "completed_partial" -> {
                company.setRevenuePerTick(company.getRevenuePerTick() + 20_000L);
            }
            case "failed" -> {
                company.setMorale((short) Math.max(0, company.getMorale() - 10));
            }
            case "critical_fail" -> {
                company.setMorale((short) Math.max(0, company.getMorale() - 20));
                company.setReputation((short) Math.max(0, company.getReputation() - 10));
                company.setCapital(company.getCapital() - 200_000L); // $2k Schaden
            }
        }
    }

    private String projectOutcomeTitle(String outcome) {
        return switch (outcome) {
            case "completed_full"    -> "✅ Projekt abgeschlossen";
            case "completed_partial" -> "⚠️ Teilerfolg";
            case "failed"            -> "❌ Projekt gescheitert";
            case "critical_fail"     -> "💥 Kritischer Fehler";
            default                  -> "❓ Unbekannt";
        };
    }

    // ── Zufalls-Events ───────────────────────────────────────────────────
    private void triggerRandomEvent(Company company) {
        int type = RANDOM.nextInt(6);
        switch (type) {
            case 0 -> {  // Guter Presseartikel
                company.setReputation((short) Math.min(100, company.getReputation() + 3));
                eventService.log(company, "random_event",
                        "📰 Positiver Presseartikel",
                        "Ein Tech-Blog schreibt über " + company.getName() + ". Reputation +3",
                        Map.of("reputation", 3L));
            }
            case 1 -> {  // Mitarbeiter kündigt (wenn Morale niedrig)
                if (company.getMorale() < 40 && !company.getActiveEmployees().isEmpty()) {
                    Employee emp = company.getActiveEmployees()
                            .get(RANDOM.nextInt(company.getActiveEmployees().size()));
                    emp.setActive(false);
                    emp.setFireReason("resigned");
                    employeeRepo.save(emp);
                    company.setBurnRate(company.getBurnRate() - emp.getSalaryPerTick());
                    eventService.log(company, "employee_resigned",
                            "😤 " + emp.getFullName() + " hat gekündigt",
                            "Niedrige Morale führt zu Fluktuation.",
                            Map.of("morale", -5L));
                }
            }
            case 2 -> {  // Marktchance
                long bonus = 30_000L + RANDOM.nextLong(70_000L); // $300–$1000
                company.setCapital(company.getCapital() + bonus);
                eventService.log(company, "random_event",
                        "💡 Marktchance genutzt",
                        "Ein unerwarteter Deal spült " + CeoEmbedBuilder_format(bonus) + " in die Kasse.",
                        Map.of("capital", bonus));
            }
            case 3 -> {  // Kleiner Rückschlag
                long loss = 20_000L + RANDOM.nextLong(50_000L);
                company.setCapital(company.getCapital() - loss);
                eventService.log(company, "random_event",
                        "⚡ Unerwartete Kosten",
                        "Serverausfall, Bugfix-Sprint oder Lieferantenproblem — " + CeoEmbedBuilder_format(loss) + " weg.",
                        Map.of("capital", -loss));
            }
            case 4 -> {  // Morale-Boost (Pizza-Friday etc.)
                company.setMorale((short) Math.min(100, company.getMorale() + 5));
                eventService.log(company, "random_event",
                        "🍕 Team-Event",
                        "Das Team hat gut zusammengearbeitet. Morale +5",
                        Map.of("morale", 5L));
            }
            // case 5: kein Event — Stille Tick
        }
    }

    // ── Insolvenz ────────────────────────────────────────────────────────
    private void handleInsolvency(Company company) {
        short bankruptTicks = (short) (company.getBankruptTicks() + 1);
        company.setBankruptTicks(bankruptTicks);
        company.setBankrupt(true);

        if (bankruptTicks >= 2) {
            // Reset
            eventService.log(company, "insolvency",
                    "💀 Insolvenz — Firma aufgelöst",
                    "2 Ticks im Notbetrieb ohne Rettung. Neustart mit Startbonus.",
                    null);
            // TODO: Reset-Flow inkl. Prestige-History
        } else {
            eventService.log(company, "insolvency",
                    "⚠️ Notbetrieb! Tick " + bankruptTicks + "/2",
                    "Kapital negativ! Nutze `/fundraise` oder `/loan` sofort!",
                    null);
        }
    }

    // Hilfsmethode: Cents → $-String (ohne Import von CeoEmbedBuilder)
    private String CeoEmbedBuilder_format(long cents) {
        long d = cents / 100;
        if (d >= 1_000_000) return String.format("$%.1fM", d / 1_000_000.0);
        if (d >= 1_000)     return String.format("$%,d", d);
        return "$" + d;
    }
}
