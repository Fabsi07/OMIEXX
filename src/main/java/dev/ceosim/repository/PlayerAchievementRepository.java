package dev.ceosim.repository;

import dev.ceosim.entity.PlayerAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public interface PlayerAchievementRepository extends JpaRepository<PlayerAchievement, Long> {
    List<PlayerAchievement> findByPlayerId(Long playerId);
    boolean existsByPlayerIdAndAchievementKey(Long playerId, String achievementKey);

    default Set<String> getUnlockedKeys(Long playerId) {
        return findByPlayerId(playerId).stream()
                .map(PlayerAchievement::getAchievementKey)
                .collect(Collectors.toSet());
    }
}
