#!/usr/bin/env bash
set -euo pipefail

BASE="http://localhost:8080"
json() { jq -r "$1"; }

##### 1. Libro «Zanna Bianca» ##################################################
echo "▶ 1. Crea / ottiene «Zanna Bianca»"
ZANNA_PAYLOAD='{
  "title":"Zanna Bianca",
  "author":"Jack London",
  "genre":"Avventura",
  "publicationYear":1906,
  "isbn":"9788893813006",
  "price":18.90,
  "stockQuantity":10
}'
ID1=$(curl -sG "$BASE/books" --data-urlencode "isbn=9788893813006" \
      | json '.[0].id // empty')
if [[ -z "$ID1" ]]; then
  ID1=$(curl -s -X POST "$BASE/books" \
    -H "Content-Type: application/json" \
    -d "$ZANNA_PAYLOAD" \
    | json '.id')
fi
echo "   -> ID1 = $ID1"

##### 1.b Libro «L’Ultima Legione» #############################################
echo "▶ 1.b Crea / ottiene «L’Ultima Legione»"
LEGIO_PAYLOAD='{
  "title":"L’Ultima Legione",
  "author":"Valerio Massimo Manfredi",
  "genre":"Romanzo Storico",
  "publicationYear":2002,
  "isbn":"9788804509783",
  "price":21.00,
  "stockQuantity":5
}'
ID2=$(curl -sG "$BASE/books" --data-urlencode "isbn=9788804509783" \
      | json '.[0].id // empty')
if [[ -z "$ID2" ]]; then
  ID2=$(curl -s -X POST "$BASE/books" \
    -H "Content-Type: application/json" \
    -d "$LEGIO_PAYLOAD" \
    | json '.id')
fi
echo "   -> ID2 = $ID2"

##### 2. Aggiorna libro 1 ######################################################
echo "▶ 2. Aggiorna price/stock libro ID1"
curl -s -o /dev/null -w '%{http_code}' -X PUT "$BASE/books/$ID1" \
     -H "Content-Type: application/json" \
     -d "$ZANNA_PAYLOAD" \
  | grep -q '^200$' && echo "   -> aggiornamento confermato"

##### 3. HOLD su libro 1 #######################################################
echo "▶ 3. Crea HOLD su ID1"
PATRON="11111111-1111-1111-1111-111111111111"

# cerco un’eventuale prenotazione già esistente
HID=$(curl -sG "$BASE/holds" \
        --data-urlencode "bibId=$ID1" \
        --data-urlencode "patronId=$PATRON" \
      | json '.[0].id // empty')

if [[ -z "$HID" ]]; then
  HOLD_JSON=$(curl -s -X POST "$BASE/holds" \
    -H "Content-Type: application/json" \
    -d "{
      \"patronId\":\"$PATRON\",
      \"bibId\":\"$ID1\",
      \"pickupBranch\":\"Nord\",
      \"status\":\"PLACED\",
      \"position\":1
    }")
  # estraggo l'id robustamente
  HID=$(echo "$HOLD_JSON" | jq -r 'try .id catch .[0].id')
fi

[[ -n "$HID" ]] || { echo "✖ Creazione HOLD fallita"; exit 1; }
echo "   -> HID = $HID"

##### 4. Ordine su libro 2 #####################################################
echo "▶ 4. Crea ordine su ID2"
OID=$(curl -s -X POST "$BASE/orders" \
      -H "Content-Type: application/json" \
      -d "{
        \"bookId\":\"$ID2\",
        \"patronId\":42,
        \"quantity\":2
      }" \
    | json '.id')
echo "   -> OID = $OID"

##### 5. Cancel + Mark-paid ####################################################
echo "▶ 5. PUT /cancel (204 atteso)"
curl -s -o /dev/null -w '%{http_code}' \
     -X PUT "$BASE/orders/$OID/cancel" \
  | grep -q '^204$' && echo "   -> cancellazione OK"

echo "▶ 6. PUT /mark-paid (409 atteso)"
curl -s -o /dev/null -w '%{http_code}' \
     -X PUT "$BASE/orders/$OID/mark-paid?gatewayRef=MOCK" \
  | grep -q '^409$' && echo "   -> business-rule OK (già CANCELLATO)"

# assert finale
curl -s "$BASE/orders/$OID" \
  | jq -e '.status == "CANCELLED"' >/dev/null
echo "✅ Regression-suite completata con successo"
