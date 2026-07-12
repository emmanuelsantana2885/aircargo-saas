-- Seed flights
INSERT INTO flight (id, flight_number, origin, destination, flight_date, status, aircraft_type, airline_id, created_at, updated_at)
VALUES
  ('a1b2c3d4-0001-4000-8000-000000000001', '5X101', 'SDQ', 'MIA', '2026-07-01', 'SCHEDULED', 'B767', '00000000-0000-0000-0000-000000000001', NOW(), NOW()),
  ('a1b2c3d4-0002-4000-8000-000000000002', '5X202', 'SDQ', 'SDF', '2026-07-01', 'SCHEDULED', 'B767', '00000000-0000-0000-0000-000000000001', NOW(), NOW()),
  ('a1b2c3d4-0003-4000-8000-000000000003', '5X303', 'MIA', 'SDQ', '2026-07-01', 'SCHEDULED', 'B757', '00000000-0000-0000-0000-000000000001', NOW(), NOW()),
  ('a1b2c3d4-0004-4000-8000-000000000004', 'FX801', 'SDQ', 'MEM', '2026-07-02', 'SCHEDULED', 'B777', '00000000-0000-0000-0000-000000000002', NOW(), NOW()),
  ('a1b2c3d4-0005-4000-8000-000000000005', '5X505', 'SDQ', 'MIA', '2026-07-02', 'SCHEDULED', 'B767', '00000000-0000-0000-0000-000000000001', NOW(), NOW());

-- Seed MAWBs
INSERT INTO mawb (id, awb_number, origin, destination, pieces, reported_weight_kg, commodity_type, status, airline_id, shipper_name, consignee_name, created_at, updated_at)
VALUES
  ('b1c2d3e4-0001-4000-8000-000000000001', '5X-001', 'SDQ', 'MIA', 150, 1200.50, 'GENERAL', 'BOOKED', '00000000-0000-0000-0000-000000000001', 'Exportadora ABC SRL', 'Miami Cargo Inc', NOW(), NOW()),
  ('b1c2d3e4-0002-4000-8000-000000000002', '5X-002', 'SDQ', 'MIA', 200, 2500.00, 'GENERAL', 'BOOKED', '00000000-0000-0000-0000-000000000001', 'Textiles Dominicanos SA', 'Fashion Forward LLC', NOW(), NOW()),
  ('b1c2d3e4-0003-4000-8000-000000000003', '5X-003', 'SDQ', 'SDF', 80, 900.75, 'PERISHABLE', 'RECEIVED', '00000000-0000-0000-0000-000000000001', 'Agropecuaria del Caribe', 'Logistics Hub Inc', NOW(), NOW()),
  ('b1c2d3e4-0004-4000-8000-000000000004', '5X-004', 'SDQ', 'MIA', 300, 3500.00, 'ELECTRONICS', 'BOOKED', '00000000-0000-0000-0000-000000000001', 'Electrónica del Este', 'Tech Distributors LLC', NOW(), NOW()),
  ('b1c2d3e4-0005-4000-8000-000000000005', 'FX-001', 'SDQ', 'MEM', 120, 1800.00, 'GENERAL', 'BOOKED', '00000000-0000-0000-0000-000000000002', 'Farmacéutica Nacional', 'Pharma Logistics Corp', NOW(), NOW());

-- Seed bookings
INSERT INTO booking (id, client_name, destination, commodity_type, contact_name, skids, confirmed_kg, reserved_kg, is_confirmed, flight_id, airline_id, mawb_id, created_at, updated_at)
VALUES
  ('c1d2e3f4-0001-4000-8000-000000000001', 'Exportadora ABC SRL', 'MIA', 'GENERAL', 'Juan Perez', 150, 1200.50, 0, true, 'a1b2c3d4-0001-4000-8000-000000000001', '00000000-0000-0000-0000-000000000001', 'b1c2d3e4-0001-4000-8000-000000000001', NOW(), NOW()),
  ('c1d2e3f4-0002-4000-8000-000000000002', 'Textiles Dominicanos SA', 'MIA', 'GENERAL', 'Maria Gomez', 200, 2500.00, 0, true, 'a1b2c3d4-0001-4000-8000-000000000001', '00000000-0000-0000-0000-000000000001', 'b1c2d3e4-0002-4000-8000-000000000002', NOW(), NOW()),
  ('c1d2e3f4-0003-4000-8000-000000000003', 'Agropecuaria del Caribe', 'SDF', 'PERISHABLE', 'Carlos Jimenez', 80, 900.75, 0, true, 'a1b2c3d4-0002-4000-8000-000000000002', '00000000-0000-0000-0000-000000000001', 'b1c2d3e4-0003-4000-8000-000000000003', NOW(), NOW()),
  ('c1d2e3f4-0004-4000-8000-000000000004', 'Electrónica del Este', 'MIA', 'ELECTRONICS', 'Pedro Martinez', 300, 3500.00, 0, true, 'a1b2c3d4-0005-4000-8000-000000000005', '00000000-0000-0000-0000-000000000001', 'b1c2d3e4-0004-4000-8000-000000000004', NOW(), NOW()),
  ('c1d2e3f4-0005-4000-8000-000000000005', 'Farmacéutica Nacional', 'MEM', 'GENERAL', 'Ana Rodriguez', 120, 1800.00, 0, false, 'a1b2c3d4-0004-4000-8000-000000000004', '00000000-0000-0000-0000-000000000002', 'b1c2d3e4-0005-4000-8000-000000000005', NOW(), NOW());
