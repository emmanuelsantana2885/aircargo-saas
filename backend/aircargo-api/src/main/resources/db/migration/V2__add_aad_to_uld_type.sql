DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_type WHERE typname = 'uld_type') THEN
        IF NOT EXISTS (
            SELECT 1 FROM pg_type t
            JOIN pg_enum e ON t.oid = e.enumtypid
            WHERE t.typname = 'uld_type' AND e.enumlabel = 'AAD'
        ) THEN
            EXECUTE 'ALTER TYPE uld_type ADD VALUE ''AAD''';
        END IF;
    END IF;
END$$;
