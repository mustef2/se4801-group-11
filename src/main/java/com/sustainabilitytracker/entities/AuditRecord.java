package com.sustainabilitytracker.entities;

import com.sustainabilitytracker.enums.AuditAction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "audit_records")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuditRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private EsgReport report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auditor_id")
    private User auditor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(name = "action")
    @Enumerated(EnumType.STRING)
    private AuditAction action;

    @Lob
    @Column(name = "comments")
    private String comments;

    @Lob
    @Column(name = "flagged_items")
    private String flaggedItems;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;


}