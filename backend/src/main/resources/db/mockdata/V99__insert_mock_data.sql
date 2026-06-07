-- ============================================================
-- MOCK DATA — tylko dla profili dev/test
-- Hasło dla wszystkich użytkowników: Haslo123
-- ============================================================

-- ==========================================
-- UŻYTKOWNICY
-- ==========================================
INSERT INTO users (email, password_hash, first_name, last_name, role, is_active) VALUES
-- Zarządcy
('mock.zarzadca1@blokur.pl',   '$2a$10$ph8OkTlQwzFsUEVEqQj3xe6O82a.VzLccFLO4Ed6WoAYs/Uw0hwyW', 'Tomasz',   'Kwiatkowski',  'ZARZADCA',    true),
('mock.zarzadca2@blokur.pl',   '$2a$10$ph8OkTlQwzFsUEVEqQj3xe6O82a.VzLccFLO4Ed6WoAYs/Uw0hwyW', 'Elżbieta', 'Jabłońska',    'ZARZADCA',    true),
-- Konserwatorzy
('mock.hydraulik@blokur.pl',   '$2a$10$ph8OkTlQwzFsUEVEqQj3xe6O82a.VzLccFLO4Ed6WoAYs/Uw0hwyW', 'Stanisław','Wróbel',       'KONSERWATOR', true),
('mock.elektryk@blokur.pl',    '$2a$10$ph8OkTlQwzFsUEVEqQj3xe6O82a.VzLccFLO4Ed6WoAYs/Uw0hwyW', 'Grzegorz', 'Krawczyk',     'KONSERWATOR', true),
('mock.serwisant@blokur.pl',   '$2a$10$ph8OkTlQwzFsUEVEqQj3xe6O82a.VzLccFLO4Ed6WoAYs/Uw0hwyW', 'Mirosław', 'Mazur',        'KONSERWATOR', true),
-- Mieszkańcy aktywni
('mock.mieszkaniec1@test.pl',  '$2a$10$ph8OkTlQwzFsUEVEqQj3xe6O82a.VzLccFLO4Ed6WoAYs/Uw0hwyW', 'Katarzyna','Zielińska',    'MIESZKANIEC', true),
('mock.mieszkaniec2@test.pl',  '$2a$10$ph8OkTlQwzFsUEVEqQj3xe6O82a.VzLccFLO4Ed6WoAYs/Uw0hwyW', 'Marcin',   'Szymański',    'MIESZKANIEC', true),
('mock.mieszkaniec3@test.pl',  '$2a$10$ph8OkTlQwzFsUEVEqQj3xe6O82a.VzLccFLO4Ed6WoAYs/Uw0hwyW', 'Agnieszka','Woźniak',      'MIESZKANIEC', true),
('mock.mieszkaniec4@test.pl',  '$2a$10$ph8OkTlQwzFsUEVEqQj3xe6O82a.VzLccFLO4Ed6WoAYs/Uw0hwyW', 'Łukasz',   'Piotrowski',   'MIESZKANIEC', true),
('mock.mieszkaniec5@test.pl',  '$2a$10$ph8OkTlQwzFsUEVEqQj3xe6O82a.VzLccFLO4Ed6WoAYs/Uw0hwyW', 'Monika',   'Kamińska',     'MIESZKANIEC', true),
('mock.mieszkaniec6@test.pl',  '$2a$10$ph8OkTlQwzFsUEVEqQj3xe6O82a.VzLccFLO4Ed6WoAYs/Uw0hwyW', 'Robert',   'Jankowski',    'MIESZKANIEC', true),
('mock.mieszkaniec7@test.pl',  '$2a$10$ph8OkTlQwzFsUEVEqQj3xe6O82a.VzLccFLO4Ed6WoAYs/Uw0hwyW', 'Joanna',   'Wiśniewska',   'MIESZKANIEC', true),
('mock.mieszkaniec8@test.pl',  '$2a$10$ph8OkTlQwzFsUEVEqQj3xe6O82a.VzLccFLO4Ed6WoAYs/Uw0hwyW', 'Paweł',    'Wójcik',       'MIESZKANIEC', true),
-- Mieszkaniec nieaktywny (do testowania blokady konta)
('mock.inactive@test.pl',      '$2a$10$ph8OkTlQwzFsUEVEqQj3xe6O82a.VzLccFLO4Ed6WoAYs/Uw0hwyW', 'Nieaktywny','Testowy',     'MIESZKANIEC', false);

