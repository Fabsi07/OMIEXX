package dev.omniexx.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "prestige_history")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrestigeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // hard_reset | ipo | hostile_takeover | market_dominance
    @Column(name = "prestige_type", nullable = false, length = 16)
    private String prestigeType;

    @Column(name = "valuation_at", nullable = false)
    private Long valuationAt;

    @Column(name = "tick_count", nullable = false)
    private Integer tickCount;

    @Column(name = "legacy_mult_after", nullable = false, precision = 5, scale = 4)
    private BigDecimal legacyMultAfter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carried_employee_id")
    private Employee carriedEmployee;

    @CreationTimestamp
    @Column(name = "prestiged_at", nullable = false, updatable = false)
    private OffsetDateTime prestigedAt;
}
