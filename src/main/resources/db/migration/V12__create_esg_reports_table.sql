CREATE TABLE esg_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    company_id BIGINT NOT NULL,

    score_id BIGINT,

    generated_by BIGINT,

    report_title VARCHAR(200),

    report_type VARCHAR(30)
        CHECK (
            report_type IN (
                            'FULL_ESG',
                             'ENVIRONMENT',
                            'SOCIAL',
                            'GOVERNANCE',
                            'MONTHLY',
                             'ANNUAL'
                )
            ),
    file_path VARCHAR(500),
    file_format VARCHAR(10)
        CHECK (
            file_format IN (
   'PDF',
   'EXCEL'
                )
            ),
    audit_status VARCHAR(20)
  DEFAULT 'PENDING'
        CHECK (
            audit_status IN (
                'PENDING',
                'UNDER_REVIEW',
                'VERIFIED',
                'FLAGGED',
                'REJECTED'
                )
            ),
    period_start DATE,
    period_end DATE,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id)
        REFERENCES companies(id),
    FOREIGN KEY (score_id)
        REFERENCES sustainability_scores(id),
    FOREIGN KEY (generated_by)
        REFERENCES users(id)
);