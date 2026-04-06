
ALTER TABLE estates
    ADD COLUMN IF NOT EXISTS address VARCHAR(255),
    ADD COLUMN IF NOT EXISTS nip VARCHAR(20),
    ADD COLUMN IF NOT EXISTS contact_info TEXT,
    ADD COLUMN IF NOT EXISTS logo_url VARCHAR(255);

ALTER TABLE apartments
    ADD COLUMN IF NOT EXISTS floor VARCHAR(50);

ALTER TABLE tickets
ALTER COLUMN planned_visit_date TYPE TIMESTAMP;

ALTER TABLE user_apartments
DROP CONSTRAINT IF EXISTS user_apartments_user_id_apartment_id_key,
ADD CONSTRAINT uq_user_apartments_user_id UNIQUE (user_id);


ALTER TABLE users
    ADD COLUMN IF NOT EXISTS phone_number VARCHAR(20);

ALTER TABLE resolutions
    ADD COLUMN IF NOT EXISTS estate_id UUID REFERENCES estates(id) ON DELETE CASCADE;

UPDATE resolutions r
SET estate_id = b.estate_id
    FROM buildings b
WHERE r.building_id = b.id AND r.estate_id IS NULL;

ALTER TABLE resolutions
DROP CONSTRAINT IF EXISTS resolutions_building_id_fkey,
    ALTER COLUMN estate_id SET NOT NULL,
    DROP COLUMN IF EXISTS building_id;

ALTER TABLE tickets
    ADD COLUMN IF NOT EXISTS resolution_summary TEXT;
