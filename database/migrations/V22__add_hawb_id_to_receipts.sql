-- Agrega hawb_id (nullable) a warehouse_receipt y receipt_piece para
-- poder identificar qué recibo pertenece a qué HAWB y así actualizar
-- en lugar de insertar duplicados en cada corrección.
ALTER TABLE warehouse_receipt ADD COLUMN IF NOT EXISTS hawb_id UUID;
ALTER TABLE receipt_piece     ADD COLUMN IF NOT EXISTS hawb_id UUID;

CREATE INDEX IF NOT EXISTS idx_warehouse_receipt_mawb_hawb
    ON warehouse_receipt(mawb_id, hawb_id);

CREATE INDEX IF NOT EXISTS idx_receipt_piece_hawb
    ON receipt_piece(hawb_id);
