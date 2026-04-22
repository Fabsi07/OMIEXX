package dev.omniexx.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "companies")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(nullable = false, length = 64)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Market market;

    @Enumerated(EnumType.STRING)
    @Column(name = "starter_type", nullable = false, length = 16)
    private StarterType starterType;

    // ---- KPIs (Geld immer in Cents) ----

    @Column(nullable = false)
    @Builder.Default
    private Long capital = 0L;

    @Column(name = "revenue_per_tick", nullable = false)
    @Builder.Default
    private Long revenuePerTick = 0L;

    @Column(name = "burn_rate", nullable = false)
    @Builder.Default
    private Long burnRate = 0L;

    @Column(nullable = false)
    @Builder.Default
    private Short morale = 70;

    @Column(name = "market_share", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal marketShare = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Short reputation = 50;

    @Column(nullable = false)
    @Builder.Default
    private Long valuation = 0L;

    // ---- Research ----

    @Column(name = "research_points", nullable = false)
    @Builder.Default
    private Long researchPoints = 0L;

    @Column(name = "rp_per_tick", nullable = false)
    @Builder.Default
    private Short rpPerTick = 1;

    // ---- Loan ----

    @Column(name = "loan_balance", nullable = false)
    @Builder.Default
    private Long loanBalance = 0L;

    @Column(name = "loan_interest", nullable = false, precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal loanInterest = new BigDecimal("0.0200");

    // ---- State ----

    @Column(name = "is_bankrupt", nullable = false)
    @Builder.Default
    private Boolean bankrupt = false;

    @Column(name = "bankrupt_ticks", nullable = false)
    @Builder.Default
    private Short bankruptTicks = 0;

    @Column(name = "is_paused", nullable = false)
    @Builder.Default
    private Boolean paused = false;

    @Column(name = "pause_until")
    private OffsetDateTime pauseUntil;

    @Column(name = "pauses_used", nullable = false)
    @Builder.Default
    private Short pausesUsed = 0;

    // ---- Prestige ----

    @Column(name = "prestige_level", nullable = false)
    @Builder.Default
    private Short prestigeLevel = 0;

    @Column(name = "legacy_multiplier", nullable = false, precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal legacyMultiplier = BigDecimal.ONE;

    @Column(name = "soft_prestige_used", nullable = false)
    @Builder.Default
    private Boolean softPrestigeUsed = false;

    // ---- Lifecycle ----

    @Column(name = "tick_count", nullable = false)
    @Builder.Default
    private Integer tickCount = 0;

    @Column(name = "tutorial_done", nullable = false)
    @Builder.Default
    private Boolean tutorialDone = false;

    // ---- Work-Streak ----
    @Column(name = "work_streak", nullable = false)
    @Builder.Default
    private Integer workStreak = 0;

    @Column(name = "work_streak_date")
    private java.time.LocalDate workStreakDate;

    @Column(name = "total_work_count", nullable = false)
    @Builder.Default
    private Integer totalWorkCount = 0;

    @Column(name = "last_work_at")
    private java.time.OffsetDateTime lastWorkAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;  // gesetzt bei Hard Prestige Reset

    // ---- Relations ----

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Employee> employees = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CompanyMarket> markets = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CompanyProject> projects = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CompanyTechNode> techNodes = new ArrayList<>();

    // ---- Hilfsmethoden ----

    public boolean isActive() {
        return deletedAt == null;
    }

    public long getNetIncomePerTick() {
        return revenuePerTick - burnRate;
    }

    /** Valuation berechnen: Revenue * 12 (Jahres-Multiplikator) + Kapital */
    public long calculateValuation() {
        return (revenuePerTick * 12L) + capital;
    }

    public List<Employee> getActiveEmployees() {
        return employees.stream()
                .filter(Employee::isActive)
                .toList();
    }

    public CompanyProject getActiveProject() {
        return projects.stream()
                .filter(p -> "active".equals(p.getStatus()))
                .findFirst()
                .orElse(null);
    }
}
