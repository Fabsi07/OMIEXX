package dev.omniexx.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "player_achievements",
       uniqueConstraints = @UniqueConstraint(columnNames = {"player_id", "achievement_key"}))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "achievement_key", nullable = false, length = 64)
    private String achievementKey;

    @CreationTimestamp
    @Column(name = "unlocked_at", nullable = false, updatable = false)
    private OffsetDateTime unlockedAt;
}
