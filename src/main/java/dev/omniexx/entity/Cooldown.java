package dev.omniexx.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "cooldowns",
       uniqueConstraints = @UniqueConstraint(columnNames = {"discord_id", "command"}))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cooldown {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "discord_id", nullable = false, length = 32)
    private String discordId;

    @Column(nullable = false, length = 32)
    private String command;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;
}
