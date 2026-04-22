package dev.omniexx.repository;

import dev.omniexx.entity.PlayerEnergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerEnergyRepository extends JpaRepository<PlayerEnergy, Long> {
    Optional<PlayerEnergy> findByCompanyId(Long companyId);
}
