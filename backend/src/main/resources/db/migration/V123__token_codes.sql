-- Przejście z linków na 6-cyfrowe kody zaproszenia / resetu hasła.
-- Kody nie są globalnie unikalne (znajdowane po (user_id, token)),
-- więc usuwamy unikalność kolumny token.

ALTER TABLE password_reset_tokens DROP CONSTRAINT IF EXISTS password_reset_tokens_token_key;
ALTER TABLE invitation_tokens     DROP CONSTRAINT IF EXISTS invitation_tokens_token_key;

-- Stare tokeny (UUID-y wysłane mailem jako linki) nie będą już akceptowane.
DELETE FROM password_reset_tokens;
DELETE FROM invitation_tokens;
