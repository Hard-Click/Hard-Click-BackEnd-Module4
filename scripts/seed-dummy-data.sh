#!/usr/bin/env bash
# 랭킹 / 순공·잔디(daily_study_stats) / 강사 소개(members) / 강의·레슨·카탈로그 더미데이터 시드 스크립트.
#
# 실행 위치: EC2 호스트의 docker-compose.prod.yml 이 있는 디렉터리 (.env 를 그 자리에서 읽음)
# 실행 방법: bash scripts/seed-dummy-data.sh
#
# 대상이 아닌 것 — 별도 시드 불필요:
#   - 퀴즈(quiz): QuizMockController가 DB 없이 하드코딩된 목데이터를 반환하므로 시드 대상 아님.
#
# 전제:
#   - docker-compose.prod.yml 기준 컨테이너명 hard-click-mysql / hard-click-redis 가 실행 중
#   - .env 에 DB_USERNAME, DB_PASSWORD, REDIS_PASSWORD 존재
#   - members 테이블에 role='STUDENT' / role='INSTRUCTOR' 데이터가 이미 있어야 함 (회원 자체는 생성 안 함)
#
# FE 보고 이슈 해결 항목:
#   #5  instructorName 빈값  — course.author_id 가 가리키는 강사에 name 보정
#   #6  memberName 빈값      — name='' 인 회원에 임시 이름 부여
#   #7  isPreview 0개        — lesson.order_index=0 레슨 + course_curriculum/video 카탈로그 sync
#   #8  videoId 2+ 404       — 강의별 레슨 3개 보장 후 카탈로그 동기화

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
if [ -f "$SCRIPT_DIR/.env" ]; then
    set -a
    # shellcheck disable=SC1091
    source "$SCRIPT_DIR/.env"
    set +a
fi

: "${DB_USERNAME:?DB_USERNAME 환경변수/.env 필요}"
: "${DB_PASSWORD:?DB_PASSWORD 환경변수/.env 필요}"
: "${REDIS_PASSWORD:?REDIS_PASSWORD 환경변수/.env 필요}"

DB_SCHEMA="Hard-Click"
RANKING_KEY_PREFIX="prod:ranking"
STUDENT_LIMIT=10
INSTRUCTOR_LIMIT=5

mysql_exec() {
    docker exec -i hard-click-mysql mysql --default-character-set=utf8mb4 -uroot -p"$DB_PASSWORD" -D "$DB_SCHEMA" -N -B "$@"
}

redis_exec() {
    docker exec -i hard-click-redis redis-cli -a "$REDIS_PASSWORD" --no-auth-warning "$@"
}

echo "[1/4] 대상 회원 조회 중..."
STUDENT_IDS=$(mysql_exec -e "SELECT member_id FROM members WHERE role='STUDENT' ORDER BY member_id LIMIT $STUDENT_LIMIT;")
INSTRUCTOR_IDS=$(mysql_exec -e "SELECT member_id FROM members WHERE role='INSTRUCTOR' ORDER BY member_id LIMIT $INSTRUCTOR_LIMIT;")

if [ -z "$STUDENT_IDS" ]; then
    echo "STUDENT 회원이 없어 잔디/랭킹 시드를 건너뜁니다."
