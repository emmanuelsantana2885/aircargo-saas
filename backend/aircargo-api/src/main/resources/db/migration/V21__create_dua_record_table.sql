CREATE TABLE IF NOT EXISTS dua_record (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    mawb_id UUID NOT NULL REFERENCES mawb(id) ON DELETE CASCADE,
    dua_number VARCHAR(50) NOT NULL,
    document_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    dua_date DATE,
    notes TEXT,
    customs_broker VARCHAR(150),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_dua_record_mawb ON dua_record(mawb_id);
CREATE INDEX IF NOT EXISTS idx_dua_record_status ON dua_record(status);

-- Insert new view permissions for EXPORTS, TRACKING, COMPLIANCE
INSERT INTO view_permission (code, name, description, category) VALUES
    ('EXPORTS', 'Exportación de Datos', 'Exportación de datos operativos a CSV/Excel', 'ADMINISTRACION'),
    ('TRACKING', 'Tracking de MAWBs', 'Seguimiento y trazabilidad de conocimientos aéreos', 'PRINCIPAL'),
    ('COMPLIANCE', 'Cumplimiento DGA', 'Registro y gestión de DUAs para cumplimiento aduanal', 'OPERACIONES'),
    ('INTEGRATIONS', 'Integraciones', 'Conexiones e integraciones con sistemas externos', 'CONFIGURACION')
ON CONFLICT (code) DO NOTHING;

-- Assign to SUPER_USER
DO $$
DECLARE
    v_id UUID;
BEGIN
    FOR v_id IN SELECT id FROM view_permission WHERE code IN ('EXPORTS', 'TRACKING', 'COMPLIANCE', 'INTEGRATIONS') LOOP
        INSERT INTO role_permission (role, view_permission_id, can_access)
        VALUES ('SUPER_USER', v_id, TRUE)
        ON CONFLICT (role, view_permission_id) DO UPDATE SET can_access = TRUE;
    END LOOP;
END $$;

-- Also assign TRACKING and COMPLIANCE to ADMIN (read-only or full)
DO $$
DECLARE
    v_id UUID;
BEGIN
    FOR v_id IN SELECT id FROM view_permission WHERE code IN ('TRACKING', 'COMPLIANCE') LOOP
        INSERT INTO role_permission (role, view_permission_id, can_access)
        VALUES ('ADMIN', v_id, TRUE)
        ON CONFLICT (role, view_permission_id) DO UPDATE SET can_access = TRUE;
    END LOOP;
END $$;
