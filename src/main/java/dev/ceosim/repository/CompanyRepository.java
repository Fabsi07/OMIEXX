package dev.ceosim.repository;

import dev.ceosim.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    /** Aktive Company eines Spielers (nicht soft-deleted, nicht in Pause) */
    @Query("SELECT c FROM Company c WHERE c.player.discordId = :discordId AND c.deletedAt IS NULL")
    Optional<Company> findActiveByDiscordId(@Param("discordId") String discordId);

    /** Alle aktiven Companies für den Tick-Processor */
    @Query("SELECT c FROM Company c WHERE c.deletedAt IS NULL AND c.paused = false")
    List<Company> findAllActiveAndNotPaused();

    /** Alle aktiven Companies inkl. paused (für Leaderboard) */
    @Query("SELECT c FROM Company c WHERE c.deletedAt IS NULL ORDER BY c.valuation DESC")
    List<Company> findAllActiveOrderByValuation();

    /** Firmen deren Pause abgelaufen ist */
    @Query("SELECT c FROM Company c WHERE c.deletedAt IS NULL AND c.paused = true AND c.pauseUntil < CURRENT_TIMESTAMP")
    List<Company> findAllPausedAndExpired();

    boolean existsByPlayerDiscordIdAndDeletedAtIsNull(String discordId);
}
