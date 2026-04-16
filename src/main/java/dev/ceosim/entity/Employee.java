package dev.ceosim.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "employees")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "first_name", nullable = false, length = 32)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 32)
    private String lastName;

    @Column(nullable = false, length = 32)
    private String role;

    @Column(nullable = false)
    private Short skill;

    @Column(nullable = false)
    private Short loyalty;

    @Column(name = "salary_per_tick", nullable = false)
    private Long salaryPerTick;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "hired_at", nullable = false, updatable = false)
    private OffsetDateTime hiredAt;

    @Column(name = "fired_at")
    private OffsetDateTime firedAt;

    @Column(name = "fire_reason", length = 16)
    private String fireReason; // fired | resigned | generation

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
