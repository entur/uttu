DO $$
    BEGIN
        CREATE USER uttu WITH PASSWORD 'uttu';
    EXCEPTION WHEN duplicate_object THEN
        NULL;
    END $$;

DO $$
    BEGIN
        CREATE USER postgres;
    EXCEPTION WHEN duplicate_object THEN
        NULL;
    END $$;
