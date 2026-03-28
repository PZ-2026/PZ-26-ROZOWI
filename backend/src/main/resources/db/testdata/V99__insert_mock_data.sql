-- =============================================================================
-- V99__insert_mock_data.sql
-- DANE TESTOWE / MOCK DATA
--
-- UWAGA: Ten plik przeznaczony jest WYŁĄCZNIE dla środowisk dev i test.
-- Patrz: instrukcja izolacji profilowej w dokumentacji projektu.
--
-- Algorytm hashowania: BCrypt (strength=10) — BCryptPasswordEncoder z Spring Security
-- Hasło dla wszystkich użytkowników: "Haslo123"
-- Hash wygenerowany przez BCryptPasswordEncoder().encode("Haslo123"):
--   $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
-- =============================================================================

-- =============================================================================
-- 1. UŻYTKOWNICY
-- =============================================================================
INSERT INTO users (email, password_hash, first_name, last_name, role, is_active) VALUES
    -- Zarządcy
    ('zarzadca.kowalczyk@blokur.pl',   '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Tomasz',   'Kowalczyk',    'ZARZADCA',    true),
    ('zarzadca.mazur@blokur.pl',       '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Dorota',   'Mazur',        'ZARZADCA',    true),
    -- Konserwatorzy
    ('hydraulik.wrona@blokur.pl',      '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Mariusz',  'Wrona',        'KONSERWATOR', true),
    ('elektryk.czajka@blokur.pl',      '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Zygmunt',  'Czajka',       'KONSERWATOR', true),
    ('serwis.baran@blokur.pl',         '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Łukasz',   'Baran',        'KONSERWATOR', true),
    -- Mieszkańcy
    ('m.zielinska@gmail.com',          '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Magdalena','Zielińska',    'MIESZKANIEC', true),
    ('k.szymanski@wp.pl',              '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Kamil',    'Szymański',    'MIESZKANIEC', true),
    ('a.wieczorek@poczta.pl',          '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Agnieszka','Wieczorek',    'MIESZKANIEC', true),
    ('p.kubiak@onet.pl',               '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Paweł',    'Kubiak',       'MIESZKANIEC', true),
    ('e.wojcik@interia.pl',            '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Elżbieta', 'Wójcik',       'MIESZKANIEC', true),
    -- Nieaktywny mieszkaniec (do testów ograniczeń)
    ('b.krol@wp.pl',                   '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Bartosz',  'Król',         'MIESZKANIEC', false);

-- =============================================================================
-- 2. KATEGORIE ZGŁOSZEŃ
-- (Idempotentne — nie wstawiamy jeśli nazwy już istnieją)
-- =============================================================================
INSERT INTO ticket_categories (name) VALUES
    ('Hydraulika'),
    ('Elektryka'),
    ('Domofony i Monitoring'),
    ('Czystość i Porządek'),
    ('Administracja'),
    ('Remonty i Modernizacje'),
    ('Winda'),
    ('Inne')
ON CONFLICT (name) DO NOTHING;

-- =============================================================================
-- 3. BUDYNKI
-- =============================================================================
INSERT INTO buildings (estate_name, name, address, latitude, longitude) VALUES
    ('Osiedle Północne', 'Budynek 1 (Aquila)',   'ul. Orla 2, 35-001 Rzeszów',       50.041187, 21.999121),
    ('Osiedle Północne', 'Budynek 2 (Cygnus)',   'ul. Orla 4, 35-001 Rzeszów',       50.041305, 21.999450),
    ('Wspólnota Zachodnia', 'Blok Pod Kasztanem','ul. Kasztanowa 8, 35-215 Rzeszów', 50.036000, 21.982000);

-- =============================================================================
-- 4. KLATKI
-- =============================================================================
INSERT INTO staircases (building_id, label)
SELECT id, klatka FROM buildings
    CROSS JOIN (VALUES ('Klatka A'), ('Klatka B')) AS k(klatka)
WHERE name IN ('Budynek 1 (Aquila)', 'Budynek 2 (Cygnus)');

INSERT INTO staircases (building_id, label)
SELECT id, 'Wejście Główne' FROM buildings WHERE name = 'Blok Pod Kasztanem';

-- =============================================================================
-- 5. LOKALE (APARTMENTS)
-- =============================================================================
-- Budynek 1, Klatka A  → nr 1–4
INSERT INTO apartments (staircase_id, number, current_balance)
SELECT s.id, a.nr, a.bal
FROM staircases s
JOIN buildings b ON s.building_id = b.id
CROSS JOIN (VALUES ('1', 320.00::DECIMAL), ('2', -45.50::DECIMAL), ('3', 0.00::DECIMAL), ('4', 1200.00::DECIMAL)) AS a(nr, bal)
WHERE b.name = 'Budynek 1 (Aquila)' AND s.label = 'Klatka A';

