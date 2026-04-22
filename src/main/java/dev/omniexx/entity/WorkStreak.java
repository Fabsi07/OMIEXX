package dev.omniexx.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "work_streaks")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkStreak {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false, unique = true)
    private Company company;

    @Column(name = "current_streak", nullable = false)
    @Builder.Default private Integer currentStreak = 0;

    @Column(name = "longest_streak", nullable = false)
    @Builder.Default private Integer longestStreak = 0;

    @Column(name = "last_work_date")
    private LocalDate lastWorkDate;

    @Column(name = "total_sessions", nullable = false)
    @Builder.Default private Integer totalSessions = 0;
}
