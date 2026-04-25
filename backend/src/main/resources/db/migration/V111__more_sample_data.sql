-- ==========================================
-- 1. NIERUCHOMOŚĆ (PROPERTY)
-- ==========================================
INSERT INTO properties (name, address, nip, manager_phone, manager_email)
VALUES ('Wspólnota Mieszkaniowa "Blokur"', 'ul. Promienna 1, 00-001 Warszawa', '1234567890', '+48 22 123 45 67', 'zarzad@blokur.pl');

-- Powiązanie wszystkich budynków z nową nieruchomością
UPDATE buildings SET property_id = (SELECT id FROM properties WHERE nip = '1234567890');

-- ==========================================
-- 2. AKTUALIZACJA UŻYTKOWNIKÓW (TELEFONY)
-- ==========================================
UPDATE users SET phone = '+48 501 111 111' WHERE email = 'admin1@blokur.pl';
UPDATE users SET phone = '+48 502 222 222' WHERE email = 'hydraulik@blokur.pl';
UPDATE users SET phone = '+48 601 234 567' WHERE email = 'jan.kowalski@gmail.com';
UPDATE users SET phone = '+48 701 987 654' WHERE email = 'anna.nowak@poczta.pl';

-- ==========================================
-- 3. AKTUALIZACJA LOKALI (DODATKOWE POLA)
-- ==========================================
UPDATE apartments SET floor = 0, area_m2 = 45.50, ownership_type = 'WLASNOSCIOWY' 
WHERE number = '1' AND staircase_id IN (SELECT id FROM staircases WHERE building_id = (SELECT id FROM buildings WHERE name = 'Budynek A (Solaris)'));

UPDATE apartments SET floor = 1, area_m2 = 62.20, ownership_type = 'WLASNOSCIOWY' 
WHERE number = '6' AND staircase_id IN (SELECT id FROM staircases WHERE building_id = (SELECT id FROM buildings WHERE name = 'Budynek A (Solaris)'));

UPDATE apartments SET floor = 2, area_m2 = 38.00, ownership_type = 'NAJEM' 
WHERE number = '2' AND staircase_id IN (SELECT id FROM staircases WHERE building_id = (SELECT id FROM buildings WHERE name = 'Budynek A (Solaris)'));

UPDATE apartments SET floor = 0, area_m2 = 55.10, ownership_type = 'WLASNOSCIOWY' 
WHERE number = '101' AND staircase_id IN (SELECT id FROM staircases WHERE building_id = (SELECT id FROM buildings WHERE name = 'Rezydencja Parkowa'));

-- ==========================================
-- 4. LICZNIKI (METERS)
-- ==========================================
-- Liczniki dla Jana Kowalskiego (Budynek A, Lokal 1)
INSERT INTO meters (apartment_id, serial_number, medium_type, installation_date)
SELECT a.id, 'WOD-ZIM-001', 'ZIMNA_WODA', '2025-01-10' 
FROM apartments a JOIN user_apartments ua ON a.id = ua.apartment_id JOIN users u ON ua.user_id = u.id 
WHERE u.email = 'jan.kowalski@gmail.com' AND a.number = '1';

INSERT INTO meters (apartment_id, serial_number, medium_type, installation_date)
SELECT a.id, 'WOD-CIE-001', 'CIEPLA_WODA', '2025-01-10' 
FROM apartments a JOIN user_apartments ua ON a.id = ua.apartment_id JOIN users u ON ua.user_id = u.id 
WHERE u.email = 'jan.kowalski@gmail.com' AND a.number = '1';

INSERT INTO meters (apartment_id, serial_number, medium_type, installation_date)
SELECT a.id, 'GAZ-001', 'GAZ', '2025-01-10' 
FROM apartments a JOIN user_apartments ua ON a.id = ua.apartment_id JOIN users u ON ua.user_id = u.id 
WHERE u.email = 'jan.kowalski@gmail.com' AND a.number = '1';

