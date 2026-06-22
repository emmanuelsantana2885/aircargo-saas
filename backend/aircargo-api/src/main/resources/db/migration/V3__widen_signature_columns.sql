ALTER TABLE warehouse_receipt
    ALTER COLUMN dock_signature         TYPE text,
    ALTER COLUMN delivered_by_sig_url   TYPE text,
    ALTER COLUMN received_by_sig_url    TYPE text,
    ALTER COLUMN broker_sig_url         TYPE text;
