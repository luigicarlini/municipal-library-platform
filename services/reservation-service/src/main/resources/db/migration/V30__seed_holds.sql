-- V30__seed_holds.sql
-- Popola la tabella holds con 20 prenotazioni aggiuntive, evitando conflitti
-- con eventuali record esistenti (es. quelli creati da V27).

CREATE EXTENSION IF NOT EXISTS pgcrypto;

BEGIN;

WITH books_free AS (
    -- Prendi libri che non hanno ancora una hold (o comunque non PLACED/READY)
    SELECT b.id,
           row_number() OVER () AS seq
    FROM   books b
    WHERE  NOT EXISTS (
        SELECT 1 FROM holds h WHERE h.bib_id = b.id
    )
    ORDER  BY RANDOM()
    LIMIT  20
)
INSERT INTO holds
       (id, hold_id, patron_id, bib_id,
        status, pickup_branch, created_at, position)
SELECT
    gen_random_uuid()                       AS id,
    gen_random_uuid()                       AS hold_id,
    gen_random_uuid()                       AS patron_id,
    bf.id                                   AS bib_id,
    CASE floor(random()*3)::int
         WHEN 0 THEN 'PLACED'
         WHEN 1 THEN 'READY'
         ELSE        'CANCELLED'
    END                                      AS status,
    (ARRAY['CENTRAL','NORTH','SOUTH','WEST'])[
        (floor(random()*4)::int + 1)]        AS pickup_branch,
    NOW() - (bf.seq || ' days')::INTERVAL    AS created_at,
    /* posizione = numero hold esistenti + 1 per quel libro */
    (
      SELECT COALESCE(MAX(position),0) + 1
      FROM   holds h2
      WHERE  h2.bib_id = bf.id
    )                                        AS position
FROM books_free bf
ON CONFLICT (hold_id) DO NOTHING;

COMMIT;
