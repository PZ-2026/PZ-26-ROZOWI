-- ==========================================
-- TABELA PRZEGLĄDÓW I INSPEKCJI
-- ==========================================
-- scope_type – poziom zasięgu przeglądu: NIERUCHOMOSC, BUDYNEK lub KLATKA
-- scope_id   – UUID encji odpowiadającej wybranemu scope_type

CREATE TABLE inspections (
    id           UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    title        VARCHAR(255) NOT NULL,
    description  TEXT,
    scheduled_at TIMESTAMP    NOT NULL,
    scope_type   VARCHAR(20)  NOT NULL
                     CHECK (scope_type IN ('NIERUCHOMOSC', 'BUDYNEK', 'KLATKA')),
    scope_id     UUID         NOT NULL,
    created_by   UUID         NOT NULL REFERENCES users(id),
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
