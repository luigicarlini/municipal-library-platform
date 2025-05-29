-- Tabella che registra ogni prenotazione (Hold) effettuata da un utente su un libro
CREATE TABLE holds (
  id UUID PRIMARY KEY,                                 -- Identificativo univoco interno (PK)
  hold_id UUID NOT NULL UNIQUE,                        -- Identificativo pubblico della prenotazione (può coincidere con id)
  patron_id UUID NOT NULL,                             -- Identificativo dell'utente che effettua la prenotazione
  bib_id UUID NOT NULL,                                -- Identificativo del libro prenotato (FK verso books.id)
  pickup_branch VARCHAR(100) NOT NULL,                 -- Filiale bibliotecaria dove si ritirerà il libro
  status VARCHAR(20) NOT NULL,                         -- Stato della prenotazione (PLACED, READY, COLLECTED, CANCELLED, EXPIRED)
  position INTEGER,                                    -- Posizione nella coda di prenotazione (1 = prima, 2 = seconda, ecc.)
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),  -- Timestamp della creazione della prenotazione
  expires_at TIMESTAMP WITH TIME ZONE                  -- Data di scadenza della prenotazione (opzionale)
);