-- ==========================================
-- STRUKTURA BUDYNKÓW
-- ==========================================

-- Osiedle Tęczowe — Blok 1
INSERT INTO buildings (estate_name, name, address, latitude, longitude)
VALUES ('Osiedle Tęczowe', 'Blok 1 (Tęczowy)', 'ul. Tęczowa 10, 35-001 Rzeszów', 50.041187, 22.002585);

-- Osiedle Tęczowe — Blok 2
INSERT INTO buildings (estate_name, name, address, latitude, longitude)
VALUES ('Osiedle Tęczowe', 'Blok 2 (Kryształowy)', 'ul. Tęczowa 12, 35-001 Rzeszów', 50.041500, 22.003100);

-- Wspólnota Botaniczna — Kamienica
INSERT INTO buildings (estate_name, name, address, latitude, longitude)
VALUES ('Wspólnota Botaniczna', 'Kamienica Różana', 'ul. Botaniczna 5, 35-005 Rzeszów', 50.039000, 21.999000);

-- KLATKI — Blok 1 (3 klatki)
INSERT INTO staircases (building_id, label)
SELECT id, 'Klatka A' FROM buildings WHERE name = 'Blok 1 (Tęczowy)';
INSERT INTO staircases (building_id, label)
SELECT id, 'Klatka B' FROM buildings WHERE name = 'Blok 1 (Tęczowy)';
INSERT INTO staircases (building_id, label)
SELECT id, 'Klatka C' FROM buildings WHERE name = 'Blok 1 (Tęczowy)';

-- KLATKI — Blok 2 (2 klatki)
INSERT INTO staircases (building_id, label)
SELECT id, 'Klatka Lewa'  FROM buildings WHERE name = 'Blok 2 (Kryształowy)';
INSERT INTO staircases (building_id, label)
SELECT id, 'Klatka Prawa' FROM buildings WHERE name = 'Blok 2 (Kryształowy)';

-- KLATKI — Kamienica Różana (1 klatka)
INSERT INTO staircases (building_id, label)
SELECT id, 'Wejście Główne' FROM buildings WHERE name = 'Kamienica Różana';

-- LOKALE — Blok 1, Klatka A (mieszkania 1–4)
INSERT INTO apartments (staircase_id, number, current_balance)
SELECT s.id, apt.num, apt.bal
FROM staircases s
JOIN buildings b ON s.building_id = b.id,
(VALUES ('1', 200.00::DECIMAL), ('2', -50.00::DECIMAL), ('3', 0.00::DECIMAL), ('4', 750.50::DECIMAL)) AS apt(num, bal)
WHERE b.name = 'Blok 1 (Tęczowy)' AND s.label = 'Klatka A';

-- LOKALE — Blok 1, Klatka B (mieszkania 5–8)
INSERT INTO apartments (staircase_id, number, current_balance)
SELECT s.id, apt.num, apt.bal
FROM staircases s
JOIN buildings b ON s.building_id = b.id,
(VALUES ('5', 0.00::DECIMAL), ('6', -300.00::DECIMAL), ('7', 100.00::DECIMAL), ('8', 25.75::DECIMAL)) AS apt(num, bal)
WHERE b.name = 'Blok 1 (Tęczowy)' AND s.label = 'Klatka B';

