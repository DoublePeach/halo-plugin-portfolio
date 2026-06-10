#!/bin/sh
# Backend reconciler validation (admin:admin @ :8090)
set -e
AUTH="admin:admin"
BASE="http://127.0.0.1:8090/apis/portfolio.plugin.halo.run/v1alpha1"

wait_reconcile() { sleep 2; }

cleanup() {
  for name in $CLEANUP_PROJECTS; do
    curl -s -u "$AUTH" -X DELETE "$BASE/portfolioprojects/$name" >/dev/null || true
  done
  for name in $CLEANUP_OPTIONS; do
    curl -s -u "$AUTH" -X DELETE "$BASE/portfoliooptions/$name" >/dev/null || true
  done
  for name in $CLEANUP_PORTFOLIOS; do
    curl -s -u "$AUTH" -X DELETE "$BASE/portfolios/$name" >/dev/null || true
  done
  wait_reconcile
}
trap cleanup EXIT

CLEANUP_PORTFOLIOS=""
CLEANUP_PROJECTS=""
CLEANUP_OPTIONS=""

json_field() {
  echo "$1" | sed -n "s/.*\"$2\":\([^,}]*\).*/\1/p" | head -1 | tr -d '"'
}

echo "=== 1. Slug conflict detection ==="
CREATE=$(curl -s -u "$AUTH" -X POST "$BASE/portfolios" \
  -H "Content-Type: application/json" \
  -d '{"apiVersion":"portfolio.plugin.halo.run/v1alpha1","kind":"Portfolio","metadata":{"generateName":"portfolio-"},"spec":{"displayName":"Dup Slug","slug":"default","publicView":true}}')
DUP_NAME=$(json_field "$CREATE" name)
CLEANUP_PORTFOLIOS="$DUP_NAME"
wait_reconcile
DUP=$(curl -s -u "$AUTH" "$BASE/portfolios/$DUP_NAME")
SLUG_CONFLICT=$(json_field "$DUP" slugConflict)
echo "slugConflict=$SLUG_CONFLICT (expect true)"
test "$SLUG_CONFLICT" = "true"

echo "=== 2. Portfolio delete blocked when children exist ==="
PF=$(curl -s -u "$AUTH" -X POST "$BASE/portfolios" \
  -H "Content-Type: application/json" \
  -d '{"apiVersion":"portfolio.plugin.halo.run/v1alpha1","kind":"Portfolio","metadata":{"generateName":"portfolio-"},"spec":{"displayName":"Delete Guard","slug":"delete-guard-test","publicView":true}}')
GUARD_NAME=$(json_field "$PF" name)
CLEANUP_PORTFOLIOS="$CLEANUP_PORTFOLIOS $GUARD_NAME"
CHILD=$(curl -s -u "$AUTH" -X POST "$BASE/portfolioprojects" \
  -H "Content-Type: application/json" \
  -d "{\"apiVersion\":\"portfolio.plugin.halo.run/v1alpha1\",\"kind\":\"PortfolioProject\",\"metadata\":{\"generateName\":\"portfolio-project-\"},\"spec\":{\"portfolioName\":\"$GUARD_NAME\",\"title\":\"Child\",\"published\":false}}")
CHILD_NAME=$(json_field "$CHILD" name)
CLEANUP_PROJECTS="$CHILD_NAME"
wait_reconcile
curl -s -u "$AUTH" -X DELETE "$BASE/portfolios/$GUARD_NAME" >/dev/null
wait_reconcile
TERM=$(curl -s -u "$AUTH" "$BASE/portfolios/$GUARD_NAME")
DELETION_TS=$(json_field "$TERM" deletionTimestamp)
echo "deletionTimestamp present (expect yes)"
test -n "$DELETION_TS"

