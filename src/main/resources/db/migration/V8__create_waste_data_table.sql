CREATE TABLE waste_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    company_id BIGINT NOT NULL,
    department_id BIGINT,

    submitted_by BIGINT,
    approved_by BIGINT,

    total_kg DECIMAL(15,2) NOT NULL,

    recycled_kg DECIMAL(15,2) DEFAULT 0,

    hazardous_kg DECIMAL(15,2) DEFAULT 0,

    waste_type VARCHAR(20)
        CHECK (
            waste_type IN (
   'SOLID',
   'LIQUID',
   'HAZARDOUS',
   'ELECTRONIC',
   'ORGANIC'
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