-- Budynek 1, Klatka B  → nr 5–8
INSERT INTO apartments (staircase_id, number, current_balance)
SELECT s.id, a.nr, a.bal
FROM staircases s
JOIN buildings b ON s.building_id = b.id
CROSS JOIN (VALUES ('5', 0.00::DECIMAL), ('6', -220.00::DECIMAL), ('7', 80.00::DECIMAL), ('8', 0.00::DECIMAL)) AS a(nr, bal)
WHERE b.name = 'Budynek 1 (Aquila)' AND s.label = 'Klatka B';

-- Budynek 2, Klatka A  → nr 1–3
INSERT INTO apartments (staircase_id, number, current_balance)
SELECT s.id, a.nr, a.bal
FROM staircases s
JOIN buildings b ON s.building_id = b.id
CROSS JOIN (VALUES ('1', 500.00::DECIMAL), ('2', 0.00::DECIMAL), ('3', -99.99::DECIMAL)) AS a(nr, bal)
WHERE b.name = 'Budynek 2 (Cygnus)' AND s.label = 'Klatka A';

-- Blok Pod Kasztanem → nr 10–12
INSERT INTO apartments (staircase_id, number, current_balance)
SELECT s.id, a.nr, a.bal
FROM staircases s
JOIN buildings b ON s.building_id = b.id
CROSS JOIN (VALUES ('10', 0.00::DECIMAL), ('11', 150.00::DECIMAL), ('12', -600.00::DECIMAL)) AS a(nr, bal)
WHERE b.name = 'Blok Pod Kasztanem' AND s.label = 'Wejście Główne';

-- =============================================================================
-- 6. PRZYPISANIE MIESZKAŃCÓW DO LOKALI
-- =============================================================================
-- Magdalena Zielińska  → Budynek 1, Klatka A, nr 1
INSERT INTO user_apartments (user_id, apartment_id)
SELECT u.id, a.id
FROM users u, apartments a, staircases s, buildings b
WHERE u.email = 'm.zielinska@gmail.com'
  AND a.number = '1' AND a.staircase_id = s.id AND s.label = 'Klatka A'
  AND s.building_id = b.id AND b.name = 'Budynek 1 (Aquila)';

-- Kamil Szymański → Budynek 1, Klatka B, nr 5
INSERT INTO user_apartments (user_id, apartment_id)
SELECT u.id, a.id
FROM users u, apartments a, staircases s, buildings b
WHERE u.email = 'k.szymanski@wp.pl'
  AND a.number = '5' AND a.staircase_id = s.id AND s.label = 'Klatka B'
  AND s.building_id = b.id AND b.name = 'Budynek 1 (Aquila)';

-- Agnieszka Wieczorek → Budynek 2, Klatka A, nr 2
INSERT INTO user_apartments (user_id, apartment_id)
SELECT u.id, a.id
FROM users u, apartments a, staircases s, buildings b
WHERE u.email = 'a.wieczorek@poczta.pl'
  AND a.number = '2' AND a.staircase_id = s.id AND s.label = 'Klatka A'
  AND s.building_id = b.id AND b.name = 'Budynek 2 (Cygnus)';

-- Paweł Kubiak → Blok Pod Kasztanem, nr 11
INSERT INTO user_apartments (user_id, apartment_id)
SELECT u.id, a.id
FROM users u, apartments a, staircases s, buildings b
WHERE u.email = 'p.kubiak@onet.pl'
  AND a.number = '11' AND a.staircase_id = s.id AND s.label = 'Wejście Główne'
  AND s.building_id = b.id AND b.name = 'Blok Pod Kasztanem';

-- Elżbieta Wójcik → Budynek 1, Klatka A, nr 3
INSERT INTO user_apartments (user_id, apartment_id)
SELECT u.id, a.id
FROM users u, apartments a, staircases s, buildings b
WHERE u.email = 'e.wojcik@interia.pl'
  AND a.number = '3' AND a.staircase_id = s.id AND s.label = 'Klatka A'
  AND s.building_id = b.id AND b.name = 'Budynek 1 (Aquila)';

-- =============================================================================
-- 7. ZGŁOSZENIA (TICKETS) — 15 rekordów, wszystkie statusy
-- Statusy: NOWE | W_REALIZACJI | ZAKONCZONE
-- =============================================================================

-- ────────────────────────────────────────────────────────
-- STATUS: NOWE (5 zgłoszeń)
-- ────────────────────────────────────────────────────────

