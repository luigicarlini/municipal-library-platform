/*
 * V10 – crea la sequence mancante per orders.id
 * (solo se non esiste già)
 */

-- 1️⃣  Sequence
CREATE SEQUENCE IF NOT EXISTS orders_seq
  START WITH 1
  INCREMENT BY 1
  NO MINVALUE
  NO MAXVALUE
  CACHE 50;

-- 2️⃣  Collega la colonna id alla sequence
ALTER TABLE orders
  ALTER COLUMN id
  SET DEFAULT nextval('orders_seq');
