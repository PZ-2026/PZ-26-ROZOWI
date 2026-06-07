-- Dodaje kolumnę wersji dla optimistic locking salda lokalu (JPA @Version).
ALTER TABLE apartments ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