-- ZGL/2026/101 — wyciek, mieszkanie, brak przypisania
INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, apartment_id)
SELECT
    'ZGL/2026/101',
    'Wyciek z rury pod wanną',
    'W łazience słychać kapanie. Pod wanną widoczna wilgoć na podłodze.',
    'NOWE',
    (SELECT id FROM ticket_categories WHERE name = 'Hydraulika'),
    (SELECT id FROM users WHERE email = 'm.zielinska@gmail.com'),
    (SELECT a.id FROM apartments a JOIN staircases s ON a.staircase_id = s.id JOIN buildings b ON s.building_id = b.id
     WHERE a.number = '1' AND s.label = 'Klatka A' AND b.name = 'Budynek 1 (Aquila)');

-- ZGL/2026/102 — uszkodzona skrzynka na listy, budynek
INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, building_id)
SELECT
    'ZGL/2026/102',
    'Zniszczona skrzynka pocztowa',
    'Skrzynka nr 7 jest wyrwana z mocowania i blokuje pozostałe.',
    'NOWE',
    (SELECT id FROM ticket_categories WHERE name = 'Administracja'),
    (SELECT id FROM users WHERE email = 'k.szymanski@wp.pl'),
    (SELECT id FROM buildings WHERE name = 'Budynek 1 (Aquila)');

-- ZGL/2026/103 — śmieci w klatce
INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, staircase_id)
SELECT
    'ZGL/2026/103',
    'Gruz porzucony na klatce schodowej',
    'Ktoś zostawił workowane odpady budowlane na półpiętrze między 1. a 2. piętrem.',
    'NOWE',
    (SELECT id FROM ticket_categories WHERE name = 'Czystość i Porządek'),
    (SELECT id FROM users WHERE email = 'a.wieczorek@poczta.pl'),
    (SELECT s.id FROM staircases s JOIN buildings b ON s.building_id = b.id
     WHERE s.label = 'Klatka A' AND b.name = 'Budynek 2 (Cygnus)');

-- ZGL/2026/104 — awaria windy, cały budynek
INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, building_id)
SELECT
    'ZGL/2026/104',
    'Winda nie działa — zatrzymuje się między piętrami',
    'Od wczoraj winda zatrzymuje się losowo i nie otwiera drzwi.',
    'NOWE',
    (SELECT id FROM ticket_categories WHERE name = 'Winda'),
    (SELECT id FROM users WHERE email = 'p.kubiak@onet.pl'),
    (SELECT id FROM buildings WHERE name = 'Blok Pod Kasztanem');

-- ZGL/2026/105 — przepalony bezpiecznik, mieszkanie
INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, apartment_id)
SELECT
    'ZGL/2026/105',
    'Brak prądu w całym mieszkaniu',
    'Po podłączeniu zmywarki wyskoczył bezpiecznik i nie można go ponownie wcisnąć.',
    'NOWE',
    (SELECT id FROM ticket_categories WHERE name = 'Elektryka'),
    (SELECT id FROM users WHERE email = 'e.wojcik@interia.pl'),
    (SELECT a.id FROM apartments a JOIN staircases s ON a.staircase_id = s.id JOIN buildings b ON s.building_id = b.id
     WHERE a.number = '3' AND s.label = 'Klatka A' AND b.name = 'Budynek 1 (Aquila)');

-- ────────────────────────────────────────────────────────
-- STATUS: W_REALIZACJI (5 zgłoszeń)
-- ────────────────────────────────────────────────────────

-- ZGL/2026/106 — awaria domofonu przypisana do elektryka
INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, assigned_to_id, building_id)
SELECT
    'ZGL/2026/106',
    'Domofon nie reaguje na dzwonek z zewnątrz',
    'Goście dzwonią, ale mieszkańcy nic nie słyszą. Problem dotyczy całego budynku.',
    'W_REALIZACJI',
    (SELECT id FROM ticket_categories WHERE name = 'Domofony i Monitoring'),
    (SELECT id FROM users WHERE email = 'm.zielinska@gmail.com'),
    (SELECT id FROM users WHERE email = 'elektryk.czajka@blokur.pl'),
    (SELECT id FROM buildings WHERE name = 'Budynek 1 (Aquila)');

