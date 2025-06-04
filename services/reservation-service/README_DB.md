README_DB.md — Documentazione del Modello Dati
📂 Contesto del progetto
Sistema di prenotazione per una piattaforma bibliotecaria, dove gli utenti (patron) possono effettuare richieste (holds) per i libri presenti nel catalogo (books).


📚 Tabella books
Contiene il catalogo dei libri disponibili nella biblioteca.
CREATE TABLE books (
    id UUID PRIMARY KEY,           -- Identificativo univoco del libro
    title VARCHAR(255) NOT NULL,   -- Titolo del libro
    author VARCHAR(255) NOT NULL,  -- Autore del libro
    genre VARCHAR(100),            -- Genere letterario
    publication_year INTEGER,      -- Anno di pubblicazione
    isbn VARCHAR(20) UNIQUE        -- Codice ISBN (se disponibile)
);

| Campo              | Tipo         | Descrizione                                             |
| ------------------ | ------------ | ------------------------------------------------------- |
| `id`               | UUID         | Identificativo univoco (PK)                             |
| `title`            | VARCHAR(255) | Titolo del libro                                        |
| `author`           | VARCHAR(255) | Nome e cognome dell’autore                              |
| `genre`            | VARCHAR(100) | Genere letterario (es. Storico, Narrativa, ecc.)        |
| `publication_year` | INTEGER      | Anno di pubblicazione                                   |
| `isbn`             | VARCHAR(20)  | ISBN univoco (può essere assente per libri di fantasia) |


🗃️ Tabella holds
Contiene le prenotazioni effettuate dagli utenti sui libri presenti nel catalogo.
CREATE TABLE holds (
  id UUID PRIMARY KEY,                                 -- Identificativo interno della hold
  hold_id UUID NOT NULL UNIQUE,                        -- Identificativo pubblico (può coincidere con `id`)
  patron_id UUID NOT NULL,                             -- Identificativo dell’utente che effettua la prenotazione
  bib_id UUID NOT NULL,                                -- ID del libro prenotato (FK verso books.id)
  pickup_branch VARCHAR(100) NOT NULL,                 -- Filiale di ritiro
  status VARCHAR(20) NOT NULL,                         -- Stato (PLACED, READY, COLLECTED, CANCELLED, EXPIRED)
  position INTEGER,                                    -- Posizione nella lista di attesa
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(), -- Data e ora di creazione
  expires_at TIMESTAMP WITH TIME ZONE                  -- Data di scadenza (opzionale)
);

| Campo           | Tipo         | Descrizione                                         |
| --------------- | ------------ | --------------------------------------------------- |
| `id`            | UUID         | Identificativo interno della prenotazione (PK)      |
| `hold_id`       | UUID         | Identificativo pubblico (può essere uguale a `id`)  |
| `patron_id`     | UUID         | ID dell’utente che ha effettuato la prenotazione    |
| `bib_id`        | UUID         | ID del libro prenotato (`books.id`)                 |
| `pickup_branch` | VARCHAR(100) | Nome della filiale in cui ritirare il libro         |
| `status`        | VARCHAR(20)  | Stato della prenotazione                            |
| `position`      | INTEGER      | Posizione in coda per il libro                      |
| `created_at`    | TIMESTAMP    | Timestamp di creazione                              |
| `expires_at`    | TIMESTAMP    | Data e ora in cui la prenotazione scade (opzionale) |


🔗 Relazione tra holds e books
Ogni riga di holds si riferisce a un solo libro tramite il campo bib_id.

Un libro può essere prenotato più volte da utenti diversi → relazione One-to-Many:
books (1) ← (N) holds

✅ Esempio: una prenotazione
{
  "holdId": "6a496513-b1e5-4f2a-8b6d-6877e6680974",
  "patronId": "11111111-1111-1111-1111-111111111111",
  "bibId": "c1dd3865-ff8f-4de3-8ab1-0e150b367d88",
  "pickupBranch": "Centrale",
  "status": "PLACED",
  "position": 1,
  "createdAt": "2025-05-22T13:14:31.260Z"
}
----------------------------------------------------------------------------------------------------------
🔍 Query utili
📌 Libri non prenotati:
SELECT books.id, books.title
FROM books
LEFT JOIN holds ON books.id = holds.bib_id
WHERE holds.id IS NULL;

docker exec -it pg-library psql -U library -d library -c "
SELECT books.id, books.title
FROM books
LEFT JOIN holds ON books.id = holds.bib_id
WHERE holds.id IS NULL;"
----------------------------------------------------------------------------------------------------------
📌 Libri più prenotati:
SELECT b.title, COUNT(h.id) AS num_holds
FROM books b
JOIN holds h ON b.id = h.bib_id
GROUP BY b.title
ORDER BY num_holds DESC;

