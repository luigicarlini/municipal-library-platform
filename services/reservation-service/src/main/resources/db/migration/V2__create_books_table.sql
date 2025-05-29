-- Tabella che memorizza l'elenco dei libri disponibili nella biblioteca
CREATE TABLE books (
    id UUID PRIMARY KEY,                               -- Identificativo univoco del libro
    title VARCHAR(255) NOT NULL,                       -- Titolo del libro
    author VARCHAR(255) NOT NULL,                      -- Nome dell'autore
    genre VARCHAR(100),                                -- Genere letterario (es. Narrativa, Storico, Giallo, ecc.)
    publication_year INTEGER,                          -- Anno di pubblicazione
    isbn VARCHAR(20) UNIQUE                            -- Codice ISBN (univoco per edizione), può essere opzionale
);

