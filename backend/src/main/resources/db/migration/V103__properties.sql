-- ==========================================
-- NIERUCHOMOŚCI (PROPERTIES)
-- ==========================================
-- Encja nadrzędna wobec budynku. Każda nieruchomość (wspólnota mieszkaniowa)
-- posiada dane identyfikacyjne (NIP), kontaktowe oraz opcjonalne logo
-- używane w nagłówkach generowanych dokumentów PDF.

CREATE TABLE properties (
    id            UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    name          VARCHAR(255) NOT NULL,
    address       VARCHAR(255) NOT NULL,
    nip           VARCHAR(10)  NOT NULL,
    manager_phone VARCHAR(20),
    manager_email VARCHAR(255),
    logo_path     VARCHAR(500)
);

-- ==========================================
-- POWIĄZANIE BUILDINGS → PROPERTIES
-- ==========================================
-- Kolumna property_id jest nullable, aby nie blokować istniejących
-- rekordów buildings, które nie mają jeszcze przypisanej nieruchomości.

ALTER TABLE buildings
    ADD COLUMN property_id UUID REFERENCES properties(id) ON DELETE SET NULL;

CREATE INDEX idx_buildings_property_id ON buildings (property_id);
