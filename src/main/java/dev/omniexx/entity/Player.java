package dev.omniexx.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "players")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "discord_id", nullable = false, unique = true, length = 32)
    private String discordId;

    @Column(name = "discord_name", nullable = false, length = 64)
    private String discordName;

    @OneToMany(mappedBy = "player", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Company> companies = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // Hilfsmethode: aktive Company des Spielers
    public Company getActiveCompany() {
        return companies.stream()
                .filter(c -> c.getDeletedAt() == null)
                .findFirst()
                .orElse(null);
    }
}
