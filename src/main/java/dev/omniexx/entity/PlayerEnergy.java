package dev.omniexx.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "player_energy")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerEnergy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false, unique = true)
    private Company company;

    /** Aktuelle Energie — 0 bis max */
    @Column(nullable = false)
    @Builder.Default
    private Short current = 5;

    /** Maximale Energie (kann durch Tech-Tree erhöht werden) */
    @Column(name = "max_energy", nullable = false)
    @Builder.Default
    private Short maxEnergy = 5;

    /** Letzter Zeitpunkt an dem Energie regeneriert/aktualisiert wurde */
    @Column(name = "last_regen", nullable = false)
    @Builder.Default
    private OffsetDateTime lastRegen = OffsetDateTime.now();

    /** Gesamte Work-Sessions aller Zeiten */
    @Column(name = "total_sessions", nullable = false)
    @Builder.Default
    private Integer totalSessions = 0;

    /** Tage in Folge mindestens 1x /work genutzt */
    @Column(name = "work_streak", nullable = false)
    @Builder.Default
    private Integer workStreak = 0;

    /** Datum des letzten /work — für Streak-Berechnung */
    @Column(name = "last_work_date")
    private LocalDate lastWorkDate;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /** Energie-Anzeige z.B. "⚡⚡⚡○○" */
    public String display() {
        String filled = "⚡".repeat(current);
        String empty  = "○".repeat(maxEnergy - current);
        return filled + empty;
    }

    public boolean isEmpty() { return current <= 0; }
    public boolean isFull()  { return current >= maxEnergy; }
}
