package dev.omniexx.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "company_markets",
       uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "market"}))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyMarket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Market market;

    @Column(nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal share = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private OffsetDateTime joinedAt;
}
