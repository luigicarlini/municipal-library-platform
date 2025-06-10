-- V5__extend_books_for_shop.sql
------------------------------------------------------------
-- Estende la tabella books con campi e vincoli utili al Book-shop
-- Compatibile con PostgreSQL ≥ 9.6 e H2 2.2
------------------------------------------------------------

/*-----------------------------------------------------------------
  1) Aggiungo le nuove colonne SENZA vincoli restrittivi per non
     violare le righe già presenti (H2 non permette più ALTER
     nel medesimo statement).
------------------------------------------------------------------*/
ALTER TABLE books
    ADD COLUMN price NUMERIC(10,2);

ALTER TABLE books
    ADD COLUMN stock_quantity INT;

/*-----------------------------------------------------------------
  2) Inizializzo i valori null a 0, così potrò impostare NOT NULL.
------------------------------------------------------------------*/
UPDATE books SET price = 0        WHERE price IS NULL;
UPDATE books SET stock_quantity = 0 WHERE stock_quantity IS NULL;

/*-----------------------------------------------------------------
  3) Rendo le colonne NOT NULL e aggiungo i vincoli di dominio.
------------------------------------------------------------------*/
ALTER TABLE books
    ALTER COLUMN price SET NOT NULL;

ALTER TABLE books
    ALTER COLUMN stock_quantity SET NOT NULL;

ALTER TABLE books
    ADD CONSTRAINT chk_books_price_positive CHECK (price >= 0);

ALTER TABLE books
    ADD CONSTRAINT chk_books_stock_quantity_nonneg CHECK (stock_quantity >= 0);

/*-----------------------------------------------------------------
  4) L’ISBN diventa obbligatorio (era opzionale in V2).
------------------------------------------------------------------*/
ALTER TABLE books
    ALTER COLUMN isbn SET NOT NULL;

/*-----------------------------------------------------------------
  5) Indice univoco sull’ISBN (con “IF NOT EXISTS” supportato sia
     da Postgres sia da H2 ≥ 2.x).
------------------------------------------------------------------*/
CREATE UNIQUE INDEX IF NOT EXISTS idx_books_isbn ON books(isbn);
