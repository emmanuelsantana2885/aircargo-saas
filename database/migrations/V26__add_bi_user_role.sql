-- BI_USER role: read-only access to /api/bi/** endpoints
-- No DDL needed — role column is varchar(50) and accepts any value.
-- The UserRole enum in Java enforces valid values at the application layer.

-- Seed BI_USER (read-only access to BI endpoints)
INSERT INTO app_user (id, airline_id, email, full_name, role, is_active, created_at, updated_at)
SELECT gen_random_uuid(), id, 'bi@rannik.com', 'BI User', 'BI_USER', true, now(), now()
FROM airline WHERE code = 'UPS'
AND NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'bi@rannik.com');

-- Assign BI_USER to SDQ site
INSERT INTO user_sites (user_id, site_id)
SELECT u.id, s.id
FROM app_user u
CROSS JOIN site s
WHERE u.email = 'bi@rannik.com' AND s.code = 'SDQ'
ON CONFLICT DO NOTHING;
