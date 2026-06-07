-- Zmiana typu komentarza z 'INTERNAL' na 'WEWNETRZNY' dla zapewnienia spójności słowników w istniejących bazach
UPDATE ticket_comments SET comment_type = 'WEWNETRZNY' WHERE comment_type = 'INTERNAL';
