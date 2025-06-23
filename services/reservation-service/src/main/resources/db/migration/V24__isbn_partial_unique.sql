-- Rimuovi eventuale unique esistente
DO $$
BEGIN
    IF EXISTS (SELECT 1
                 FROM pg_indexes
                WHERE schemaname = 'public'
                  AND indexname  = 'ux_books_isbn') THEN
        EXECUTE 'DROP INDEX public.ux_books_isbn';
    END IF;
END$$;

-- Unique valido solo sui libri non cancellati
CREATE UNIQUE INDEX IF NOT EXISTS ux_books_isbn_active
  ON books (isbn)
  WHERE deleted = false;
