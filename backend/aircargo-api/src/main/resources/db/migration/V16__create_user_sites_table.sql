CREATE TABLE IF NOT EXISTS user_sites (
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    site_id UUID NOT NULL REFERENCES site(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, site_id)
);

-- Assign all existing users to SDQ site
INSERT INTO user_sites (user_id, site_id)
SELECT u.id, s.id
FROM app_user u
CROSS JOIN site s
WHERE s.code = 'SDQ'
ON CONFLICT DO NOTHING;