-- Liczniki dla Anny Nowak (Budynek A, Lokal 6)
INSERT INTO meters (apartment_id, serial_number, medium_type, installation_date)
SELECT a.id, 'WOD-ZIM-006', 'ZIMNA_WODA', '2025-02-15' 
FROM apartments a JOIN user_apartments ua ON a.id = ua.apartment_id JOIN users u ON ua.user_id = u.id 
WHERE u.email = 'anna.nowak@poczta.pl' AND a.number = '6';

INSERT INTO meters (apartment_id, serial_number, medium_type, installation_date)
SELECT a.id, 'CIE-006', 'CIEPLO', '2025-02-15' 
FROM apartments a JOIN user_apartments ua ON a.id = ua.apartment_id JOIN users u ON ua.user_id = u.id 
WHERE u.email = 'anna.nowak@poczta.pl' AND a.number = '6';

-- ==========================================
-- 5. ODCZYTY LICZNIKÓW (METER READINGS)
-- ==========================================
-- Odczyty dla Jana (Marzec)
INSERT INTO meter_readings (meter_id, apartment_id, value, reading_date)
SELECT id, apartment_id, 120.500, '2026-03-31' FROM meters WHERE serial_number = 'WOD-ZIM-001';
INSERT INTO meter_readings (meter_id, apartment_id, value, reading_date)
SELECT id, apartment_id, 45.200, '2026-03-31' FROM meters WHERE serial_number = 'WOD-CIE-001';
INSERT INTO meter_readings (meter_id, apartment_id, value, reading_date)
SELECT id, apartment_id, 890.000, '2026-03-31' FROM meters WHERE serial_number = 'GAZ-001';

-- Odczyty dla Jana (Kwiecień)
INSERT INTO meter_readings (meter_id, apartment_id, value, reading_date)
SELECT id, apartment_id, 125.800, '2026-04-25' FROM meters WHERE serial_number = 'WOD-ZIM-001';
INSERT INTO meter_readings (meter_id, apartment_id, value, reading_date)
SELECT id, apartment_id, 47.100, '2026-04-25' FROM meters WHERE serial_number = 'WOD-CIE-001';
INSERT INTO meter_readings (meter_id, apartment_id, value, reading_date)
SELECT id, apartment_id, 912.350, '2026-04-25' FROM meters WHERE serial_number = 'GAZ-001';

-- ==========================================
-- 6. INSPEKCJE (INSPECTIONS)
-- ==========================================
INSERT INTO inspections (title, description, scheduled_at, scope_type, scope_id, created_by)
VALUES 
('Przegląd roczny budynku', 'Ogólny przegląd stanu technicznego elewacji i dachu.', '2026-05-10 09:00:00', 'BUDYNEK', 
 (SELECT id FROM buildings WHERE name = 'Budynek A (Solaris)'),
 (SELECT id FROM users WHERE email = 'admin1@blokur.pl')),
('Kontrola gaśnic', 'Sprawdzenie daty ważności gaśnic w klatce.', '2026-05-12 10:00:00', 'KLATKA', 
 (SELECT id FROM staircases WHERE label = 'Klatka 1' AND building_id = (SELECT id FROM buildings WHERE name = 'Budynek A (Solaris)')),
 (SELECT id FROM users WHERE email = 'admin1@blokur.pl')),
('Audit energetyczny', 'Analiza zużycia energii w całej nieruchomości.', '2026-06-01 08:00:00', 'NIERUCHOMOSC', 
 (SELECT id FROM properties WHERE nip = '1234567890'),
 (SELECT id FROM users WHERE email = 'admin1@blokur.pl'));

-- ==========================================
-- 7. KOMENTARZE I DETALE ZGŁOSZEŃ (TICKETS)
-- ==========================================
-- Detale dla ZGL/2026/001 (Wyciek)
UPDATE tickets SET 
    planned_visit_at = CURRENT_TIMESTAMP + INTERVAL '1 day',
    internal_note = 'Hydraulik Marian ma wziąć zapasowy syfon marki X.',
    work_description = 'Wymieniono uszczelkę, ale syfon nadal poci się. Planowana wymiana całego elementu.'
