/*
 * V9 – Extra sample holds (20 libri) con status diversi
 */

WITH data AS (
    SELECT *
    FROM (VALUES
        -- title                               , pickup , position , status
        ('Il nome della rosa'                 , 'Nord' , 1 , 'PLACED'),
        ('Cent''anni di solitudine'           , 'Sud'  , 2 , 'READY'),
        ('1984'                               , 'Est'  , 1 , 'COLLECTED'),
        ('Orgoglio e pregiudizio'             , 'Ovest', 1 , 'CANCELLED'),
        ('Il piccolo principe'                , 'Centrale', 1 , 'EXPIRED'),
        ('Il Gattopardo'                      , 'Nord' , 2 , 'PLACED'),
        ('Il processo'                        , 'Sud'  , 1 , 'READY'),
        ('Delitto e castigo'                  , 'Est'  , 3 , 'COLLECTED'),
        ('I promessi sposi'                   , 'Ovest', 1 , 'PLACED'),
        ('Siddhartha'                         , 'Centrale', 2 , 'READY'),
        ('Il ritratto di Dorian Gray 11'      , 'Nord' , 1 , 'COLLECTED'),
        ('Le notti bianche 14'                , 'Sud'  , 1 , 'PLACED'),
        ('Viaggio al centro della Terra 15'   , 'Est'  , 1 , 'READY'),
        ('Le notti bianche 17'                , 'Ovest', 2 , 'EXPIRED'),
        ('Le notti bianche 33'                , 'Centrale', 1 , 'CANCELLED'),
        ('Dracula 16'                         , 'Nord' , 1 , 'PLACED'),
        ('Fahrenheit 451 27'                  , 'Sud'  , 1 , 'READY'),
        ('Uno, nessuno e centomila 10'        , 'Est'  , 1 , 'COLLECTED'),
        ('Il barone rampante 1'               , 'Ovest', 1 , 'PLACED'),
        ('La coscienza di Zeno 2'             , 'Centrale', 1 , 'READY')
    ) AS t(title, pickup_branch, position, status)
)

INSERT INTO holds (id, hold_id, patron_id,
                   bib_id, pickup_branch, status, position, created_at)
SELECT gen_random_uuid(), gen_random_uuid(), gen_random_uuid(),
       b.id, d.pickup_branch, d.status, d.position, NOW()
FROM   data d
JOIN   books b        ON b.title = d.title
LEFT   JOIN holds h   ON h.bib_id = b.id
WHERE  h.id IS NULL;
