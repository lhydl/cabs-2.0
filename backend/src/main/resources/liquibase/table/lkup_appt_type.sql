CREATE TABLE lkup_appt_type
(
    id           INT NOT NULL,
    label        VARCHAR(255),
    order_number INT,
    is_active    TINYINT(1),
    PRIMARY KEY (id)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;