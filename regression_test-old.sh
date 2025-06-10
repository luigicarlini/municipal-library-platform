#!/usr/bin/env bash
set -euo pipefail
BASE=${BASE:-http://localhost:8080}
JQ=${JQ:-jq}

green='\e[32m'; red='\e[31m'; bold='\e[1m'; reset='\e[0m'
pass(){ echo -e "${green}✔ $*${reset}"; }
fail(){ echo -e "${red}✖ $*${reset}"; exit 1; }
step(){ echo -e "\n${bold}▶ $*${reset}"; }

len() {
  local j=$1 k=$2
  echo "$j" | $JQ -r '
    if type=="array"        then length
    elif has("_embedded")   then ._embedded[$k]|length
    else .[$k]? // []|length
    end' --arg k "$k"
}

contains_id() {
  local j=$1 id=$2
  echo "$j" | $JQ -e '
    ( if type=="array"
      then .
      elif has("_embedded") then ._embedded.holds
      else .holds? // []
      end )
    | any(.[]?; .id == "'"$id"'")'
}

# ----- pre-check ---------------------------------------------------
ID1=ef89e9be-70df-4730-813c-e6f035121ca8
curl -fs "$BASE/books/$ID1" >/dev/null || fail "Libro Zanna Bianca mancante!"

# ----- BOOKS alias -------------------------------------------------
step "BOOKS – alias legacy"
json=$(curl -fs "$BASE/books/search/find-by-title?title=zanna")
[ "$(len "$json" books)" -gt 0 ] && pass "find-by-title OK" || fail "find-by-title KO"

json=$(curl -fs "$BASE/books/search/find-by-author?author=london")
echo "$json" | $JQ -e '
 (if type=="array" then . else ._embedded.books end)
 | any(.author|ascii_downcase|test("london"))' >/dev/null \
 && pass "find-by-author OK" || fail "find-by-author KO"

json=$(curl -fs "$BASE/books/search/find-by-genre?genre=Inesistente")
[ "$(len "$json" books)" -eq 0 ] && pass "find-by-genre (vuoto) OK" || fail "find-by-genre (vuoto) KO"

# ----- HOLDS alias -------------------------------------------------
step "HOLDS – alias legacy"
HID=$(curl -fsG "$BASE/holds" \
        --data-urlencode "author=Jack London" \
        --data-urlencode "status=PLACED" | $JQ -r '.[0].id')
[ -n "$HID" ] || fail "Hold Jack London mancante"

json=$(curl -fs "$BASE/holds/search/find-by-title?title=zanna")
contains_id "$json" "$HID" && pass "holds find-by-title OK" || fail "holds find-by-title KO"

json=$(curl -fs "$BASE/holds/search/find-by-author?author=london")
contains_id "$json" "$HID" && pass "holds find-by-author OK" || fail "holds find-by-author KO"

json=$(curl -fs "$BASE/holds/search/find-by-author?author=Nessuno")
[ "$(len "$json" holds)" -eq 0 ] && pass "holds find-by-author (vuoto) OK" \
                                  || fail "holds find-by-author (vuoto) KO"

echo -e "\n${bold}${green}✅ Back-compat regression suite COMPLETATA${reset}"
