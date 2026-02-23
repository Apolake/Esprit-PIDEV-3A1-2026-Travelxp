USE travelxp;

-- Add duration to booking table and make trip/service nullable if they weren't
ALTER TABLE booking 
ADD COLUMN IF NOT EXISTS duration INT DEFAULT 1,
MODIFY COLUMN trip_id INT NULL,
MODIFY COLUMN service_id INT NULL;

-- Ensure role is present in users
-- (Already handled by role_migration.sql but good to keep in mind)
-- ALTER TABLE users MODIFY COLUMN role ENUM('USER','ADMIN') DEFAULT 'USER';
