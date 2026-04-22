package dev.omniexx.repository;

import dev.omniexx.entity.WorkStreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkStreakRepository extends JpaRepository<WorkStreak, Long> {
    Optional<WorkStreak> findByCompanyId(Long companyId);
}
