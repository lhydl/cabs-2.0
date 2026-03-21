CREATE TABLE jhi_user
(
    id                 BIGINT      NOT NULL AUTO_INCREMENT,
    login              VARCHAR(50) NOT NULL,
    password_hash      CHAR(60)    NOT NULL,
    first_name         VARCHAR(50),
    last_name          VARCHAR(50),
    email              VARCHAR(254) UNIQUE,
    activated          BIT         NOT NULL DEFAULT 0,
    lang_key           VARCHAR(10),
    image_url          VARCHAR(256),
    activation_key     VARCHAR(20),
    reset_key          VARCHAR(20),
    reset_date         DATETIME,
    phone_number       VARCHAR(255),
    dob                DATE,
    gender             VARCHAR(20),

    -- Audit fields from AbstractAuditingEntity
    created_by         VARCHAR(50) NOT NULL,
    created_date       DATETIME    NOT NULL,
    last_modified_by   VARCHAR(50),
    last_modified_date DATETIME,

    PRIMARY KEY (id),
    UNIQUE KEY uq_user_login (login)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;