CREATE TABLE sustainability_scores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    environment_score DECIMAL(5,2),
    social_score DECIMAL(5,2),
    governance_score DECIMAL(5,2),
    total_score DECIMAL(5,2),
    grade VARCHAR(2)
        CHECK (
            grade IN (
                      'A',
                      'B',
                      'C',
                      'D',
                      'F'
                )
            ),
    period_type VARCHAR(20),
    period_start DATE,
    period_end DATE,
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id)
        REFERENCES companies(id)
);