-- Add must_change_password column
-- New users will be forced to change their password on first login
ALTER TABLE app_user ADD COLUMN must_change_password BOOLEAN DEFAULT TRUE;

-- Existing users already have passwords, so they don't need to change
UPDATE app_user SET must_change_password = FALSE WHERE password_hash IS NOT NULL;
