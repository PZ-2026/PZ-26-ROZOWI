-- STEP 1: estates table
CREATE TABLE IF NOT EXISTS estates (
                                       id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name       VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

INSERT INTO estates (name)
SELECT DISTINCT estate_name
FROM buildings
WHERE estate_name IS NOT NULL
    ON CONFLICT (name) DO NOTHING;

ALTER TABLE buildings
    ADD COLUMN IF NOT EXISTS estate_id UUID REFERENCES estates(id) ON DELETE SET NULL;

UPDATE buildings b
SET estate_id = e.id
    FROM estates e
WHERE b.estate_name = e.name
  AND b.estate_id IS NULL;

-- STEP 2: apartments technical fields
ALTER TABLE apartments
    ADD COLUMN IF NOT EXISTS area             DECIMAL(8,2),
    ADD COLUMN IF NOT EXISTS ownership_status VARCHAR(50) DEFAULT 'WLASNOSC';

ALTER TABLE apartments
DROP CONSTRAINT IF EXISTS chk_apartments_ownership_status;

ALTER TABLE apartments
    ADD CONSTRAINT chk_apartments_ownership_status
        CHECK (ownership_status IN ('WLASNOSC', 'NAJEM', 'SPOLDZIELCZE'));

-- STEP 3: meters table
CREATE TABLE IF NOT EXISTS meters (
                                      id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    apartment_id  UUID NOT NULL REFERENCES apartments(id) ON DELETE CASCADE,
    meter_type    VARCHAR(100) NOT NULL,
    serial_number VARCHAR(100),
    installed_at  DATE,
    is_active     BOOLEAN DEFAULT TRUE,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (apartment_id, meter_type, serial_number)
    );

INSERT INTO meters (apartment_id, meter_type)
SELECT DISTINCT apartment_id, meter_type
FROM meter_readings
    ON CONFLICT DO NOTHING;

ALTER TABLE meter_readings
    ADD COLUMN IF NOT EXISTS meter_id UUID REFERENCES meters(id) ON DELETE SET NULL;

UPDATE meter_readings mr
SET meter_id = m.id
    FROM meters m
WHERE mr.apartment_id = m.apartment_id
  AND mr.meter_type   = m.meter_type
  AND mr.meter_id IS NULL;

-- STEP 4: SLA fields in ticket_categories
ALTER TABLE ticket_categories
    ADD COLUMN IF NOT EXISTS sla_response_hours   INTEGER,
    ADD COLUMN IF NOT EXISTS sla_resolution_hours INTEGER,
    ADD COLUMN IF NOT EXISTS is_active            BOOLEAN DEFAULT TRUE;

UPDATE ticket_categories
SET sla_response_hours = 4, sla_resolution_hours = 48
WHERE name = 'Hydraulika' AND sla_response_hours IS NULL;

UPDATE ticket_categories
SET sla_response_hours = 4, sla_resolution_hours = 48
WHERE name = 'Elektryka' AND sla_response_hours IS NULL;

UPDATE ticket_categories
SET sla_response_hours = 8, sla_resolution_hours = 72
WHERE name = 'Domofony i Monitoring' AND sla_response_hours IS NULL;

UPDATE ticket_categories
SET sla_response_hours = 24, sla_resolution_hours = 120
WHERE name IN ('Czystosc i Porzadek', 'Administracja', 'Remonty i Modernizacje')
  AND sla_response_hours IS NULL;

-- STEP 5: tickets manager fields
ALTER TABLE tickets
    ADD COLUMN IF NOT EXISTS planned_visit_date DATE,
    ADD COLUMN IF NOT EXISTS internal_note      TEXT;

-- STEP 6: ticket_comments table
CREATE TABLE IF NOT EXISTS ticket_comments (
                                               id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ticket_id   UUID NOT NULL REFERENCES tickets(id)  ON DELETE CASCADE,
    author_id   UUID NOT NULL REFERENCES users(id)    ON DELETE RESTRICT,
    content     TEXT NOT NULL,
    is_internal BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_ticket_comments_ticket_id
    ON ticket_comments (ticket_id);

CREATE INDEX IF NOT EXISTS idx_ticket_comments_is_internal
    ON ticket_comments (ticket_id, is_internal);

INSERT INTO ticket_comments (ticket_id, author_id, content, is_internal, created_at)
SELECT th.ticket_id, th.changed_by, th.comment, TRUE, th.created_at
FROM ticket_history th
WHERE th.comment IS NOT NULL
  AND th.comment <> ''
  AND NOT EXISTS (
    SELECT 1 FROM ticket_comments tc
    WHERE tc.ticket_id  = th.ticket_id
      AND tc.author_id  = th.changed_by
      AND tc.created_at = th.created_at
);

-- STEP 7: announcements estate targeting
ALTER TABLE announcements
    ADD COLUMN IF NOT EXISTS target_estate_id UUID REFERENCES estates(id) ON DELETE CASCADE;

ALTER TABLE announcements
DROP CONSTRAINT IF EXISTS announcements_check;

ALTER TABLE announcements
    ADD CONSTRAINT announcements_check CHECK (
        (CASE WHEN target_estate_id    IS NOT NULL THEN 1 ELSE 0 END) +
        (CASE WHEN target_building_id  IS NOT NULL THEN 1 ELSE 0 END) +
        (CASE WHEN target_staircase_id IS NOT NULL THEN 1 ELSE 0 END) +
        (CASE WHEN target_apartment_id IS NOT NULL THEN 1 ELSE 0 END) <= 1
        );

CREATE INDEX IF NOT EXISTS idx_announcements_target_estate_id
    ON announcements (target_estate_id)
    WHERE target_estate_id IS NOT NULL;

-- STEP 8: notification_settings table
CREATE TABLE IF NOT EXISTS notification_settings (
                                                     id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    estate_id     UUID NOT NULL REFERENCES estates(id) ON DELETE CASCADE,
    event_type    VARCHAR(100) NOT NULL,
    push_enabled  BOOLEAN NOT NULL DEFAULT TRUE,
    email_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (estate_id, event_type)
    );

INSERT INTO notification_settings (estate_id, event_type, push_enabled, email_enabled)
SELECT e.id, evt.event_type, TRUE, FALSE
FROM estates e
         CROSS JOIN (VALUES
                         ('TICKET_CREATED'),
                         ('TICKET_STATUS_CHANGED'),
                         ('ANNOUNCEMENT_PUBLISHED'),
                         ('RESOLUTION_PUBLISHED'),
                         ('METER_READING_DUE')
) AS evt(event_type)
    ON CONFLICT (estate_id, event_type) DO NOTHING;

-- STEP 9: indexes
CREATE INDEX IF NOT EXISTS idx_buildings_estate_id
    ON buildings (estate_id);

CREATE INDEX IF NOT EXISTS idx_meters_apartment_id
    ON meters (apartment_id);

CREATE INDEX IF NOT EXISTS idx_meter_readings_meter_id
    ON meter_readings (meter_id);

CREATE INDEX IF NOT EXISTS idx_tickets_planned_visit_date
    ON tickets (planned_visit_date)
    WHERE planned_visit_date IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_notification_settings_estate_id
    ON notification_settings (estate_id);