-- LOKALE — Blok 1, Klatka C (mieszkania 9–11)
INSERT INTO apartments (staircase_id, number, current_balance)
SELECT s.id, apt.num, apt.bal
FROM staircases s
JOIN buildings b ON s.building_id = b.id,
(VALUES ('9', 0.00::DECIMAL), ('10', 50.00::DECIMAL), ('11', -80.00::DECIMAL)) AS apt(num, bal)
WHERE b.name = 'Blok 1 (Tęczowy)' AND s.label = 'Klatka C';

-- LOKALE — Blok 2, Klatka Lewa (mieszkania 1–3)
INSERT INTO apartments (staircase_id, number, current_balance)
SELECT s.id, apt.num, apt.bal
FROM staircases s
JOIN buildings b ON s.building_id = b.id,
(VALUES ('1', 500.00::DECIMAL), ('2', 0.00::DECIMAL), ('3', -120.00::DECIMAL)) AS apt(num, bal)
WHERE b.name = 'Blok 2 (Kryształowy)' AND s.label = 'Klatka Lewa';

-- LOKALE — Blok 2, Klatka Prawa (mieszkania 4–6)
INSERT INTO apartments (staircase_id, number, current_balance)
SELECT s.id, apt.num, apt.bal
FROM staircases s
JOIN buildings b ON s.building_id = b.id,
(VALUES ('4', 0.00::DECIMAL), ('5', 90.00::DECIMAL), ('6', -45.00::DECIMAL)) AS apt(num, bal)
WHERE b.name = 'Blok 2 (Kryształowy)' AND s.label = 'Klatka Prawa';

-- LOKALE — Kamienica Różana (mieszkania 201–204)
INSERT INTO apartments (staircase_id, number, current_balance)
SELECT s.id, apt.num, apt.bal
FROM staircases s
JOIN buildings b ON s.building_id = b.id,
(VALUES ('201', 1000.00::DECIMAL), ('202', 200.00::DECIMAL), ('203', -60.00::DECIMAL), ('204', 0.00::DECIMAL)) AS apt(num, bal)
WHERE b.name = 'Kamienica Różana' AND s.label = 'Wejście Główne';

-- ==========================================
-- PRZYPISANIE LOKALI DO MIESZKAŃCÓW
-- ==========================================
-- mock.mieszkaniec1 -> Blok 1 / Klatka A / Lokal 1
INSERT INTO user_apartments (user_id, apartment_id)
SELECT u.id, a.id
FROM users u, apartments a, staircases s, buildings b
WHERE u.email = 'mock.mieszkaniec1@test.pl'
  AND a.number = '1' AND a.staircase_id = s.id AND s.label = 'Klatka A'
  AND s.building_id = b.id AND b.name = 'Blok 1 (Tęczowy)';

-- mock.mieszkaniec2 -> Blok 1 / Klatka A / Lokal 2
INSERT INTO user_apartments (user_id, apartment_id)
SELECT u.id, a.id
FROM users u, apartments a, staircases s, buildings b
WHERE u.email = 'mock.mieszkaniec2@test.pl'
  AND a.number = '2' AND a.staircase_id = s.id AND s.label = 'Klatka A'
  AND s.building_id = b.id AND b.name = 'Blok 1 (Tęczowy)';

-- mock.mieszkaniec3 -> Blok 1 / Klatka B / Lokal 5
INSERT INTO user_apartments (user_id, apartment_id)
SELECT u.id, a.id
FROM users u, apartments a, staircases s, buildings b
WHERE u.email = 'mock.mieszkaniec3@test.pl'
  AND a.number = '5' AND a.staircase_id = s.id AND s.label = 'Klatka B'
  AND s.building_id = b.id AND b.name = 'Blok 1 (Tęczowy)';

