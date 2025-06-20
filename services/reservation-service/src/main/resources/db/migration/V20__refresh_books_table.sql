-- V20__refresh_books_table.sql
-- Ripulisce record demo e reinserisce 50 libri reali

-- 0.  Abilita pgcrypto se manca (serve gen_random_uuid)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

BEGIN;

-------------------------------------------------------------------------------
-- 1. Cancella i record “demo”
-------------------------------------------------------------------------------
DELETE FROM books
WHERE title ILIKE ANY (ARRAY[
  '%Soft-Delete demo%',
  '%LockDemo%',
  '%Cascade test%',
  '%History Demo%'
]);

-------------------------------------------------------------------------------
-- 2. (Opzionale) pulizia totale e reset sequenze
--    NB: togli il commento se vuoi proprio azzerare tutto.
-- TRUNCATE TABLE books;

-------------------------------------------------------------------------------
-- 3. Inserisci 50 titoli “veri”
--    • ISBN tutti diversi
--    • deleted = FALSE
--    • version  = 0
-------------------------------------------------------------------------------
INSERT INTO books
        (id,                title,                   author,                 genre,
         publication_year,  isbn,                    price,  stock_quantity, version, deleted)
VALUES
-- 01
(gen_random_uuid(), '1984',                      'George Orwell',          'Dystopia',          1949, '9780451524935',  12.50, 20, 0, FALSE),
-- 02
(gen_random_uuid(), 'Orgoglio e Pregiudizio',    'Jane Austen',            'Classici',          1813, '9780141439518',  10.00, 14, 0, FALSE),
-- 03
(gen_random_uuid(), 'Norwegian Wood',            'Haruki Murakami',        'Narrativa',         1987, '9780375704024',  13.90, 13, 0, FALSE),
-- 04
(gen_random_uuid(), 'Il nome della rosa',        'Umberto Eco',            'Giallo storico',    1980, '9788807900525',  14.90, 18, 0, FALSE),
-- 05
(gen_random_uuid(), 'Il Signore degli Anelli',   'J. R. R. Tolkien',      'Fantasy',            1954, '9780261103252',  25.00, 15, 0, FALSE),
-- 06
(gen_random_uuid(), 'Il Grande Gatsby',          'F. S. Fitzgerald',       'Classici',          1925, '9780743273565',  11.00, 12, 0, FALSE),
-- 07
(gen_random_uuid(), 'Sulla strada',              'Jack Kerouac',           'Narrativa',         1957, '9780141182674',  12.00, 10, 0, FALSE),
-- 08
(gen_random_uuid(), 'Uno studio in rosso',       'Arthur Conan Doyle',     'Giallo',            1887, '9788804485261',  10.50, 10, 0, FALSE),
-- 09
(gen_random_uuid(), 'Centanni di solitudine',   'Gabriel G. Márquez',     'Realismo magico',   1967, '9788804681960',  13.50, 10, 0, FALSE),
-- 10
(gen_random_uuid(), 'La strada',                 'Cormac McCarthy',        'Post-apocalittico', 2006, '9780307387899',  12.30,  9, 0, FALSE),
-- 11
(gen_random_uuid(), 'Crime and Punishment',      'Fëdor Dostoevskij',      'Classici',          1866, '9780143058144',  14.20,  9, 0, FALSE),
-- 12
(gen_random_uuid(), 'Dracula',                   'Bram Stoker',            'Horror',            1897, '9780141439846',  11.80,  8, 0, FALSE),
-- 13
(gen_random_uuid(), 'Fahrenheit 451',            'Ray Bradbury',           'Sci-Fi',            1953, '9780006546061',  12.60,  8, 0, FALSE),
-- 14
(gen_random_uuid(), 'La coscienza di Zeno',      'Italo Svevo',            'Classici',          1923, '9788806221058',  11.50,  7, 0, FALSE),
-- 15
(gen_random_uuid(), 'Il barone rampante',        'Italo Calvino',          'Narrativa',         1957, '9788807811616',  12.40,  7, 0, FALSE),
-- 16
(gen_random_uuid(), 'Moby Dick',                 'Herman Melville',        'Avventura',         1851, '9780142437247',  13.50,  7, 0, FALSE),
-- 17
(gen_random_uuid(), 'Orgoglio di Leeds',         'Lucy Score',             'Romance',           2022, '9781405966995',  15.90,  6, 0, FALSE),
-- 18
(gen_random_uuid(), 'Il piccolo principe',       'Antoine de Saint-Exupéry','Narrativa',        1943, '9780156013987',  10.20,  6, 0, FALSE),
-- 19
(gen_random_uuid(), 'Il vecchio e il mare',      'Ernest Hemingway',       'Narrativa',         1952, '9780684801223',  11.10,  6, 0, FALSE),
-- 20
(gen_random_uuid(), 'Se questo è un uomo',       'Primo Levi',            'Memorie',            1947, '9788806219321',  12.00,  6, 0, FALSE),
-- 21
(gen_random_uuid(), 'Il potere del cane',        'Don Winslow',            'Thriller',          2005, '9788806204365',  14.90,  5, 0, FALSE),
-- 22
(gen_random_uuid(), 'Dieci piccoli indiani',     'Agatha Christie',        'Giallo',            1939, '9788804561682',  10.50,  5, 0, FALSE),
-- 23
(gen_random_uuid(), 'Lo Hobbit',                 'J. R. R. Tolkien',      'Fantasy',            1937, '9780007458424',  14.50,  5, 0, FALSE),
-- 24
(gen_random_uuid(), 'Il Profeta',                'Kahlil Gibran',          'Saggistica',        1923, '9780141187013',  10.90,  5, 0, FALSE),
-- 25
(gen_random_uuid(), 'La casa degli spiriti',     'Isabel Allende',         'Narrativa',         1982, '9788807896132',  13.90,  5, 0, FALSE),
-- 26
(gen_random_uuid(), 'Shining',                   'Stephen King',           'Horror',            1977, '9780307743657',  13.20,  4, 0, FALSE),
-- 27
(gen_random_uuid(), 'Il codice Da Vinci',       'Dan Brown',              'Thriller',           2003, '9788804676485',  11.80,  4, 0, FALSE),
-- 28
(gen_random_uuid(), 'Il gattopardo',             'Giuseppe Tomasi',        'Classici',          1958, '9788804565307',  12.60,  4, 0, FALSE),
-- 29
(gen_random_uuid(), 'La ragazza del treno',      'Paula Hawkins',          'Thriller',          2015, '9788806214418',  12.70,  4, 0, FALSE),
-- 30
(gen_random_uuid(), 'Neuromante',                'William Gibson',         'Cyberpunk',         1984, '9780553281743',  11.90,  4, 0, FALSE),
-- 31
(gen_random_uuid(), 'La meccanica del cuore',    'Mathias Malzieu',        'Narrativa',         2007, '9788804679103',  10.90,  3, 0, FALSE),
-- 32
(gen_random_uuid(), 'Il conte di Montecristo',   'Alexandre Dumas',        'Avventura',         1846, '9788807902819',  16.80,  3, 0, FALSE),
-- 33
(gen_random_uuid(), 'Il paziente inglese',       'Michael Ondaatje',       'Narrativa',         1992, '9788807812989',  12.30,  3, 0, FALSE),
-- 34
(gen_random_uuid(), 'American Gods',             'Neil Gaiman',            'Fantasy',           2001, '9780062472106',  14.20,  3, 0, FALSE),
-- 35
(gen_random_uuid(), 'Il silenzio degli innocenti','Thomas Harris',         'Thriller',          1988, '9788804596540',  12.90,  3, 0, FALSE),
-- 36
(gen_random_uuid(), 'Il Circolo Pickwick',       'Charles Dickens',        'Classici',          1836, '9788807902829',  14.40,  2, 0, FALSE),
-- 37
(gen_random_uuid(), 'Le cronache di Narnia',     'C. S. Lewis',            'Fantasy',           1950, '9780066238500',  15.30,  2, 0, FALSE),
-- 38
(gen_random_uuid(), 'Il pianista',               'Władysław Szpilman',     'Memorie',           1946, '9788806167205',  11.70,  2, 0, FALSE),
-- 39
(gen_random_uuid(), 'L’ombra del vento',         'Carlos Ruiz Zafón',      'Narrativa',         2001, '9788804598865',  12.60,  2, 0, FALSE),
-- 40
(gen_random_uuid(), 'Solaris',                   'Stanisław Lem',          'Sci-Fi',            1961, '9780156027601',  11.80,  2, 0, FALSE),
-- 41
(gen_random_uuid(), 'La solitudine dei numeri primi','Paolo Giordano',    'Narrativa',          2008, '9788807881100',  11.00,  1, 0, FALSE),
-- 42
(gen_random_uuid(), 'Kitchen',                   'Banana Yoshimoto',       'Narrativa',         1988, '9788807880356',  10.50,  1, 0, FALSE),
-- 43
(gen_random_uuid(), 'Siddharta',                 'Hermann Hesse',          'Classici',          1922, '9788807818922',  11.20,  1, 0, FALSE),
-- 44
(gen_random_uuid(), 'Il lupo della steppa',      'Hermann Hesse',          'Narrativa',         1927, '9788807818946',  11.50,  1, 0, FALSE),
-- 45
(gen_random_uuid(), 'La montagna incantata',     'Thomas Mann',            'Classici',          1924, '9788806221884',  14.80,  1, 0, FALSE),
-- 46
(gen_random_uuid(), 'La città e le stelle',      'Arthur C. Clarke',       'Sci-Fi',            1956, '9788807891238',  11.70,  1, 0, FALSE),
-- 47
(gen_random_uuid(), 'I miserabili',              'Victor Hugo',            'Classici',          1862, '9788804686103',  17.20,  1, 0, FALSE),
-- 48
(gen_random_uuid(), 'Il buio oltre la siepe',    'Harper Lee',             'Narrativa',         1960, '9788804687179',  11.90,  1, 0, FALSE),
-- 49
(gen_random_uuid(), 'Neuromante – Trilogia',     'William Gibson',         'Cyberpunk',         1986, '9780553382563',  18.00,  1, 0, FALSE),
-- 50
(gen_random_uuid(), 'Tartarughe all’infinito',   'John Green',             'Young adult',       2017, '9780141346045',  12.40,  1, 0, FALSE);

COMMIT;

