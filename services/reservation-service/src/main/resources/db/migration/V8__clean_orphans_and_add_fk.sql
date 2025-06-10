/*
 * V8 – ripulisce hold orfane e protegge con FK
 */

-- 1. rimozione difensiva di eventuali orfani
DELETE FROM holds
WHERE bib_id NOT IN (SELECT id FROM books);

-- 2. aggiunta FK
ALTER TABLE holds
    ADD CONSTRAINT fk_holds_books
    FOREIGN KEY (bib_id)
    REFERENCES books(id)
    ON DELETE CASCADE;
