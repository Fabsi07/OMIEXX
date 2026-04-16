package dev.ceosim.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "weekly_snapshots",
       uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "week_number", "year"}))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "week_number", nullable = false)
    private Integer weekNumber;

    @Column(nullable = false)
    private Short year;

    @Column(nullable = false)
    private Long capital;

    @Column(nullable = false)
    private Long valuation;

    @Column(name = "market_share", nullable = false, precision = 5, scale = 2)
    private BigDecimal marketShare;

    @Column(nullable = false)
    private Short reputation;

    @Column(name = "employee_count", nullable = false)
    private Short employeeCount;

    @Column(name = "tick_count", nullable = false)
    private Integer tickCount;

    @CreationTimestamp
    @Column(name = "snapshot_at", nullable = false, updatable = false)
    private OffsetDateTime snapshotAt;
}
