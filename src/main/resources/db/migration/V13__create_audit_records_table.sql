CREATE TABLE audit_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    report_id BIGINT,

    auditor_id BIGINT,

    company_id BIGINT,

    action VARCHAR(20)
        CHECK (
        action IN (
        'VERIFIED',
        'FLAGGED',
        'REJECTED',
        'REQUESTED_INFO'
        )
),

    comments TEXT,

    flagged_items TEXT,

    audit_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (report_id)
        REFERENCES esg_reports(id),

    FOREIGN KEY (auditor_id)
        REFERENCES users(id),

    FOREIGN KEY (company_id)
        REFERENCES companies(id)
);