-- Remove site restrictions for esantana@rannik.com (SuperUser with all-sites access)
DELETE FROM user_sites
WHERE user_id = (SELECT id FROM app_user WHERE email = 'esantana@rannik.com');
