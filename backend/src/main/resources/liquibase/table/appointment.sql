CREATE TABLE appointment
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    appt_type     VARCHAR(100) NOT NULL,
    appt_datetime DATETIME     NOT NULL,
    remarks       VARCHAR(200),
    patient_id    INT          NOT NULL,
    doctor_id     INT DEFAULT NULL,
    status        INT DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;