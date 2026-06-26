package com.sustainabilitytracker.entities;

import com.sustainabilitytracker.enums.PeriodType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "sustainability_scores")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SustainabilityScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(name = "environment_score")
    private BigDecimal environmentScore;

    @Column(name = "social_score")
    private BigDecimal socialScore;

    @Column(name = "governance_score")
    private BigDecimal governanceScore;

    @Column(name = "total_score")
    private BigDecimal totalScore;

    @Column(name = "grade")
    private String grade;

    @Column(name = "period_type")
    @Enumerated(EnumType.STRING)
    private PeriodType periodType;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "calculated_at")
    private Instant calculatedAt;


}