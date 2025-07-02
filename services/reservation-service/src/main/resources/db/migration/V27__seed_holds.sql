-- V27__seed_holds.sql
-- Semina di 10 prenotazioni “realistiche” agganciate a libri esistenti
-- (sostituisce la vecchia versione con bib_id/proprietà non compatibili)

--------------------------------------------------------------------------------
-- 0. Abilita pgcrypto per gen_random_uuid()
--------------------------------------------------------------------------------
CREATE EXTENSION IF NOT EXISTS pgcrypto;

BEGIN;

--------------------------------------------------------------------------------
-- 1. Pulisci eventuali record demo precedenti di questa migration
--------------------------------------------------------------------------------
DELETE FROM holds
WHERE pickup_branch LIKE 'demo-%';

--------------------------------------------------------------------------------
-- 2. Inserisci 10 HOLD collegate a 10 libri differenti
--------------------------------------------------------------------------------
WITH sel AS (
    SELECT id AS book_id,
           row_number() OVER () AS seq
    FROM   books
    ORDER  BY RANDOM()
    LIMIT  10
)
INSERT INTO holds
       (id, hold_id, patron_id, bib_id,
        status, pickup_branch, created_at, position)
SELECT
    gen_random_uuid()                       AS id,
    gen_random_uuid()                       AS hold_id,
    gen_random_uuid()                       AS patron_id,
    s.book_id                               AS bib_id,
    /* Stato distribuito in modo vario */
    CASE s.seq % 4
        WHEN 0 THEN 'READY'
        WHEN 1 THEN 'PLACED'
        WHEN 2 THEN 'CANCELLED'
        ELSE        'EXPIRED'
    END                                      AS status,
    /* Branch casuale fra 4 filiali */
    (ARRAY['CENTRAL','NORTH','SOUTH','WEST'])[
        (floor(random()*4)::int + 1)]        AS pickup_branch,
    now() - ((11 - s.seq) || ' days')::INTERVAL AS created_at,
    s.seq                                    AS position
FROM sel s;

COMMIT;
