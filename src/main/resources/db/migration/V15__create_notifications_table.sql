CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    user_id BIGINT,

    title VARCHAR(200),

    message TEXT,

    type VARCHAR(30)
        CHECK (
            type IN (
                     'DATA_SUBMITTED',
                     'DATA_APPROVED',
                     'DATA_REJECTED',
                     'DATA_FLAGGED',
                     'TARGET_SET',
                     'REPORT_READY',
                     'AUDIT_COMPLETE'
                )
            ),

    is_read BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id)
        REFERENCES users(id)
);