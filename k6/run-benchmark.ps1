# ============================================================
# 게시글 목록 병목 최적화 Before/After 자동 벤치마크
# 박종준 담당: 방법2 Batch IN+Map / 방법3 JOIN Projection
#
# 사용법:
#   cd "C:\Users\user\Desktop\MODULE04_PAYMENT\.claude\worktrees\inspiring-bhaskara-ec30e4"
#   .\k6\run-benchmark.ps1 [-DatadogUrl "https://app.datadoghq.com/apm/traces"]
#
# 사전 조건:
#   - MySQL localhost:3306 (Hard-Click DB) 실행 중
#   - Redis localhost:6379 실행 중
#   - k6 설치됨 (winget install k6)
#   - Grafana 실행 중 (localhost:3000)
#   - Edge/Chrome 기본 브라우저로 설정
# ============================================================
param(
    [string]$DatadogUrl = "https://ap1.datadoghq.com/apm/traces"
)

$WORKTREE    = "C:\Users\user\Desktop\MODULE04_PAYMENT\.claude\worktrees\inspiring-bhaskara-ec30e4"
$RESULTS_DIR = "$WORKTREE\k6\results"
$REPORT_FILE = "$WORKTREE\k6\benchmark-report.md"
$SERVER_PORT = 8080
$BASE_URL    = "http://localhost:$SERVER_PORT"
$GRAFANA_URL = "http://localhost:3000"
$DASHBOARD   = "post-comment-stats-6panel"

# DB / Spring 필수 환경변수 (이미 시스템에 설정돼 있으면 덮어쓰지 않음)
if (-not $env:DB_USERNAME)     { $env:DB_USERNAME     = "Hard-Click" }
if (-not $env:DB_PASSWORD)     { $env:DB_PASSWORD     = "Hard-Click" }
if (-not $env:MAIL_USERNAME)   { $env:MAIL_USERNAME   = "noreply@local" }  # 벤치마크 중 메일 미사용
if (-not $env:REDIS_PASSWORD)  { $env:REDIS_PASSWORD  = "" }               # 로컬 Redis 무인증
$env:SPRING_PROFILES_ACTIVE = "local"

Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing

function Log($msg) { Write-Host "$(Get-Date -Format 'HH:mm:ss') $msg" -ForegroundColor Cyan }
function Ok($msg)  { Write-Host "$(Get-Date -Format 'HH:mm:ss') OK $msg" -ForegroundColor Green }
function Err($msg) { Write-Host "$(Get-Date -Format 'HH:mm:ss') ERR $msg" -ForegroundColor Red }

function Wait-Server {
    param([int]$TimeoutSec = 240, [string]$LogFile = "")
    Log "서버 기동 대기 중 (최대 ${TimeoutSec}초)..."
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    while ((Get-Date) -lt $deadline) {
        # TCP 포트 체크 (HTTP 인증 문제 우회)
        try {
            $tcp = New-Object System.Net.Sockets.TcpClient
            $tcp.ConnectAsync("localhost", $SERVER_PORT).Wait(2000) | Out-Null
            if ($tcp.Connected) { $tcp.Close(); Start-Sleep 3; Ok "서버 포트 $SERVER_PORT 응답"; return $true }
        } catch {}

        # 즉시 크래시 감지 (로그에서 APPLICATION FAILED 체크)
        if ($LogFile -and (Test-Path $LogFile)) {
            $crashed = Select-String "APPLICATION FAILED TO START|BUILD FAILED|Exception in thread" $LogFile -Quiet
            if ($crashed) {
                Err "서버 즉시 크래시 감지"
                Write-Host "=== 서버 로그 (마지막 30줄) ===" -ForegroundColor Yellow
                Get-Content $LogFile -Tail 30
                return $false
            }
        }
        Start-Sleep 5
    }
    # 타임아웃 시 로그 출력
    if ($LogFile -and (Test-Path $LogFile)) {
        Write-Host "=== 서버 로그 (마지막 30줄) ===" -ForegroundColor Yellow
        Get-Content $LogFile -Tail 30
    }
    Err "서버 기동 타임아웃 ${TimeoutSec}초"
    return $false
}

