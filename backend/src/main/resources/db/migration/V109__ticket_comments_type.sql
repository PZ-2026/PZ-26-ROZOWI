ALTER TABLE ticket_comments
ADD COLUMN comment_type VARCHAR(20) DEFAULT 'PUBLICZNY';

UPDATE ticket_comments
SET comment_type = 'PUBLICZNY'
WHERE comment_type IS NULL;

ALTER TABLE ticket_comments
ALTER COLUMN comment_type SET NOT NULL;
