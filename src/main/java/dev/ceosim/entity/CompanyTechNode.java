package dev.ceosim.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "company_tech_nodes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "node_key"}))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyTechNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "node_key", nullable = false, length = 64)
    private String nodeKey;

    @Column(nullable = false, length = 16)
    private String pillar;  // operations | research | people

    @Column(name = "cost_paid", nullable = false)
    private Long costPaid;

    @Column(name = "rp_paid", nullable = false)
    private Long rpPaid;

    @CreationTimestamp
    @Column(name = "unlocked_at", nullable = false, updatable = false)
    private OffsetDateTime unlockedAt;
}
