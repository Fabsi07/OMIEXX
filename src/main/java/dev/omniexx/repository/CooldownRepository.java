package dev.omniexx.repository;

import dev.omniexx.entity.Cooldown;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface CooldownRepository extends JpaRepository<Cooldown, Long> {
    Optional<Cooldown> findByDiscordIdAndCommand(String discordId, String command);

    @Modifying
    @Transactional
    @Query("DELETE FROM Cooldown c WHERE c.expiresAt < :now")
    int deleteExpired(OffsetDateTime now);
}
