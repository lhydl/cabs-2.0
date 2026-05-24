CREATE TABLE jhi_user_authority
(
    user_id        BIGINT      NOT NULL,
    authority_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, authority_name),
    CONSTRAINT fk_ua_user
        FOREIGN KEY (user_id) REFERENCES jhi_user (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_ua_authority
        FOREIGN KEY (authority_name) REFERENCES jhi_authority (name)
            ON DELETE CASCADE
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;