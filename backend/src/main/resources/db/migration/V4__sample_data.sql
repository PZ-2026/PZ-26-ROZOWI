-- ==========================================
-- UŻYTKOWNICY (Hasło dla wszystkich: haslo123)
-- ==========================================
INSERT INTO users (email, password_hash, first_name, last_name, role, is_active) VALUES
('admin1@blokur.pl', '$2a$10$ffFK5FwOKhBnpBxPABlcve3hrARlA/vaPnn2O9Zr6UktfbdI2101e', 'Andrzej', 'Zarządczy', 'ZARZADCA', true), -- Hasło: haslo123
('admin2@blokur.pl', '$2a$10$ffFK5FwOKhBnpBxPABlcve3hrARlA/vaPnn2O9Zr6UktfbdI2101e', 'Beata', 'Wspólnotowa', 'ZARZADCA', true), -- Hasło: haslo123
('hydraulik@blokur.pl', '$2a$10$ffFK5FwOKhBnpBxPABlcve3hrARlA/vaPnn2O9Zr6UktfbdI2101e', 'Marian', 'Rura', 'KONSERWATOR', true),
('elektryk@blokur.pl', '$2a$10$ffFK5FwOKhBnpBxPABlcve3hrARlA/vaPnn2O9Zr6UktfbdI2101e', 'Zenon', 'Kabel', 'KONSERWATOR', true),
('serwis@blokur.pl', '$2a$10$ffFK5FwOKhBnpBxPABlcve3hrARlA/vaPnn2O9Zr6UktfbdI2101e', 'Adam', 'Złota-Rączka', 'KONSERWATOR', true),
('jan.kowalski@gmail.com', '$2a$10$ffFK5FwOKhBnpBxPABlcve3hrARlA/vaPnn2O9Zr6UktfbdI2101e', 'Jan', 'Kowalski', 'MIESZKANIEC', true),
('anna.nowak@poczta.pl', '$2a$10$ffFK5FwOKhBnpBxPABlcve3hrARlA/vaPnn2O9Zr6UktfbdI2101e', 'Anna', 'Nowak', 'MIESZKANIEC', true),
('piotr.wisniewski@wp.pl', '$2a$10$ffFK5FwOKhBnpBxPABlcve3hrARlA/vaPnn2O9Zr6UktfbdI2101e', 'Piotr', 'Wiśniewski', 'MIESZKANIEC', true),
('maria.dabrowska@onet.pl', '$2a$10$ffFK5FwOKhBnpBxPABlcve3hrARlA/vaPnn2O9Zr6UktfbdI2101e', 'Maria', 'Dąbrowska', 'MIESZKANIEC', true),
('krzysztof.lewandowski@interia.pl', '$2a$10$ffFK5FwOKhBnpBxPABlcve3hrARlA/vaPnn2O9Zr6UktfbdI2101e', 'Krzysztof', 'Lewandowski', 'MIESZKANIEC', true);

-- ==========================================
-- KATEGORIE ZGŁOSZEŃ
-- ==========================================
INSERT INTO ticket_categories (name) VALUES
('Hydraulika'),
('Elektryka'),
('Domofony i Monitoring'),
('Czystość i Porządek'),
('Administracja'),
('Remonty i Modernizacje');

-- ==========================================
-- STRUKTURA BUDYNKÓW
-- ==========================================
-- Budynek A na Osiedlu Słonecznym
INSERT INTO buildings (estate_name, name, address, latitude, longitude)
VALUES ('Osiedle Słoneczne', 'Budynek A (Solaris)', 'ul. Promienna 1, 00-001 Warszawa', 52.229675, 21.012229);

-- Budynek B na Osiedlu Słonecznym
INSERT INTO buildings (estate_name, name, address, latitude, longitude)
VALUES ('Osiedle Słoneczne', 'Budynek B (Luna)', 'ul. Promienna 3, 00-001 Warszawa', 52.230111, 21.013555);

-- Budynek na Wspólnocie Zielonej
INSERT INTO buildings (estate_name, name, address, latitude, longitude)
VALUES ('Wspólnota Zielona', 'Rezydencja Parkowa', 'ul. Ogrodowa 15, 05-077 Warszawa', 52.245000, 21.050000);

-- KLATKI
-- Klatki w Budynku A
INSERT INTO staircases (building_id, label)
SELECT id, 'Klatka 1' FROM buildings WHERE name = 'Budynek A (Solaris)';
INSERT INTO staircases (building_id, label)
SELECT id, 'Klatka 2' FROM buildings WHERE name = 'Budynek A (Solaris)';

-- Klatki w Budynku B
INSERT INTO staircases (building_id, label)
SELECT id, 'Klatka Główna' FROM buildings WHERE name = 'Budynek B (Luna)';

