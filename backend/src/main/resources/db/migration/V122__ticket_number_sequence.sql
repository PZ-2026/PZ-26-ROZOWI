-- Tabela sekwencji numerów zgłoszeń, bezpieczna w środowiskach wieloinstancyjnych.
-- Zastępuje in-memory AtomicInteger w TicketNumberGenerator.
CREATE TABLE IF NOT EXISTS ticket_number_sequences (
    year     INTEGER PRIMARY KEY,
    next_val INTEGER NOT NULL DEFAULT 1
);

-- Inicjalizuje bieżący rok wartością opartą na istniejących zgłoszeniach.
INSERT INTO ticket_number_sequences (year, next_val)
SELECT
    EXTRACT(YEAR FROM NOW())::INTEGER,
    COALESCE(
        MAX(CAST(SPLIT_PART(ticket_number, '/', 3) AS INTEGER)),
        0
    ) + 1
FROM tickets
WHERE ticket_number LIKE CONCAT('ZGL/', EXTRACT(YEAR FROM NOW())::TEXT, '/%')
ON CONFLICT (year) DO NOTHING;
