#!/usr/bin/env bash
set -euo pipefail
BASE=${BASE:-http://localhost:8080}
JQ=${JQ:-jq}

# ------------------ ISBN helper (13 cifre + check) ------------------
rand_isbn() {
  local body
  body=$(tr -dc 0-9 < /dev/urandom | head -c 12)
  local sum=0 w=1 d
  for ((i=0; i<12; i++)); do
      d=${body:i:1}
      (( sum += d * w, w = 4 - w ))      # 1,3,1,3…
  done
  local cd=$(( (10 - sum % 10) % 10 ))
  echo "${body}${cd}"
}

ISBN=$(rand_isbn)

# ------------------ 1. create book ------------------
payload_book=$(jq -n --arg isbn "$ISBN" '
  {title:"LockDemo", author:"T", genre:"Test", publicationYear:2024,
   isbn:$isbn, price:10, stockQuantity:1}')
book_json=$(curl -fsS -X POST "$BASE/books" \
               -H 'Content-Type: application/json' \
               -d "$payload_book")

book_id=$(echo "$book_json" | $JQ -r .id)
version=$(echo "$book_json" | $JQ -r .version)
echo "→ bookId=$book_id version=$version (ISBN=$ISBN)"

# ------------------ 2. two competing updates ------------------
payload1=$(echo "$book_json" | \
          jq '.price=11 | .version = '"$version")
payload2=$(echo "$book_json" | \
          jq '.price=11 | .version = '"$version")

# send in background
code1_file=$(mktemp); code2_file=$(mktemp)
curl -s -o /dev/null -w '%{http_code}' -X PUT "$BASE/books/$book_id" \
     -H 'Content-Type: application/json' -d "$payload1" >"$code1_file" &
curl -s -o /dev/null -w '%{http_code}' -X PUT "$BASE/books/$book_id" \
     -H 'Content-Type: application/json' -d "$payload2" >"$code2_file" &
wait

code1=$(cat "$code1_file"); code2=$(cat "$code2_file")
rm -f "$code1_file" "$code2_file"
echo "HTTP codes: $code1 , $code2"

if [[ "$code1" == 409 || "$code2" == 409 ]]; then
  echo -e "\e[32m✔ ottimistic-locking dimostrato\e[0m"
else
  echo -e "\e[33mℹ nessun 409; riprova (race non scattata)\e[0m"
fi
