/* ------------------------------------------------------------------
   V21 – AFTER UPDATE trigger: quando un ordine passa a PAID,
         decrementa lo stock del libro e blocca quantità negative.
   Il servizio effettua già il controllo a livello JPA; il trigger funge da ultima linea di difesa 
   contro race-condition inter-servizio o accessi diretti al DB.
   ------------------------------------------------------------------ */

-- funzione
CREATE OR REPLACE FUNCTION trg_decrement_stock()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.status = 'PAID' AND OLD.status <> 'PAID' THEN
        UPDATE books
           SET stock_quantity = stock_quantity - NEW.quantity
         WHERE id = NEW.book_id
           AND stock_quantity >= NEW.quantity;

        IF NOT FOUND THEN
            RAISE EXCEPTION 'Insufficient stock for book %', NEW.book_id
                USING ERRCODE = 'P0001';
        END IF;
    END IF;

    RETURN NEW;
END;
$$;

-- trigger
DROP TRIGGER IF EXISTS orders_decrement_stock ON orders;

CREATE TRIGGER orders_decrement_stock
AFTER UPDATE OF status ON orders
FOR EACH ROW
EXECUTE FUNCTION trg_decrement_stock();