#!/bin/sh
set -e
AUTH="admin:admin"
BASE="http://127.0.0.1:8090/apis/portfolio.plugin.halo.run/v1alpha1"

restore_sample_project() {
  PROJECT=$(curl -s -u "$AUTH" "$BASE/portfolioprojects/sample-ai-bill-2025")
  VERSION=$(echo "$PROJECT" | sed -n 's/.*"version":\([0-9]*\).*/\1/p' | head -1)
  curl -s -u "$AUTH" -X PUT "$BASE/portfolioprojects/sample-ai-bill-2025" \
    -H "Content-Type: application/json" \
    -d "{\"apiVersion\":\"portfolio.plugin.halo.run/v1alpha1\",\"kind\":\"PortfolioProject\",\"metadata\":{\"name\":\"sample-ai-bill-2025\",\"version\":$VERSION},\"spec\":{\"portfolioName\":\"default-portfolio\",\"title\":\"2025 数智化年度账单\",\"summary\":\"基于 VibeCoding 快速交付的 AI 应用，面向企业内部数智化场景。\",\"coverImage\":\"\",\"gallery\":[],\"domain\":\"ai-vibe\",\"techStack\":[\"python\",\"fastapi\",\"vue\"],\"source\":\"company\",\"featured\":true,\"priority\":100,\"startDate\":\"2025-01-01T00:00:00Z\",\"endDate\":\"2025-03-01T00:00:00Z\",\"published\":true}}" >/dev/null
}

echo "=== Portfolio UPDATE without version (should fail) ==="
PF=$(curl -s -u "$AUTH" "$BASE/portfolios/default-portfolio")
PF_VER=$(echo "$PF" | sed -n 's/.*"version":\([0-9]*\).*/\1/p' | head -1)
HTTP=$(curl -s -o /tmp/pfbad.json -w "%{http_code}" -u "$AUTH" -X PUT "$BASE/portfolios/default-portfolio" \
  -H "Content-Type: application/json" \
  -d '{"apiVersion":"portfolio.plugin.halo.run/v1alpha1","kind":"Portfolio","metadata":{"name":"default-portfolio"},"spec":{"displayName":"Test","slug":"default","publicView":true,"priority":100}}')
echo "HTTP $HTTP (expect 500)"

echo "=== Portfolio UPDATE with version (should succeed) ==="
HTTP=$(curl -s -o /tmp/pfgood.json -w "%{http_code}" -u "$AUTH" -X PUT "$BASE/portfolios/default-portfolio" \
  -H "Content-Type: application/json" \
  -d "{\"apiVersion\":\"portfolio.plugin.halo.run/v1alpha1\",\"kind\":\"Portfolio\",\"metadata\":{\"name\":\"default-portfolio\",\"version\":$PF_VER},\"spec\":{\"displayName\":\"默认作品集\",\"slug\":\"default\",\"description\":\"示例作品集，包含演示项目\",\"publicView\":true,\"priority\":100},\"status\":{\"projectCount\":$(echo "$PF" | sed -n 's/.*"projectCount":\([0-9]*\).*/\1/p' | head -1)}}")
echo "HTTP $HTTP (expect 200)"

echo "=== Project edit flow (fetch then update with version) ==="
CREATE=$(curl -s -u "$AUTH" -X POST "$BASE/portfolioprojects" \
  -H "Content-Type: application/json" \
  -d '{"apiVersion":"portfolio.plugin.halo.run/v1alpha1","kind":"PortfolioProject","metadata":{"generateName":"portfolio-project-"},"spec":{"portfolioName":"default-portfolio","title":"Flow Test","published":false}}')
NEW_NAME=$(echo "$CREATE" | sed -n 's/.*"name":"\([^"]*\)".*/\1/p' | head -1)
sleep 1
FRESH=$(curl -s -u "$AUTH" "$BASE/portfolioprojects/$NEW_NAME")
NEW_VER=$(echo "$FRESH" | sed -n 's/.*"version":\([0-9]*\).*/\1/p' | head -1)
HTTP=$(curl -s -o /tmp/flow.json -w "%{http_code}" -u "$AUTH" -X PUT "$BASE/portfolioprojects/$NEW_NAME" \
  -H "Content-Type: application/json" \
  -d "{\"apiVersion\":\"portfolio.plugin.halo.run/v1alpha1\",\"kind\":\"PortfolioProject\",\"metadata\":{\"name\":\"$NEW_NAME\",\"version\":$NEW_VER},\"spec\":{\"portfolioName\":\"default-portfolio\",\"title\":\"Flow Updated\",\"published\":true}}")
echo "HTTP $HTTP (expect 200)"
curl -s -u "$AUTH" -X DELETE "$BASE/portfolioprojects/$NEW_NAME" >/dev/null

echo "=== Project count after delete ==="
COUNT=$(curl -s -u "$AUTH" "$BASE/portfolios/default-portfolio" | sed -n 's/.*"projectCount":\([0-9]*\).*/\1/p' | head -1)
echo "projectCount=$COUNT"

restore_sample_project
echo "=== ALL TESTS DONE ==="
