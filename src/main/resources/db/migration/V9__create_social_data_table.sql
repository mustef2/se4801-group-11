CREATE TABLE social_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    company_id BIGINT NOT NULL,
    department_id BIGINT,

    submitted_by BIGINT,
    approved_by BIGINT,

    total_workers INTEGER,

    female_workers INTEGER,

    safety_incidents INTEGER DEFAULT 0,

    training_hours DECIMAL(10,2) DEFAULT 0,

    satisfaction_score DECIMAL(5,2),

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

    FOREIGN KEY (department_id)
        REFERENCES departments(id),

    FOREIGN KEY (submitted_by)
        REFERENCES users(id),

    FOREIGN KEY (approved_by)
        REFERENCES users(id)
);