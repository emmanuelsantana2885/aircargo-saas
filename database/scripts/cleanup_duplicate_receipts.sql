-- ============================================================
-- Script de limpieza: des-duplicación de recibos de bodega
-- ============================================================
-- 
-- Problema: Cuando un MAWB tenía HAWBs, cada corrección de piezas
-- generaba un nuevo recibo (POST /emit) en vez de actualizar el
-- anterior. Esto acumulaba filas basura en warehouse_receipt y
-- receipt_piece, inflando los totales del MAWB.
--
-- Solución:
-- 1. Inferir hawb_id para recibos existentes que mencionan un HAWB
-- 2. Eliminar recibos duplicados (conservar el más reciente por
--    combinación mawb_id + hawb_id)
-- 3. Recalcular totales de los MAWB afectados
--
-- Ejecutar SOLO después de aplicar la migración V22.
-- ============================================================

BEGIN;

-- ──────────────────────────────────────────
-- Paso 1: Inferir hawb_id para recibos que
--          tienen referencias a HAWB en remarks
-- ──────────────────────────────────────────
UPDATE warehouse_receipt wr
SET hawb_id = h.id
FROM hawb h
WHERE wr.mawb_id = h.mawb_id
  AND wr.hawb_id IS NULL
  AND wr.remarks IS NOT NULL
  AND wr.remarks != ''
  AND wr.remarks LIKE '%' || h.hawb_number || '%';

-- ──────────────────────────────────────────
-- Paso 2: Eliminar piezas de recibos duplicados
--          por HAWB (conservar solo el más reciente)
-- ──────────────────────────────────────────
DELETE FROM receipt_piece
WHERE receipt_id IN (
    SELECT id FROM (
        SELECT id,
               ROW_NUMBER() OVER (
                   PARTITION BY mawb_id, hawb_id
                   ORDER BY created_at DESC
               ) AS rn
        FROM warehouse_receipt
        WHERE hawb_id IS NOT NULL
    ) dup
    WHERE dup.rn > 1
);

-- ──────────────────────────────────────────
-- Paso 3: Eliminar los recibos duplicados
--          por HAWB (después de sus piezas)
-- ──────────────────────────────────────────
DELETE FROM warehouse_receipt
WHERE id IN (
    SELECT id FROM (
        SELECT id,
               ROW_NUMBER() OVER (
                   PARTITION BY mawb_id, hawb_id
                   ORDER BY created_at DESC
               ) AS rn
        FROM warehouse_receipt
        WHERE hawb_id IS NOT NULL
    ) dup
    WHERE dup.rn > 1
);

-- ──────────────────────────────────────────
-- Paso 4: Eliminar piezas de recibos generales
--          duplicados (hawb_id IS NULL)
-- ──────────────────────────────────────────
DELETE FROM receipt_piece
WHERE receipt_id IN (
    SELECT id FROM (
        SELECT id,
               ROW_NUMBER() OVER (
                   PARTITION BY mawb_id
                   ORDER BY created_at DESC
               ) AS rn
        FROM warehouse_receipt
        WHERE hawb_id IS NULL
    ) dup
    WHERE dup.rn > 1
);

-- ──────────────────────────────────────────
-- Paso 5: Eliminar los recibos generales
--          duplicados
-- ──────────────────────────────────────────
DELETE FROM warehouse_receipt
WHERE id IN (
    SELECT id FROM (
        SELECT id,
               ROW_NUMBER() OVER (
                   PARTITION BY mawb_id
                   ORDER BY created_at DESC
               ) AS rn
        FROM warehouse_receipt
        WHERE hawb_id IS NULL
    ) dup
    WHERE dup.rn > 1
);

-- ──────────────────────────────────────────
-- Paso 6: Recalcular piezas y pesos en los
--          MAWB afectados usando los datos
--          limpios (MAX de los recibos restantes)
-- ──────────────────────────────────────────
WITH mawb_totals AS (
    SELECT
        wr.mawb_id,
        MAX(wr.piece_count) AS max_pieces,
        MAX(wr.chargeable_weight_kg) AS max_chargeable,
        MAX(wr.actual_weight_kg) AS max_actual_kg
    FROM warehouse_receipt wr
    WHERE wr.mawb_id IS NOT NULL
    GROUP BY wr.mawb_id
)
UPDATE mawb m
SET
    pieces = COALESCE(mt.max_pieces, m.pieces),
    chargeable_weight_kg = COALESCE(mt.max_chargeable, m.chargeable_weight_kg),
    reported_weight_kg = CASE
        WHEN mt.max_actual_kg IS NOT NULL AND mt.max_actual_kg > 0
        THEN mt.max_actual_kg
        ELSE m.reported_weight_kg
    END,
    status = CASE
        WHEN mt.max_pieces IS NOT NULL AND mt.max_pieces > 0
        THEN 'RECEIVED'::mawb_status
        ELSE m.status
    END
FROM mawb_totals mt
WHERE m.id = mt.mawb_id;

COMMIT;

-- ============================================================
-- Verificación post-ejecución:
--   SELECT mawb_id, hawb_id, COUNT(*)
--   FROM warehouse_receipt
--   GROUP BY mawb_id, hawb_id
--   HAVING COUNT(*) > 1;
-- Debe devolver 0 filas.
-- ============================================================
