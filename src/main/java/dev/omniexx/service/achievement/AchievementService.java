package dev.omniexx.service.achievement;

import dev.omniexx.entity.Company;
import dev.omniexx.entity.Player;
import dev.omniexx.entity.PlayerAchievement;
import dev.omniexx.repository.PlayerAchievementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementService {

    private final PlayerAchievementRepository achievementRepo;

    /**
     * Prüft nach jedem Tick alle relevanten Bedingungen.
     * Gibt neu freigeschaltete Achievements zurück (für Discord-Notification).
     */
    @Transactional
    public List<AchievementType> checkAndUnlock(Company company) {
        Player player = company.getPlayer();
        Set<String> unlocked = achievementRepo.getUnlockedKeys(player.getId());
        List<AchievementType> newlyUnlocked = new ArrayList<>();

        // Team-Größe
        int teamSize = company.getActiveEmployees().size();
        tryUnlock(player, unlocked, newlyUnlocked, AchievementType.FIRST_HIRE,  teamSize >= 1);
        tryUnlock(player, unlocked, newlyUnlocked, AchievementType.TEAM_OF_5,   teamSize >= 5);
        tryUnlock(player, unlocked, newlyUnlocked, AchievementType.TEAM_OF_10,  teamSize >= 10);

        // Kapital
        tryUnlock(player, unlocked, newlyUnlocked, AchievementType.FIRST_100K,  company.getCapital() >= 10_000_000L);
        tryUnlock(player, unlocked, newlyUnlocked, AchievementType.FIRST_1M,    company.getCapital() >= 100_000_000L);

        // Valuation
        long val = company.calculateValuation();
        tryUnlock(player, unlocked, newlyUnlocked, AchievementType.VALUATION_50K,  val >= 5_000_000L);
        tryUnlock(player, unlocked, newlyUnlocked, AchievementType.VALUATION_500K, val >= 50_000_000L);
        tryUnlock(player, unlocked, newlyUnlocked, AchievementType.VALUATION_1M,   val >= 100_000_000L);
        tryUnlock(player, unlocked, newlyUnlocked, AchievementType.VALUATION_10M,  val >= 1_000_000_000L);

        // Ticks
        tryUnlock(player, unlocked, newlyUnlocked, AchievementType.VETERAN, company.getTickCount() >= 100);

        // Märkte
        int marketCount = company.getMarkets().size();
        tryUnlock(player, unlocked, newlyUnlocked, AchievementType.MARKET_EXPAND, marketCount >= 2);
        tryUnlock(player, unlocked, newlyUnlocked, AchievementType.MULTI_MARKET,  marketCount >= 3);

        // Tech-Tree
        int nodeCount = company.getTechNodes().size();
        tryUnlock(player, unlocked, newlyUnlocked, AchievementType.FIRST_NODE, nodeCount >= 1);
        tryUnlock(player, unlocked, newlyUnlocked, AchievementType.ALL_NODES,  nodeCount >= 15);

        // Prestige
        tryUnlock(player, unlocked, newlyUnlocked, AchievementType.FIRST_PRESTIGE,  company.getPrestigeLevel() >= 1);
        tryUnlock(player, unlocked, newlyUnlocked, AchievementType.TRIPLE_PRESTIGE, company.getPrestigeLevel() >= 3);

        return newlyUnlocked;
    }

    /** Manuelles Unlock für spezifische Events (Sabotage, Bankrott, etc.) */
    @Transactional
    public boolean unlock(Player player, AchievementType type) {
        if (achievementRepo.existsByPlayerIdAndAchievementKey(player.getId(), type.name())) {
            return false; // bereits freigeschaltet
        }
        achievementRepo.save(PlayerAchievement.builder()
                .player(player)
                .achievementKey(type.name())
                .build());
        log.info("Achievement freigeschaltet: {} → {}", player.getDiscordName(), type.name());
        return true;
    }

    private void tryUnlock(Player player, Set<String> unlocked,
                           List<AchievementType> newList, AchievementType type, boolean condition) {
        if (condition && !unlocked.contains(type.name())) {
            achievementRepo.save(PlayerAchievement.builder()
                    .player(player)
                    .achievementKey(type.name())
                    .build());
            unlocked.add(type.name()); // Set lokal aktualisieren
            newList.add(type);
        }
    }
}
