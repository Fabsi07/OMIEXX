package dev.omniexx.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "work_sessions")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "scenario_key", nullable = false, length = 64)
    private String scenarioKey;

    @Column(name = "chosen_option", nullable = false)
    private Short chosenOption;

    /** normal | good | great | jackpot | bad | crisis */
    @Column(name = "outcome_tier", nullable = false, length = 16)
    private String outcomeTier;

    @Column(name = "capital_gained", nullable = false)
    @Builder.Default
    private Long capitalGained = 0L;

    @Column(name = "rp_gained", nullable = false)
    @Builder.Default
    private Short rpGained = 0;

    @Column(name = "morale_delta", nullable = false)
    @Builder.Default
    private Short moraleDelta = 0;

    @CreationTimestamp
    @Column(name = "played_at", nullable = false, updatable = false)
    private OffsetDateTime playedAt;
}
