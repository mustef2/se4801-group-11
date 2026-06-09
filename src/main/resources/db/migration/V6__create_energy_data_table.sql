CREATE TABLE energy_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    department_id BIGINT,
    submitted_by BIGINT,
    approved_by BIGINT,
    total_kwh DECIMAL(15,2) NOT NULL,
    renewable_kwh DECIMAL(15,2) DEFAULT 0,
    source_type VARCHAR(20)
        CHECK (
            source_type IN (
                            'SOLAR',
                            'WIND',
                            'COAL',
                            'GAS',
                            'HYDRO',
                            'NUCLEAR',
                            'MIXED'
                )
            ),
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