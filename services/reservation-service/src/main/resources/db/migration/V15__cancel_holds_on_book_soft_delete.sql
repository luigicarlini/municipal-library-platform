/*
  ➕ Aggiunge nell’enum hold_status:
     • tutti i valori distinti presenti in holds.status
     • il nuovo valore 'CANCELLED'

  ◽ Se l’enum non esiste, lo crea subito con l’intero set.
  ◽ Se esiste, aggiunge solo le etichette mancanti.
*/
DO
$$
DECLARE
    val text;
BEGIN
    -- 1. Crea l’enum se non esiste
    IF NOT EXISTS (
        SELECT 1
        FROM pg_type
        WHERE typname = 'hold_status'
    ) THEN
        EXECUTE format(
            'CREATE TYPE hold_status AS ENUM (%s)',
            (
                SELECT string_agg(quote_literal(s), ',')
                FROM (
                    SELECT DISTINCT status AS s
                    FROM holds
                    UNION
                    SELECT 'CANCELLED'
                ) AS src
            )
        );

    -- 2. Altrimenti aggiunge i valori mancanti
    ELSE
        FOR val IN
            SELECT s
            FROM (
                SELECT DISTINCT status AS s
                FROM holds
                UNION
                SELECT 'CANCELLED'
            ) AS src
        LOOP
            EXECUTE format(
                'ALTER TYPE hold_status ADD VALUE IF NOT EXISTS %L',
                val
            );
        END LOOP;
    END IF;
END;
$$ LANGUAGE plpgsql;
