-- ==========================================
-- LICZNIKI (METERS)
-- ==========================================
-- Wyodrębnienie licznika jako osobnej encji zamiast pola tekstowego na MeterReading.
-- Każdy licznik posiada numer seryjny, typ medium oraz datę montażu
-- i jest powiązany z konkretnym lokalem.

CREATE TABLE meters (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    apartment_id      UUID         NOT NULL REFERENCES apartments(id) ON DELETE CASCADE,
    serial_number     VARCHAR(100) NOT NULL,
    medium_type       VARCHAR(20)  NOT NULL
        CHECK (medium_type IN ('ZIMNA_WODA', 'CIEPLA_WODA', 'GAZ', 'CIEPLO')),
    installation_date DATE         NOT NULL,
    is_active         BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_meter_serial_number UNIQUE (serial_number)
);

CREATE INDEX idx_meters_apartment_id ON meters (apartment_id);

-- ==========================================
-- AKTUALIZACJA METER_READINGS
-- ==========================================
-- Zastąpienie pola tekstowego meter_type referencją do encji meters.
-- Istniejące odczyty (np. z mock data) są usuwane, ponieważ nie mają
-- odpowiadającego rekordu w tabeli meters. Zostaną odtworzone razem
-- z licznikami w nowym modelu danych.
DELETE FROM meter_readings;

ALTER TABLE meter_readings
    DROP CONSTRAINT IF EXISTS uq_apartment_meter_date;

ALTER TABLE meter_readings
    ADD COLUMN meter_id UUID REFERENCES meters(id) ON DELETE CASCADE;

ALTER TABLE meter_readings
    DROP COLUMN IF EXISTS meter_type;

ALTER TABLE meter_readings
    ALTER COLUMN meter_id SET NOT NULL;

ALTER TABLE meter_readings
    ADD CONSTRAINT uq_meter_reading_date UNIQUE (meter_id, reading_date);

CREATE INDEX IF NOT EXISTS idx_meter_readings_meter_id ON meter_readings (meter_id);
