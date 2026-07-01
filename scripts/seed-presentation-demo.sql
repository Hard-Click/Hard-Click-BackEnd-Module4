-- ============================================================
-- Hard-Click LMS  발표 시연용 더미 데이터 시드 스크립트
-- ============================================================
-- 목적: 4막 시나리오 실시간 시연에 필요한 최소 데이터를 주입한다.
--
-- [jongjunn 파트] 4막 신고 자동정지 시나리오
--   막1: admin_demo가 GLOBAL 공지 등록 → 전체 학생+강사에게 SSE NOTICE 발송
--   막2: villain_demo가 FREE 게시판에 스팸 글·댓글 등록
--   막3: 수강생 60명이 신고 → 49개 사전 주입, 50번째는 실시간 시연
--   막4: 50번째 신고 클릭 → villain 계정 자동 정지(SUSPENDED)
--
-- [곽시윤 파트] 커뮤니티·신고처리·공지 시나리오
--   - 질문게시판: 6개 글(과목필터 데모), 채택 있는 글 1개, 이미지 첨부 1개
--   - 자유게시판: 3개 글
--   - 신고 목록: 5건(미처리4+처리완료1), 게시글/댓글/대댓글 혼합
--   - 이용제한 데모: 52회 신고 계정(ACTIVE) + 이미 제한된 계정(SUSPENDED)
--   - 공지: 전체공지 2개(상단고정 1) + 강의별 공지 1개
--
-- 전제 조건:
--   1. 기존 시드(seed-6-students.sql 등)가 이미 적용된 상태여야 한다.
--   2. 기존 member_id 최댓값은 ~5000대. 여기서는 9001+ 대역을 사용한다.
--   3. 실행: mysql -u <user> -p <db> < scripts/seed-presentation-demo.sql
--
-- 비밀번호: 모든 데모 계정은 Test1234! (bcrypt)
-- bcrypt hash: $2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. 데모 멤버 주입
-- ============================================================
INSERT IGNORE INTO members
    (member_id, username, email, password, name, gender, birth_date, phone_number,
     role, status, is_password_change_required, login_fail_count, is_locked,
     optional_terms_agreed, created_at, updated_at)
