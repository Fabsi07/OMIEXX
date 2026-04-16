package dev.omniexx.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "npc_companies")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NpcCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Market market;

    // aggressive | conservative | innovative
    @Column(nullable = false, length = 16)
    private String personality;

    @Column(nullable = false)
    @Builder.Default
    private Long capital = 0L;

    @Column(name = "market_share", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal marketShare = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Long valuation = 0L;

    @Column(name = "is_acquired", nullable = false)
    @Builder.Default
    private Boolean acquired = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acquired_by")
    private Company acquiredBy;

    @Column(name = "acquired_at")
    private OffsetDateTime acquiredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public boolean isAvailable() {
        return !acquired;
    }
}
