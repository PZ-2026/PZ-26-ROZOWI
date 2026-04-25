-- Moduł zgłoszeń: nowe kolumny w tabeli tickets
-- Zmiana: dodanie planned_visit_at, internal_note, updated_at
-- Zmiana: skrócenie title do 100 znaków, zmiana statusu na VARCHAR z wartościami enum

ALTER TABLE tickets
    ALTER COLUMN title TYPE VARCHAR(100),
    ALTER COLUMN status TYPE VARCHAR(50),
    ADD COLUMN IF NOT EXISTS planned_visit_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS internal_note TEXT,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- Sekwencja do generowania numerów zgłoszeń per rok
-- Pozwala na thread-safe generowanie numerów ZGL-RRRR-NNNN
CREATE SEQUENCE IF NOT EXISTS ticket_number_seq_2024 START 1;
CREATE SEQUENCE IF NOT EXISTS ticket_number_seq_2025 START 1;
CREATE SEQUENCE IF NOT EXISTS ticket_number_seq_2026 START 1;