-- ZGL/2026/107 — hydraulik pracuje nad wyciekiem z pionów
INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, assigned_to_id, staircase_id)
SELECT
    'ZGL/2026/107',
    'Wyciek z instalacji wodnej na klatce',
    'Na ścianie klatki widać zaciek i mokre plamy. Sączyć zaczęło po 2 dniach deszczu.',
    'W_REALIZACJI',
    (SELECT id FROM ticket_categories WHERE name = 'Hydraulika'),
    (SELECT id FROM users WHERE email = 'k.szymanski@wp.pl'),
    (SELECT id FROM users WHERE email = 'hydraulik.wrona@blokur.pl'),
    (SELECT s.id FROM staircases s JOIN buildings b ON s.building_id = b.id
     WHERE s.label = 'Klatka B' AND b.name = 'Budynek 1 (Aquila)');

-- ZGL/2026/108 — remont dachu, cały budynek
INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, assigned_to_id, building_id)
SELECT
    'ZGL/2026/108',
    'Naprawa przeciekającego dachu',
    'Zalanie strychu po obfitych opadach. Uszkodzona papa w sekcji zachodniej.',
    'W_REALIZACJI',
    (SELECT id FROM ticket_categories WHERE name = 'Remonty i Modernizacje'),
    (SELECT id FROM users WHERE email = 'a.wieczorek@poczta.pl'),
    (SELECT id FROM users WHERE email = 'serwis.baran@blokur.pl'),
    (SELECT id FROM buildings WHERE name = 'Budynek 2 (Cygnus)');

-- ZGL/2026/109 — czyszczenie piwnicy
INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, assigned_to_id, building_id)
SELECT
    'ZGL/2026/109',
    'Porzucone meble w piwnicy',
    'Część wspólna piwnicy jest zastawiona starymi meblami przez nieznanych lokatorów.',
    'W_REALIZACJI',
    (SELECT id FROM ticket_categories WHERE name = 'Czystość i Porządek'),
    (SELECT id FROM users WHERE email = 'p.kubiak@onet.pl'),
    (SELECT id FROM users WHERE email = 'serwis.baran@blokur.pl'),
    (SELECT id FROM buildings WHERE name = 'Blok Pod Kasztanem');

-- ZGL/2026/110 — remont oświetlenia klatki
INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, assigned_to_id, staircase_id)
SELECT
    'ZGL/2026/110',
    'Wymiana oświetlenia klatki na LED',
    'Trwa montaż nowych opraw. Chwilowe przerwy w dostępie do oświetlenia.',
    'W_REALIZACJI',
    (SELECT id FROM ticket_categories WHERE name = 'Elektryka'),
    (SELECT id FROM users WHERE email = 'e.wojcik@interia.pl'),
    (SELECT id FROM users WHERE email = 'elektryk.czajka@blokur.pl'),
    (SELECT s.id FROM staircases s JOIN buildings b ON s.building_id = b.id
     WHERE s.label = 'Klatka A' AND b.name = 'Budynek 1 (Aquila)');

-- ────────────────────────────────────────────────────────
-- STATUS: ZAKONCZONE (5 zgłoszeń)
-- ────────────────────────────────────────────────────────

-- ZGL/2026/111 — naprawa zamka
INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, assigned_to_id, apartment_id, closed_at)
SELECT
    'ZGL/2026/111',
    'Zepsuty zamek w drzwiach wejściowych do mieszkania',
    'Klucz nie obracał się w zamku. Zamek wymieniony przez konserwatora.',
    'ZAKONCZONE',
    (SELECT id FROM ticket_categories WHERE name = 'Administracja'),
    (SELECT id FROM users WHERE email = 'm.zielinska@gmail.com'),
    (SELECT id FROM users WHERE email = 'serwis.baran@blokur.pl'),
    (SELECT a.id FROM apartments a JOIN staircases s ON a.staircase_id = s.id JOIN buildings b ON s.building_id = b.id
     WHERE a.number = '1' AND s.label = 'Klatka A' AND b.name = 'Budynek 1 (Aquila)'),
    CURRENT_TIMESTAMP - INTERVAL '10 days';

-- ZGL/2026/112 — przepalona żarówka na podwórku
INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, assigned_to_id, building_id, closed_at)
SELECT
    'ZGL/2026/112',
    'Brak oświetlenia przed wejściem do budynku (lampa zewnętrzna)',
    'Lampa przy wejściu głównym nie świeciła. Wymieniono żarówkę i uszczelniono oprawę.',
    'ZAKONCZONE',
    (SELECT id FROM ticket_categories WHERE name = 'Elektryka'),
    (SELECT id FROM users WHERE email = 'k.szymanski@wp.pl'),
    (SELECT id FROM users WHERE email = 'elektryk.czajka@blokur.pl'),
    (SELECT id FROM buildings WHERE name = 'Budynek 1 (Aquila)'),
    CURRENT_TIMESTAMP - INTERVAL '5 days';

