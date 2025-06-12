#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# all_tests.sh – lancia in sequenza le suite/regression script del progetto
# ---------------------------------------------------------------------------
# Ordine di esecuzione:
#   1) regression_test-old.sh         (back-compat alias HAL)
#   2) regression_test.sh             (API corrente)
#   3) concurrency_rest_conflict.sh   (optimistic-locking 409)
#   4) soft_hard_delete_demo.sh       (soft-/hard-delete libro)
#
# Se uno script fallisce (exit≠0) lo stoppa immediatamente.
# ---------------------------------------------------------------------------

set -euo pipefail

green=$'\e[32m'
red=$'\e[31m'
bold=$'\e[1m'
reset=$'\e[0m'

run() {
  local name="$1"
  shift
  echo -e "${bold}▶ Avvio: ${name}${reset}"
  "$@"            # esegue lo script passato come argomento
  echo -e "  ${green}✔ Terminato: ${name}${reset}\n"
}

# percorso base (se gli script si trovano nella stessa cartella di questo)
SCRIPTS_DIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

run "Nuova API regression"          "$SCRIPTS_DIR/regression_test.sh"
run "Optimistic-locking demo"       "$SCRIPTS_DIR/concurrency_rest_conflict.sh"
run "Soft/Hard-delete demo"         "$SCRIPTS_DIR/soft_hard_delete_demo.sh"
run "Back-compat regression"        "$SCRIPTS_DIR/regression_test-old.sh"

echo -e "${bold}${green}🎉 Tutti i test completati con successo.${reset}"