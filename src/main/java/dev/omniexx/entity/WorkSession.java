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

    @Column(name = "chosen_option", nullable = false, length = 4)
    private String chosenOption;

    @Column(name = "outcome_tier", nullable = false, length = 16)
    private String outcomeTier;

    @Column(name = "capital_delta", nullable = false)
    @Builder.Default private Long capitalDelta = 0L;

    @Column(name = "morale_delta", nullable = false)
    @Builder.Default private Short moraleDelta = 0;

    @Column(name = "rp_delta", nullable = false)
    @Builder.Default private Short rpDelta = 0;

    @Column(name = "rare_card_drop", nullable = false)
    @Builder.Default private Boolean rareCardDrop = false;

    @CreationTimestamp
    @Column(name = "executed_at", nullable = false, updatable = false)
    private OffsetDateTime executedAt;
}
