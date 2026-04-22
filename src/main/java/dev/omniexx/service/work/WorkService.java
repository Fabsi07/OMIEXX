package dev.omniexx.service.work;

import dev.omniexx.entity.Company;
import dev.omniexx.entity.PlayerEnergy;
import dev.omniexx.entity.WorkSession;
import dev.omniexx.repository.CompanyRepository;
import dev.omniexx.repository.WorkSessionRepository;
import dev.omniexx.service.EventLogService;
import dev.omniexx.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkService {

    private final CompanyRepository    companyRepo;
    private final WorkSessionRepository sessionRepo;
    private final EnergyService        energyService;
    private final EventService         eventService;
    private final EventLogService      eventLogService;

    private static final Random RANDOM = new Random();

    /**
     * Führt eine Work-Session durch.
     *
     * @param company      Die spielende Firma
     * @param energy       Aktueller Energie-Stand (bereits regen-berechnet)
     * @param scenarioKey  Welches Szenario
     * @param optionIndex  Welche Option gewählt (0, 1, 2)
     * @param isFirstEver  Ob das der allererste /work ist (garantierter Positive Outcome)
     * @return WorkOutcome mit allen Belohnungen
     */
    @Transactional
    public WorkOutcome executeWork(
            Company company,
            PlayerEnergy energy,
            String scenarioKey,
            int optionIndex,
            boolean isFirstEver
    ) {
        WorkScenario scenario = WorkScenario.ALL.stream()
                .filter(s -> s.getKey().equals(scenarioKey))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown scenario: " + scenarioKey));

        if (optionIndex < 0 || optionIndex >= scenario.getOptions().size()) {
            optionIndex = 0;
        }
        WorkScenario.WorkOption option = scenario.getOptions().get(optionIndex);

        // Energie verbrauchen
        int energyCost = isCrunchScenario(scenarioKey) ? 2 : 1;
        if (!energyService.consume(energy, energyCost)) {
            throw new IllegalStateException("Keine Energie!");
        }

        // Outcome berechnen
        WorkOutcome outcome = WorkOutcome.calculate(scenario, option, null, RANDOM, isFirstEver);

        // Belohnungen anwenden
        applyOutcome(company, outcome);

        // Bonus-Energie bei GREAT/JACKPOT
        if (outcome.isBonusEnergy()) {
            energyService.addBonus(energy);
        }

        // Streak und Session tracken
        energyService.recordWorkSession(energy);

        // Session loggen
        sessionRepo.save(WorkSession.builder()
                .company(company)
                .scenarioKey(scenarioKey)
                .chosenOption((short) optionIndex)
                .outcomeTier(outcome.getTier().name().toLowerCase())
                .capitalGained(outcome.getCapitalGained())
                .rpGained((short) outcome.getRpGained())
                .moraleDelta((short) outcome.getMoraleDelta())
                .build());

        // Event-Log + Jackpot-Announcement
        String tierLabel = outcome.getTier().name().toLowerCase();
        eventService.log(company, "work_session",
                outcome.getEmoji() + " Work: " + outcome.tierLabel(),
                scenario.getTitle() + " → " + option.getFlavor(),
                Map.of("capital", outcome.getCapitalGained(),
                       "rp", (long) outcome.getRpGained()));

        if (outcome.getTier() == WorkOutcome.Tier.JACKPOT) {
            eventLogService.postWorkJackpot(company,
                    scenario.getTitle(), outcome.getCapitalGained());
        }

        log.debug("Work: {} | Tier: {} | Cap: {}", company.getName(),
                  outcome.getTier(), outcome.getCapitalGained());

        return outcome;
    }

    @Transactional
    protected void applyOutcome(Company company, WorkOutcome outcome) {
        // Kapital
        company.setCapital(Math.max(0, company.getCapital() + outcome.getCapitalGained()));

        // Research Points
        company.setResearchPoints(company.getResearchPoints() + outcome.getRpGained());

        // Morale (0–100)
        int newMorale = Math.max(0, Math.min(100,
                company.getMorale() + outcome.getMoraleDelta()));
        company.setMorale((short) newMorale);

        // Passive Revenue-Boost: Work-Sessions akkumulieren kleinen Dauerbuff
        // Jede GREAT+ Session gibt +$50/Tick (max +$2k/Tick aus Work)
        if (outcome.getTier() == WorkOutcome.Tier.GREAT ||
            outcome.getTier() == WorkOutcome.Tier.JACKPOT) {
            long maxWorkBonus = 200_000L; // $2.000
            long currentWorkBonus = company.getRevenuePerTick();
            if (currentWorkBonus < maxWorkBonus) {
                company.setRevenuePerTick(company.getRevenuePerTick() + 5_000L); // +$50
            }
        }

        companyRepo.save(company);
    }

    /** Letzten Session-Key für Anti-Repeat-Mechanik */
    public String getLastScenarioKey(Long companyId) {
        return sessionRepo.findByCompanyIdOrderByPlayedAtDesc(
                companyId, org.springframework.data.domain.PageRequest.of(0, 1))
                .stream().findFirst()
                .map(WorkSession::getScenarioKey)
                .orElse(null);
    }

    public boolean isFirstEverWork(Long companyId) {
        return sessionRepo.countTodayByCompanyId(companyId) == 0
               && sessionRepo.findByCompanyIdOrderByPlayedAtDesc(companyId,
                   org.springframework.data.domain.PageRequest.of(0, 1)).isEmpty();
    }

    private boolean isCrunchScenario(String key) {
        return key.startsWith("crunch_");
    }
}
