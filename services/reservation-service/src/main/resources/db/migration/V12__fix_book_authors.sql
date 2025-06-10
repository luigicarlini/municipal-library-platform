/* ---------------------------------------------------------------------------
 * V11 – Fix book authors
 * Corregge gli abbinamenti titolo ⇄ autore nel dataset “books”.
 * (non tocca price/stock/version: solo campo author)
 * ------------------------------------------------------------------------- */

-- Dracula → Bram Stoker
UPDATE books SET author = 'Bram Stoker'
WHERE id IN (
  '35ea576e-8c6c-4d14-ba0b-1ac4e33b24e7', -- Dracula 16
  '4d5a2b2b-1e93-4ecb-a14d-4b7421cdd1d2', -- Dracula 35
  '7c21c6d1-4dc6-4de6-9cbc-d83ccbf7e9d8', -- Dracula 32
  '1b7e327f-256a-43b8-b8f0-5385f30bd300', -- Dracula 16 (hold test)
  'a4052a15-2a15-4d39-8781-83eec1087345'  -- Dracula 40
);

-- Il barone rampante → Italo Calvino
UPDATE books SET author = 'Italo Calvino'
WHERE id IN (
  '05bdd788-77dc-4558-b692-1628853d9bc9', -- Il barone rampante 1
  '4d943910-e6cc-4db4-b45b-59217170b7e3', -- Il barone rampante 7
  'b82549a5-fdc7-4a27-99fe-dda562fe7dd9'  -- Il barone rampante 26
);

-- La coscienza di Zeno → Italo Svevo
UPDATE books SET author = 'Italo Svevo'
WHERE title ILIKE 'La coscienza di Zeno%';

-- Uno, nessuno e centomila → Luigi Pirandello
UPDATE books SET author = 'Luigi Pirandello'
WHERE title ILIKE 'Uno, nessuno e centomila%';

-- Il ritratto di Dorian Gray → Oscar Wilde
UPDATE books SET author = 'Oscar Wilde'
WHERE title ILIKE 'Il ritratto di Dorian Gray%';

-- Viaggio al centro della Terra 36 → Jules Verne
UPDATE books SET author = 'Jules Verne'
WHERE id = '60e0ab9c-566c-4018-88c9-2b2ada42db74';

-- Le notti bianche (tutte) → Fëdor Dostoevskij
UPDATE books SET author = 'Fëdor Dostoevskij'
WHERE title ILIKE 'Le notti bianche%';

-- Il conte di Montecristo → Alexandre Dumas
UPDATE books SET author = 'Alexandre Dumas'
WHERE title ILIKE 'Il conte di Montecristo%';

/* ---------------------------------------------------------------------------
 * Verifica rapida (facoltativa in sviluppo, Flyway ignora result-sets)
 * SELECT author, COUNT(*) FROM books
 * WHERE title ILIKE '%Dracula%' GROUP BY author;
 * ------------------------------------------------------------------------- */
