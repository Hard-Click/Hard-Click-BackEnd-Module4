-- study_timer_sessions.status 컬럼을 ENUM → VARCHAR(20)으로 변경
-- 배경: 원래 ENUM('RUNNING','PAUSED','ENDED')로 생성돼 있어서
--       PAUSED/CANCELED 저장 시 실패하는 환경이 있음.
-- 적용 대상: 로컬 MySQL, 배포 서버 등 직접 ALTER가 필요한 환경
-- (ddl-auto: update는 컬럼 타입을 변경하지 않으므로 수동 실행 필요)

ALTER TABLE study_timer_sessions
    MODIFY COLUMN status VARCHAR(20) NOT NULL;