$script:ServerJob = $null

function Start-AppServer {
    param([string]$Method)
    Log "서버 시작 (benchmark.method=$Method)"
    $env:BENCHMARK_METHOD = $Method
    $logFile = "$RESULTS_DIR\server-$Method.log"
    "" | Out-File $logFile -Encoding UTF8

    $script:ServerJob = Start-Job -ScriptBlock {
        param($wt, $log, $du, $dp, $mu, $rp, $bm)
        Set-Location $wt
        $env:DB_USERNAME     = $du;  $env:DB_PASSWORD    = $dp
        $env:MAIL_USERNAME   = $mu;  $env:REDIS_PASSWORD = $rp
        $env:BENCHMARK_METHOD = $bm
        & "$wt\gradlew.bat" bootRun --no-daemon 2>&1 | Tee-Object -FilePath $log
    } -ArgumentList $WORKTREE, $logFile, $env:DB_USERNAME, $env:DB_PASSWORD,
                    $env:MAIL_USERNAME, $env:REDIS_PASSWORD, $Method

    Log "서버 Job $($script:ServerJob.Id) 시작 | 로그: $logFile"
}

function Stop-Server {
    Log "서버 종료 중..."
    if ($script:ServerJob) {
        Stop-Job  $script:ServerJob -PassThru -EA SilentlyContinue | Out-Null
        Remove-Job $script:ServerJob -Force  -EA SilentlyContinue
        $script:ServerJob = $null
    }
    # 혹시 남은 Java 프로세스 중 포트 8080 점유한 것 종료
    $pids = netstat -ano | Select-String ":8080.*LISTEN" |
            ForEach-Object { ($_ -split '\s+')[-1] }
    foreach ($p in $pids) {
        Stop-Process -Id $p -Force -EA SilentlyContinue
    }
    Start-Sleep 3
}

function Run-K6 {
    param([string]$Stage, [string]$OutJson)
    Log "k6 실행 (STAGE=$Stage)..."
    $env:K6_WEB_DASHBOARD = "false"
    $k6Args = @(
        "run",
        "-e", "BASE_URL=$BASE_URL",
        "-e", "STAGE=$Stage",
        "-e", "SCENARIO=comments",
        "--summary-export=$OutJson",
        "$WORKTREE\k6\post-list-load-test.js"
    )
    & k6 @k6Args
    return $LASTEXITCODE
}

function Parse-K6 {
    param([string]$JsonPath)
    if (-not (Test-Path $JsonPath)) {
        return @{ p95 = 0; tps = 0; err = 0; reqs = 0 }
    }
    $j    = Get-Content $JsonPath -Raw | ConvertFrom-Json
    $p95  = [math]::Round($j.metrics.'post_list_latency'.values.'p(95)', 1)
    $tps  = [math]::Round($j.metrics.'http_reqs'.values.rate, 1)
    $err  = [math]::Round($j.metrics.'post_list_errors'.values.rate * 100, 2)
    $reqs = [int]$j.metrics.'http_reqs'.values.count
    return @{ p95 = $p95; tps = $tps; err = $err; reqs = $reqs }
}

function Get-PctImprove {
    param([double]$Base, [double]$New)
    if ($Base -eq 0) { return "N/A" }
    return [math]::Round((1 - $New / $Base) * 100, 1).ToString() + "%"
}

