CREATE TABLE IF NOT EXISTS view_permission (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    category VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS role_permission (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    role VARCHAR(50) NOT NULL,
    view_permission_id UUID NOT NULL REFERENCES view_permission(id) ON DELETE CASCADE,
    can_access BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(role, view_permission_id)
);

CREATE INDEX IF NOT EXISTS idx_role_permission_role ON role_permission(role);
CREATE INDEX IF NOT EXISTS idx_role_permission_view ON role_permission(view_permission_id);

-- Seed views (transaction master data)
INSERT INTO view_permission (code, name, description, category) VALUES
    ('DASHBOARD', 'Dashboard', 'Panel principal con resumen de operaciones', 'PRINCIPAL'),
    ('BOOKINGS', 'Bookings', 'Gestión de reservas y bookings', 'PRINCIPAL'),
    ('RECEIPTS', 'Recibos de Almacén', 'Emisión y consulta de recibos de bodega', 'PRINCIPAL'),
    ('FLIGHTS', 'Vuelos', 'Administración de vuelos y programación', 'PRINCIPAL'),
    ('MAWBS', 'MAWBs', 'Gestión de conocimientos aéreos maestros', 'PRINCIPAL'),
    ('LOAD_PLANNING', 'Load Planning', 'Planificación de carga y distribución', 'PRINCIPAL'),
    ('ULDS', 'ULDs / Pallet Sheets', 'Administración de contenedores y pallets', 'PRINCIPAL'),
    ('HAWBS', 'HAWBs', 'Gestión de conocimientos aéreos hijos', 'OPERACIONES'),
    ('AIRLINES', 'Aerolíneas', 'Administración de líneas aéreas y compañías', 'OPERACIONES'),
    ('RAMP_MANIFEST', 'Manifiesto de Rampa', 'Manifiesto de carga para operaciones de rampa', 'OPERACIONES'),
    ('DIM_FACTOR', 'Factor Dimensional', 'Configuración del factor dimensional para cálculos', 'CONFIGURACION'),
    ('ULD_TYPE_CONFIG', 'Tipos de ULD', 'Configuración de tipos de contenedores y pallets', 'CONFIGURACION'),
    ('USERS', 'Usuarios', 'Gestión de usuarios del sistema', 'ADMINISTRACION'),
    ('AUDIT_LOG', 'Auditoría', 'Consulta de bitácora de transacciones del sistema', 'ADMINISTRACION'),
    ('ROLES', 'Roles y Permisos', 'Administración de roles y permisos de acceso', 'ADMINISTRACION'),
    ('SITES', 'Sitios / Aeropuertos', 'Administración de códigos de sitio y aeropuertos', 'ADMINISTRACION'),
    ('SETTINGS', 'Configuración Global', 'Configuración general del sistema y parámetros', 'ADMINISTRACION'),
    ('REPORTS', 'Reportes', 'Generación y exportación de reportes operativos', 'OPERACIONES');

-- Helper: assign a view to a role
CREATE OR REPLACE FUNCTION assign_view(p_role VARCHAR, p_view_code VARCHAR)
RETURNS VOID AS $$
BEGIN
    INSERT INTO role_permission (role, view_permission_id, can_access)
    SELECT p_role, id, TRUE FROM view_permission WHERE code = p_view_code
    ON CONFLICT (role, view_permission_id) DO UPDATE SET can_access = TRUE;
END;
$$ LANGUAGE plpgsql;

-- ── READ_ONLY: all views (GET-only at HTTP level) ──
SELECT assign_view('READ_ONLY', 'DASHBOARD');
SELECT assign_view('READ_ONLY', 'BOOKINGS');
SELECT assign_view('READ_ONLY', 'RECEIPTS');
SELECT assign_view('READ_ONLY', 'FLIGHTS');
SELECT assign_view('READ_ONLY', 'MAWBS');
SELECT assign_view('READ_ONLY', 'LOAD_PLANNING');
SELECT assign_view('READ_ONLY', 'ULDS');
SELECT assign_view('READ_ONLY', 'HAWBS');
SELECT assign_view('READ_ONLY', 'AIRLINES');
SELECT assign_view('READ_ONLY', 'RAMP_MANIFEST');
SELECT assign_view('READ_ONLY', 'DIM_FACTOR');
SELECT assign_view('READ_ONLY', 'ULD_TYPE_CONFIG');
SELECT assign_view('READ_ONLY', 'REPORTS');

-- ── WAREHOUSE_ASSISTANT: Dashboard + Receipts ──
SELECT assign_view('WAREHOUSE_ASSISTANT', 'DASHBOARD');
SELECT assign_view('WAREHOUSE_ASSISTANT', 'RECEIPTS');

-- ── OPERATIONS: Dashboard, Flights, MAWBs, Load Planning, ULDs ──
SELECT assign_view('OPERATIONS', 'DASHBOARD');
SELECT assign_view('OPERATIONS', 'FLIGHTS');
SELECT assign_view('OPERATIONS', 'MAWBS');
SELECT assign_view('OPERATIONS', 'LOAD_PLANNING');
SELECT assign_view('OPERATIONS', 'ULDS');
SELECT assign_view('OPERATIONS', 'HAWBS');
SELECT assign_view('OPERATIONS', 'AIRLINES');
SELECT assign_view('OPERATIONS', 'RAMP_MANIFEST');
SELECT assign_view('OPERATIONS', 'REPORTS');

-- ── TRAFFIC: Dashboard, Bookings, MAWBs, Load Planning, ULDs ──
SELECT assign_view('TRAFFIC', 'DASHBOARD');
SELECT assign_view('TRAFFIC', 'BOOKINGS');
SELECT assign_view('TRAFFIC', 'MAWBS');
SELECT assign_view('TRAFFIC', 'LOAD_PLANNING');
SELECT assign_view('TRAFFIC', 'ULDS');
SELECT assign_view('TRAFFIC', 'HAWBS');
SELECT assign_view('TRAFFIC', 'AIRLINES');
SELECT assign_view('TRAFFIC', 'REPORTS');

-- ── LOAD_PLANNER: Dashboard, Flights, Load Planning, ULDs ──
SELECT assign_view('LOAD_PLANNER', 'DASHBOARD');
SELECT assign_view('LOAD_PLANNER', 'FLIGHTS');
SELECT assign_view('LOAD_PLANNER', 'LOAD_PLANNING');
SELECT assign_view('LOAD_PLANNER', 'ULDS');
SELECT assign_view('LOAD_PLANNER', 'ULD_TYPE_CONFIG');
SELECT assign_view('LOAD_PLANNER', 'REPORTS');

-- ── ADMIN: all except Settings ──
SELECT assign_view('ADMIN', 'DASHBOARD');
SELECT assign_view('ADMIN', 'BOOKINGS');
SELECT assign_view('ADMIN', 'RECEIPTS');
SELECT assign_view('ADMIN', 'FLIGHTS');
SELECT assign_view('ADMIN', 'MAWBS');
SELECT assign_view('ADMIN', 'LOAD_PLANNING');
SELECT assign_view('ADMIN', 'ULDS');
SELECT assign_view('ADMIN', 'HAWBS');
SELECT assign_view('ADMIN', 'AIRLINES');
SELECT assign_view('ADMIN', 'RAMP_MANIFEST');
SELECT assign_view('ADMIN', 'DIM_FACTOR');
SELECT assign_view('ADMIN', 'ULD_TYPE_CONFIG');
SELECT assign_view('ADMIN', 'USERS');
SELECT assign_view('ADMIN', 'AUDIT_LOG');
SELECT assign_view('ADMIN', 'SITES');
SELECT assign_view('ADMIN', 'REPORTS');

-- ── SUPER_USER: all views ──
SELECT assign_view('SUPER_USER', 'DASHBOARD');
SELECT assign_view('SUPER_USER', 'BOOKINGS');
SELECT assign_view('SUPER_USER', 'RECEIPTS');
SELECT assign_view('SUPER_USER', 'FLIGHTS');
SELECT assign_view('SUPER_USER', 'MAWBS');
SELECT assign_view('SUPER_USER', 'LOAD_PLANNING');
SELECT assign_view('SUPER_USER', 'ULDS');
SELECT assign_view('SUPER_USER', 'HAWBS');
SELECT assign_view('SUPER_USER', 'AIRLINES');
SELECT assign_view('SUPER_USER', 'RAMP_MANIFEST');
SELECT assign_view('SUPER_USER', 'DIM_FACTOR');
SELECT assign_view('SUPER_USER', 'ULD_TYPE_CONFIG');
SELECT assign_view('SUPER_USER', 'USERS');
SELECT assign_view('SUPER_USER', 'AUDIT_LOG');
SELECT assign_view('SUPER_USER', 'ROLES');
SELECT assign_view('SUPER_USER', 'SITES');
SELECT assign_view('SUPER_USER', 'SETTINGS');
SELECT assign_view('SUPER_USER', 'REPORTS');

DROP FUNCTION IF EXISTS assign_view;
