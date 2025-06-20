#!/usr/bin/env bash
# ------------------------------------------------------------
# history_demo.sh – dimostra che una Hold resta nello storico
# ------------------------------------------------------------
set -Eeuo pipefail
trap 'echo "✖  Errore in linea $LINENO"; exit 1' ERR

JQ=${JQ:-jq}
BASE_URL=${BASE_URL:-http://localhost:8080}

rand_isbn() { shuf -i 1000000000000-9999999999999 -n1; }

curl_json() {
  local method=$1 url=$2
  curl -sS -X "$method" "$url" \
       -H 'Content-Type: application/json' \
       -d @- -w '\n%{http_code}'
}

create_book() {
  local isbn=$1
  jq -nc \
     --arg title  "History Demo" --arg author "Demo" \
     --arg genre  "Test"         --arg isbn "$isbn"  \
     --argjson year 2024         --argjson price 9.99 \
     --argjson stock 3           \
     '{title:$title,author:$author,genre:$genre,
       publicationYear:$year,isbn:$isbn,
       price:$price,stockQuantity:$stock}' |
  curl_json POST "$BASE_URL/books"
}

printf "📚  crea libro di prova\n"

for i in {1..5}; do
  ISBN=$(rand_isbn)
  RESPONSE=$(create_book "$ISBN")
  STATUS=$(tail -n1 <<<"$RESPONSE")
  BODY=$(sed '$d' <<<"$RESPONSE")

  if [[ $STATUS == 2* ]]; then
    BOOK_ID=$(jq -r .id <<<"$BODY")
    echo "  ✔ Book creato (ISBN=$ISBN) id=$BOOK_ID"
    break
  fi

  echo "   → tentativo $i fallito (HTTP $STATUS)"
  echo "     risposta: $BODY"
  (( i == 5 )) && { echo "✖ impossibile creare il libro"; exit 1; }
done

echo "➕  crea hold PLACED"
HOLD_BODY=$(jq -nc \
  --arg patron "$(uuidgen)" --arg bib "$BOOK_ID" --arg pickup "Nord" \
  '{patronId:$patron,bibId:$bib,pickupBranch:$pickup}')

RESPONSE=$(printf '%s' "$HOLD_BODY" | curl_json POST "$BASE_URL/holds")
STATUS=$(tail -n1 <<<"$RESPONSE")
BODY=$(sed '$d' <<<"$RESPONSE")

[[ $STATUS == 2* ]] || { echo "✖ POST /holds → $STATUS : $BODY"; exit 1; }

# Estrai l'id controllando il tipo JSON
HOLD_ID=$(jq -r 'if type=="array" then .[0].id else .id end' <<<"$BODY")
echo "  ✔ Hold creata id=$HOLD_ID"

echo "🗑   soft-delete libro"
curl -sS -X DELETE "$BASE_URL/books/$BOOK_ID?mode=soft" >/dev/null

echo "🔍  verifica hold -> CANCELLED"
STATUS=$(curl -s "$BASE_URL/holds/$HOLD_ID" | jq -r .status)

[[ $STATUS == CANCELLED ]] \
  && echo "✅  History-demo completato con SUCCESSO" \
  || { echo "✖ Hold non è CANCELLED (è $STATUS)"; exit 1; }
