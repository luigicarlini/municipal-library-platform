#!/usr/bin/env bash
set -euo pipefail
BASE=${BASE:-http://localhost:8080}
green=$'\e[32m'; red=$'\e[31m'; bold=$'\e[1m'; reset=$'\e[0m'
ok(){   echo "${green}✔ $*${reset}"; }
die(){  echo "${red}✖ $*${reset}"; exit 1; }

gen_isbn() {
  base="97$((RANDOM%2+8))$(printf '%09d' $RANDOM$RANDOM | tail -c 9)"
  sum=0
  for i in {0..11}; do d=${base:i:1}; (( sum += (i%2==0?d:d*3) )); done
  echo "${base}$(( (10-sum%10)%10 ))"
}

echo "${bold}→ crea libro di test…${reset}"
ISBN=$(gen_isbn)
BOOK_JSON=$(curl -sf -X POST "$BASE/books" -H 'Content-Type: application/json' -d "{
  \"title\":\"Concurrency-REST\",\"author\":\"Tester\",\"genre\":\"Demo\",
  \"publicationYear\":2024,\"isbn\":\"$ISBN\",\"price\":10,\"stockQuantity\":1
}") || die "POST /books failed"

ID=$(echo "$BOOK_JSON" | jq -r .id)

payload(){                       # ricarica e +1 € di prezzo
  curl -sf "$BASE/books/$ID" |
  jq '.price = (.price + 1) | {id,title,author,genre,
      publicationYear,isbn,price,stockQuantity,version}'
}
update(){ payload | curl -s -w '%{http_code}' -o /dev/null \
                    -X PUT "$BASE/books/$ID" -H 'Content-Type: application/json' -d @-; }

echo "→ lancio due PUT concorrenti…"
c1=$(update & pid1=$!; wait $pid1; echo $?)
c2=$(update & pid2=$!; wait $pid2; echo $?)

echo "HTTP codes: $c1 , $c2"
([[ $c1 == 409 || $c2 == 409 ]]) && ok "optimistic-lock confermato" \
                                 || die "nessun 409: verifica la colonna version"
