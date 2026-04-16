package dev.omniexx.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_preferences",
       uniqueConstraints = @UniqueConstraint(columnNames = {"player_id", "event_type"}))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "event_type", nullable = false, length = 32)
    private String eventType;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private boolean enabled = true;
}
