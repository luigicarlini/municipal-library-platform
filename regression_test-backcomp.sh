#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# regression_test-old.sh  –  Suite “back-compat” sugli alias HAL legacy
# ---------------------------------------------------------------------------
set -euo pipefail
BASE=${BASE:-http://localhost:8080}
JQ=${JQ:-jq}

green='\e[32m'; red='\e[31m'; bold='\e[1m'; reset='\e[0m'
pass () { echo -e "${green}✔ $*${reset}"; }
fail () { echo -e "${red}✖ $*${reset}"; exit 1; }
step () { echo -e "\n${bold}▶ $*${reset}"; }

# ------------------------------------------------------------------
# 0. Pre-check: il libro “Zanna Bianca” deve esistere
# ------------------------------------------------------------------
ID1=ef89e9be-70df-4730-813c-e6f035121ca8
curl -fs "$BASE/books/$ID1" >/dev/null 2>&1 \
  || fail "Libro di controllo (ID=$ID1) non trovato – popola il DB!"

# ------------------------------------------------------------------
# 1. BOOKS – alias legacy
# ------------------------------------------------------------------
step "BOOKS – alias legacy"

curl -sf "$BASE/books/search/find-by-title?title=zanna" |
  $JQ -e '._embedded.books | length > 0' >/dev/null &&
  pass "find-by-title restituisce risultati" ||
  fail "find-by-title KO"

curl -sf "$BASE/books/search/find-by-author?author=london" |
  $JQ -e '._embedded.books[].author | ascii_downcase | contains("london")' >/dev/null &&
  pass "find-by-author contiene Jack London" ||
  fail "find-by-author KO"

curl -sf "$BASE/books/search/find-by-genre?genre=Inesistente" |
  $JQ -e '._embedded.books? // [] | length == 0' >/dev/null &&
  pass "find-by-genre (vuoto) OK" ||
  fail "find-by-genre (vuoto) KO"

# ------------------------------------------------------------------
# 2. HOLDS – alias legacy
# ------------------------------------------------------------------
# 2.2 holds /search/find-by-author (Jack London deve comparire)
resp=$(curl -sf "$BASE/holds/search/find-by-author?author=london")

# se è HAL → estrai _embedded.holds  – altrimenti usa l’array così com’è
holds=$(echo "$resp" | $JQ -e '._embedded.holds? // .')

echo "$holds" | $JQ -e "map(.id) | index(\"$HID\")" >/dev/null \
  && pass "holds find-by-author eseguito" \
  || fail "holds find-by-author KO"

# 2.3  autore certamente inesistente (UUID random)
RND_AUTHOR=$(uuidgen)
curl -sf "$BASE/holds/search/find-by-author?author=$RND_AUTHOR" |
  $JQ -e 'length == 0' >/dev/null &&
  pass "holds find-by-author (empty) OK" ||
  fail "holds find-by-author (empty) KO"

# ------------------------------------------------------------------
echo -e "\n${bold}${green}✅ Back-compat regression suite completata con successo${reset}"
