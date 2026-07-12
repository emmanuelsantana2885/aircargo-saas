DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_type t
        JOIN pg_enum e ON t.oid = e.enumtypid
        WHERE t.typname = 'uld_type' AND e.enumlabel = 'AAD'
    ) THEN
        ALTER TYPE public."uld_type" ADD VALUE 'AAD';
    END IF;
END$$;
