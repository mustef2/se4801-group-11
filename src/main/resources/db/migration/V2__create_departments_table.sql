CREATE TABLE departments (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id   BIGINT NOT NULL,
    name         VARCHAR(100) NOT NULL,
    description  TEXT,
    is_active    BOOLEAN DEFAULT TRUE NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT departments_companies_id_fk
        FOREIGN KEY (company_id)
            REFERENCES companies(id)
            ON DELETE CASCADE
);