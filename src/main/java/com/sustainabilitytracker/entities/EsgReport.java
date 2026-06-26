package com.sustainabilitytracker.entities;

import com.sustainabilitytracker.enums.AuditStatus;
import com.sustainabilitytracker.enums.ReportType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "esg_reports")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EsgReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "score_id")
    private SustainabilityScore score;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by")
    private User generatedBy;

    @Column(name = "report_title")
    private String reportTitle;

    @Column(name = "report_type")
    @Enumerated(EnumType.STRING)
    private ReportType reportType;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_format")
    private String fileFormat;

    @ColumnDefault("'PENDING'")
    @Column(name = "audit_status")
    @Enumerated(EnumType.STRING)
    private AuditStatus auditStatus;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant generatedAt;


}