-- Klatka w Rezydencji Parkowej
INSERT INTO staircases (building_id, label)
SELECT id, 'Wejście A' FROM buildings WHERE name = 'Rezydencja Parkowa';

-- LOKALE
-- Lokale w Budynku A, Klatka 1 (Mieszkania 1-5)
INSERT INTO apartments (staircase_id, number, current_balance)
SELECT id, '1', 150.50 FROM staircases WHERE label = 'Klatka 1' AND building_id = (SELECT id FROM buildings WHERE name = 'Budynek A (Solaris)');
INSERT INTO apartments (staircase_id, number, current_balance)
SELECT id, '2', -20.00 FROM staircases WHERE label = 'Klatka 1' AND building_id = (SELECT id FROM buildings WHERE name = 'Budynek A (Solaris)');
INSERT INTO apartments (staircase_id, number, current_balance)
SELECT id, '3', 0.00 FROM staircases WHERE label = 'Klatka 1' AND building_id = (SELECT id FROM buildings WHERE name = 'Budynek A (Solaris)');
INSERT INTO apartments (staircase_id, number, current_balance)
SELECT id, '4', 450.00 FROM staircases WHERE label = 'Klatka 1' AND building_id = (SELECT id FROM buildings WHERE name = 'Budynek A (Solaris)');
INSERT INTO apartments (staircase_id, number, current_balance)
SELECT id, '5', -1200.00 FROM staircases WHERE label = 'Klatka 1' AND building_id = (SELECT id FROM buildings WHERE name = 'Budynek A (Solaris)');

-- Lokale w Budynku A, Klatka 2 (Mieszkania 6-10)
INSERT INTO apartments (staircase_id, number, current_balance)
SELECT id, '6', 50.00 FROM staircases WHERE label = 'Klatka 2' AND building_id = (SELECT id FROM buildings WHERE name = 'Budynek A (Solaris)');
INSERT INTO apartments (staircase_id, number, current_balance)
SELECT id, '7', 0.00 FROM staircases WHERE label = 'Klatka 2' AND building_id = (SELECT id FROM buildings WHERE name = 'Budynek A (Solaris)');
INSERT INTO apartments (staircase_id, number, current_balance)
SELECT id, '8', 12.30 FROM staircases WHERE label = 'Klatka 2' AND building_id = (SELECT id FROM buildings WHERE name = 'Budynek A (Solaris)');
INSERT INTO apartments (staircase_id, number, current_balance)
SELECT id, '9', -5.00 FROM staircases WHERE label = 'Klatka 2' AND building_id = (SELECT id FROM buildings WHERE name = 'Budynek A (Solaris)');
INSERT INTO apartments (staircase_id, number, current_balance)
SELECT id, '10', 0.00 FROM staircases WHERE label = 'Klatka 2' AND building_id = (SELECT id FROM buildings WHERE name = 'Budynek A (Solaris)');

-- Lokale w Budynku B (Mieszkania 1-5)
INSERT INTO apartments (staircase_id, number, current_balance)
SELECT id, s.num, 0.00 FROM staircases JOIN (VALUES ('1'), ('2'), ('3'), ('4'), ('5')) AS s(num) ON label = 'Klatka Główna';

-- Lokale w Rezydencji Parkowej (Mieszkania 101-103)
INSERT INTO apartments (staircase_id, number, current_balance)
SELECT id, s.num, 300.00 FROM staircases JOIN (VALUES ('101'), ('102'), ('103')) AS s(num) ON label = 'Wejście A';

-- ==========================================
-- PRZYPISANIE LOKALI DO UŻYTKOWNIKÓW
-- ==========================================
-- Jan Kowalski -> Budynek A, Mieszkanie 1
INSERT INTO user_apartments (user_id, apartment_id)
SELECT u.id, a.id FROM users u, apartments a, staircases s, buildings b
WHERE u.email = 'jan.kowalski@gmail.com' AND a.number = '1' AND a.staircase_id = s.id AND s.label = 'Klatka 1' AND s.building_id = b.id AND b.name = 'Budynek A (Solaris)';

-- Anna Nowak -> Budynek A, Mieszkanie 6
INSERT INTO user_apartments (user_id, apartment_id)
SELECT u.id, a.id FROM users u, apartments a, staircases s, buildings b
WHERE u.email = 'anna.nowak@poczta.pl' AND a.number = '6' AND a.staircase_id = s.id AND s.label = 'Klatka 2' AND s.building_id = b.id AND b.name = 'Budynek A (Solaris)';

