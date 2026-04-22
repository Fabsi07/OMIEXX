package dev.omniexx.repository;

import dev.omniexx.entity.WorkSession;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkSessionRepository extends JpaRepository<WorkSession, Long> {
    List<WorkSession> findByCompanyIdOrderByPlayedAtDesc(Long companyId, Pageable pageable);

    @Query("SELECT COUNT(w) FROM WorkSession w WHERE w.company.id = :companyId " +
           "AND w.playedAt >= CURRENT_DATE")
    long countTodayByCompanyId(Long companyId);

    @Query("SELECT COUNT(w) FROM WorkSession w WHERE w.company.id = :companyId " +
           "AND w.outcomeTier = 'jackpot'")
    long countJackpotsByCompanyId(Long companyId);
}