VALUES
-- 관리자
(9001, 'admin_demo',   'admin_demo@hardclick.dev',   '$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm', '데모관리자',  'M', '1985-03-15', '010-9001-0001', 'ADMIN',      'ACTIVE', 0, 0, 0, 1, NOW(), NOW()),
-- 강사 1 (수강생 공지 수신 테스트용)
(9002, 'inst_demo1',   'inst_demo1@hardclick.dev',   '$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm', '데모강사1',   'F', '1982-07-20', '010-9002-0001', 'INSTRUCTOR', 'ACTIVE', 0, 0, 0, 1, NOW(), NOW()),
-- 강사 2 (강의 개설자)
(9003, 'inst_demo2',   'inst_demo2@hardclick.dev',   '$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm', '데모강사2',   'M', '1980-11-05', '010-9003-0001', 'INSTRUCTOR', 'ACTIVE', 0, 0, 0, 1, NOW(), NOW()),
-- 빌런 학생 (스팸 포스터, 49개 신고 대상)
(9004, 'villain_demo', 'villain_demo@hardclick.dev', '$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm', '빌런데모',    'M', '2000-01-01', '010-9004-0001', 'STUDENT',    'ACTIVE', 0, 0, 0, 1, NOW(), NOW()),
-- 수강생 01~60
(9005, 'student_demo01','s01@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생01','F','2001-01-01','010-9005-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9006, 'student_demo02','s02@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생02','M','2001-01-02','010-9006-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9007, 'student_demo03','s03@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생03','F','2001-01-03','010-9007-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9008, 'student_demo04','s04@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생04','M','2001-01-04','010-9008-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9009, 'student_demo05','s05@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생05','F','2001-01-05','010-9009-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9010, 'student_demo06','s06@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생06','M','2001-01-06','010-9010-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9011, 'student_demo07','s07@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생07','F','2001-01-07','010-9011-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9012, 'student_demo08','s08@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생08','M','2001-01-08','010-9012-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9013, 'student_demo09','s09@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생09','F','2001-01-09','010-9013-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9014, 'student_demo10','s10@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생10','M','2001-01-10','010-9014-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9015, 'student_demo11','s11@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생11','F','2001-01-11','010-9015-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9016, 'student_demo12','s12@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생12','M','2001-01-12','010-9016-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9017, 'student_demo13','s13@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생13','F','2001-01-13','010-9017-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9018, 'student_demo14','s14@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생14','M','2001-01-14','010-9018-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9019, 'student_demo15','s15@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생15','F','2001-01-15','010-9019-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9020, 'student_demo16','s16@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생16','M','2001-01-16','010-9020-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9021, 'student_demo17','s17@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생17','F','2001-01-17','010-9021-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9022, 'student_demo18','s18@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생18','M','2001-01-18','010-9022-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9023, 'student_demo19','s19@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생19','F','2001-01-19','010-9023-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9024, 'student_demo20','s20@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생20','M','2001-01-20','010-9024-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9025, 'student_demo21','s21@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생21','F','2001-01-21','010-9025-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9026, 'student_demo22','s22@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생22','M','2001-01-22','010-9026-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9027, 'student_demo23','s23@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생23','F','2001-01-23','010-9027-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9028, 'student_demo24','s24@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생24','M','2001-01-24','010-9028-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9029, 'student_demo25','s25@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생25','F','2001-01-25','010-9029-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9030, 'student_demo26','s26@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생26','M','2001-01-26','010-9030-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9031, 'student_demo27','s27@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생27','F','2001-01-27','010-9031-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9032, 'student_demo28','s28@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생28','M','2001-01-28','010-9032-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9033, 'student_demo29','s29@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생29','F','2001-02-01','010-9033-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9034, 'student_demo30','s30@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생30','M','2001-02-02','010-9034-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9035, 'student_demo31','s31@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생31','F','2001-02-03','010-9035-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9036, 'student_demo32','s32@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생32','M','2001-02-04','010-9036-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9037, 'student_demo33','s33@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생33','F','2001-02-05','010-9037-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9038, 'student_demo34','s34@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생34','M','2001-02-06','010-9038-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9039, 'student_demo35','s35@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생35','F','2001-02-07','010-9039-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9040, 'student_demo36','s36@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생36','M','2001-02-08','010-9040-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9041, 'student_demo37','s37@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생37','F','2001-02-09','010-9041-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9042, 'student_demo38','s38@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생38','M','2001-02-10','010-9042-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9043, 'student_demo39','s39@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생39','F','2001-02-11','010-9043-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9044, 'student_demo40','s40@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생40','M','2001-02-12','010-9044-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9045, 'student_demo41','s41@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생41','F','2001-02-13','010-9045-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9046, 'student_demo42','s42@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생42','M','2001-02-14','010-9046-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9047, 'student_demo43','s43@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생43','F','2001-02-15','010-9047-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9048, 'student_demo44','s44@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생44','M','2001-02-16','010-9048-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9049, 'student_demo45','s45@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생45','F','2001-02-17','010-9049-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9050, 'student_demo46','s46@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생46','M','2001-02-18','010-9050-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9051, 'student_demo47','s47@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생47','F','2001-02-19','010-9051-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9052, 'student_demo48','s48@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생48','M','2001-02-20','010-9052-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9053, 'student_demo49','s49@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생49','F','2001-02-21','010-9053-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
-- student_demo50: 발표 시연 중 50번째 신고를 직접 누를 계정
(9054, 'student_demo50','s50@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생50','M','2001-02-22','010-9054-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9055, 'student_demo51','s51@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생51','F','2001-02-23','010-9055-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9056, 'student_demo52','s52@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생52','M','2001-02-24','010-9056-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9057, 'student_demo53','s53@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생53','F','2001-02-25','010-9057-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9058, 'student_demo54','s54@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생54','M','2001-02-26','010-9058-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9059, 'student_demo55','s55@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생55','F','2001-02-27','010-9059-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9060, 'student_demo56','s56@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생56','M','2001-02-28','010-9060-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9061, 'student_demo57','s57@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생57','F','2001-03-01','010-9061-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9062, 'student_demo58','s58@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생58','M','2001-03-02','010-9062-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9063, 'student_demo59','s59@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생59','F','2001-03-03','010-9063-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW()),
(9064, 'student_demo60','s60@hardclick.dev','$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm','데모학생60','M','2001-03-04','010-9064-0001','STUDENT','ACTIVE',0,0,0,1,NOW(),NOW());

-- ============================================================
-- 2. 데모 강의 (inst_demo2가 개설)
-- ============================================================
INSERT IGNORE INTO course
    (course_id, author_id, title, subject, description, thumbnail_url,
     price_type, price, status, created_at,
     learning_objectives, target_audience, tech_tags, level)
VALUES
(9001, 9003,
 '실전 자바 스프링 풀스택 과정',
 'Backend',
 '발표 시연용 강의입니다. 스프링 부트와 JPA를 활용한 실전 프로젝트를 다룹니다.',
 NULL,
 'FREE', 0, 'PUBLISHED', NOW(),
 'Spring Boot|JPA|REST API',
 '자바 기초를 아는 개발자',
 'Java|Spring Boot|JPA|MySQL',
 'BEGINNER');

-- ============================================================
-- 3. 강의 섹션 + 레슨 (enrollment FK 만족용)
-- ============================================================
INSERT IGNORE INTO course_section (id, course_id, title, order_index)
VALUES
(9001, 9001, '1강: Spring Boot 시작하기', 1),
(9002, 9001, '2강: JPA 기초',             2);

-- [BUG FIX] s3_key 컬럼은 lesson 테이블에 존재하지 않으므로 제거
INSERT IGNORE INTO lesson
    (id, section_id, title, description, order_index, video_url,
     duration_seconds, file_processing_status, created_at)
VALUES
(9001, 9001, '개발 환경 세팅', 'JDK, IDE, Gradle 설치', 1, NULL, 900,  NULL, NOW()),
(9002, 9001, 'Hello World API',   'GET /hello 만들기',       2, NULL, 1200, NULL, NOW()),
(9003, 9002, 'Entity 설계',        '테이블 매핑 기초',        1, NULL, 1500, NULL, NOW()),
(9004, 9002, 'CRUD Repository',   'JpaRepository 활용',      2, NULL, 1800, NULL, NOW());

-- ============================================================
-- 4. 수강 등록 (villain + 학생 60명 → 61건)
-- ============================================================
INSERT IGNORE INTO enrollment (member_id, course_id, enrolled_at, status)
VALUES
(9004, 9001, NOW(), 'IN_PROGRESS'),
(9005, 9001, NOW(), 'IN_PROGRESS'),
(9006, 9001, NOW(), 'IN_PROGRESS'),
(9007, 9001, NOW(), 'IN_PROGRESS'),
(9008, 9001, NOW(), 'IN_PROGRESS'),
(9009, 9001, NOW(), 'IN_PROGRESS'),
(9010, 9001, NOW(), 'IN_PROGRESS'),
(9011, 9001, NOW(), 'IN_PROGRESS'),
(9012, 9001, NOW(), 'IN_PROGRESS'),
(9013, 9001, NOW(), 'IN_PROGRESS'),
(9014, 9001, NOW(), 'IN_PROGRESS'),
(9015, 9001, NOW(), 'IN_PROGRESS'),
(9016, 9001, NOW(), 'IN_PROGRESS'),
(9017, 9001, NOW(), 'IN_PROGRESS'),
(9018, 9001, NOW(), 'IN_PROGRESS'),
(9019, 9001, NOW(), 'IN_PROGRESS'),
(9020, 9001, NOW(), 'IN_PROGRESS'),
(9021, 9001, NOW(), 'IN_PROGRESS'),
(9022, 9001, NOW(), 'IN_PROGRESS'),
(9023, 9001, NOW(), 'IN_PROGRESS'),
(9024, 9001, NOW(), 'IN_PROGRESS'),
(9025, 9001, NOW(), 'IN_PROGRESS'),
(9026, 9001, NOW(), 'IN_PROGRESS'),
(9027, 9001, NOW(), 'IN_PROGRESS'),
(9028, 9001, NOW(), 'IN_PROGRESS'),
(9029, 9001, NOW(), 'IN_PROGRESS'),
(9030, 9001, NOW(), 'IN_PROGRESS'),
(9031, 9001, NOW(), 'IN_PROGRESS'),
(9032, 9001, NOW(), 'IN_PROGRESS'),
(9033, 9001, NOW(), 'IN_PROGRESS'),
(9034, 9001, NOW(), 'IN_PROGRESS'),
(9035, 9001, NOW(), 'IN_PROGRESS'),
(9036, 9001, NOW(), 'IN_PROGRESS'),
(9037, 9001, NOW(), 'IN_PROGRESS'),
(9038, 9001, NOW(), 'IN_PROGRESS'),
(9039, 9001, NOW(), 'IN_PROGRESS'),
(9040, 9001, NOW(), 'IN_PROGRESS'),
(9041, 9001, NOW(), 'IN_PROGRESS'),
(9042, 9001, NOW(), 'IN_PROGRESS'),
(9043, 9001, NOW(), 'IN_PROGRESS'),
(9044, 9001, NOW(), 'IN_PROGRESS'),
(9045, 9001, NOW(), 'IN_PROGRESS'),
(9046, 9001, NOW(), 'IN_PROGRESS'),
(9047, 9001, NOW(), 'IN_PROGRESS'),
(9048, 9001, NOW(), 'IN_PROGRESS'),
(9049, 9001, NOW(), 'IN_PROGRESS'),
(9050, 9001, NOW(), 'IN_PROGRESS'),
(9051, 9001, NOW(), 'IN_PROGRESS'),
(9052, 9001, NOW(), 'IN_PROGRESS'),
(9053, 9001, NOW(), 'IN_PROGRESS'),
(9054, 9001, NOW(), 'IN_PROGRESS'),
(9055, 9001, NOW(), 'IN_PROGRESS'),
(9056, 9001, NOW(), 'IN_PROGRESS'),
(9057, 9001, NOW(), 'IN_PROGRESS'),
(9058, 9001, NOW(), 'IN_PROGRESS'),
(9059, 9001, NOW(), 'IN_PROGRESS'),
(9060, 9001, NOW(), 'IN_PROGRESS'),
(9061, 9001, NOW(), 'IN_PROGRESS'),
(9062, 9001, NOW(), 'IN_PROGRESS'),
(9063, 9001, NOW(), 'IN_PROGRESS'),
(9064, 9001, NOW(), 'IN_PROGRESS');

-- ============================================================
-- 5. 빌런 게시글 5개 (FREE 게시판)
-- [BUG FIX] posts 테이블에 subject 컬럼이 없음 (subject_id FK만 존재).
--            FREE 게시글은 subject_id NULL이므로 컬럼 자체를 제거.
-- ============================================================
INSERT IGNORE INTO posts
    (post_id, author_id, board_type, title, content,
     view_count, status, is_accepted, created_at, updated_at)
VALUES
(9001, 9004, 'FREE', '[광고] 최저가 마케팅 문의하세요 클릭하세요',
 '안녕하세요. 저렴한 마케팅 대행 업체입니다. 지금 바로 연락주시면 특가 혜택을 드립니다. 010-xxxx-xxxx',
 0, 'ACTIVE', 0, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),

(9002, 9004, 'FREE', '[스팸] 부업 알바 모집 하루 10만원',
 '재택근무 가능, 하루 10만원 보장. 지금 바로 오픈채팅 참여하세요. 링크: http://spam.example.com',
 0, 'ACTIVE', 0, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),

(9003, 9004, 'FREE', '★★★무료 코인 지급★★★ 클릭필수',
 '저희 서비스에 가입하시면 무료 코인 5000개를 드립니다. 이벤트 기간 한정! 지금 바로 가입하세요.',
 0, 'ACTIVE', 0, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),

(9004, 9004, 'FREE', '개인정보 팝니다 DB 저렴하게',
 '각종 개인정보 DB 보유 중. 마케팅 용도로 저렴하게 판매합니다. 비밀 연락 주세요.',
 0, 'ACTIVE', 0, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),

(9005, 9004, 'FREE', '도박 사이트 초대 링크 공유합니다',
 '합법 사이트에요. 안전합니다. 첫 충전 보너스 100%. 지금 바로 링크 타고 가입하세요.',
 0, 'ACTIVE', 0, NOW(), NOW());

-- ============================================================
-- 6. 빌런 댓글 10개 (자신의 게시글에 도배)
-- [BUG FIX] comments 테이블에 status 컬럼이 없음. is_deleted bit(1)로 삭제 여부를 관리.
--            status 컬럼 및 'ACTIVE' 값 제거.
-- ============================================================
INSERT IGNORE INTO comments
    (comment_id, post_id, author_id, parent_id, content,
     is_accepted, is_deleted, accept_count, image_url, created_at, updated_at)
VALUES
(9001, 9001, 9004, NULL, '지금 바로 연락주세요! 010-xxxx-xxxx 할인 마감 임박!',
 0, 0, 0, NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9002, 9001, 9004, NULL, '★오늘만 특가★ 절대 후회 없는 선택입니다',
 0, 0, 0, NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9003, 9002, 9004, NULL, '진짜 돈 됩니다. 저도 하루 20만원 벌었어요',
 0, 0, 0, NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9004, 9002, 9004, NULL, '의심하지 마세요 100% 합법 알바입니다',
 0, 0, 0, NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9005, 9003, 9004, NULL, '이벤트 끝나기 전에 빨리 가입하세요!',
 0, 0, 0, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9006, 9003, 9004, NULL, '저 어제 가입해서 코인 받았습니다 ㅋㅋ',
 0, 0, 0, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9007, 9004, 9004, NULL, '연락 주시면 샘플 무료로 드립니다',
 0, 0, 0, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9008, 9004, 9004, NULL, '빠른 납기 가능 지금 바로 DM 주세요',
 0, 0, 0, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9009, 9005, 9004, NULL, '첫 충전 100% 보너스 오늘만!!!!',
 0, 0, 0, NULL, NOW(), NOW()),
(9010, 9005, 9004, NULL, '가입코드 VILLAIN 입력하시면 추가 보너스',
 0, 0, 0, NULL, NOW(), NOW());

-- ============================================================
-- 7. 신고 49개 사전 주입 [jongjunn 파트]
--
-- 분배 전략 (unique constraint: reporter_id + target_type + target_id):
--   post 9001: student_demo01~10  (10건) → countByReportedMember = 10
--   post 9002: student_demo11~20  (10건) → cumulative = 20
--   post 9003: student_demo21~30  (10건) → cumulative = 30
--   post 9004: student_demo31~40  (10건) → cumulative = 40
--   post 9005: student_demo41~49  ( 9건) → cumulative = 49
--
-- 발표 시연: student_demo50(9054)가 post 9005를 신고 → 50번째 → villain 자동 정지
-- ============================================================
INSERT IGNORE INTO reports
    (reporter_id, reported_member_id, target_type, target_id,
     report_types, reason, memo, status, created_at)
VALUES
-- post 9001 신고: student_demo01~10 (9005~9014)
(9005,  9004, 'POST', 9001, 'SPAM',             '광고성 스팸 게시글입니다', NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9006,  9004, 'POST', 9001, 'COMMERCIAL',       '상업적 홍보 게시글',     NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9007,  9004, 'POST', 9001, 'SPAM',             '반복 광고 게시글',       NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9008,  9004, 'POST', 9001, 'SPAM',             '스팸성 내용',            NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9009,  9004, 'POST', 9001, 'COMMERCIAL',       '광고 도배',              NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9010,  9004, 'POST', 9001, 'SPAM',             '스팸 게시물',            NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9011,  9004, 'POST', 9001, 'SPAM',             '광고 스팸',              NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9012,  9004, 'POST', 9001, 'COMMERCIAL',       '홍보 목적 게시글',       NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9013,  9004, 'POST', 9001, 'SPAM',             '스팸 게시물 신고합니다', NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9014,  9004, 'POST', 9001, 'SPAM',             '광고 게시물 신고',       NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
-- post 9002 신고: student_demo11~20 (9015~9024)
(9015,  9004, 'POST', 9002, 'SPAM',             '부업 사기 게시물',       NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9016,  9004, 'POST', 9002, 'COMMERCIAL',       '불법 알바 모집',         NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9017,  9004, 'POST', 9002, 'SPAM',             '스팸',                   NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9018,  9004, 'POST', 9002, 'SPAM',             '광고 스팸 게시글',       NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9019,  9004, 'POST', 9002, 'COMMERCIAL',       '상업성 게시물',          NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9020,  9004, 'POST', 9002, 'SPAM',             '부업 사기',              NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9021,  9004, 'POST', 9002, 'SPAM',             '불법 링크 포함',         NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9022,  9004, 'POST', 9002, 'COMMERCIAL',       '스팸 광고',              NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9023,  9004, 'POST', 9002, 'SPAM',             '사기성 게시물',          NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9024,  9004, 'POST', 9002, 'SPAM',             '광고 도배 신고',         NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
-- post 9003 신고: student_demo21~30 (9025~9034)
(9025,  9004, 'POST', 9003, 'COMMERCIAL',       '코인 사기 광고',         NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9026,  9004, 'POST', 9003, 'SPAM',             '스팸',                   NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9027,  9004, 'POST', 9003, 'SPAM',             '광고 스팸',              NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9028,  9004, 'POST', 9003, 'COMMERCIAL',       '이벤트 사기 광고',       NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9029,  9004, 'POST', 9003, 'SPAM',             '가입 유도 스팸',         NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9030,  9004, 'POST', 9003, 'SPAM',             '스팸 게시물',            NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9031,  9004, 'POST', 9003, 'COMMERCIAL',       '상업 광고',              NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9032,  9004, 'POST', 9003, 'SPAM',             '반복 광고',              NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9033,  9004, 'POST', 9003, 'SPAM',             '스팸 신고',              NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9034,  9004, 'POST', 9003, 'SPAM',             '광고성 글',              NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
-- post 9004 신고: student_demo31~40 (9035~9044)
(9035,  9004, 'POST', 9004, 'PRIVACY',          '개인정보 불법 거래',     NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9036,  9004, 'POST', 9004, 'ABUSE',            '불법 개인정보 판매',     NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9037,  9004, 'POST', 9004, 'PRIVACY',          '개인정보 거래 신고',     NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9038,  9004, 'POST', 9004, 'PRIVACY',          '개인정보 판매 게시물',   NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9039,  9004, 'POST', 9004, 'ABUSE',            '불법 행위 신고',         NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9040,  9004, 'POST', 9004, 'PRIVACY',          '개인정보 불법 게시물',   NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9041,  9004, 'POST', 9004, 'SPAM',             '스팸 및 불법 게시물',    NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9042,  9004, 'POST', 9004, 'PRIVACY',          '개인정보 침해',          NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9043,  9004, 'POST', 9004, 'ABUSE',            '불법 내용 신고',         NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9044,  9004, 'POST', 9004, 'PRIVACY',          '개인정보 위반 신고',     NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
-- post 9005 신고: student_demo41~49 (9045~9053) → 9건만 → 누적 49건
(9045,  9004, 'POST', 9005, 'OBSCENE',          '도박 사이트 홍보',       NULL, 'PENDING', NOW()),
(9046,  9004, 'POST', 9005, 'ABUSE',            '도박 불법 광고',         NULL, 'PENDING', NOW()),
(9047,  9004, 'POST', 9005, 'SPAM',             '도박 스팸 게시물',       NULL, 'PENDING', NOW()),
(9048,  9004, 'POST', 9005, 'OBSCENE',          '불법 도박 사이트 광고',  NULL, 'PENDING', NOW()),
(9049,  9004, 'POST', 9005, 'ABUSE',            '불법 도박 홍보',         NULL, 'PENDING', NOW()),
(9050,  9004, 'POST', 9005, 'SPAM',             '도박 링크 스팸',         NULL, 'PENDING', NOW()),
(9051,  9004, 'POST', 9005, 'OBSCENE',          '도박 광고 신고',         NULL, 'PENDING', NOW()),
(9052,  9004, 'POST', 9005, 'ABUSE',            '불법 게시물 신고',       NULL, 'PENDING', NOW()),
(9053,  9004, 'POST', 9005, 'SPAM',             '스팸 도박 광고',         NULL, 'PENDING', NOW());
-- ↑ 여기까지 신고 총 49건
-- 발표 시연 시 student_demo50(9054)가 POST 9005를 API로 신고하면 50번째가 되어 villain 자동 정지

-- ============================================================
-- 8. 사전 알림 (UI 알림 탭에 이력이 보이도록)
-- ============================================================
INSERT IGNORE INTO notification
    (receiver_id, type, message, is_read, redirect_url, created_at)
VALUES
-- 강사 inst_demo1 → 수강 등록 알림 이력
(9002, 'COURSE_REGISTER', '데모학생01님이 실전 자바 스프링 풀스택 과정에 등록하셨습니다.',
 1, '/courses/9001', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9002, 'COURSE_REGISTER', '데모학생02님이 실전 자바 스프링 풀스택 과정에 등록하셨습니다.',
 1, '/courses/9001', DATE_SUB(NOW(), INTERVAL 1 DAY)),
-- admin 알림 이력 (신고 접수)
(9001, 'REPORT', '빌런데모 게시글에 대한 신고가 접수되었습니다.',
 0, '/admin/reports', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9001, 'REPORT', '빌런데모 게시글에 대한 신고가 접수되었습니다.',
 0, '/admin/reports', DATE_SUB(NOW(), INTERVAL 12 HOUR));

-- ============================================================
-- ============================================================
-- [곽시윤 파트] 커뮤니티·신고·공지 시연 데이터
-- ============================================================
-- ============================================================

-- ============================================================
-- 9. 과목(subjects) 추가
-- ============================================================
INSERT IGNORE INTO subjects (name) VALUES
('미적분'), ('독서'), ('화학Ⅰ'), ('영어Ⅰ'), ('확통'), ('물리Ⅰ');

-- subject_id 캐싱 (이미 존재하는 과목도 올바른 ID를 참조하도록 SELECT 사용)
SET @s_미적분 = (SELECT id FROM subjects WHERE name = '미적분');
SET @s_독서   = (SELECT id FROM subjects WHERE name = '독서');
SET @s_화학   = (SELECT id FROM subjects WHERE name = '화학Ⅰ');
SET @s_영어   = (SELECT id FROM subjects WHERE name = '영어Ⅰ');
SET @s_확통   = (SELECT id FROM subjects WHERE name = '확통');
SET @s_물리   = (SELECT id FROM subjects WHERE name = '물리Ⅰ');

-- ============================================================
-- 10. 신고 처리·이용제한 데모용 멤버 (6명)
-- ============================================================
INSERT IGNORE INTO members
    (member_id, username, email, password, name, gender, birth_date, phone_number,
     role, status, is_password_change_required, login_fail_count, is_locked,
     optional_terms_agreed, created_at, updated_at)
VALUES
-- 52회 신고 대상 (ACTIVE → 관리자가 클릭해서 이용제한 처리 데모)
(9100, 'spam_user1', 'spam1@hardclick.dev',
 '$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm',
 '스팸유저1', 'M', '1995-06-01', '010-9100-0001', 'STUDENT', 'ACTIVE', 0, 0, 0, 1, NOW(), NOW()),
-- 이미 이용제한된 계정 (SUSPENDED → 해제 데모용)
(9101, 'spam_user2', 'spam2@hardclick.dev',
 '$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm',
 '스팸유저2', 'F', '1995-06-02', '010-9101-0001', 'STUDENT', 'SUSPENDED', 0, 0, 0, 1, NOW(), NOW()),
-- 신고 0~5회 일반 유저 4명 (admin UI 비교 대상)
(9102, 'normal_user1', 'normal1@hardclick.dev',
 '$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm',
 '일반유저1', 'M', '2000-03-10', '010-9102-0001', 'STUDENT', 'ACTIVE', 0, 0, 0, 1, NOW(), NOW()),
(9103, 'normal_user2', 'normal2@hardclick.dev',
 '$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm',
 '일반유저2', 'F', '2000-03-11', '010-9103-0001', 'STUDENT', 'ACTIVE', 0, 0, 0, 1, NOW(), NOW()),
(9104, 'normal_user3', 'normal3@hardclick.dev',
 '$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm',
 '일반유저3', 'M', '2000-03-12', '010-9104-0001', 'STUDENT', 'ACTIVE', 0, 0, 0, 1, NOW(), NOW()),
(9105, 'normal_user4', 'normal4@hardclick.dev',
 '$2a$10$/mT8RjKyRgPgwQ8WT16QJeqT7HesyqOEErEOEN.BhbSqcg6xS4Vmm',
 '일반유저4', 'F', '2000-03-13', '010-9105-0001', 'STUDENT', 'ACTIVE', 0, 0, 0, 1, NOW(), NOW());

-- ============================================================
-- 11. 질문게시판 글 6개 (QUESTION board)
--     미적분×2 (과목 필터 데모), 독서·화학Ⅰ·영어Ⅰ·확통 각 1
--     해결 2개(is_accepted=1) + 미해결 4개
-- ============================================================
INSERT IGNORE INTO posts
    (post_id, author_id, board_type, subject_id, title, content,
     view_count, status, is_accepted, created_at, updated_at)
VALUES
-- 미적분 ①: 해결된 질문 (댓글 3개 + 채택 1개 → 섹션 13에서 추가)
(9006, 9005, 'QUESTION', @s_미적분,
 '미적분 극한값 계산 도와주세요',
 'lim(x→0) (sin x / x) 를 L''Hopital 없이 증명하는 방법을 모르겠습니다. 샌드위치 정리를 써야 하나요?',
 12, 'ACTIVE', 1, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),

-- 미적분 ②: 미해결 + 이미지 첨부 (post_files → 섹션 12에서 추가)
(9007, 9006, 'QUESTION', @s_미적분,
 '연쇄법칙 적용 과정이 이해가 안 돼요 [이미지 첨부]',
 '교재 p.147 예제 3번인데 f(g(x)) 미분할 때 어디서 틀렸는지 봐주세요. 이미지 첨부했습니다.',
 8, 'ACTIVE', 0, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),

-- 독서: 미해결
(9008, 9007, 'QUESTION', @s_독서,
 '박경리 토지 1부 주제를 한 문장으로 요약하면?',
 '수행평가에서 토지 1부 핵심 주제를 요약하라고 하는데 어떻게 쓰면 좋을까요?',
 5, 'ACTIVE', 0, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),

-- 화학Ⅰ: 해결된 질문
(9009, 9008, 'QUESTION', @s_화학,
 '몰 농도 계산 공식이 헷갈려요',
 '0.5 M NaCl 용액 500 mL를 만들려면 NaCl이 몇 g 필요한가요? 풀이 과정 알려주세요.',
 9, 'ACTIVE', 1, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),

-- 영어Ⅰ: 미해결
(9010, 9009, 'QUESTION', @s_영어,
 '수동태 전환할 때 by 생략 조건이 뭔가요?',
 '"The window was broken." 처럼 행위자를 모르거나 중요하지 않을 때 by 절을 생략한다고 배웠는데 예외가 있나요?',
 4, 'ACTIVE', 0, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),

-- 확통: 미해결
(9011, 9010, 'QUESTION', @s_확통,
 '조건부 확률 P(A|B) 계산 헷갈립니다',
 'P(A)=0.3, P(B)=0.4, P(A∩B)=0.12 일 때 P(A|B)와 P(B|A)를 구해야 하는데 공식이 헷갈려요.',
 6, 'ACTIVE', 0, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- ============================================================
-- 12. 이미지 첨부 (post 9007 — 미적분 ② 질문글)
-- ============================================================
INSERT IGNORE INTO post_files (post_id, file_url, sort_order) VALUES
(9007, 'https://hard-click-bucket.s3.ap-northeast-2.amazonaws.com/posts/demo/chain_rule_question.jpg', 1);

-- ============================================================
-- 13. 자유게시판 글 3개 (normal_user 작성 → 신고 UI 시연 대상)
-- ============================================================
INSERT IGNORE INTO posts
    (post_id, author_id, board_type, title, content,
     view_count, status, is_accepted, created_at, updated_at)
VALUES
(9012, 9103, 'FREE',
 '스터디 같이 하실 분 계세요?',
 '매주 토요일 온라인 스터디 모집합니다. 수학·과학 위주로 진행 예정입니다. 댓글 달아주세요!',
 7, 'ACTIVE', 0, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9013, 9104, 'FREE',
 '강의 추천 해주세요',
 '물리Ⅰ 기초부터 시작하기 좋은 강의 추천 부탁드립니다. 수능 3등급 목표입니다.',
 4, 'ACTIVE', 0, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9014, 9105, 'FREE',
 '이 사이트 진짜 좋네요',
 '오늘 처음 가입했는데 강의 퀄리티가 생각보다 훨씬 좋습니다. 앞으로 열심히 공부할게요!',
 2, 'ACTIVE', 0, NOW(), NOW());

-- ============================================================
-- 14. 댓글
--     A. post 9006 (미적분 해결됨): 댓글 3개, comment 9012가 채택
--     B. post 9012 (자유게시판): 댓글(9014) + 대댓글(9015) — 신고 대상
-- ============================================================
INSERT IGNORE INTO comments
    (comment_id, post_id, author_id, parent_id, content,
     is_accepted, is_deleted, accept_count, image_url, created_at, updated_at)
VALUES
-- A. post 9006: 미적분 질문 댓글 3개
(9011, 9006, 9006, NULL,
 '샌드위치 정리(Squeeze Theorem)를 쓰는 게 맞아요! sin x ≤ x ≤ tan x 부등식을 세우고 코사인 연속성으로 극한을 끼워넣으면 됩니다.',
 0, 0, 1, NULL, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
-- 채택된 답변: is_accepted=1
(9012, 9006, 9007, NULL,
 '보조 증명으로 단위원 넓이 비교법도 있어요. 각도 θ(라디안)에 대해 sin θ < θ < tan θ 가 성립함을 단위원으로 시각화하면 이해가 훨씬 빠릅니다. 교재 p.53 그림 참고해보세요!',
 1, 0, 5, NULL, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
(9013, 9006, 9008, NULL,
 '저도 이 부분 어려웠는데 위 댓글 설명 덕분에 이해했습니다. 감사합니다!',
 0, 0, 0, NULL, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
-- B. post 9012: 신고 시연용 댓글 + 대댓글
(9014, 9012, 9104, NULL,
 '저도 관심 있어요! 토요일 오후 2시 이후 가능한데 어떤가요?',
 0, 0, 0, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
-- 대댓글 (parent_id=9014)
(9015, 9012, 9105, 9014,
 '저도 같은 시간 가능합니다! 오픈채팅 링크 공유해주세요.',
 0, 0, 0, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- ============================================================
-- 15. 신고 목록 시연용 5건 (미처리 4 + 처리완료 1)
--     대상: 게시글 3건 + 댓글 1건 + 대댓글 1건 / 사유 다양
--     reporters: student_demo56~60 (9060~9064, 기존 신고 기록 없는 학생)
-- ============================================================
INSERT IGNORE INTO reports
    (reporter_id, reported_member_id, target_type, target_id,
     report_types, reason, memo, status, created_at)
VALUES
-- PENDING 4건
(9061, 9103, 'POST',    9012, 'ABUSIVE_LANGUAGE', '부적절한 표현이 포함되어 있습니다.',             NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9062, 9104, 'POST',    9013, 'COMMERCIAL',        '강의 홍보성 게시물로 의심됩니다.',               NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9063, 9104, 'COMMENT', 9014, 'OBSCENE',           '부적절한 내용이 포함된 댓글입니다.',             NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9064, 9105, 'COMMENT', 9015, 'PRIVACY',           '타인의 개인정보(연락처)를 무단으로 요청합니다.', NULL, 'PENDING', NOW()),
-- RESOLVED 1건
(9060, 9105, 'POST',    9014, 'SPAM',              '광고성 도배 게시물입니다.',                      '확인 후 경고 조치 완료', 'RESOLVED', DATE_SUB(NOW(), INTERVAL 3 DAY));

-- ============================================================
-- 16. 이용제한 데모: spam_user1(9100) 게시글·댓글 + 신고 52건
--     52건 = AUTO_SUSPEND_REPORT_THRESHOLD(50) 초과 → 관리자가 직접 클릭 후 정지
--     unique constraint 보장: reporter_id는 기존 villain 신고와 target_id가 다름
-- ============================================================
-- spam_user1 콘텐츠
INSERT IGNORE INTO posts
    (post_id, author_id, board_type, title, content,
     view_count, status, is_accepted, created_at, updated_at)
VALUES
(9015, 9100, 'FREE', '[광고] 과외 학생 모집 전 과목 1:1',
 '입시 전문 과외 선생님입니다. 수학·영어·과학 전 과목 가능. 첫 달 50% 할인. 지금 DM 주세요.',
 0, 'ACTIVE', 0, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9016, 9100, 'FREE', '★성적 보장★ 온라인 과외 모집',
 '수능 만점자 출신 과외 선생님. 100% 성적 향상 보장. 선착순 5명. 연락처: 010-xxxx-xxxx',
 0, 'ACTIVE', 0, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9017, 9100, 'FREE', '인강 할인코드 공유 (전 강좌 80% OFF)',
 '저만 알고있는 할인 코드 공유합니다. 비공개 루트로 구한 코드라 곧 막힐 수 있으니 빨리 사용하세요.',
 0, 'ACTIVE', 0, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9018, 9100, 'FREE', '수능 족보 판매 (역대 기출 + 예상문제)',
 '10년치 기출 + 올해 예상문제 PDF 패키지 판매. 3만원. 입금 후 파일 전송.',
 0, 'ACTIVE', 0, NOW(), NOW());

INSERT IGNORE INTO comments
    (comment_id, post_id, author_id, parent_id, content,
     is_accepted, is_deleted, accept_count, image_url, created_at, updated_at)
VALUES
(9016, 9015, 9100, NULL,
 '지금 DM 주시면 무료 샘플 커리큘럼 드립니다! 자리 한정이니 서두르세요.',
 0, 0, 0, NULL, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9017, 9016, 9100, NULL,
 '수능 D-100 특가 진행 중입니다. 지금 바로 연락 주세요!!',
 0, 0, 0, NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY));

-- spam_user1 신고 52건
-- POST 9015: reporters 9005~9013 (9명)
INSERT IGNORE INTO reports (reporter_id, reported_member_id, target_type, target_id, report_types, reason, memo, status, created_at) VALUES
(9005, 9100, 'POST', 9015, 'COMMERCIAL', '상업적 과외 모집 광고', NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9006, 9100, 'POST', 9015, 'SPAM',       '스팸 광고 게시물',      NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9007, 9100, 'POST', 9015, 'COMMERCIAL', '반복 광고성 게시물',    NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9008, 9100, 'POST', 9015, 'SPAM',       '상업 광고 도배',        NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9009, 9100, 'POST', 9015, 'COMMERCIAL', '유료 과외 광고',        NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9010, 9100, 'POST', 9015, 'SPAM',       '광고 스팸',             NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9011, 9100, 'POST', 9015, 'COMMERCIAL', '홍보 목적 게시글',      NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9012, 9100, 'POST', 9015, 'SPAM',       '스팸 신고합니다',       NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9013, 9100, 'POST', 9015, 'COMMERCIAL', '광고 게시물 신고',      NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 3 DAY));
-- POST 9016: reporters 9014~9022 (9명)
INSERT IGNORE INTO reports (reporter_id, reported_member_id, target_type, target_id, report_types, reason, memo, status, created_at) VALUES
(9014, 9100, 'POST', 9016, 'SPAM',       '스팸 광고',             NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9015, 9100, 'POST', 9016, 'COMMERCIAL', '상업성 과외 광고',      NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9016, 9100, 'POST', 9016, 'SPAM',       '반복 스팸',             NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9017, 9100, 'POST', 9016, 'COMMERCIAL', '유료 과외 홍보',        NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9018, 9100, 'POST', 9016, 'SPAM',       '광고 도배 신고',        NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9019, 9100, 'POST', 9016, 'COMMERCIAL', '상업 광고',             NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9020, 9100, 'POST', 9016, 'SPAM',       '스팸 게시물',           NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9021, 9100, 'POST', 9016, 'COMMERCIAL', '광고성 스팸',           NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9022, 9100, 'POST', 9016, 'SPAM',       '과외 광고 스팸',        NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY));
-- POST 9017: reporters 9023~9031 (9명)
INSERT IGNORE INTO reports (reporter_id, reported_member_id, target_type, target_id, report_types, reason, memo, status, created_at) VALUES
(9023, 9100, 'POST', 9017, 'SPAM',       '할인코드 사기 스팸',    NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9024, 9100, 'POST', 9017, 'COMMERCIAL', '상업적 홍보',           NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9025, 9100, 'POST', 9017, 'SPAM',       '스팸 게시물 신고',      NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9026, 9100, 'POST', 9017, 'COMMERCIAL', '불법 할인코드 광고',    NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9027, 9100, 'POST', 9017, 'SPAM',       '사기성 스팸 광고',      NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9028, 9100, 'POST', 9017, 'COMMERCIAL', '광고 게시물',           NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9029, 9100, 'POST', 9017, 'SPAM',       '반복 광고 스팸',        NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9030, 9100, 'POST', 9017, 'COMMERCIAL', '홍보성 스팸',           NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9031, 9100, 'POST', 9017, 'SPAM',       '광고 스팸 신고',        NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY));
-- POST 9018: reporters 9032~9040 (9명)
INSERT IGNORE INTO reports (reporter_id, reported_member_id, target_type, target_id, report_types, reason, memo, status, created_at) VALUES
(9032, 9100, 'POST', 9018, 'COMMERCIAL', '족보 불법 판매',        NULL, 'PENDING', NOW()),
(9033, 9100, 'POST', 9018, 'SPAM',       '유료 족보 판매 스팸',   NULL, 'PENDING', NOW()),
(9034, 9100, 'POST', 9018, 'COMMERCIAL', '불법 자료 판매 광고',   NULL, 'PENDING', NOW()),
(9035, 9100, 'POST', 9018, 'SPAM',       '스팸 광고 게시물',      NULL, 'PENDING', NOW()),
(9036, 9100, 'POST', 9018, 'COMMERCIAL', '상업성 족보 판매',      NULL, 'PENDING', NOW()),
(9037, 9100, 'POST', 9018, 'SPAM',       '사기 족보 판매 신고',   NULL, 'PENDING', NOW()),
(9038, 9100, 'POST', 9018, 'COMMERCIAL', '유료 판매 광고',        NULL, 'PENDING', NOW()),
(9039, 9100, 'POST', 9018, 'SPAM',       '반복 광고 게시물',      NULL, 'PENDING', NOW()),
(9040, 9100, 'POST', 9018, 'COMMERCIAL', '불법 판매 신고',        NULL, 'PENDING', NOW());
-- COMMENT 9016: reporters 9041~9049 (9명)
INSERT IGNORE INTO reports (reporter_id, reported_member_id, target_type, target_id, report_types, reason, memo, status, created_at) VALUES
(9041, 9100, 'COMMENT', 9016, 'COMMERCIAL', '광고성 댓글',         NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9042, 9100, 'COMMENT', 9016, 'SPAM',       '스팸 댓글 신고',      NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9043, 9100, 'COMMENT', 9016, 'COMMERCIAL', '홍보 댓글',           NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9044, 9100, 'COMMENT', 9016, 'SPAM',       '광고 스팸 댓글',      NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9045, 9100, 'COMMENT', 9016, 'COMMERCIAL', '상업성 댓글',         NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9046, 9100, 'COMMENT', 9016, 'SPAM',       '반복 광고 댓글',      NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9047, 9100, 'COMMENT', 9016, 'COMMERCIAL', '광고 댓글 신고',      NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9048, 9100, 'COMMENT', 9016, 'SPAM',       '스팸 광고 댓글',      NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9049, 9100, 'COMMENT', 9016, 'COMMERCIAL', '과외 홍보 댓글',      NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 3 DAY));
-- COMMENT 9017: reporters 9050~9056 (7명) → 누적 52건
INSERT IGNORE INTO reports (reporter_id, reported_member_id, target_type, target_id, report_types, reason, memo, status, created_at) VALUES
(9050, 9100, 'COMMENT', 9017, 'SPAM',       '스팸 댓글',           NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9051, 9100, 'COMMENT', 9017, 'COMMERCIAL', '광고성 댓글',         NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9052, 9100, 'COMMENT', 9017, 'SPAM',       '반복 스팸 댓글',      NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9053, 9100, 'COMMENT', 9017, 'COMMERCIAL', '홍보 댓글 신고',      NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9054, 9100, 'COMMENT', 9017, 'SPAM',       '광고 댓글 도배',      NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9055, 9100, 'COMMENT', 9017, 'COMMERCIAL', '상업성 광고 댓글',    NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9056, 9100, 'COMMENT', 9017, 'SPAM',       '스팸 광고 신고',      NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY));
-- ↑ spam_user1 신고 총 52건

-- ============================================================
-- 17. 공지 3개
--     ① 전체공지 상단고정 (is_pinned=1) — admin 등록
--     ② 전체공지 일반 (is_pinned=0) — admin 등록
--     ③ 강의별 공지 (course_id=9001) — inst_demo2 등록
-- ============================================================
INSERT IGNORE INTO notices
    (notice_id, author_id, course_id, title, content,
     is_pinned, type, status, created_at, updated_at)
VALUES
(9001, 9001, NULL,
 '[공지] 2026년 1학기 서비스 이용 안내',
 '안녕하세요, Hard-Click 운영팀입니다.\n2026년 1학기 서비스 운영 일정을 안내드립니다.\n- 정기 점검: 매주 화요일 새벽 2시~4시\n- 강의 업로드 지연 시 공지사항을 확인해주세요.\n- 문의: support@hardclick.dev',
 1, 'GLOBAL', 'PUBLISHED', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY)),

(9002, 9001, NULL,
 '[공지] 커뮤니티 이용 규칙 개정 안내',
 '커뮤니티 이용 규칙이 개정되었습니다.\n주요 변경사항:\n1. 광고·홍보 게시글 즉시 삭제 및 계정 이용제한\n2. 신고 누적 50회 이상 자동 이용제한 적용\n위반 시 불이익이 생길 수 있으니 꼭 확인해주세요.',
 0, 'GLOBAL', 'PUBLISHED', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),

(9003, 9003, 9001,
 '[강의 공지] 4주차 라이브 Q&A 세션 일정 안내',
 '수강생 여러분, 4주차 라이브 Q&A 세션 일정입니다.\n일시: 2026년 7월 5일(토) 오후 3시\n장소: Zoom (링크는 강의실 내 공지사항 확인)\n질문은 미리 댓글로 남겨주세요.',
 0, 'COURSE', 'PUBLISHED', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- ============================================================
-- 18. AUTO_INCREMENT 충돌 방지 (9200 이후로 이동)
-- ============================================================
ALTER TABLE members         AUTO_INCREMENT = 9200;
ALTER TABLE course          AUTO_INCREMENT = 9200;
ALTER TABLE course_section  AUTO_INCREMENT = 9200;
ALTER TABLE lesson          AUTO_INCREMENT = 9200;
ALTER TABLE posts           AUTO_INCREMENT = 9200;
ALTER TABLE comments        AUTO_INCREMENT = 9200;
ALTER TABLE notices         AUTO_INCREMENT = 9200;
ALTER TABLE post_files      AUTO_INCREMENT = 9200;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 검증 쿼리 (실행 후 아래 SELECT로 확인)
-- ============================================================
-- [jongjunn 파트]
-- SELECT COUNT(*) AS member_count   FROM members   WHERE member_id BETWEEN 9001 AND 9099;  -- 예상: 64
-- SELECT COUNT(*) AS enroll_count   FROM enrollment WHERE course_id = 9001;                 -- 예상: 61
-- SELECT COUNT(*) AS villain_post   FROM posts     WHERE author_id = 9004;                  -- 예상: 5
-- SELECT COUNT(*) AS villain_cmt    FROM comments  WHERE author_id = 9004;                  -- 예상: 10
-- SELECT COUNT(*) AS villain_report FROM reports   WHERE reported_member_id = 9004;          -- 예상: 49
-- SELECT status FROM members WHERE member_id = 9004;  -- 신고 전 ACTIVE, 50번째 신고 후 SUSPENDED
--
-- [곽시윤 파트]
-- SELECT COUNT(*) FROM members   WHERE member_id BETWEEN 9100 AND 9105;  -- 예상: 6
-- SELECT COUNT(*) FROM posts     WHERE board_type = 'QUESTION' AND post_id BETWEEN 9006 AND 9011;  -- 예상: 6
-- SELECT COUNT(*) FROM posts     WHERE board_type = 'FREE'     AND post_id BETWEEN 9012 AND 9014;  -- 예상: 3
-- SELECT COUNT(*) FROM post_files WHERE post_id = 9007;         -- 예상: 1
-- SELECT COUNT(*) FROM comments  WHERE post_id = 9006;          -- 예상: 3
-- SELECT is_accepted FROM comments WHERE comment_id = 9012;      -- 예상: 1 (채택)
-- SELECT COUNT(*) FROM reports   WHERE reported_member_id = 9103; -- 예상: 1
-- SELECT COUNT(*) FROM reports   WHERE reported_member_id = 9104; -- 예상: 2
-- SELECT COUNT(*) FROM reports   WHERE reported_member_id = 9105; -- 예상: 2 (1 PENDING + 1 RESOLVED)
-- SELECT COUNT(*) FROM reports   WHERE reported_member_id = 9100; -- 예상: 52
-- SELECT status   FROM members   WHERE member_id = 9101;           -- 예상: SUSPENDED (해제 데모용)
-- SELECT COUNT(*) FROM notices   WHERE notice_id BETWEEN 9001 AND 9003;  -- 예상: 3
-- SELECT is_pinned, type FROM notices WHERE notice_id IN (9001,9002,9003);
--   → (1, GLOBAL), (0, GLOBAL), (0, COURSE)
