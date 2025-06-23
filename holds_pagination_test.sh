#!/usr/bin/env bash
# Simple smoke test for /holds pagination

BASE=http://localhost:8080

echo "▶ Total count:"
TOTAL=$(curl -s -I "$BASE/holds" | awk '/X-Total-Count/ {print $2}')
echo "   $TOTAL record(s)"

PAGE_SIZE=10
PAGES=$(( (TOTAL + PAGE_SIZE - 1) / PAGE_SIZE ))

echo "▶ Fetching $PAGES page(s)…"
for ((i=0;i<PAGES;i++)); do
  echo -n "  page $i → "
  curl -s "$BASE/holds?page=$i&size=$PAGE_SIZE" | jq -c '. | length'
done
echo "✓ done"
