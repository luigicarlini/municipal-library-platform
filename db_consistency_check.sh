#!/usr/bin/env bash
set -euo pipefail
PG_CONTAINER="pg-library"
PG_DB="library"
PG_USER="library"

run_sql () {
  local sql="$1"
  docker exec -it "$PG_CONTAINER" \
    psql -U "$PG_USER" -d "$PG_DB" -c "$sql"
}

echo "🔍 1. Numero totale di HOLD"
run_sql "SELECT COUNT(*) AS total_holds FROM holds;"

echo -e "\n🔍 2. HOLD per STATO"
run_sql "SELECT status, COUNT(*) AS num FROM holds GROUP BY status ORDER BY num DESC;"

echo -e "\n🔍 3. HOLD per GIORNO (created_at, day-trunc)"
run_sql "SELECT date_trunc('day', created_at) AS day, COUNT(*) FROM holds GROUP BY day ORDER BY day;"

echo -e "\n🔍 4. Top PATRON per numero di prenotazioni"
run_sql "SELECT patron_id, COUNT(*) AS num FROM holds GROUP BY patron_id ORDER BY num DESC LIMIT 10;"

echo -e "\n🔍 5. Numero di HOLD per LIBRO (TOP 25)"
run_sql "$(cat <<'SQL'
SELECT b.title,
       COUNT(h.id) AS num_holds
FROM   books b
LEFT   JOIN holds h ON h.bib_id = b.id
GROUP  BY b.title
ORDER  BY num_holds DESC, b.title
LIMIT  25;
SQL
)"

echo -e "\n🔍 6. Associazione HOLD → BOOK (prime 30 righe)"
run_sql "$(cat <<'SQL'
SELECT h.id AS hold_id, b.title
FROM   holds h
JOIN   books b ON b.id = h.bib_id
LIMIT  30;
SQL
)"

echo -e "\n🔍 7. HOLD orfane (dovrebbe essere 0)"
run_sql "$(cat <<'SQL'
SELECT COUNT(*) AS orphan_holds
FROM   holds h
LEFT   JOIN books b ON b.id = h.bib_id
WHERE  b.id IS NULL;
SQL
)"

# echo -e "\n🔍 8. prenotazioni con stato PLACED"
# run_sql "$(cat <<'SQL'
# SELECT * FROM holds WHERE status = 'PLACED';
# SQL
# )"

echo -e "\n🔍 8. Conteggio rapido per status PLACED/CANCELLED/EXPIRED"
run_sql "SELECT status, COUNT(*) FROM holds WHERE status IN ('PLACED','CANCELLED','EXPIRED') GROUP BY status;"

echo -e "\n✅  Test di coerenza terminato!"