docker exec -it pg-library psql -U library -d library -c "SELECT b.title, COUNT(h.id) AS num_holds
FROM books b
JOIN holds h ON b.id = h.bib_id
GROUP BY b.title
ORDER BY num_holds DESC;"
------------------------------------------------------------------------------------------------
📌 Verifica consistenza tra holds e books:
SELECT COUNT(*) FROM holds h
LEFT JOIN books b ON h.bib_id = b.id
WHERE b.id IS NULL;

docker exec -it pg-library psql -U library -d library -c "SELECT COUNT(*) FROM holds h
LEFT JOIN books b ON h.bib_id = b.id
WHERE b.id IS NULL;"

✅ Esegui Backup Periodici:
Crea un comando per esportare il database:
docker exec -t pg-library pg_dump -U library -d library > backup.sql

Ripristino:
docker exec -i pg-library psql -U library -d library < backup.sql

-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
📌 comando docker exec per interrogare la tabella books e visualizzare i campi id, title, author, genre, publication_year, isbn:
✅ Questo comando:
Accede al container PostgreSQL chiamato pg-library
Esegue una query sul database library
Mostra i primi 20 libri con tutti i campi richiesti

docker exec -it pg-library psql -U library -d library -c "SELECT id, title, author, genre, publication_year, isbn FROM books LIMIT 20;"
                  id                  |            title             |            author            |        genre        | publication_year |     isbn
--------------------------------------+------------------------------+------------------------------+---------------------+------------------+---------------
 85dce958-53f0-4f7a-8a0f-7f698f163603 | Il nome della rosa           | Umberto Eco                  | Giallo storico      |             1980 | 9788804494216
 c1dd3865-ff8f-4de3-8ab1-0e150b367d88 | Cent'anni di solitudine      | Gabriel García Márquez       | Realismo magico     |             1967 | 9788807818933
 edcd9ecb-5f95-42e9-9e36-93547d3ef397 | 1984                         | George Orwell                | Distopia            |             1949 | 9780451524935
 78df7ab2-8a8d-47e6-bce7-3da0ff61d6b9 | Orgoglio e pregiudizio       | Jane Austen                  | Romanzo             |             1813 | 9788807900386
 9f8b7eee-6227-4526-9a97-dec29f58d133 | Il piccolo principe          | Antoine de Saint-Exupéry     | Fiaba               |             1943 | 9780156013987
 e644c534-6874-495f-9491-bd6b4c963a12 | Il Gattopardo                | Giuseppe Tomasi di Lampedusa | Storico             |             1958 | 9788807900706
 fd492643-4f28-42fa-becf-b4d3c0113246 | Il processo                  | Franz Kafka                  | Romanzo             |             1925 | 9788806173170
 e8cd37b9-591b-4554-953f-92fc0b97b586 | Delitto e castigo            | Fëdor Dostoevskij            | Psicologico         |             1866 | 9788807900585
 bd35d312-7eaf-48fe-aeb4-e357e1babd91 | I promessi sposi             | Alessandro Manzoni           | Romanzo storico     |             1827 | 9788806215215
 8d74303f-b364-4b77-958e-c0f6dec7c038 | Siddhartha                   | Hermann Hesse                | Spirituale          |             1922 | 9788807880931
 05bdd788-77dc-4558-b692-1628853d9bc9 | Il barone rampante 1         | Fëdor Dostoevskij            | Letteratura         |             2017 | 9784104116691
 bc999286-d5df-4c47-aab0-6b1780ac1847 | La coscienza di Zeno 2       | Fëdor Dostoevskij            | Letteratura         |             1928 | 9783155764139
 bda70257-9b35-4977-974a-f23d07816e10 | Il ritratto di Dorian Gray 3 | Jules Verne                  | Classico            |             1912 | 9782685818531
 5841efcf-bc5e-43db-a842-bda81861c70b | Uno, nessuno e centomila 4   | Alexandre Dumas              | Avventura           |             1875 | 9789876260891
 14c16130-6a60-404a-acf8-94608bcae41c | Dracula 5                    | Luigi Pirandello             | Avventura           |             1948 | 9788450439693
 b509dec2-de41-4f92-a957-e54eb7f0ea50 | Il conte di Montecristo 6    | Italo Svevo                  | Avventura           |             2010 | 9788043280629
 4d943910-e6cc-4db4-b45b-59217170b7e3 | Il barone rampante 7         | Italo Calvino                | Fantascienza        |             1992 | 9786766333196
 aae66fdd-23e7-4a8d-a457-9b2005c5f2b1 | L’isola del tesoro 8         | Oscar Wilde                  | Fantascienza        |             1922 | 9781866857779
 a99f5686-0262-403a-bcf5-d6dfba29b4a1 | Uno, nessuno e centomila 9   | Alexandre Dumas              | Romanzo psicologico |             1921 | 9783295069156
 9ab0da2e-8844-417a-aba6-02eeabb0ef42 | Uno, nessuno e centomila 10  | Robert L. Stevenson          | Gotico              |             2014 | 9785182413773
(20 rows)


SWAGGER DOCS:
http://localhost:8080/swagger-ui/index.html#/







