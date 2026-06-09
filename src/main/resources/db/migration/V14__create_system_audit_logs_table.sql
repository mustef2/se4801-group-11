CREATE TABLE system_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    user_id BIGINT,

    action VARCHAR(100) NOT NULL,

    entity_type VARCHAR(50),

    entity_id BIGINT,

    old_value TEXT,

    new_value TEXT,

    ip_address VARCHAR(45),

    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id)
        REFERENCES users(id)
);