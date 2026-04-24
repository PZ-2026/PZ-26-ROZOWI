-- ==========================================
-- DODATKOWE POLA LOKALU MIESZKALNEGO
-- ==========================================
-- floor        – piętro, na którym znajduje się lokal (nullable)
-- area_m2      – powierzchnia lokalu w m² (nullable)
-- ownership_type – typ własności: WLASNOSCIOWY lub NAJEM (nullable)

ALTER TABLE apartments
    ADD COLUMN floor          INTEGER,
    ADD COLUMN area_m2        DECIMAL(6, 2),
    ADD COLUMN ownership_type VARCHAR(20)
        CHECK (ownership_type IN ('WLASNOSCIOWY', 'NAJEM'));
