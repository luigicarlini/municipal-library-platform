#!/usr/bin/env bash
# soft_delete_cascade_demo.sh – verifica cascata soft-delete Book → Hold
set -euo pipefail

BASE=http://localhost:8080
DB_CONTAINER=pg-library
JQ=${JQ:-jq}

# ───── helper ──────────────────────────────────────────────────────────────
gen_isbn() {
  local body=$(date +%s%N | sha1sum | tr -dc 0-9 | head -c12) sum=0 d
  for i in {0..11}; do
    d=${body:i:1}; (( i % 2 == 0 )) && sum=$((sum+d)) || sum=$((sum+d*3))
  done
  echo "${body}$(( (10-sum%10)%10 ))"
}

die() { echo "✖  $1"; exit 1; }

# ───── 1️⃣  nuovo libro ────────────────────────────────────────────────────
ISBN=$(gen_isbn)
echo "📚  crea libro di prova ISBN=$ISBN"

BOOK_JSON=$(curl -sS -X POST "$BASE/books" -H 'Content-Type: application/json' \
  -d "{\"title\":\"Cascade test\",\"author\":\"Demo\",\"genre\":\"Test\",\
\"publicationYear\":2024,\"isbn\":\"$ISBN\",\"price\":1,\"stockQuantity\":1}")

BOOK_ID=$(echo "$BOOK_JSON" | $JQ -r .id)
[ -n "$BOOK_ID" ] || die "Creazione libro fallita"

# ───── 2️⃣  Hold PLACED ────────────────────────────────────────────────────
PATRON_ID=$(uuidgen)
echo "➕  crea hold PLACED"

TMP=$(mktemp)
CODE=$(curl -s -o "$TMP" -w '%{http_code}' -X POST "$BASE/holds" \
  -H 'Content-Type: application/json' \
  -d "{\"patronId\":\"$PATRON_ID\",\"bibId\":\"$BOOK_ID\",\"pickupBranch\":\"Nord\"}")

if [[ "$CODE" != "200" ]]; then
  echo "✖  POST /holds → $CODE"; cat "$TMP" | jq .; rm "$TMP"; exit 1
fi

HOLD_ID=$(jq -r .id <"$TMP"); rm "$TMP"
echo "  ✔ Hold creata id=$HOLD_ID"

# ───── 3️⃣  soft-delete libro ──────────────────────────────────────────────
echo "🗑   soft-delete libro"
curl -sS -X DELETE "$BASE/books/$BOOK_ID?mode=soft" || die "DELETE /books/$BOOK_ID failed"

# ───── 4️⃣  verifica stato HOLD ────────────────────────────────────────────
echo "🔍  verifica hold -> CANCELLED"
STATUS=$(curl -s "$BASE/holds/$HOLD_ID" | jq -r .status)
[[ "$STATUS" == "CANCELLED" ]] || die "Hold non è CANCELLED (è $STATUS)"

# conferma diretta in DB
docker exec -i "$DB_CONTAINER" \
  psql -U library -d library -Atc \
  "SELECT status FROM holds WHERE id = '$HOLD_ID';"

# ───── 5️⃣  nuova Hold deve fallire con 409 ───────────────────────────────
echo "➕  nuovo hold (atteso 409)"
CODE2=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$BASE/holds" \
  -H 'Content-Type: application/json' \
  -d "{\"patronId\":\"$(uuidgen)\",\"bibId\":\"$BOOK_ID\",\"pickupBranch\":\"Nord\"}")

[[ "$CODE2" == "409" ]] || die "Atteso 409, ottenuto $CODE2"
echo "✅  Soft-delete cascade test concluso con SUCCESSO"

# ... dopo la soft-delete ...
echo "🔍  verifica che TUTTE le hold PLACED siano ora CANCELLED"
cnt=$(docker exec -i "$DB_CONTAINER" psql -qAtU library -d library \
      -c "SELECT count(*) FROM holds WHERE bib_id='$BOOK_ID' AND status='PLACED';")
(( cnt == 0 )) || die "Hold ancora PLACED ($cnt)"
echo "✅  trigger 'PLACED→CANCELLED' test superato"
