package dev.omniexx.repository;

import dev.omniexx.entity.CompanyEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyEventRepository extends JpaRepository<CompanyEvent, Long> {
    List<CompanyEvent> findByCompanyIdOrderByOccurredAtDesc(Long companyId, Pageable pageable);
}