WHERE ticket_number = 'ZGL/2026/001';

INSERT INTO ticket_comments (ticket_id, author_id, content, comment_type)
VALUES 
((SELECT id FROM tickets WHERE ticket_number = 'ZGL/2026/001'), 
 (SELECT id FROM users WHERE email = 'hydraulik@blokur.pl'), 
 'Będę jutro około 10:00. Proszę o zapewnienie dostępu do kuchni.', 'PUBLICZNY'),
((SELECT id FROM tickets WHERE ticket_number = 'ZGL/2026/001'), 
 (SELECT id FROM users WHERE email = 'admin1@blokur.pl'), 
 'Użytkownik zgłaszał, że pies może być agresywny. Uważać.', 'INTERNAL');

-- ==========================================
-- 8. OGŁOSZENIA (ANNOUNCEMENTS)
-- ==========================================
INSERT INTO announcements (type, title, content, author_id, target_type, target_staircase_id)
VALUES ('INFORMACJA', 'Brak wody - Klatka 1', 'W dniu 27.04 nastąpi przerwa w dostawie wody w godzinach 8:00-12:00.',
 (SELECT id FROM users WHERE email = 'admin1@blokur.pl'), 'KLATKA',
 (SELECT id FROM staircases WHERE label = 'Klatka 1' AND building_id = (SELECT id FROM buildings WHERE name = 'Budynek A (Solaris)')));

INSERT INTO announcements (type, title, content, author_id, target_type)
VALUES ('OGLOSZENIE', 'Piknik sąsiedzki', 'Zapraszamy na wspólnego grilla na terenie zielonym w sobotę!',
 (SELECT id FROM users WHERE email = 'admin2@blokur.pl'), 'WSZYSCY');

-- ==========================================
-- 9. TRANSAKCJE FINANSOWE (FINANCIAL)
-- ==========================================
INSERT INTO financial_transactions (apartment_id, type, amount, description, transaction_date, recorded_by_id)
VALUES 
((SELECT a.id FROM apartments a JOIN user_apartments ua ON a.id = ua.apartment_id JOIN users u ON ua.user_id = u.id WHERE u.email = 'jan.kowalski@gmail.com' AND a.number = '1'), 
 'NALEZNOSC', 450.00, 'Czynsz 04/2026', '2026-04-01', (SELECT id FROM users WHERE email = 'admin1@blokur.pl')),
((SELECT a.id FROM apartments a JOIN user_apartments ua ON a.id = ua.apartment_id JOIN users u ON ua.user_id = u.id WHERE u.email = 'jan.kowalski@gmail.com' AND a.number = '1'), 
 'WPLATA', 450.00, 'Przelew Czynsz 04/2026', '2026-04-05', (SELECT id FROM users WHERE email = 'admin1@blokur.pl')),
((SELECT a.id FROM apartments a JOIN user_apartments ua ON a.id = ua.apartment_id JOIN users u ON ua.user_id = u.id WHERE u.email = 'jan.kowalski@gmail.com' AND a.number = '1'), 
 'NALEZNOSC', 12.50, 'Rozliczenie wody 03/2026', '2026-04-10', (SELECT id FROM users WHERE email = 'admin1@blokur.pl'));

-- ==========================================
-- 10. UCHWAŁY (RESOLUTIONS)
-- ==========================================
INSERT INTO resolutions (building_id, title, description, author_id, end_date)
VALUES 
((SELECT id FROM buildings WHERE name = 'Budynek B (Luna)'), 
 'Monitoring w windzie', 'Montaż kamer w windach w celu poprawy bezpieczeństwa.', 
 (SELECT id FROM users WHERE email = 'admin1@blokur.pl'), 
 CURRENT_TIMESTAMP + INTERVAL '30 days');

INSERT INTO resolution_options (resolution_id, option_text)
SELECT id, 'TAK' FROM resolutions WHERE title = 'Monitoring w windzie';
INSERT INTO resolution_options (resolution_id, option_text)
SELECT id, 'NIE' FROM resolutions WHERE title = 'Monitoring w windzie';
