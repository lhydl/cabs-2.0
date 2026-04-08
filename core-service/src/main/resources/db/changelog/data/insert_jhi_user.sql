INSERT INTO jhi_user (id, login, password_hash, first_name, last_name, email, activated, lang_key, image_url,
                      activation_key, reset_key, reset_date, phone_number, dob, gender, created_by, created_date,
                      last_modified_by, last_modified_date)
VALUES (1, 'admin', '$2a$10$uNbv9EUJPtTUXUXlQF2H2OzCGkFgw7g4LorHMI09ltb2gDVA.H9Qm', 'Administrator', 'Administrator',
        'admin@localhost', true, 'en', null, 'xI3zdGajVEsD9g63MSDI', null, null, null, null,
        null, 'system', sysdate(), 'system', sysdate());