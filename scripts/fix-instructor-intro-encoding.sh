#!/usr/bin/env bash
# seed-dummy-data.sh로 들어간 강사소개 3필드(one_line_intro/introduction/career)가
# mysql 클라이언트 charset 미지정(latin1 기본 연결)으로 mojibake 저장된 것만 재시드.
# 잔디/랭킹 데이터는 정상이라 건드리지 않음.
#
# 실행 위치: ~/hard-click (docker-compose.prod.yml / .env 있는 디렉터리)
# 실행 방법: bash scripts/fix-instructor-intro-encoding.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
if [ -f "$SCRIPT_DIR/.env" ]; then
    set -a
    # shellcheck disable=SC1091
    source "$SCRIPT_DIR/.env"
    set +a
fi

: "${DB_PASSWORD:?DB_PASSWORD 환경변수/.env 필요}"

DB_SCHEMA="Hard-Click"
INSTRUCTOR_LIMIT=5

mysql_exec() {
    docker exec -i hard-click-mysql mysql --default-character-set=utf8mb4 -uroot -p"$DB_PASSWORD" -D "$DB_SCHEMA" -N -B "$@"
}

echo "[1/2] 컬럼 charset을 utf8mb4로 보정..."
mysql_exec -e "
    ALTER TABLE members MODIFY COLUMN one_line_intro VARCHAR(100) CHARACTER SET utf8mb4;
    ALTER TABLE members MODIFY COLUMN introduction TEXT CHARACTER SET utf8mb4;
    ALTER TABLE members MODIFY COLUMN career TEXT CHARACTER SET utf8mb4;
"

echo "[2/2] 강사소개 3필드 재시드..."
INSTRUCTOR_IDS=$(mysql_exec -e "SELECT member_id FROM members WHERE role='INSTRUCTOR' ORDER BY member_id LIMIT $INSTRUCTOR_LIMIT;")

if [ -z "$INSTRUCTOR_IDS" ]; then
    echo "INSTRUCTOR 회원이 없습니다."
    exit 0
fi

ONE_LINERS=(
    "10년차 프론트엔드 개발자, 실무 위주 강의를 합니다"
    "백엔드/인프라 전문, 대규모 트래픽 처리 경험 다수"
    "전직 빅테크 엔지니어, 코딩테스트 강의 전문"
    "풀스택 개발자 출신 강사, 실전 프로젝트 중심 강의"
    "데이터베이스/쿼리 최적화 전문가"
)
INTROS=(
    "안녕하세요, 현업에서 10년간 프론트엔드 개발을 해온 강사입니다. 실무에서 바로 쓸 수 있는 내용 위주로 강의를 구성했습니다."
    "백엔드 시스템 설계와 인프라 운영을 오래 해왔습니다. 이론보다 실제 장애 대응 경험을 강의에 많이 담았습니다."
    "빅테크에서 근무하며 쌓은 코딩테스트/알고리즘 노하우를 전달해 드립니다."
    "스타트업에서 풀스택으로 서비스를 처음부터 끝까지 만들어본 경험을 바탕으로 강의합니다."
    "수많은 슬로우 쿼리를 고쳐본 경험을 바탕으로 실전 DB 최적화를 알려드립니다."
)
CAREERS=(
    "前 OO테크 프론트엔드 리드 / 現 프리랜서 강사"
    "前 OO클라우드 백엔드 엔지니어 / 現 인프라 컨설턴트"
    "前 OO 빅테크 SWE / 現 코딩테스트 강사"
    "前 OO스타트업 CTO / 現 풀스택 강사"
    "前 OO데이터 DBA / 現 DB 강의 전문 강사"
)

idx=0
for member_id in $INSTRUCTOR_IDS; do
    i=$(( idx % ${#ONE_LINERS[@]} ))
    mysql_exec -e "UPDATE members SET one_line_intro='${ONE_LINERS[$i]}', introduction='${INTROS[$i]}', career='${CAREERS[$i]}' WHERE member_id=$member_id;"
    idx=$(( idx + 1 ))
done

echo "완료. 확인:"
mysql_exec -e "SELECT member_id, one_line_intro FROM members WHERE member_id IN ($(echo "$INSTRUCTOR_IDS" | paste -sd, -));"