-- mock.mieszkaniec4 -> Blok 2 / Klatka Lewa / Lokal 1
INSERT INTO user_apartments (user_id, apartment_id)
SELECT u.id, a.id
FROM users u, apartments a, staircases s, buildings b
WHERE u.email = 'mock.mieszkaniec4@test.pl'
  AND a.number = '1' AND a.staircase_id = s.id AND s.label = 'Klatka Lewa'
  AND s.building_id = b.id AND b.name = 'Blok 2 (Kryształowy)';

-- mock.mieszkaniec5 -> Blok 2 / Klatka Prawa / Lokal 4
INSERT INTO user_apartments (user_id, apartment_id)
SELECT u.id, a.id
FROM users u, apartments a, staircases s, buildings b
WHERE u.email = 'mock.mieszkaniec5@test.pl'
  AND a.number = '4' AND a.staircase_id = s.id AND s.label = 'Klatka Prawa'
  AND s.building_id = b.id AND b.name = 'Blok 2 (Kryształowy)';

-- mock.mieszkaniec6 -> Kamienica Różana / Lokal 201
INSERT INTO user_apartments (user_id, apartment_id)
SELECT u.id, a.id
FROM users u, apartments a, staircases s, buildings b
WHERE u.email = 'mock.mieszkaniec6@test.pl'
  AND a.number = '201' AND a.staircase_id = s.id AND s.label = 'Wejście Główne'
  AND s.building_id = b.id AND b.name = 'Kamienica Różana';

-- mock.mieszkaniec7 -> Kamienica Różana / Lokal 202
INSERT INTO user_apartments (user_id, apartment_id)
SELECT u.id, a.id
FROM users u, apartments a, staircases s, buildings b
WHERE u.email = 'mock.mieszkaniec7@test.pl'
  AND a.number = '202' AND a.staircase_id = s.id AND s.label = 'Wejście Główne'
  AND s.building_id = b.id AND b.name = 'Kamienica Różana';

-- mock.mieszkaniec8 -> Blok 1 / Klatka C / Lokal 9
INSERT INTO user_apartments (user_id, apartment_id)
SELECT u.id, a.id
FROM users u, apartments a, staircases s, buildings b
WHERE u.email = 'mock.mieszkaniec8@test.pl'
  AND a.number = '9' AND a.staircase_id = s.id AND s.label = 'Klatka C'
  AND s.building_id = b.id AND b.name = 'Blok 1 (Tęczowy)';

-- ==========================================
-- ZGŁOSZENIA (TICKETS)
-- Status: NOWE, W_REALIZACJI, ZAKONCZONE_DO_WERYFIKACJI
-- ==========================================

-- --- NOWE ---

INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, apartment_id)
SELECT 'ZGL/TEST/001', 'Brak ciepłej wody', 'Od trzech dni nie ma ciepłej wody w łazience i kuchni.', 'NOWE',
  (SELECT id FROM ticket_categories WHERE name = 'Hydraulika'),
  (SELECT id FROM users WHERE email = 'mock.mieszkaniec1@test.pl'),
  (SELECT a.id FROM apartments a JOIN staircases s ON a.staircase_id = s.id JOIN buildings b ON s.building_id = b.id
   WHERE a.number = '1' AND s.label = 'Klatka A' AND b.name = 'Blok 1 (Tęczowy)');

INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, staircase_id)
SELECT 'ZGL/TEST/002', 'Uszkodzona poręcz na klatce', 'Poręcz na 3. piętrze Klatki B jest obluzowana — niebezpieczna dla dzieci.', 'NOWE',
  (SELECT id FROM ticket_categories WHERE name = 'Remonty i Modernizacje'),
  (SELECT id FROM users WHERE email = 'mock.mieszkaniec3@test.pl'),
  (SELECT s.id FROM staircases s JOIN buildings b ON s.building_id = b.id
   WHERE s.label = 'Klatka B' AND b.name = 'Blok 1 (Tęczowy)');

INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, building_id)
SELECT 'ZGL/TEST/003', 'Zaśmiecone śmietniki zewnętrzne', 'Kontenery na odpady przepełnione — wywóz nie był realizowany od 2 tygodni.', 'NOWE',
  (SELECT id FROM ticket_categories WHERE name = 'Czystość i Porządek'),
  (SELECT id FROM users WHERE email = 'mock.mieszkaniec4@test.pl'),
  (SELECT id FROM buildings WHERE name = 'Blok 2 (Kryształowy)');

INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, apartment_id)
SELECT 'ZGL/TEST/004', 'Awaria grzejnika', 'Grzejnik w salonie nie grzeje mimo otwartego zaworu.', 'NOWE',
  (SELECT id FROM ticket_categories WHERE name = 'Hydraulika'),
  (SELECT id FROM users WHERE email = 'mock.mieszkaniec6@test.pl'),
  (SELECT a.id FROM apartments a JOIN staircases s ON a.staircase_id = s.id JOIN buildings b ON s.building_id = b.id
   WHERE a.number = '201' AND s.label = 'Wejście Główne' AND b.name = 'Kamienica Różana');

INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, staircase_id)
SELECT 'ZGL/TEST/005', 'Przepalone oświetlenie klatki', 'Wszystkie żarówki na parterze Klatki Lewej nie świecą.', 'NOWE',
  (SELECT id FROM ticket_categories WHERE name = 'Elektryka'),
  (SELECT id FROM users WHERE email = 'mock.mieszkaniec5@test.pl'),
  (SELECT s.id FROM staircases s JOIN buildings b ON s.building_id = b.id
   WHERE s.label = 'Klatka Lewa' AND b.name = 'Blok 2 (Kryształowy)');

-- --- W_REALIZACJI ---

INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, assigned_to_id, apartment_id)
SELECT 'ZGL/TEST/006', 'Przeciek z dachu', 'Po opadach deszczu woda przesiąka przez sufit w pokoju.', 'W_REALIZACJI',
  (SELECT id FROM ticket_categories WHERE name = 'Remonty i Modernizacje'),
  (SELECT id FROM users WHERE email = 'mock.mieszkaniec2@test.pl'),
  (SELECT id FROM users WHERE email = 'mock.serwisant@blokur.pl'),
  (SELECT a.id FROM apartments a JOIN staircases s ON a.staircase_id = s.id JOIN buildings b ON s.building_id = b.id
   WHERE a.number = '2' AND s.label = 'Klatka A' AND b.name = 'Blok 1 (Tęczowy)');

INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, assigned_to_id, staircase_id)
SELECT 'ZGL/TEST/007', 'Naprawa domofonu — Klatka C', 'Domofon nie otwiera zamka elektromagnetycznego.', 'W_REALIZACJI',
  (SELECT id FROM ticket_categories WHERE name = 'Domofony i Monitoring'),
  (SELECT id FROM users WHERE email = 'mock.mieszkaniec8@test.pl'),
  (SELECT id FROM users WHERE email = 'mock.elektryk@blokur.pl'),
  (SELECT s.id FROM staircases s JOIN buildings b ON s.building_id = b.id
   WHERE s.label = 'Klatka C' AND b.name = 'Blok 1 (Tęczowy)');

INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, assigned_to_id, building_id)
SELECT 'ZGL/TEST/008', 'Wymiana pomp ciepła', 'Planowa wymiana pomp ciepła zgodnie z harmonogramem modernizacji.', 'W_REALIZACJI',
  (SELECT id FROM ticket_categories WHERE name = 'Remonty i Modernizacje'),
  (SELECT id FROM users WHERE email = 'mock.zarzadca1@blokur.pl'),
  (SELECT id FROM users WHERE email = 'mock.hydraulik@blokur.pl'),
  (SELECT id FROM buildings WHERE name = 'Blok 2 (Kryształowy)');

INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, assigned_to_id, apartment_id)
SELECT 'ZGL/TEST/009', 'Uszkodzone gniazdko elektryczne', 'Gniazdko w kuchni iskrzy i nie trzyma wtyczek.', 'W_REALIZACJI',
  (SELECT id FROM ticket_categories WHERE name = 'Elektryka'),
  (SELECT id FROM users WHERE email = 'mock.mieszkaniec7@test.pl'),
  (SELECT id FROM users WHERE email = 'mock.elektryk@blokur.pl'),
  (SELECT a.id FROM apartments a JOIN staircases s ON a.staircase_id = s.id JOIN buildings b ON s.building_id = b.id
   WHERE a.number = '202' AND s.label = 'Wejście Główne' AND b.name = 'Kamienica Różana');

INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, assigned_to_id, staircase_id)
SELECT 'ZGL/TEST/010', 'Malowanie klatki schodowej', 'Ściany klatki wymagają odświeżenia — widoczne zabrudzenia i zarysowania.', 'W_REALIZACJI',
  (SELECT id FROM ticket_categories WHERE name = 'Remonty i Modernizacje'),
  (SELECT id FROM users WHERE email = 'mock.zarzadca2@blokur.pl'),
  (SELECT id FROM users WHERE email = 'mock.serwisant@blokur.pl'),
  (SELECT s.id FROM staircases s JOIN buildings b ON s.building_id = b.id
   WHERE s.label = 'Klatka Prawa' AND b.name = 'Blok 2 (Kryształowy)');

-- --- ZAKONCZONE_DO_WERYFIKACJI ---

INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, assigned_to_id, apartment_id, closed_at)
SELECT 'ZGL/TEST/011', 'Wymiana uszczelki pod zlewem', 'Kapanie wody pod zlewem w kuchni — wymieniono uszczelkę.', 'ZAKONCZONE_DO_WERYFIKACJI',
  (SELECT id FROM ticket_categories WHERE name = 'Hydraulika'),
  (SELECT id FROM users WHERE email = 'mock.mieszkaniec1@test.pl'),
  (SELECT id FROM users WHERE email = 'mock.hydraulik@blokur.pl'),
  (SELECT a.id FROM apartments a JOIN staircases s ON a.staircase_id = s.id JOIN buildings b ON s.building_id = b.id
   WHERE a.number = '1' AND s.label = 'Klatka A' AND b.name = 'Blok 1 (Tęczowy)'),
  CURRENT_TIMESTAMP - INTERVAL '10 days';

INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, assigned_to_id, building_id, closed_at)
SELECT 'ZGL/TEST/012', 'Przegląd instalacji gazowej', 'Roczny przegląd instalacji gazowej — bez uwag.', 'ZAKONCZONE_DO_WERYFIKACJI',
  (SELECT id FROM ticket_categories WHERE name = 'Administracja'),
  (SELECT id FROM users WHERE email = 'mock.zarzadca1@blokur.pl'),
  (SELECT id FROM users WHERE email = 'mock.hydraulik@blokur.pl'),
  (SELECT id FROM buildings WHERE name = 'Kamienica Różana'),
  CURRENT_TIMESTAMP - INTERVAL '30 days';

INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, assigned_to_id, staircase_id, closed_at)
SELECT 'ZGL/TEST/013', 'Naprawiono oświetlenie awaryjne', 'Lampa awaryjna na 2. piętrze nie działała — wymieniono akumulator.', 'ZAKONCZONE_DO_WERYFIKACJI',
  (SELECT id FROM ticket_categories WHERE name = 'Elektryka'),
  (SELECT id FROM users WHERE email = 'mock.mieszkaniec4@test.pl'),
  (SELECT id FROM users WHERE email = 'mock.elektryk@blokur.pl'),
  (SELECT s.id FROM staircases s JOIN buildings b ON s.building_id = b.id
   WHERE s.label = 'Klatka Lewa' AND b.name = 'Blok 2 (Kryształowy)'),
  CURRENT_TIMESTAMP - INTERVAL '7 days';

INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, building_id, closed_at)
SELECT 'ZGL/TEST/014', 'Czyszczenie rynien', 'Sezonowe czyszczenie rynien i spustów dachowych — wykonano.', 'ZAKONCZONE_DO_WERYFIKACJI',
  (SELECT id FROM ticket_categories WHERE name = 'Czystość i Porządek'),
  (SELECT id FROM users WHERE email = 'mock.zarzadca2@blokur.pl'),
  (SELECT id FROM buildings WHERE name = 'Blok 1 (Tęczowy)'),
  CURRENT_TIMESTAMP - INTERVAL '14 days';

INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, assigned_to_id, apartment_id, closed_at)
SELECT 'ZGL/TEST/015', 'Naprawa klamki drzwi wejściowych', 'Klamka drzwi do lokalu była obluzowana — dokręcono i wymieniono śruby.', 'ZAKONCZONE_DO_WERYFIKACJI',
  (SELECT id FROM ticket_categories WHERE name = 'Remonty i Modernizacje'),
  (SELECT id FROM users WHERE email = 'mock.mieszkaniec6@test.pl'),
  (SELECT id FROM users WHERE email = 'mock.serwisant@blokur.pl'),
  (SELECT a.id FROM apartments a JOIN staircases s ON a.staircase_id = s.id JOIN buildings b ON s.building_id = b.id
   WHERE a.number = '201' AND s.label = 'Wejście Główne' AND b.name = 'Kamienica Różana'),
  CURRENT_TIMESTAMP - INTERVAL '5 days';

-- ==========================================
-- HISTORIA ZGŁOSZEŃ (ticket_history)
-- ==========================================

-- Historia dla ZGL/TEST/006 (W_REALIZACJI)
INSERT INTO ticket_history (ticket_id, status, changed_by, comment)
SELECT t.id, 'NOWE', (SELECT id FROM users WHERE email = 'mock.mieszkaniec2@test.pl'), 'Zgłoszenie zostało zarejestrowane.'
FROM tickets t WHERE t.ticket_number = 'ZGL/TEST/006';

INSERT INTO ticket_history (ticket_id, status, changed_by, comment)
SELECT t.id, 'W_REALIZACJI', (SELECT id FROM users WHERE email = 'mock.zarzadca1@blokur.pl'), 'Przydzielono serwisanta. Planowane oględziny w ciągu 48h.'
FROM tickets t WHERE t.ticket_number = 'ZGL/TEST/006';

-- Historia dla ZGL/TEST/011 (ZAKONCZONE_DO_WERYFIKACJI)
INSERT INTO ticket_history (ticket_id, status, changed_by, comment)
SELECT t.id, 'NOWE', (SELECT id FROM users WHERE email = 'mock.mieszkaniec1@test.pl'), 'Zgłoszono problem z przeciekiem.'
FROM tickets t WHERE t.ticket_number = 'ZGL/TEST/011';

INSERT INTO ticket_history (ticket_id, status, changed_by, comment)
SELECT t.id, 'W_REALIZACJI', (SELECT id FROM users WHERE email = 'mock.zarzadca1@blokur.pl'), 'Przydzielono hydraulika.'
FROM tickets t WHERE t.ticket_number = 'ZGL/TEST/011';

INSERT INTO ticket_history (ticket_id, status, changed_by, comment)
SELECT t.id, 'ZAKONCZONE_DO_WERYFIKACJI', (SELECT id FROM users WHERE email = 'mock.hydraulik@blokur.pl'), 'Uszczelka wymieniona. Problem rozwiązany.'
FROM tickets t WHERE t.ticket_number = 'ZGL/TEST/011';

