/* V14 – flag soft-delete sui libri */
ALTER TABLE books
    ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- indice di supporto (ricerche filtrate)
CREATE INDEX IF NOT EXISTS idx_books_not_deleted
    ON books(id, deleted)
    WHERE deleted = FALSE;
