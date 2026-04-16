package dev.omniexx.service.project;

import dev.omniexx.entity.Company;
import dev.omniexx.entity.CompanyProject;
import dev.omniexx.repository.CompanyRepository;
import dev.omniexx.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final CompanyRepository companyRepo;
    private final EventService      eventService;

    @Transactional
    public CompanyProject startProject(Company company, String projectKey) {
        ProjectDefinition def = ProjectDefinition.byKey(projectKey);
        if (def == null) throw new IllegalArgumentException("Unbekanntes Projekt: " + projectKey);

        if (company.getActiveProject() != null) {
            throw new IllegalStateException("Es ist bereits ein Projekt aktiv: **" +
                    ProjectDefinition.byKey(company.getActiveProject().getProjectKey()).getName() + "**");
        }

        if (company.getCapital() < def.getCost()) {
            throw new IllegalStateException("Zu wenig Kapital! Benötigt: **$" +
                    (def.getCost() / 100) + "**, vorhanden: **$" +
                    (company.getCapital() / 100) + "**");
        }

        // Kapital abziehen
        company.setCapital(company.getCapital() - def.getCost());

        CompanyProject project = CompanyProject.builder()
                .company(company)
                .projectKey(projectKey)
                .market(def.getMarket())
                .status("active")
                .ticksTotal((short) def.getDurationTicks())
                .ticksRemaining((short) def.getDurationTicks())
                .costPaid(def.getCost())
                .build();

        company.getProjects().add(project);
        companyRepo.save(company);

        eventService.log(company, "project_started",
                "🚀 Projekt gestartet: " + def.getName(),
                "Dauer: " + def.getDurationTicks() + " Ticks | Kosten: $" + (def.getCost() / 100),
                Map.of("capital", -def.getCost()));

        return project;
    }

    @Transactional
    public void boostProject(Company company) {
        CompanyProject project = company.getActiveProject();
        if (project == null) throw new IllegalStateException("Kein aktives Projekt vorhanden.");
        if (!project.canBoost()) throw new IllegalStateException("Boost bereits 2× genutzt — Maximum erreicht.");

        project.setTicksRemaining((short) Math.max(1, project.getTicksRemaining() - 1));
        project.setBoostsUsed((short) (project.getBoostsUsed() + 1));

        // Morale -2
        company.setMorale((short) Math.max(0, company.getMorale() - 2));
        companyRepo.save(company);

        eventService.log(company, "project_boosted",
                "⚡ Projekt beschleunigt",
                "Ticks −1, Morale −2 | Noch " + project.getTicksRemaining() + " Ticks übrig",
                Map.of("morale", -2L));
    }

    @Transactional
    public void cancelProject(Company company) {
        CompanyProject project = company.getActiveProject();
        if (project == null) throw new IllegalStateException("Kein aktives Projekt vorhanden.");

        long refund = project.getCostPaid() / 2;
        project.setStatus("cancelled");
        project.setCompletedAt(OffsetDateTime.now());

        // 50% Rückerstattung
        company.setCapital(company.getCapital() + refund);
        companyRepo.save(company);

        ProjectDefinition def = ProjectDefinition.byKey(project.getProjectKey());
        eventService.log(company, "project_cancelled",
                "❌ Projekt abgebrochen: " + (def != null ? def.getName() : project.getProjectKey()),
                "Rückerstattung: $" + (refund / 100) + " (50%)",
                Map.of("capital", refund));
    }

    /** Prüft ob ein Projekt die Voraussetzungen erfüllt — gibt Fehlertext zurück oder null */
    public String checkRequirements(Company company, ProjectDefinition def) {
        // Markt-Check
        if (!company.getMarkets().stream().anyMatch(m -> m.getMarket() == def.getMarket())) {
            return "Du bist nicht im Markt **" + def.getMarket().getDisplayName() + "**";
        }
        // Requirement-Strings sind lesbar — Logik-Checks kommen in Phase 6 mit Tech-Tree
        // Vorerst: requirements sind nur zur Anzeige
        return null;
    }
}
