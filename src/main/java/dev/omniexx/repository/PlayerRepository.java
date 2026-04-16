package dev.omniexx.repository;

import dev.omniexx.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByDiscordId(String discordId);
    boolean existsByDiscordId(String discordId);
}
