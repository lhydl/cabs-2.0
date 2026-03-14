CREATE TABLE patient
(
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    name         VARCHAR(200) NOT NULL,
    email        VARCHAR(255) NOT NULL,
    phone_number VARCHAR(8)   NOT NULL,
    user_id      INT,
    PRIMARY KEY (id)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;