-- ZGL/2026/113 — pęknięta szklana tablica
INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, building_id, closed_at)
SELECT
    'ZGL/2026/113',
    'Pęknięta szyba w gablocie ogłoszeniowej',
    'Gablota na parterze miała pękniętą szybę. Wymieniono na nową.',
    'ZAKONCZONE',
    (SELECT id FROM ticket_categories WHERE name = 'Administracja'),
    (SELECT id FROM users WHERE email = 'a.wieczorek@poczta.pl'),
    (SELECT id FROM buildings WHERE name = 'Budynek 2 (Cygnus)'),
    CURRENT_TIMESTAMP - INTERVAL '3 days';

-- ZGL/2026/114 — awaria wodomierza
INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, assigned_to_id, apartment_id, closed_at)
SELECT
    'ZGL/2026/114',
    'Wodomierz wskazuje błędne odczyty',
    'Licznik przekręcał wskazanie mimo zamkniętego zaworu. Wodomierz wymieniony.',
    'ZAKONCZONE',
    (SELECT id FROM ticket_categories WHERE name = 'Hydraulika'),
    (SELECT id FROM users WHERE email = 'p.kubiak@onet.pl'),
    (SELECT id FROM users WHERE email = 'hydraulik.wrona@blokur.pl'),
    (SELECT a.id FROM apartments a JOIN staircases s ON a.staircase_id = s.id JOIN buildings b ON s.building_id = b.id
     WHERE a.number = '11' AND s.label = 'Wejście Główne' AND b.name = 'Blok Pod Kasztanem'),
    CURRENT_TIMESTAMP - INTERVAL '7 days';

-- ZGL/2026/115 — czyszczenie klatki po zalaniu
INSERT INTO tickets (ticket_number, title, description, status, category_id, author_id, assigned_to_id, staircase_id, closed_at)
SELECT
    'ZGL/2026/115',
    'Sprzątanie po awarii kanalizacji na klatce',
    'Wylanie ścieków na parterze klatki B. Usunięto przyczynę, klatka wydezynfekowana.',
    'ZAKONCZONE',
    (SELECT id FROM ticket_categories WHERE name = 'Czystość i Porządek'),
    (SELECT id FROM users WHERE email = 'e.wojcik@interia.pl'),
    (SELECT id FROM users WHERE email = 'hydraulik.wrona@blokur.pl'),
    (SELECT s.id FROM staircases s JOIN buildings b ON s.building_id = b.id
     WHERE s.label = 'Klatka B' AND b.name = 'Budynek 1 (Aquila)'),
    CURRENT_TIMESTAMP - INTERVAL '1 day';

-- =============================================================================
-- 8. HISTORIA ZGŁOSZEŃ (TICKET HISTORY) — przykładowe zmiany statusu
-- =============================================================================
-- Historia dla ZGL/2026/106 (W_REALIZACJI)
INSERT INTO ticket_history (ticket_id, status, changed_by, comment)
SELECT t.id, 'NOWE', u.id, 'Zgłoszenie zarejestrowane automatycznie.'
FROM tickets t, users u WHERE t.ticket_number = 'ZGL/2026/106' AND u.email = 'm.zielinska@gmail.com';

INSERT INTO ticket_history (ticket_id, status, changed_by, comment)
SELECT t.id, 'W_REALIZACJI', u.id, 'Przypisano do elektryka. Wizyta zaplanowana na jutro.'
FROM tickets t, users u WHERE t.ticket_number = 'ZGL/2026/106' AND u.email = 'zarzadca.kowalczyk@blokur.pl';

-- Historia dla ZGL/2026/111 (ZAKONCZONE)
INSERT INTO ticket_history (ticket_id, status, changed_by, comment)
SELECT t.id, 'NOWE', u.id, 'Zgłoszenie odebrane.'
FROM tickets t, users u WHERE t.ticket_number = 'ZGL/2026/111' AND u.email = 'm.zielinska@gmail.com';

INSERT INTO ticket_history (ticket_id, status, changed_by, comment)
SELECT t.id, 'W_REALIZACJI', u.id, 'Konserwator przyjedzie w ciągu 48h.'
FROM tickets t, users u WHERE t.ticket_number = 'ZGL/2026/111' AND u.email = 'zarzadca.mazur@blokur.pl';

INSERT INTO ticket_history (ticket_id, status, changed_by, comment)
SELECT t.id, 'ZAKONCZONE', u.id, 'Zamek wymieniony, problem rozwiązany. Zgłoszenie zamknięte.'
FROM tickets t, users u WHERE t.ticket_number = 'ZGL/2026/111' AND u.email = 'zarzadca.mazur@blokur.pl';

-- =============================================================================
-- KONIEC PLIKU
-- =============================================================================
