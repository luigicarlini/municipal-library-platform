-- V6__create_orders_table.sql   (compatibile Postgres + H2 2.x)

CREATE TABLE orders (
  id                  BIGSERIAL PRIMARY KEY,            -- OK per Postgres; H2 lo mappa a IDENTITY
  book_id             UUID          NOT NULL,
  patron_id           BIGINT        NOT NULL,
  quantity            INT           NOT NULL CHECK (quantity > 0),
  unit_price_snapshot NUMERIC(10,2) NOT NULL,
  total_price         NUMERIC(10,2) NOT NULL,
  status              VARCHAR(20)   NOT NULL,
  payment_reference   VARCHAR(64),

  created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

  version             INT           NOT NULL DEFAULT 0
);
