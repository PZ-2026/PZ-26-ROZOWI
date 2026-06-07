ALTER TABLE announcements
ADD COLUMN target_type VARCHAR(20) DEFAULT 'WSZYSCY';

UPDATE announcements
SET target_type = CASE
    WHEN target_building_id IS NOT NULL THEN 'BUDYNEK'
    WHEN target_staircase_id IS NOT NULL THEN 'KLATKA'
    WHEN target_apartment_id IS NOT NULL THEN 'NIERUCHOMOSC'
    ELSE 'WSZYSCY'
END;

ALTER TABLE announcements
ALTER COLUMN target_type SET NOT NULL;

ALTER TABLE announcements
ADD COLUMN attachment_path VARCHAR(500);
