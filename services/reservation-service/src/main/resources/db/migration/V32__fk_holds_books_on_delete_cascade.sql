/* -----------------------------------------------------------------------------------------------------------------
 * V32__fk_holds_books_on_delete_cascade.sql
 *
 * Scopo:
 *   - Eliminare l’eventuale vincolo precedente fk_holds_books senza ON DELETE CASCADE.
 *   - Ricrearlo con ON DELETE CASCADE per garantire la rimozione automatica delle hold
 *     quando un book viene hard-deleted.
 *   - Lo statement DROP è idempotente: se il vincolo non esiste la migration non fallisce.
 * ----------------------------------------------------------------------------------------------------------------- */

-- 1️⃣  Rimuove il vincolo legacy (se presente)
ALTER TABLE holds
    DROP CONSTRAINT IF EXISTS fk_holds_books;

-- 2️⃣  Crea il nuovo vincolo cascade
ALTER TABLE holds
    ADD CONSTRAINT fk_holds_books
        FOREIGN KEY (bib_id)
        REFERENCES books(id)
        ON DELETE CASCADE;