else
    echo "[2/4] daily_study_stats(순공·잔디) 시드 중... (대상 학생 수: $(echo "$STUDENT_IDS" | wc -l))"

    SQL_FILE=$(mktemp)
    {
        echo "DELETE FROM daily_study_stats WHERE member_id IN ($(echo "$STUDENT_IDS" | paste -sd, -)) AND stat_date >= DATE_SUB(CURDATE(), INTERVAL 35 DAY);"
        for member_id in $STUDENT_IDS; do
            for day_offset in $(seq 0 34); do
                # 약 30% 확률로 그날은 학습 안 함(잔디 빈 칸)을 만들기 위해 스킵
                if [ $(( RANDOM % 10 )) -lt 3 ]; then
                    continue
                fi
                lesson_count=$(( (RANDOM % 5) + 1 ))
                completed_count=$(( RANDOM % (lesson_count + 1) ))
                study_seconds=$(( (lesson_count * 600) + (RANDOM % 1800) ))
                echo "INSERT INTO daily_study_stats (member_id, stat_date, watched_lesson_count, study_seconds, completed_lesson_count, created_at, updated_at) VALUES ($member_id, DATE_SUB(CURDATE(), INTERVAL $day_offset DAY), $lesson_count, $study_seconds, $completed_count, NOW(), NOW()) ON DUPLICATE KEY UPDATE watched_lesson_count=VALUES(watched_lesson_count), study_seconds=VALUES(study_seconds), completed_lesson_count=VALUES(completed_lesson_count), updated_at=NOW();"
            done
        done
    } > "$SQL_FILE"
    mysql_exec < "$SQL_FILE"
    rm -f "$SQL_FILE"

    echo "[3/4] 랭킹(Redis ZSET) 시드 중..."
    for period in daily weekly monthly; do
        case "$period" in
            daily)   days=1 ;;
            weekly)  days=7 ;;
            monthly) days=30 ;;
        esac
        for member_id in $STUDENT_IDS; do
            study_total=$(mysql_exec -e "SELECT COALESCE(SUM(study_seconds),0) FROM daily_study_stats WHERE member_id=$member_id AND stat_date >= DATE_SUB(CURDATE(), INTERVAL $days DAY);")
            lesson_total=$(mysql_exec -e "SELECT COALESCE(SUM(watched_lesson_count),0) FROM daily_study_stats WHERE member_id=$member_id AND stat_date >= DATE_SUB(CURDATE(), INTERVAL $days DAY);")
            accepted_comment_total=$(( RANDOM % 20 ))

            redis_exec ZADD "${RANKING_KEY_PREFIX}:study-time:${period}" "$study_total" "$member_id" > /dev/null
            redis_exec ZADD "${RANKING_KEY_PREFIX}:lessons:${period}" "$lesson_total" "$member_id" > /dev/null
            redis_exec ZADD "${RANKING_KEY_PREFIX}:accepted-comments:${period}" "$accepted_comment_total" "$member_id" > /dev/null
        done
    done
fi

if [ -z "$INSTRUCTOR_IDS" ]; then
    echo "INSTRUCTOR 회원이 없어 강사 소개 시드를 건너뜁니다."
else
    echo "[4/4] 강사 소개(members.one_line_intro/introduction/career) 시드 중..."

    # members 테이블에 강사 소개 컬럼이 아직 없을 수 있어 안전하게 추가 (이미 있으면 무시)
    mysql_exec -e "
        SET @col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='$DB_SCHEMA' AND TABLE_NAME='members' AND COLUMN_NAME='one_line_intro');
        SET @ddl := IF(@col=0, 'ALTER TABLE members ADD COLUMN one_line_intro VARCHAR(100) CHARACTER SET utf8mb4', 'SELECT 1');
        PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

        SET @col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='$DB_SCHEMA' AND TABLE_NAME='members' AND COLUMN_NAME='introduction');
        SET @ddl := IF(@col=0, 'ALTER TABLE members ADD COLUMN introduction TEXT CHARACTER SET utf8mb4', 'SELECT 1');
        PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

        SET @col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='$DB_SCHEMA' AND TABLE_NAME='members' AND COLUMN_NAME='career');
        SET @ddl := IF(@col=0, 'ALTER TABLE members ADD COLUMN career TEXT CHARACTER SET utf8mb4', 'SELECT 1');
        PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

        ALTER TABLE members MODIFY COLUMN one_line_intro VARCHAR(100) CHARACTER SET utf8mb4;
        ALTER TABLE members MODIFY COLUMN introduction TEXT CHARACTER SET utf8mb4;
        ALTER TABLE members MODIFY COLUMN career TEXT CHARACTER SET utf8mb4;
    "

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
fi

echo "완료: 잔디/순공 ${STUDENT_LIMIT}명, 랭킹 daily/weekly/monthly x 3지표, 강사소개 ${INSTRUCTOR_LIMIT}명"

# ──────────────────────────────────────────────────────────────────────────────
# FE 이슈 #5 #6: name='' 인 회원 이름 보정 (장바구니·찜 강사명 / 랭킹 이름 빈값)
# ──────────────────────────────────────────────────────────────────────────────
echo "[5/8] 빈 이름 회원 보정 중..."
mysql_exec -e "
UPDATE members
SET name = CONCAT(CASE role WHEN 'INSTRUCTOR' THEN '강사' WHEN 'STUDENT' THEN '수강생' ELSE '회원' END,
                  '_', member_id)
WHERE name = '' OR name IS NULL;
"

# ──────────────────────────────────────────────────────────────────────────────
# FE 이슈 #7 #8: 강의별 레슨 3개 보장 + course_curriculum/video 카탈로그 sync
# (isPreview — order_index=0 레슨이 있어야 미리보기 표시됨)
# (videoId 2+ 404 — lesson.id 가 video_id 이므로 레슨이 여러 개 있어야 함)
# ──────────────────────────────────────────────────────────────────────────────
echo "[6/8] 강의별 레슨 seed 중 (섹션 없으면 생성, 레슨 3개 보장)..."

