-- Zmiana typu transakcji z 'NALEZNOSC' na 'NALICZENIE' w celu zapewnienia spójności słowników
UPDATE financial_transactions SET type = 'NALICZENIE' WHERE type = 'NALEZNOSC';
