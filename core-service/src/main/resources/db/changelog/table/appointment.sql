CREATE TABLE appointment
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    appt_type     VARCHAR(100) NOT NULL,
    appt_datetime DATETIME     NOT NULL,
    remarks       VARCHAR(200),
    patient_id    BIGINT          NOT NULL,
    status        INT DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_appt_user
        FOREIGN KEY (patient_id) REFERENCES jhi_user (id)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;