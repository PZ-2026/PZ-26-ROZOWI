ALTER TABLE meter_readings
    ADD COLUMN IF NOT EXISTS created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS recorded_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS is_deleted  BOOLEAN      DEFAULT FALSE;

ALTER TABLE meter_readings
    DROP CONSTRAINT IF EXISTS uq_apartment_meter_date;

ALTER TABLE meter_readings
    ADD CONSTRAINT uq_apartment_meter_date
        UNIQUE (apartment_id, meter_type, reading_date);
