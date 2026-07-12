-- Purge all operational data while preserving users, permissions, airlines, sites, and config.
-- This is a destructive migration: receipts, MAWBs, HAWBs, bookings, flights, ULDs, DUA records,
-- and audit logs are permanently deleted.

TRUNCATE TABLE receipt_piece CASCADE;
TRUNCATE TABLE dua_record CASCADE;
TRUNCATE TABLE uld_awb CASCADE;
TRUNCATE TABLE hawb CASCADE;
TRUNCATE TABLE warehouse_receipt CASCADE;
TRUNCATE TABLE booking CASCADE;
TRUNCATE TABLE mawb CASCADE;
TRUNCATE TABLE uld CASCADE;
TRUNCATE TABLE flight CASCADE;
TRUNCATE TABLE audit_log CASCADE;
