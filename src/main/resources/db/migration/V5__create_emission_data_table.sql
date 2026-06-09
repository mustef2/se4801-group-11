CREATE TABLE emission_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    company_id BIGINT NOT NULL,
    department_id BIGINT,

    submitted_by BIGINT,
    approved_by BIGINT,

    co2_amount DECIMAL(15,2),
    ch4_amount DECIMAL(15,2),
    n2o_amount DECIMAL(15,2),

    scope VARCHAR(10)
        CHECK (
            scope IN (
                      'SCOPE1',
                      'SCOPE2',
                      'SCOPE3'
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