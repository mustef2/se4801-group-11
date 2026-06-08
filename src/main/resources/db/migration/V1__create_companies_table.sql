CREATE TABLE companies (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL UNIQUE,
    industry      VARCHAR(50)  NOT NULL,
    country       VARCHAR(50)  NOT NULL,
    city          VARCHAR(50),
    size          VARCHAR(20) CHECK (size IN ('SMALL','MEDIUM','LARGE')),
    email         VARCHAR(100),
    phone         VARCHAR(20),
    is_active     BOOLEAN DEFAULT TRUE,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                  ON UPDATE CURRENT_TIMESTAMP
);