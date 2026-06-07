-- Korygowanie statusu zmockowanego zgłoszenia elektrycznego, aby zgadzał się z regułami domenowymi (przypisany konserwator = status ZAPLANOWANO)
UPDATE tickets
SET status = 'ZAPLANOWANO'
WHERE ticket_number = 'ZGL/2026/002' AND status = 'NOWE';
