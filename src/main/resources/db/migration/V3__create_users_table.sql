CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT,
    department_id BIGINT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL
        CHECK (
            role IN (
                     'ADMIN',
                     'SUSTAINABILITY_MANAGER',
                     'DEPT_MANAGER',
                     'EMPLOYEE',
                     'AUDITOR'
                )
            ),
    is_active BOOLEAN DEFAULT TRUE,
    is_first_login BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id)
        REFERENCES companies(id),
    FOREIGN KEY (department_id)
        REFERENCES departments(id)
);