/* V33 – introduce la scadenza del prestito (due_date) --------------- */

-- 1. Nuova colonna con default a 14 giorni dalla creazione
ALTER TABLE holds
    ADD COLUMN due_date DATE NOT NULL
        DEFAULT (CURRENT_DATE + INTERVAL '14 days');

COMMENT ON COLUMN holds.due_date IS 'Data di restituzione prevista (loan period)';

-- 2. Allinea le righe esistenti (NB: per sicurezza se il default non si applica)
UPDATE holds
   SET due_date = COALESCE(due_date, created_at::date + INTERVAL '14 days');
