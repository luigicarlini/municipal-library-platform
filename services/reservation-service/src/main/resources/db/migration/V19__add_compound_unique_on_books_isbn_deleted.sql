/* -------------------------------------------------------------------------
   V19__add_compound_unique_on_books_isbn_deleted.sql
   Impone l’unicità dell’accoppiata (isbn, deleted) e, se presente,
   rimuove il vecchio vincolo/indice che garantiva la sola unicità su isbn.
   Aggiunge inoltre un indice parziale per accelerare le ricerche sui
   record “live” (deleted = FALSE).
   ------------------------------------------------------------------------- */

-- 1) DROP del vecchio UNIQUE (isbn) se esiste
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
          FROM pg_constraint
         WHERE conrelid = 'books'::regclass
           AND contype   = 'u'
           AND conname   = 'books_isbn_key'      -- nome standard di pg per UNIQUE(isbn)
    ) THEN
        ALTER TABLE books
          DROP CONSTRAINT books_isbn_key;
    END IF;
END
$$;

-- 2) CREATE UNIQUE (isbn, deleted) IF NOT EXISTS
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
          FROM pg_constraint
         WHERE conrelid = 'books'::regclass
           AND contype   = 'u'
           AND conname   = 'books_isbn_deleted_unique'
    ) THEN
        ALTER TABLE books
          ADD CONSTRAINT books_isbn_deleted_unique
              UNIQUE (isbn, deleted);
    END IF;
END
$$;

-- 3) (Opzionale ma consigliato) Indice parziale per ricerche sui record “live”
CREATE INDEX IF NOT EXISTS idx_books_isbn_live
    ON books (isbn)
 WHERE deleted = FALSE;
