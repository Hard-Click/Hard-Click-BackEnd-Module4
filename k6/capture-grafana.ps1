# ============================================================
# Grafana post-comment-stats-6panel 대시보드 스크린샷 자동 캡처
# 사용법: .\k6\capture-grafana.ps1 -Stage before|method2|method3
# ============================================================
param(
    [string]$Stage = "before"
)

$GRAFANA_URL  = "http://localhost:3000"
$GRAFANA_USER = "admin"
$GRAFANA_PASS = "admin"
$DASHBOARD_UID = "post-comment-stats-6panel"
$RESULTS_DIR  = "$PSScriptRoot\results"
$OUT_FILE     = "$RESULTS_DIR\grafana-$Stage.png"

New-Item -ItemType Directory -Force -Path $RESULTS_DIR | Out-Null

$cred = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("${GRAFANA_USER}:${GRAFANA_PASS}"))
$headers = @{ Authorization = "Basic $cred" }

# Grafana Image Renderer API 시도
$renderUrl = "$GRAFANA_URL/render/d/$DASHBOARD_UID/?orgId=1&width=1400&height=900&kiosk=tv&theme=dark&timeout=60"

Write-Host "Grafana 스크린샷 캡처 중 (Stage=$Stage)..." -ForegroundColor Cyan
try {
    Invoke-WebRequest -Uri $renderUrl -Headers $headers -OutFile $OUT_FILE -TimeoutSec 70 -UseBasicParsing
    Write-Host "✔ 저장: $OUT_FILE" -ForegroundColor Green
} catch {
    Write-Host "⚠ Grafana Image Renderer 미설치 — 수동 캡처 필요" -ForegroundColor Yellow
    Write-Host "  수동 URL: $GRAFANA_URL/d/$DASHBOARD_UID" -ForegroundColor Yellow
    Write-Host "  파일로 저장: $OUT_FILE" -ForegroundColor Yellow
    # 플레이스홀더 파일 생성 (나중에 수동으로 교체)
    "Grafana screenshot placeholder - Stage: $Stage - $(Get-Date)" | Out-File "$RESULTS_DIR\grafana-$Stage.txt"
}
