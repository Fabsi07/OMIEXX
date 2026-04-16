package dev.ceosim.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "company_projects")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "project_key", nullable = false, length = 64)
    private String projectKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Market market;

    // active | completed_full | completed_partial | failed | critical_fail | cancelled
    @Column(nullable = false, length = 16)
    @Builder.Default
    private String status = "active";

    @Column(name = "ticks_total", nullable = false)
    private Short ticksTotal;

    @Column(name = "ticks_remaining", nullable = false)
    private Short ticksRemaining;

    @Column(name = "boosts_used", nullable = false)
    @Builder.Default
    private Short boostsUsed = 0;

    @Column(name = "cost_paid", nullable = false)
    @Builder.Default
    private Long costPaid = 0L;

    @CreationTimestamp
    @Column(name = "started_at", nullable = false, updatable = false)
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    public boolean isActive() {
        return "active".equals(status);
    }

    public boolean canBoost() {
        return boostsUsed < 2;
    }
}
