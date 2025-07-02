-- V32__fk_holds_books_on_delete_cascade.sql
DO $$
BEGIN
  IF EXISTS (
      SELECT 1
      FROM   pg_constraint
      WHERE  conname = 'fk_holds_books') THEN
    ALTER TABLE holds
      DROP CONSTRAINT fk_holds_books;
  END IF;

  ALTER TABLE holds
    ADD CONSTRAINT fk_holds_books
    FOREIGN KEY (bib_id) REFERENCES books(id)
    ON DELETE CASCADE;
END $$;