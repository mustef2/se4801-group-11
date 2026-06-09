CREATE TABLE governance_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    company_id BIGINT NOT NULL,

    submitted_by BIGINT,

    approved_by BIGINT,

    compliance_score DECIMAL(5,2),

    policy_count INTEGER DEFAULT 0,

    violations_count INTEGER DEFAULT 0,

    board_diversity_pct DECIMAL(5,2),

    ethics_training_done BOOLEAN DEFAULT FALSE,

    status VARCHAR(20)
        DEFAULT 'DRAFT'
        CHECK (
            status IN (
                       'DRAFT',
                       'PENDING',
                       'APPROVED',
                       'REJECTED',
                       'FLAGGED'
                )
            ),

    notes TEXT,

    rejection_reason TEXT,

    recorded_at DATE NOT NULL,

    submitted_at TIMESTAMP NULL,

    approved_at TIMESTAMP NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (company_id)
        REFERENCES companies(id),

    FOREIGN KEY (submitted_by)
        REFERENCES users(id),

    FOREIGN KEY (approved_by)
        REFERENCES users(id)
);