COURSE_IDS=$(mysql_exec -e "SELECT course_id FROM course WHERE status='PUBLISHED' ORDER BY course_id LIMIT 10;")

if [ -z "$COURSE_IDS" ]; then
    echo "PUBLISHED 강의가 없어 레슨 시드를 건너뜁니다."
else
    LESSON_SQL=$(mktemp)
    {
        for course_id in $COURSE_IDS; do
            # 섹션이 없으면 order_index=0 섹션 생성
            echo "INSERT IGNORE INTO course_section (course_id, title, order_index)
                  SELECT $course_id, '1장. 시작하기', 0
                  WHERE NOT EXISTS (SELECT 1 FROM course_section WHERE course_id=$course_id AND order_index=0);"

            # order_index=0 섹션의 id 조회해서 레슨 3개 삽입 (이미 있으면 건너뜀)
            echo "SET @sec0 := (SELECT id FROM course_section WHERE course_id=$course_id AND order_index=0 LIMIT 1);"

            # 레슨 0: 미리보기용 (order_index=0) — isPreview 결정 기준
            echo "INSERT INTO lesson (section_id, title, description, order_index, video_url, s3_key, duration_seconds, created_at)
                  SELECT @sec0, '1강. 오리엔테이션', '강의 소개 및 환경 설정', 0,
                         'https://example.com/preview.mp4', 'courses/$course_id/preview.mp4', 300, NOW()
                  WHERE @sec0 IS NOT NULL
                    AND NOT EXISTS (SELECT 1 FROM lesson WHERE section_id=@sec0 AND order_index=0);"

            # 레슨 1
            echo "INSERT INTO lesson (section_id, title, description, order_index, video_url, s3_key, duration_seconds, created_at)
                  SELECT @sec0, '2강. 핵심 개념 정리', '이론 설명', 1,
                         'https://example.com/lesson1.mp4', 'courses/$course_id/lesson1.mp4', 600, NOW()
                  WHERE @sec0 IS NOT NULL
                    AND NOT EXISTS (SELECT 1 FROM lesson WHERE section_id=@sec0 AND order_index=1);"

            # 레슨 2
            echo "INSERT INTO lesson (section_id, title, description, order_index, video_url, s3_key, duration_seconds, created_at)
                  SELECT @sec0, '3강. 실습 프로젝트', '직접 구현해보기', 2,
                         'https://example.com/lesson2.mp4', 'courses/$course_id/lesson2.mp4', 900, NOW()
                  WHERE @sec0 IS NOT NULL
                    AND NOT EXISTS (SELECT 1 FROM lesson WHERE section_id=@sec0 AND order_index=2);"
        done
    } > "$LESSON_SQL"
    mysql_exec < "$LESSON_SQL"
    rm -f "$LESSON_SQL"

    echo "[7/8] course_curriculum / video 카탈로그 sync 중..."
    CATALOG_SQL=$(mktemp)
    {
        echo "-- course_curriculum (section 미러링)"
        echo "INSERT INTO course_curriculum (curriculum_id, course_id, order_index)
              SELECT cs.id, cs.course_id, cs.order_index
              FROM course_section cs
              WHERE cs.course_id IN ($(echo "$COURSE_IDS" | paste -sd, -))
              ON DUPLICATE KEY UPDATE order_index = VALUES(order_index);"

        echo "-- video (lesson 미러링, is_preview: 섹션 order_index 최소 && 레슨 order_index 최소)"
        echo "INSERT INTO video (video_id, curriculum_id, s3_key, duration_seconds, sort_order, is_preview)
              SELECT l.id, l.section_id, l.s3_key, l.duration_seconds, l.order_index,
                     (cs.order_index = (SELECT MIN(cs2.order_index) FROM course_section cs2 WHERE cs2.course_id = cs.course_id)
                      AND l.order_index = (SELECT MIN(l2.order_index) FROM lesson l2 WHERE l2.section_id = cs.id))
              FROM lesson l
              JOIN course_section cs ON l.section_id = cs.id
              WHERE cs.course_id IN ($(echo "$COURSE_IDS" | paste -sd, -))
              ON DUPLICATE KEY UPDATE
                  curriculum_id    = VALUES(curriculum_id),
                  s3_key           = VALUES(s3_key),
                  duration_seconds = VALUES(duration_seconds),
                  sort_order       = VALUES(sort_order),
                  is_preview       = VALUES(is_preview);"
    } > "$CATALOG_SQL"
    mysql_exec < "$CATALOG_SQL"
    rm -f "$CATALOG_SQL"
fi

echo "[8/8] 완료"
echo "완료: 이름 보정, 레슨 seed, 카탈로그 sync 완료"
