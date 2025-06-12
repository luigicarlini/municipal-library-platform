#!/usr/bin/env bash
set -eu
# soft_hard_delete_demo.sh
# ------------------------------------------------------------------
# Dimostrazione end-to-end di soft-delete → hard-delete su /books
# ------------------------------------------------------------------

BASE=http://localhost:8080
JQ=${JQ:-jq}

# --- helper --------------------------------------------------------
gen_isbn() {                     # 12 cifre + check-digit ISBN-13
  local body
  body=$(date +%s%N | sha1sum | tr -dc 0-9 | head -c12)
  local sum=0 d
  for i in {0..11}; do
    d=${body:i:1}
    (( i % 2 == 0 )) && sum=$((sum+d)) || sum=$((sum+d*3))
  done
  local cd=$(( (10 - sum%10) %10 ))
  echo "${body}${cd}"
}

ISBN=$(gen_isbn)
printf "→ ISBN scelto: %s\n" "$ISBN"

# 1️⃣ CREATE libro demo ------------------------------------------------
BOOK_JSON=$(curl -sf -X POST "$BASE/books" -H "Content-Type: application/json" \
  -d "{\"title\":\"Soft-Delete demo\",\"author\":\"Demo\",\"genre\":\"Test\",\
       \"publicationYear\":2024,\"isbn\":\"$ISBN\",\
       \"price\":1,\"stockQuantity\":1}")

BOOK_ID=$(echo "$BOOK_JSON" | $JQ -r .id)
echo "→ creato libro $BOOK_ID"

# 2️⃣ SOFT-DELETE ------------------------------------------------------
curl -i -X DELETE "$BASE/books/$BOOK_ID?mode=soft"

# 3️⃣ verifica flag deleted = true -------------------------------------
docker exec -i pg-library psql -U library -d library -c \
  "SELECT id, deleted FROM books WHERE id = '$BOOK_ID';"

# 4️⃣ HARD-DELETE ------------------------------------------------------
curl -i -X DELETE "$BASE/books/$BOOK_ID?mode=hard"

# 5️⃣ controllo finale: nessuna riga rimasta ---------------------------
docker exec -i pg-library psql -U library -d library -c \
  "SELECT count(*) FROM books WHERE id = '$BOOK_ID';"

# ------------------------------------------------------------------
echo -e "\n\033[32m✅ Soft/Hard-delete demo completata con successo\033[0m"
