package dev.omniexx.repository;

import dev.omniexx.entity.PrestigeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrestigeHistoryRepository extends JpaRepository<PrestigeHistory, Long> {
    List<PrestigeHistory> findByPlayerIdOrderByPrestigedAtDesc(Long playerId);
    long countByPlayerId(Long playerId);
}
