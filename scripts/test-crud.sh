#!/bin/sh
set -e
AUTH="admin:admin"
BASE="http://127.0.0.1:8090/apis/portfolio.plugin.halo.run/v1alpha1"

echo "=== LIST portfolios ==="
curl -s -u "$AUTH" "$BASE/portfolios" | head -c 200
echo ""

echo "=== LIST projects (fieldSelector string) ==="
curl -s -u "$AUTH" "$BASE/portfolioprojects?page=1&size=500&fieldSelector=spec.portfolioName=default-portfolio" | head -c 200
echo ""

echo "=== LIST options ==="
curl -s -u "$AUTH" "$BASE/portfoliooptions?page=1&size=500&fieldSelector=spec.portfolioName=default-portfolio" | head -c 200
echo ""

echo "=== GET project sample-ai-bill-2025 ==="
PROJECT=$(curl -s -u "$AUTH" "$BASE/portfolioprojects/sample-ai-bill-2025")
VERSION=$(echo "$PROJECT" | sed -n 's/.*"version":\([0-9]*\).*/\1/p' | head -1)
echo "version=$VERSION"

echo "=== PUT without version (should fail) ==="
HTTP=$(curl -s -o /tmp/bad.json -w "%{http_code}" -u "$AUTH" -X PUT "$BASE/portfolioprojects/sample-ai-bill-2025" \
  -H "Content-Type: application/json" \
  -d '{"apiVersion":"portfolio.plugin.halo.run/v1alpha1","kind":"PortfolioProject","metadata":{"name":"sample-ai-bill-2025"},"spec":{"portfolioName":"default-portfolio","title":"CRUD Test No Version","published":true}}')
echo "HTTP $HTTP"
head -c 300 /tmp/bad.json
echo ""

echo "=== PUT with version (should succeed) ==="
HTTP=$(curl -s -o /tmp/good.json -w "%{http_code}" -u "$AUTH" -X PUT "$BASE/portfolioprojects/sample-ai-bill-2025" \
  -H "Content-Type: application/json" \
  -d "{\"apiVersion\":\"portfolio.plugin.halo.run/v1alpha1\",\"kind\":\"PortfolioProject\",\"metadata\":{\"name\":\"sample-ai-bill-2025\",\"version\":$VERSION},\"spec\":{\"portfolioName\":\"default-portfolio\",\"title\":\"CRUD Test With Version\",\"summary\":\"test\",\"published\":true,\"featured\":true,\"priority\":100,\"startDate\":\"2025-01-01T00:00:00Z\",\"endDate\":\"2025-03-01T00:00:00Z\",\"domain\":\"ai-vibe\",\"techStack\":[\"python\"],\"source\":\"company\"}}")
echo "HTTP $HTTP"
head -c 300 /tmp/good.json
echo ""

echo "=== CREATE project ==="
CREATE=$(curl -s -u "$AUTH" -X POST "$BASE/portfolioprojects" \
  -H "Content-Type: application/json" \
  -d '{"apiVersion":"portfolio.plugin.halo.run/v1alpha1","kind":"PortfolioProject","metadata":{"generateName":"portfolio-project-"},"spec":{"portfolioName":"default-portfolio","title":"CRUD Create Test","published":false}}')
NEW_NAME=$(echo "$CREATE" | sed -n 's/.*"name":"\([^"]*\)".*/\1/p' | head -1)
echo "created=$NEW_NAME"
echo "$CREATE" | head -c 200
echo ""

if [ -n "$NEW_NAME" ]; then
  NEW_VER=$(echo "$CREATE" | sed -n 's/.*"version":\([0-9]*\).*/\1/p' | head -1)
  echo "=== UPDATE created project ==="
  HTTP=$(curl -s -o /tmp/up.json -w "%{http_code}" -u "$AUTH" -X PUT "$BASE/portfolioprojects/$NEW_NAME" \
    -H "Content-Type: application/json" \
    -d "{\"apiVersion\":\"portfolio.plugin.halo.run/v1alpha1\",\"kind\":\"PortfolioProject\",\"metadata\":{\"name\":\"$NEW_NAME\",\"version\":$NEW_VER},\"spec\":{\"portfolioName\":\"default-portfolio\",\"title\":\"CRUD Updated\",\"published\":true}}")
  echo "HTTP $HTTP"
  head -c 200 /tmp/up.json
  echo ""

  echo "=== DELETE created project ==="
  HTTP=$(curl -s -o /tmp/del.json -w "%{http_code}" -u "$AUTH" -X DELETE "$BASE/portfolioprojects/$NEW_NAME")
  echo "HTTP $HTTP"
fi

echo "=== CREATE option ==="
OPT=$(curl -s -u "$AUTH" -X POST "$BASE/portfoliooptions" \
  -H "Content-Type: application/json" \
  -d '{"apiVersion":"portfolio.plugin.halo.run/v1alpha1","kind":"PortfolioOption","metadata":{"generateName":"portfolio-option-"},"spec":{"portfolioName":"default-portfolio","type":"TECH_STACK","value":"crud-test","label":"CRUD Test","sortOrder":99}}')
OPT_NAME=$(echo "$OPT" | sed -n 's/.*"name":"\([^"]*\)".*/\1/p' | head -1)
echo "option=$OPT_NAME HTTP create ok"

if [ -n "$OPT_NAME" ]; then
  OPT_VER=$(echo "$OPT" | sed -n 's/.*"version":\([0-9]*\).*/\1/p' | head -1)
  echo "=== UPDATE option ==="
  HTTP=$(curl -s -o /tmp/optup.json -w "%{http_code}" -u "$AUTH" -X PUT "$BASE/portfoliooptions/$OPT_NAME" \
    -H "Content-Type: application/json" \
    -d "{\"apiVersion\":\"portfolio.plugin.halo.run/v1alpha1\",\"kind\":\"PortfolioOption\",\"metadata\":{\"name\":\"$OPT_NAME\",\"version\":$OPT_VER},\"spec\":{\"portfolioName\":\"default-portfolio\",\"type\":\"TECH_STACK\",\"value\":\"crud-test\",\"label\":\"CRUD Updated\",\"sortOrder\":100}}")
  echo "HTTP $HTTP"

  echo "=== DELETE option ==="
  HTTP=$(curl -s -o /tmp/optdel.json -w "%{http_code}" -u "$AUTH" -X DELETE "$BASE/portfoliooptions/$OPT_NAME")
  echo "HTTP $HTTP"
fi

echo "=== DONE ==="
