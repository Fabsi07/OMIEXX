package dev.omniexx.service;

import dev.omniexx.entity.*;
import dev.omniexx.repository.CompanyRepository;
import dev.omniexx.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepo;
    private final EmployeeRepository employeeRepo;
    private final EventService eventService;

    private static final Random RANDOM = new Random();

    // ─────────────────────────────────────────────────────────────
    // Firma anlegen
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public Company createCompany(Player player, String name, Market market, StarterType type) {
        Company company = Company.builder()
                .player(player)
                .name(name)
                .market(market)
                .starterType(type)
                .capital(type.startCapital())
                .rpPerTick((short) type.startRpPerTick())
                .build();

        // Startmarkt direkt eintragen
        CompanyMarket cm = CompanyMarket.builder()
                .company(company)
                .market(market)
                .build();
        company.getMarkets().add(cm);

        companyRepo.save(company);

        // Networker bekommt 2 Gratis-Mitarbeiter
        if (type == StarterType.NETWORKER) {
            spawnEmployee(company, "dev");
            spawnEmployee(company, "marketing");
        }

        eventService.log(company, "company_founded",
                "🎉 " + name + " wurde gegründet",
                "Markt: " + market.getDisplayName() + " | Typ: " + type.getDisplayName(),
                null);

        log.info("Neue Firma gegründet: {} ({})", name, market);
        return company;
    }

    // ─────────────────────────────────────────────────────────────
    // Mitarbeiter
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public Employee hireEmployee(Company company, String role) {
        Employee e = spawnEmployee(company, role);
        eventService.log(company, "employee_hired",
                "👤 " + e.getFullName() + " eingestellt",
                "Rolle: " + role + " | Skill: " + e.getSkill() + " | Loyalty: " + e.getLoyalty(),
                null);
        return e;
    }

    @Transactional
    public void fireEmployee(Company company, String name) {
        Employee emp = company.getActiveEmployees().stream()
                .filter(e -> e.getFullName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Mitarbeiter nicht gefunden: " + name));

        emp.setActive(false);
        emp.setFireReason("fired");
        employeeRepo.save(emp);

        // Morale-Penalty
        short newMorale = (short) Math.max(0, company.getMorale() - 8);
        company.setMorale(newMorale);

        // Reputation-Penalty bei zu viel Fluktuation
        long recentFired = company.getEmployees().stream()
                .filter(e -> !e.isActive() && "fired".equals(e.getFireReason()))
                .count();
        if (recentFired > 3) {
            company.setReputation((short) Math.max(0, company.getReputation() - 3));
        }

        companyRepo.save(company);
        eventService.log(company, "employee_fired",
                "🚪 " + emp.getFullName() + " wurde gefeuert",
                "Morale −8", java.util.Map.of("morale", -8L));
    }

    // ─────────────────────────────────────────────────────────────
    // Interner Spawn (kein Event)
    // ─────────────────────────────────────────────────────────────

    private Employee spawnEmployee(Company company, String role) {
        String[] name = NamePool.randomName(role);
        short skill   = (short) (RANDOM.nextInt(6) + 4);   // 4–9
        short loyalty = (short) (RANDOM.nextInt(7) + 3);   // 3–9
        long salary   = salaryForRole(role);

        Employee emp = Employee.builder()
                .company(company)
                .firstName(name[0])
                .lastName(name[1])
                .role(role)
                .skill(skill)
                .loyalty(loyalty)
                .salaryPerTick(salary)
                .build();

        company.getEmployees().add(emp);
        employeeRepo.save(emp);

        // Burn Rate erhöhen
        company.setBurnRate(company.getBurnRate() + salary);
        companyRepo.save(company);

        return emp;
    }

    private long salaryForRole(String role) {
        // Gehalt in Cents pro Tick
        return switch (role.toLowerCase()) {
            case "cto", "cfo"            -> 150_000L;  // $1.500
            case "dev"                   -> 80_000L;   // $800
            case "designer", "marketing",
                 "sales"                 -> 60_000L;   // $600
            case "hr",
                 "compliance_officer"    -> 70_000L;   // $700
            case "pen_tester",
                 "malware_analyst",
                 "soc_analyst"           -> 120_000L;  // $1.200
            default                      -> 50_000L;
        };
    }
}