# 전체화면 스크린샷 -> PNG 저장
function Save-Screenshot {
    param([string]$OutPath)
    $bounds = [System.Windows.Forms.Screen]::PrimaryScreen.Bounds
    $bmp    = New-Object System.Drawing.Bitmap($bounds.Width, $bounds.Height)
    $gfx    = [System.Drawing.Graphics]::FromImage($bmp)
    $gfx.CopyFromScreen($bounds.Location, [System.Drawing.Point]::Empty, $bounds.Size)
    $bmp.Save($OutPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $gfx.Dispose()
    $bmp.Dispose()
    Ok "스크린샷 저장: $OutPath"
}

# 브라우저 열고 -> 대기 -> 스크린샷
function Capture-Grafana {
    param(
        [string]$Stage,
        [long]$FromMs,
        [long]$ToMs
    )
    $outFile = "$RESULTS_DIR\grafana-$Stage.png"
    $url = "$GRAFANA_URL/d/$DASHBOARD/?from=$FromMs&to=$ToMs&orgId=1&kiosk=tv&theme=dark"
    Log "Grafana 캡처 ($Stage)..."
    Start-Process $url
    Start-Sleep 9    # Grafana 렌더링 대기
    Save-Screenshot -OutPath $outFile
}

function Capture-Datadog {
    param(
        [string]$Stage,
        [long]$FromMs,
        [long]$ToMs
    )
    $outFile = "$RESULTS_DIR\datadog-$Stage.png"
    # Datadog uses millisecond timestamps; preserve query/col settings from existing session
    $ddBase  = $DatadogUrl
    $ddQuery = "query=env%3Alocal&agg_m=count&agg_m_source=base&agg_t=count" +
               "&cols=core_service%2Ccore_resource_name%2Clog_duration%2Clog_http.method%2Clog_http.status_code" +
               "&graphType=waterfall&spanType=all&storage=driveline&viz=stream" +
               "&paused=true&start=${FromMs}&end=${ToMs}"
    $url = "${ddBase}?${ddQuery}"
    Log "Datadog APM 캡처 ($Stage)..."
    Start-Process $url
    Start-Sleep 12   # Datadog 로딩 대기
    Save-Screenshot -OutPath $outFile
}

# ── 사전 준비 ────────────────────────────────────────────────────────────────

New-Item -ItemType Directory -Force -Path $RESULTS_DIR | Out-Null

if (-not (Get-Command k6 -EA SilentlyContinue)) {
    Err "k6 미설치. 설치: winget install k6"
    exit 1
}

# 이전 실행에서 남은 좀비 서버 정리
$leftover = netstat -ano | Select-String ":$SERVER_PORT.*LISTEN"
if ($leftover) {
    Log "포트 $SERVER_PORT 이미 사용 중 -- 이전 서버 종료 중..."
    $pids = $leftover | ForEach-Object { ($_ -split '\s+')[-1] }
    foreach ($p in $pids) { Stop-Process -Id $p -Force -EA SilentlyContinue }
    Start-Sleep 3
}
Get-Job | Stop-Job -PassThru -EA SilentlyContinue | Remove-Job -Force -EA SilentlyContinue

# ── 1단계: Before (N+1, 21쿼리) ─────────────────────────────────────────────

Log "[1/3] BEFORE -- N+1 (21쿼리)"
$null = Start-AppServer -Method "before"
if (-not (Wait-Server -LogFile "$RESULTS_DIR\server-before.log")) { Stop-Server; exit 1 }

$beforeStart = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
$rc = Run-K6 -Stage "before" -OutJson "$RESULTS_DIR\before.json"
$beforeEnd = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()

if ($rc -ne 0) { Err "k6 Before 실패 (rc=$rc)" }
Stop-Server
$before = Parse-K6 -JsonPath "$RESULTS_DIR\before.json"
Ok "Before: P95=$($before.p95)ms TPS=$($before.tps) ERR=$($before.err)%"

Capture-Grafana -Stage "before" -FromMs $beforeStart -ToMs $beforeEnd

# ── 2단계: 방법2 (Batch IN + Map, 3쿼리) ────────────────────────────────────

Log "[2/3] 방법2 -- Batch IN+Map (3쿼리)"
$null = Start-AppServer -Method "method2"
if (-not (Wait-Server -LogFile "$RESULTS_DIR\server-method2.log")) { Stop-Server; exit 1 }

$m2Start = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
$rc = Run-K6 -Stage "method2" -OutJson "$RESULTS_DIR\method2.json"
$m2End = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()

if ($rc -ne 0) { Err "k6 방법2 실패 (rc=$rc)" }
Stop-Server
$m2 = Parse-K6 -JsonPath "$RESULTS_DIR\method2.json"
Ok "방법2: P95=$($m2.p95)ms TPS=$($m2.tps) ERR=$($m2.err)%"

Capture-Grafana -Stage "method2" -FromMs $m2Start -ToMs $m2End

# ── 3단계: 방법3 (JOIN Projection, 1쿼리) ───────────────────────────────────

Log "[3/3] 방법3 -- JOIN Projection (1쿼리)"
$null = Start-AppServer -Method "method3"
if (-not (Wait-Server -LogFile "$RESULTS_DIR\server-method3.log")) { Stop-Server; exit 1 }

$m3Start = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
$rc = Run-K6 -Stage "method3" -OutJson "$RESULTS_DIR\method3.json"
$m3End = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()

if ($rc -ne 0) { Err "k6 방법3 실패 (rc=$rc)" }
Stop-Server
$m3 = Parse-K6 -JsonPath "$RESULTS_DIR\method3.json"
Ok "방법3: P95=$($m3.p95)ms TPS=$($m3.tps) ERR=$($m3.err)%"

Capture-Grafana -Stage "method3" -FromMs $m3Start -ToMs $m3End

# ── Datadog 스크린샷 (Before / After) ───────────────────────────────────────

Log "Datadog APM 캡처 (Before)..."
Capture-Datadog -Stage "before" -FromMs $beforeStart -ToMs $beforeEnd

Start-Sleep 3

Log "Datadog APM 캡처 (method3)..."
Capture-Datadog -Stage "method3" -FromMs $m3Start -ToMs $m3End

# ── 타임스탬프 JSON 저장 ─────────────────────────────────────────────────────

$timestamps = @{
    before  = @{ startMs = $beforeStart; endMs = $beforeEnd }
    method2 = @{ startMs = $m2Start;    endMs = $m2End     }
    method3 = @{ startMs = $m3Start;    endMs = $m3End     }
} | ConvertTo-Json
$timestamps | Out-File "$RESULTS_DIR\timestamps.json" -Encoding UTF8

# ── 리포트 생성 ──────────────────────────────────────────────────────────────

$now   = Get-Date -Format "yyyy-MM-dd HH:mm"
$imp2  = Get-PctImprove -Base $before.p95 -New $m2.p95
$imp3  = Get-PctImprove -Base $before.p95 -New $m3.p95
$gain2 = [math]::Round($m2.tps - $before.tps, 1)
$gain3 = [math]::Round($m3.tps - $before.tps, 1)

if ($before.p95 -le 200 -and $before.err -le 1) { $slo0 = "Pass" } else { $slo0 = "Fail" }
if ($m2.p95     -le 200 -and $m2.err     -le 1) { $slo2 = "Pass" } else { $slo2 = "Fail" }
if ($m3.p95     -le 200 -and $m3.err     -le 1) { $slo3 = "Pass" } else { $slo3 = "Fail" }

function Write-Report {
    param([string]$Path)

    $b_p95  = $before.p95;  $b_tps  = $before.tps;  $b_err  = $before.err;  $b_reqs = $before.reqs
    $m2_p95 = $m2.p95;      $m2_tps = $m2.tps;      $m2_err = $m2.err;      $m2_reqs = $m2.reqs
    $m3_p95 = $m3.p95;      $m3_tps = $m3.tps;      $m3_err = $m3.err;      $m3_reqs = $m3.reqs

    Set-Content  $Path -Encoding UTF8 -Value "# Post List API Bottleneck Benchmark Report"
    Add-Content  $Path -Encoding UTF8 -Value "> Date: $now"
    Add-Content  $Path -Encoding UTF8 -Value "> Endpoint: GET /api/boards/FREE/posts?sort=comments"
    Add-Content  $Path -Encoding UTF8 -Value "> Owner: Jongjun Park (Method2, Method3)"
    Add-Content  $Path -Encoding UTF8 -Value "> k6: 20 to 100 to 0 VUs ramping, 80sec"
    Add-Content  $Path -Encoding UTF8 -Value ""
    Add-Content  $Path -Encoding UTF8 -Value "---"
    Add-Content  $Path -Encoding UTF8 -Value ""
    Add-Content  $Path -Encoding UTF8 -Value "## Results"
    Add-Content  $Path -Encoding UTF8 -Value ""
    Add-Content  $Path -Encoding UTF8 -Value "| Method | Queries | P95 latency | TPS | Error rate | SLO(P95<=200ms) |"
    Add-Content  $Path -Encoding UTF8 -Value "|--------|---------|-------------|-----|------------|-----------------|"
    Add-Content  $Path -Encoding UTF8 -Value "| Before (N+1 + correlated subquery) | 21 | ${b_p95} ms | ${b_tps} req/s | ${b_err}% | $slo0 |"
    Add-Content  $Path -Encoding UTF8 -Value "| Method2 (Batch IN+Map)             |  3 | ${m2_p95} ms | ${m2_tps} req/s | ${m2_err}% | $slo2 |"
    Add-Content  $Path -Encoding UTF8 -Value "| Method3 (JOIN Projection)          |  1 | ${m3_p95} ms | ${m3_tps} req/s | ${m3_err}% | $slo3 |"
    Add-Content  $Path -Encoding UTF8 -Value ""
    Add-Content  $Path -Encoding UTF8 -Value "### P95 improvement"
    Add-Content  $Path -Encoding UTF8 -Value "- Method2 vs Before: ${b_p95}ms -> ${m2_p95}ms ($imp2 reduction)"
    Add-Content  $Path -Encoding UTF8 -Value "- Method3 vs Before: ${b_p95}ms -> ${m3_p95}ms ($imp3 reduction)"
    Add-Content  $Path -Encoding UTF8 -Value ""
    Add-Content  $Path -Encoding UTF8 -Value "### TPS increase"
    Add-Content  $Path -Encoding UTF8 -Value "- Method2 vs Before: +$gain2 req/s"
    Add-Content  $Path -Encoding UTF8 -Value "- Method3 vs Before: +$gain3 req/s"
    Add-Content  $Path -Encoding UTF8 -Value ""
    Add-Content  $Path -Encoding UTF8 -Value "---"
    Add-Content  $Path -Encoding UTF8 -Value ""
    Add-Content  $Path -Encoding UTF8 -Value "## Implementation"
    Add-Content  $Path -Encoding UTF8 -Value ""
    Add-Content  $Path -Encoding UTF8 -Value "### Before (21 queries) - N+1 original"
    Add-Content  $Path -Encoding UTF8 -Value '```java'
    Add-Content  $Path -Encoding UTF8 -Value "String name = memberNamePort.getNameByMemberId(post.getAuthorId()); // SELECT x N"
    Add-Content  $Path -Encoding UTF8 -Value "int cnt = commentRepository.countByPostId(post.getId());            // COUNT  x N"
    Add-Content  $Path -Encoding UTF8 -Value "// 10 posts per page -> 1 + 10 + 10 = 21 queries"
    Add-Content  $Path -Encoding UTF8 -Value '```'
    Add-Content  $Path -Encoding UTF8 -Value ""
    Add-Content  $Path -Encoding UTF8 -Value "### Method2 (3 queries) - Batch IN + Map"
    Add-Content  $Path -Encoding UTF8 -Value '```java'
    Add-Content  $Path -Encoding UTF8 -Value "Map<Long,String> nameMap = memberNamePort.getNamesByMemberIds(authorIds); // 1 IN query"
    Add-Content  $Path -Encoding UTF8 -Value "Map<Long,Long>   cntMap  = commentRepository.countsByPostIds(postIds);    // 1 GROUP BY"
    Add-Content  $Path -Encoding UTF8 -Value "// 10 posts -> 1(list) + 1(IN) + 1(GROUP BY) = 3 queries"
    Add-Content  $Path -Encoding UTF8 -Value '```'
    Add-Content  $Path -Encoding UTF8 -Value ""
    Add-Content  $Path -Encoding UTF8 -Value "### Method3 (1 query) - JOIN + DTO Projection"
    Add-Content  $Path -Encoding UTF8 -Value '```sql'
    Add-Content  $Path -Encoding UTF8 -Value "SELECT p.id, p.board_type, p.title, m.name, p.created_at, p.view_count, COUNT(c.id)"
    Add-Content  $Path -Encoding UTF8 -Value "FROM posts p"
    Add-Content  $Path -Encoding UTF8 -Value "JOIN members m ON m.id = p.author_id"
    Add-Content  $Path -Encoding UTF8 -Value "LEFT JOIN comments c ON c.post_id = p.id"
    Add-Content  $Path -Encoding UTF8 -Value "WHERE p.board_type = ? AND p.status = 'ACTIVE'"
    Add-Content  $Path -Encoding UTF8 -Value "GROUP BY p.id, p.board_type, p.title, m.name, p.created_at, p.view_count"
    Add-Content  $Path -Encoding UTF8 -Value "ORDER BY COUNT(c.id) DESC"
    Add-Content  $Path -Encoding UTF8 -Value '```'
    Add-Content  $Path -Encoding UTF8 -Value ""
    Add-Content  $Path -Encoding UTF8 -Value "---"
    Add-Content  $Path -Encoding UTF8 -Value ""
    Add-Content  $Path -Encoding UTF8 -Value "## Raw k6 numbers"
    Add-Content  $Path -Encoding UTF8 -Value ""
    Add-Content  $Path -Encoding UTF8 -Value "| Stage   | Requests | P95 | TPS | Error |"
    Add-Content  $Path -Encoding UTF8 -Value "|---------|----------|-----|-----|-------|"
    Add-Content  $Path -Encoding UTF8 -Value "| Before  | ${b_reqs} | ${b_p95}ms | $b_tps | ${b_err}% |"
    Add-Content  $Path -Encoding UTF8 -Value "| Method2 | ${m2_reqs} | ${m2_p95}ms | $m2_tps | ${m2_err}% |"
    Add-Content  $Path -Encoding UTF8 -Value "| Method3 | ${m3_reqs} | ${m3_p95}ms | $m3_tps | ${m3_err}% |"
    Add-Content  $Path -Encoding UTF8 -Value ""
    Add-Content  $Path -Encoding UTF8 -Value "---"
    Add-Content  $Path -Encoding UTF8 -Value ""
    Add-Content  $Path -Encoding UTF8 -Value "## Screenshots"
    Add-Content  $Path -Encoding UTF8 -Value ""
    Add-Content  $Path -Encoding UTF8 -Value "Grafana: results/grafana-before.png / grafana-method2.png / grafana-method3.png"
    Add-Content  $Path -Encoding UTF8 -Value "Datadog: results/datadog-before.png / datadog-method3.png"
    Add-Content  $Path -Encoding UTF8 -Value ""
    Add-Content  $Path -Encoding UTF8 -Value "---"
    Add-Content  $Path -Encoding UTF8 -Value "*Generated by run-benchmark.ps1*"
}

Write-Report -Path $REPORT_FILE

# ── 완료 출력 ────────────────────────────────────────────────────────────────

$b_p95  = $before.p95; $b_tps  = $before.tps
$m2_p95 = $m2.p95;     $m2_tps = $m2.tps
$m3_p95 = $m3.p95;     $m3_tps = $m3.tps

Write-Host ""
Write-Host "======================================================" -ForegroundColor Yellow
Write-Host " Benchmark complete!" -ForegroundColor Yellow
Write-Host "======================================================"
Write-Host ""
Write-Host " Before  P95: ${b_p95} ms  (TPS $b_tps)"
Write-Host " Method2 P95: ${m2_p95} ms  (TPS $m2_tps)  <- $imp2 improvement"
Write-Host " Method3 P95: ${m3_p95} ms  (TPS $m3_tps)  <- $imp3 improvement"
Write-Host ""
Write-Host " Report:      $REPORT_FILE" -ForegroundColor Green
Write-Host " Screenshots: $RESULTS_DIR\" -ForegroundColor Green
Write-Host "------------------------------------------------------"
Write-Host " grafana-before.png / grafana-method2.png / grafana-method3.png" -ForegroundColor Green
Write-Host " datadog-before.png / datadog-method3.png" -ForegroundColor Green
Write-Host "======================================================"