echo "=== 3. projectCount excludes deleting projects ==="
DEFAULT=$(curl -s -u "$AUTH" "$BASE/portfolios/default-portfolio")
BEFORE=$(json_field "$DEFAULT" projectCount)
PROJ=$(curl -s -u "$AUTH" -X POST "$BASE/portfolioprojects" \
  -H "Content-Type: application/json" \
  -d '{"apiVersion":"portfolio.plugin.halo.run/v1alpha1","kind":"PortfolioProject","metadata":{"generateName":"portfolio-project-"},"spec":{"portfolioName":"default-portfolio","title":"Count Test","published":false}}')
PROJ_NAME=$(json_field "$PROJ" name)
CLEANUP_PROJECTS="$CLEANUP_PROJECTS $PROJ_NAME"
wait_reconcile
AFTER_CREATE=$(curl -s -u "$AUTH" "$BASE/portfolios/default-portfolio")
COUNT_CREATE=$(json_field "$AFTER_CREATE" projectCount)
echo "projectCount after create: $COUNT_CREATE (was $BEFORE)"
curl -s -u "$AUTH" -X DELETE "$BASE/portfolioprojects/$PROJ_NAME" >/dev/null
wait_reconcile
AFTER_DEL=$(curl -s -u "$AUTH" "$BASE/portfolios/default-portfolio")
COUNT_DEL=$(json_field "$AFTER_DEL" projectCount)
echo "projectCount after delete: $COUNT_DEL (expect $BEFORE)"
test "$COUNT_DEL" = "$BEFORE"

echo "=== 4. invalidPortfolio on orphan project ==="
ORPHAN=$(curl -s -u "$AUTH" -X POST "$BASE/portfolioprojects" \
  -H "Content-Type: application/json" \
  -d '{"apiVersion":"portfolio.plugin.halo.run/v1alpha1","kind":"PortfolioProject","metadata":{"generateName":"portfolio-project-"},"spec":{"portfolioName":"non-existent-portfolio","title":"Orphan","published":false}}')
ORPHAN_NAME=$(json_field "$ORPHAN" name)
CLEANUP_PROJECTS="$CLEANUP_PROJECTS $ORPHAN_NAME"
wait_reconcile
ORPHAN_GET=$(curl -s -u "$AUTH" "$BASE/portfolioprojects/$ORPHAN_NAME")
INVALID=$(json_field "$ORPHAN_GET" invalidPortfolio)
echo "invalidPortfolio=$INVALID (expect true)"
test "$INVALID" = "true"

echo "=== 5. Option validation (schema + reconciler) ==="
BAD_HTTP=$(curl -s -o /tmp/badopt.json -w "%{http_code}" -u "$AUTH" -X POST "$BASE/portfoliooptions" \
  -H "Content-Type: application/json" \
  -d '{"apiVersion":"portfolio.plugin.halo.run/v1alpha1","kind":"PortfolioOption","metadata":{"generateName":"portfolio-option-"},"spec":{"portfolioName":"default-portfolio","type":"BAD_TYPE","value":"x","label":"Bad","sortOrder":0}}')
echo "invalid type HTTP $BAD_HTTP (expect 400)"
test "$BAD_HTTP" = "400"
DUP=$(curl -s -u "$AUTH" -X POST "$BASE/portfoliooptions" \
  -H "Content-Type: application/json" \
  -d '{"apiVersion":"portfolio.plugin.halo.run/v1alpha1","kind":"PortfolioOption","metadata":{"generateName":"portfolio-option-"},"spec":{"portfolioName":"default-portfolio","type":"TECH_STACK","value":"python","label":"Dup Python","sortOrder":99}}')
DUP_NAME=$(json_field "$DUP" name)
CLEANUP_OPTIONS="$DUP_NAME"
wait_reconcile
DUP_GET=$(curl -s -u "$AUTH" "$BASE/portfoliooptions/$DUP_NAME")
DUPLICATE=$(json_field "$DUP_GET" duplicateValue)
echo "duplicateValue=$DUPLICATE (expect true)"
test "$DUPLICATE" = "true"

echo "=== ALL BACKEND VALIDATION TESTS PASSED ==="
