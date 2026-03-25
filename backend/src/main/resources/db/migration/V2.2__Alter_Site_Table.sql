ALTER TABLE adoption_site DROP COLUMN contact_info;
ALTER TABLE adoption_site ADD COLUMN phone VARCHAR(12) NULL;
ALTER TABLE adoption_site ADD COLUMN email VARCHAR(255) NULL;
ALTER TABLE adoption_site RENAME COLUMN location TO url;
ALTER TABLE adoption_site MODIFY COLUMN url VARCHAR(255) NOT NULL;
ALTER TABLE adoption_site ADD COLUMN status CHAR(1) NOT NULL;
