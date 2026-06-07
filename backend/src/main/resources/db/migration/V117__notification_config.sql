CREATE TABLE notification_config (
    event_type VARCHAR(50) PRIMARY KEY,
    enabled    BOOLEAN     NOT NULL DEFAULT TRUE
);

INSERT INTO notification_config (event_type, enabled)
VALUES ('OGLOSZENIE', TRUE),
       ('ZMIANA_STATUSU_ZGLOSZENIA', TRUE),
       ('PRZEGLAD', TRUE),
       ('NOWY_DOKUMENT', TRUE),
       ('WSTRZYMANIE_ZGLOSZENIA', TRUE);
