-- V6__add_book_version.sql
------------------------------------------------------------
-- Colonna di optimistic locking per l'entità Book
-- Compatibile con PostgreSQL 9.6+ e H2 2.2
------------------------------------------------------------

-- 1. Aggiungo la colonna con valore di default 0
ALTER TABLE books
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- 2. Se vuoi essere pignolo, rimuovi il default (opzionale)
-- ALTER TABLE books ALTER COLUMN version DROP DEFAULT;