-- Historia dla ZGL/TEST/012 (ZAKONCZONE_DO_WERYFIKACJI)
INSERT INTO ticket_history (ticket_id, status, changed_by, comment)
SELECT t.id, 'NOWE', (SELECT id FROM users WHERE email = 'mock.zarzadca1@blokur.pl'), 'Zlecono coroczny przegląd instalacji.'
FROM tickets t WHERE t.ticket_number = 'ZGL/TEST/012';

INSERT INTO ticket_history (ticket_id, status, changed_by, comment)
SELECT t.id, 'ZAKONCZONE_DO_WERYFIKACJI', (SELECT id FROM users WHERE email = 'mock.hydraulik@blokur.pl'), 'Przegląd zakończony. Protokół podpisany. Instalacja sprawna.'
FROM tickets t WHERE t.ticket_number = 'ZGL/TEST/012';

-- ==========================================
-- ODCZYTY LICZNIKÓW (meter_readings)
-- ==========================================
INSERT INTO meter_readings (apartment_id, meter_type, value, reading_date)
SELECT a.id, 'CIEPLA_WODA', 123.4500, '2026-04-01'
FROM apartments a JOIN staircases s ON a.staircase_id = s.id JOIN buildings b ON s.building_id = b.id
WHERE a.number = '1' AND s.label = 'Klatka A' AND b.name = 'Blok 1 (Tęczowy)';

INSERT INTO meter_readings (apartment_id, meter_type, value, reading_date)
SELECT a.id, 'ZIMNA_WODA', 456.7800, '2026-04-01'
FROM apartments a JOIN staircases s ON a.staircase_id = s.id JOIN buildings b ON s.building_id = b.id
WHERE a.number = '1' AND s.label = 'Klatka A' AND b.name = 'Blok 1 (Tęczowy)';

INSERT INTO meter_readings (apartment_id, meter_type, value, reading_date)
SELECT a.id, 'CIEPLO', 98.3200, '2026-04-01'
FROM apartments a JOIN staircases s ON a.staircase_id = s.id JOIN buildings b ON s.building_id = b.id
WHERE a.number = '201' AND s.label = 'Wejście Główne' AND b.name = 'Kamienica Różana';

INSERT INTO meter_readings (apartment_id, meter_type, value, reading_date)
SELECT a.id, 'PRAD', 312.0100, '2026-04-01'
FROM apartments a JOIN staircases s ON a.staircase_id = s.id JOIN buildings b ON s.building_id = b.id
WHERE a.number = '201' AND s.label = 'Wejście Główne' AND b.name = 'Kamienica Różana';

-- ==========================================
-- OGŁOSZENIA
-- ==========================================
INSERT INTO announcements (type, title, content, author_id, target_building_id)
SELECT 'OGLOSZENIE', 'Planowana przerwa w dostawie wody', 'W dniu 25.04.2026 od godz. 8:00 do 14:00 nastąpi przerwa w dostawie wody z powodu prac konserwacyjnych.',
  (SELECT id FROM users WHERE email = 'mock.zarzadca1@blokur.pl'),
  (SELECT id FROM buildings WHERE name = 'Blok 1 (Tęczowy)');

INSERT INTO announcements (type, title, content, author_id)
SELECT 'OGLOSZENIE', 'Zebranie wspólnoty mieszkańców', 'Zapraszamy wszystkich mieszkańców na coroczne zebranie w dniu 30.04.2026 o godz. 18:00 w świetlicy osiedlowej.',
  (SELECT id FROM users WHERE email = 'mock.zarzadca2@blokur.pl');

INSERT INTO announcements (type, title, content, author_id, target_building_id)
SELECT 'OGLOSZENIE', 'Dezynfekcja piwnic', 'Informujemy o przeprowadzonej dezynfekcji piwnic i części wspólnych. Protokół dostępny u zarządcy.',
  (SELECT id FROM users WHERE email = 'mock.zarzadca2@blokur.pl'),
  (SELECT id FROM buildings WHERE name = 'Kamienica Różana');
