package dev.ceosim.repository;

import dev.ceosim.entity.WeeklySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WeeklySnapshotRepository extends JpaRepository<WeeklySnapshot, Long> {
    Optional<WeeklySnapshot> findByCompanyIdAndWeekNumberAndYear(
            Long companyId, Integer weekNumber, Short year);

    default Optional<WeeklySnapshot> findByCompanyIdAndWeekAndYear(
            Long companyId, int week, int year) {
        return findByCompanyIdAndWeekNumberAndYear(companyId, week, (short) year);
    }
}
