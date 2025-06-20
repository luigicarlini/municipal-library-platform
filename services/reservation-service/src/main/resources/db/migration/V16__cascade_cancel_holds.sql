/* V16 – cancella (CANCELLED) le hold PLACED quando il libro
         passa deleted=false → true                                            */

-- 1) funzione
CREATE OR REPLACE FUNCTION cancel_holds_on_book_soft_delete()
RETURNS trigger LANGUAGE plpgsql AS
$$
BEGIN
  IF NEW.deleted AND NOT OLD.deleted THEN
     UPDATE holds
        SET status = 'CANCELLED'
      WHERE bib_id = NEW.id
        AND status = 'PLACED';
  END IF;
  RETURN NEW;
END;
$$;

-- 2) trigger AFTER UPDATE
DROP TRIGGER IF EXISTS trg_cancel_holds_on_book_soft_delete
            ON books;

CREATE TRIGGER trg_cancel_holds_on_book_soft_delete
AFTER UPDATE OF deleted ON books
FOR EACH ROW
EXECUTE FUNCTION cancel_holds_on_book_soft_delete();
