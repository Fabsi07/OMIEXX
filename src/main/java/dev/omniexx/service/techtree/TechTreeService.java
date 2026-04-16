package dev.omniexx.service.techtree;

import dev.omniexx.entity.Company;
import dev.omniexx.entity.CompanyTechNode;
import dev.omniexx.entity.Employee;
import dev.omniexx.repository.CompanyRepository;
import dev.omniexx.repository.EmployeeRepository;
import dev.omniexx.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TechTreeService {

    private final CompanyRepository  companyRepo;
    private final EmployeeRepository employeeRepo;
    private final EventService       eventService;

    /** Gibt alle freigeschalteten Node-Keys einer Firma zurück */
    public Set<String> getUnlockedKeys(Company company) {
        return company.getTechNodes().stream()
                .map(CompanyTechNode::getNodeKey)
                .collect(Collectors.toSet());
    }

    /**
     * Schaltet einen Node frei und wendet den Effekt an.
     * @throws IllegalStateException wenn Bedingungen nicht erfüllt
     */
    @Transactional
    public void unlock(Company company, String nodeKey) {
        TechNode node = TechNode.byKey(nodeKey);
        if (node == null) throw new IllegalArgumentException("Unbekannter Node: " + nodeKey);

        Set<String> unlocked = getUnlockedKeys(company);

        // Bereits freigeschaltet?
        if (unlocked.contains(nodeKey)) {
            throw new IllegalStateException("**" + node.getName() + "** ist bereits freigeschaltet!");
        }

        // Voraussetzungen check
        for (String req : node.getRequires()) {
            if (!unlocked.contains(req)) {
                TechNode reqNode = TechNode.byKey(req);
                throw new IllegalStateException(
                        "Voraussetzung fehlt: **" + (reqNode != null ? reqNode.getName() : req) + "**");
            }
        }

        // Kapital check
        if (company.getCapital() < node.getCost()) {
            throw new IllegalStateException(
                    "Zu wenig Kapital! Benötigt: **$" + (node.getCost() / 100) +
                    "**, vorhanden: **$" + (company.getCapital() / 100) + "**");
        }

        // Research Points check
        if (company.getResearchPoints() < node.getRpCost()) {
            throw new IllegalStateException(
                    "Zu wenig Research Points! Benötigt: **" + node.getRpCost() +
                    " RP**, vorhanden: **" + company.getResearchPoints() + " RP**");
        }

        // Kosten abziehen
        company.setCapital(company.getCapital() - node.getCost());
        company.setResearchPoints(company.getResearchPoints() - node.getRpCost());

        // Node speichern
        CompanyTechNode techNode = CompanyTechNode.builder()
                .company(company)
                .nodeKey(nodeKey)
                .pillar(node.getPillar().name().toLowerCase())
                .costPaid(node.getCost())
                .rpPaid(node.getRpCost())
                .build();
        company.getTechNodes().add(techNode);

        // Effekt anwenden
        applyEffect(company, node);
        companyRepo.save(company);

        eventService.log(company, "tech_node_unlocked",
                "🔬 Tech-Node freigeschaltet: " + node.getName(),
                node.getDescription(),
                Map.of("capital", -node.getCost()));
    }

    private void applyEffect(Company company, TechNode node) {
        switch (node.getEffect()) {
            case BURN_RATE_REDUCTION ->
                company.setBurnRate(Math.max(0, company.getBurnRate() + node.getEffectValue())); // negativ

            case REVENUE_BONUS ->
                company.setRevenuePerTick(company.getRevenuePerTick() + node.getEffectValue());

            case RP_PER_TICK ->
                company.setRpPerTick((short)(company.getRpPerTick() + node.getEffectValue()));

            case REPUTATION ->
                company.setReputation((short) Math.min(100, company.getReputation() + node.getEffectValue()));

            case HIRE_SKILL_BONUS ->
                // Wird beim /hire berücksichtigt (Service-Check)
                {} // marker — effect stored in node

            case BOOST_ALL_SKILLS -> {
                List<Employee> emps = company.getActiveEmployees();
                for (Employee e : emps) {
                    e.setSkill((short) Math.min(10, e.getSkill() + node.getEffectValue()));
                }
                employeeRepo.saveAll(emps);
            }

            case LOYALTY_BOOST -> {
                List<Employee> emps = company.getActiveEmployees();
                for (Employee e : emps) {
                    e.setLoyalty((short) Math.min(10, e.getLoyalty() + node.getEffectValue()));
                }
                employeeRepo.saveAll(emps);
            }

            case MORALE_CAP ->
                // Morale-Cap wird im TickService berücksichtigt
                company.setMorale((short) Math.min(100, company.getMorale() + 5)); // kleiner Sofort-Boost

            case UNLOCK_ROLE -> {} // HR-Rolle — wird in HireCommand geprüft

            case PROJECT_SPEED -> {} // Wird in TickService/ProjectService geprüft

            case EXPAND_DISCOUNT -> {} // Wird in ExpandCommand geprüft
        }
    }
}
