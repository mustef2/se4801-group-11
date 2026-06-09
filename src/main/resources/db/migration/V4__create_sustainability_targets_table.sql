CREATE TABLE sustainability_targets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    department_id BIGINT,
    created_by BIGINT,
    category VARCHAR(20) NOT NULL
        CHECK (
            category IN (
                         'ENVIRONMENT',
                         'SOCIAL',
                         'GOVERNANCE'
                )
            ),
    metric_type VARCHAR(30) NOT NULL,
    target_value DECIMAL(15,2) NOT NULL,
    unit VARCHAR(20),
    period_type VARCHAR(20)
        CHECK (
            period_type IN (
                            'MONTHLY',
                            'QUARTERLY',
                            'YEARLY'
                )
            ),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id)
        REFERENCES companies(id),
    FOREIGN KEY (department_id)
        REFERENCES departments(id),
    FOREIGN KEY (created_by)
        REFERENCES users(id)
);