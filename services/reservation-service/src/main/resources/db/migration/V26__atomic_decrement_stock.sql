/* V26__atomic_decrement_stock.sql
   Sostituisce la logica a due passi (SELECT + UPDATE)
   con un UPDATE atomico che fallisce se lo stock è insufficiente. */

CREATE OR REPLACE FUNCTION try_decrement_stock(
    p_book_id UUID,
    p_qty     INT)
RETURNS BOOLEAN
LANGUAGE plpgsql AS
$$
DECLARE
    v_updated INT;
BEGIN
    UPDATE books
       SET stock_quantity = stock_quantity - p_qty
     WHERE id            = p_book_id
       AND deleted       = false
       AND stock_quantity >= p_qty
     RETURNING 1 INTO v_updated;

    RETURN FOUND;     -- TRUE se l’UPDATE ha toccato 1 riga, FALSE altrimenti
END;
$$;