-- Piotr Wiśniewski -> Rezydencja Parkowa, Mieszkanie 101
INSERT INTO user_apartments (user_id, apartment_id)
SELECT u.id, a.id FROM users u, apartments a, staircases s, buildings b
WHERE u.email = 'piotr.wisniewski@wp.pl' AND a.number = '101' AND a.staircase_id = s.id AND s.label = 'Wejście A' AND s.building_id = b.id AND b.name = 'Rezydencja Parkowa';

-- Maria Dąbrowska -> Budynek B, Mieszkanie 3
INSERT INTO user_apartments (user_id, apartment_id)
SELECT u.id, a.id FROM users u, apartments a, staircases s, buildings b
WHERE u.email = 'maria.dabrowska@onet.pl' AND a.number = '3' AND a.staircase_id = s.id AND s.label = 'Klatka Główna' AND s.building_id = b.id AND b.name = 'Budynek B (Luna)';

-- ==========================================
-- KOMUNIKACJA
-- ==========================================
-- Ogłoszenia globalne i lokalne
INSERT INTO announcements (type, title, content, author_id, target_building_id)
SELECT 'OGLOSZENIE', 'Przegląd kominiarski', 'Przypominamy o obowiązkowym przeglądzie kominiarskim w dniu 15.04.', 
(SELECT id FROM users WHERE email = 'admin1@blokur.pl'),
(SELECT id FROM buildings WHERE name = 'Budynek A (Solaris)');

INSERT INTO announcements (type, title, content, author_id)
SELECT 'OGLOSZENIE', 'Modernizacja oświetlenia', 'W przyszłym miesiącu rozpoczniemy wymianę oświetlenia na LEDowe w częściach wspólnych.', 
(SELECT id FROM users WHERE email = 'admin2@blokur.pl');

-- Głosowanie
INSERT INTO resolutions (building_id, title, description, author_id, end_date)
SELECT id, 'Fundusz remontowy 2026', 'Głosowanie nad zwiększeniem stawki na fundusz remontowy o 0.20 zł/m2.', 
(SELECT id FROM users WHERE email = 'admin1@blokur.pl'),
CURRENT_TIMESTAMP + INTERVAL '14 days'
FROM buildings WHERE name = 'Budynek A (Solaris)';

INSERT INTO resolution_options (resolution_id, option_text)
SELECT id, 'Za' FROM resolutions WHERE title = 'Fundusz remontowy 2026';
INSERT INTO resolution_options (resolution_id, option_text)
SELECT id, 'Przeciw' FROM resolutions WHERE title = 'Fundusz remontowy 2026';
INSERT INTO resolution_options (resolution_id, option_text)
SELECT id, 'Wstrzymuję się' FROM resolutions WHERE title = 'Fundusz remontowy 2026';

-- ==========================================
-- ZGŁOSZENIA (TICKETS)
-- ==========================================
-- Aktywne zgłoszenie hydrauliczne
INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, assigned_to_id, apartment_id)
SELECT 'ZGL/2026/001', 'Wyciek pod zlewem', 'Woda kapie spod syfonu w kuchni.', 'W_REALIZACJI',
(SELECT id FROM ticket_categories WHERE name = 'Hydraulika'),
(SELECT id FROM users WHERE email = 'jan.kowalski@gmail.com'),
(SELECT id FROM users WHERE email = 'hydraulik@blokur.pl'),
(SELECT a.id FROM apartments a JOIN user_apartments ua ON a.id = ua.apartment_id JOIN users u ON ua.user_id = u.id WHERE u.email = 'jan.kowalski@gmail.com');

-- Nowe zgłoszenie elektryczne (klatka)
INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, assigned_to_id, staircase_id)
SELECT 'ZGL/2026/002', 'Przepalona żarówka', 'Brak światła na 2. piętrze w klatce 1.', 'NOWE',
(SELECT id FROM ticket_categories WHERE name = 'Elektryka'),
(SELECT id FROM users WHERE email = 'anna.nowak@poczta.pl'),
(SELECT id FROM users WHERE email = 'elektryk@blokur.pl'),
(SELECT s.id FROM staircases s JOIN buildings b ON s.building_id = b.id WHERE b.name = 'Budynek A (Solaris)' AND s.label = 'Klatka 1');

-- Zakończone zgłoszenie
INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, building_id, closed_at)
SELECT 'ZGL/2026/003', 'Naprawa domofonu', 'Domofon przy wejściu głównym nie dzwonił.', 'ZAKONCZONE',
(SELECT id FROM ticket_categories WHERE name = 'Domofony i Monitoring'),
(SELECT id FROM users WHERE email = 'piotr.wisniewski@wp.pl'),
(SELECT id FROM buildings WHERE name = 'Rezydencja Parkowa'),
CURRENT_TIMESTAMP - INTERVAL '2 days';
