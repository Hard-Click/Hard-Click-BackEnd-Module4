#!/bin/bash
# 신고 자동차단 동시성 버그 재현 스크립트 (디버그 출력 포함)
# 사전조건: reports 테이블에 member_id=3072 대상 신고 49건이 이미 들어있어야 함 (race window 직전 상태)

BASE_URL="http://localhost:8080"
TARGET_POST_ID=49150
SSE_LOG="sse_capture.log"

login() {
    local username=$1
    curl -s -X POST "$BASE_URL/api/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"$username\",\"password\":\"Test1234!\"}"
}

extract_token() {
    grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4
}

echo "== 1. SSE 관찰자(3072=bench_user_1)로 로그인 =="
SSE_LOGIN_RESP=$(login "bench_user_1")
SSE_TOKEN=$(echo "$SSE_LOGIN_RESP" | extract_token)
echo "SSE 로그인 응답: $SSE_LOGIN_RESP"
echo "SSE_TOKEN 길이: ${#SSE_TOKEN}"

echo "== 2. SSE 연결 시작 (백그라운드, ${SSE_LOG}에 기록) =="
rm -f "$SSE_LOG"
curl -s -N --max-time 20 \
    -H "Authorization: Bearer $SSE_TOKEN" \
    "$BASE_URL/api/members/me/status-stream" > "$SSE_LOG" &
SSE_PID=$!
sleep 2
echo "SSE 연결 직후 로그 내용:"
cat "$SSE_LOG"
echo "---"

echo "== 3. 신고자 5명(bench_user_50~54) 로그인 =="
declare -a TOKENS
for i in 50 51 52 53 54; do
    RESP=$(login "bench_user_$i")
    TOKEN=$(echo "$RESP" | extract_token)
    TOKENS+=("$TOKEN")
    echo "bench_user_$i 로그인 응답: $RESP"
    echo "bench_user_$i 토큰 길이: ${#TOKEN}"
done

echo "== 4. 동시에 신고 5건 발사 (50번째~54번째 신고) =="
idx=0
for TOKEN in "${TOKENS[@]}"; do
    idx=$((idx+1))
    curl -s -X POST "$BASE_URL/api/reports" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "{\"targetType\":\"POST\",\"targetId\":$TARGET_POST_ID,\"reportTypes\":[\"SPAM\"],\"reason\":\"concurrency test\"}" \
        > "report_resp_$idx.json" 2>&1 &
done
wait
echo "5건 신고 요청 전송 완료. 각 응답:"
for idx in 1 2 3 4 5; do
    echo "  [$idx] $(cat report_resp_$idx.json)"
done

echo "SSE 수신 대기..."
sleep 5
kill $SSE_PID 2>/dev/null || true

echo ""
echo "== 5. 결과 확인 =="
EVENT_COUNT=$(grep -c "MEMBER_STATUS_CHANGED" "$SSE_LOG" || true)
echo "MEMBER_STATUS_CHANGED 이벤트 수신 횟수: $EVENT_COUNT"
echo "(기대값: Before 코드 = 여러 번, After 코드 = 정확히 1번)"
echo ""
echo "원본 SSE 로그:"
cat "$SSE_LOG"
