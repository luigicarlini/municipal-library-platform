/* V25__cleanup_isbn_unique.sql
   Rimuove i vecchi vincoli/indici unici su ISBN e
   ne lascia UNO solo, parziale, per i record live. */

-- 1️⃣  Drop vecchi indici / constraint se esistono
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_indexes
             WHERE indexname = 'idx_books_isbn') THEN
       DROP INDEX idx_books_isbn;
  END IF;

  IF EXISTS (SELECT 1 FROM pg_constraint
             WHERE conname = 'uq_books_isbn') THEN
       ALTER TABLE books DROP CONSTRAINT uq_books_isbn;
  END IF;

  IF EXISTS (SELECT 1 FROM pg_constraint
             WHERE conname = 'books_isbn_deleted_unique') THEN
       ALTER TABLE books DROP CONSTRAINT books_isbn_deleted_unique;
  END IF;
END $$;

-- 2️⃣  Crea (o ricrea) l’unico indice corretto
CREATE UNIQUE INDEX IF NOT EXISTS ux_books_isbn_active
  ON books (isbn)
  WHERE deleted = false;
