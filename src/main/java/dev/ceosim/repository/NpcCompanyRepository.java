package dev.ceosim.repository;

import dev.ceosim.entity.Market;
import dev.ceosim.entity.NpcCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NpcCompanyRepository extends JpaRepository<NpcCompany, Long> {
    List<NpcCompany> findByMarketAndAcquiredFalse(Market market);
    List<NpcCompany> findByAcquiredFalse();
    Optional<NpcCompany> findByNameIgnoreCaseAndAcquiredFalse(String name);
    long countByMarketAndAcquiredFalse(Market market);
    boolean existsByName(String name);
}
