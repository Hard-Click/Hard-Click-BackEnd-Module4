-- ============================================================
-- Hard-Click LMS  발표 시연용 더미 데이터 시드 스크립트
-- ============================================================
-- 목적: 4막 시나리오 실시간 시연에 필요한 최소 데이터를 주입한다.
--
-- 막1: admin_demo가 GLOBAL 공지 등록 → 전체 학생+강사에게 SSE NOTICE 발송
-- 막2: villain_demo가 FREE 게시판에 스팸 글·댓글 등록
-- 막3: 수강생 60명이 신고 → 49개 사전 주입, 50번째는 실시간 시연
-- 막4: 50번째 신고 클릭 → villain 계정 자동 정지(SUSPENDED)
--
-- 전제 조건:
--   1. 이 스크립트를 실행하기 전에 기존 시드(seed-6-students.sql 등)가 이미 적용된 상태여야 한다.
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
-- 기존 데이터와 충돌 방지를 위해 INSERT IGNORE 사용
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

INSERT IGNORE INTO lesson
    (id, section_id, title, description, order_index, video_url, s3_key,
     duration_seconds, file_processing_status, created_at)
VALUES
(9001, 9001, '개발 환경 세팅', 'JDK, IDE, Gradle 설치', 1, NULL, NULL, 900,  NULL, NOW()),
(9002, 9001, 'Hello World API',   'GET /hello 만들기',       2, NULL, NULL, 1200, NULL, NOW()),
(9003, 9002, 'Entity 설계',        '테이블 매핑 기초',        1, NULL, NULL, 1500, NULL, NOW()),
(9004, 9002, 'CRUD Repository',   'JpaRepository 활용',      2, NULL, NULL, 1800, NULL, NOW());

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
-- ============================================================
INSERT IGNORE INTO posts
    (post_id, author_id, board_type, subject, title, content,
     view_count, status, is_accepted, created_at, updated_at)
VALUES
(9001, 9004, 'FREE', '홍보', '[광고] 최저가 마케팅 문의하세요 클릭하세요',
 '안녕하세요. 저렴한 마케팅 대행 업체입니다. 지금 바로 연락주시면 특가 혜택을 드립니다. 010-xxxx-xxxx',
 0, 'ACTIVE', 0, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),

(9002, 9004, 'FREE', '홍보', '[스팸] 부업 알바 모집 하루 10만원',
 '재택근무 가능, 하루 10만원 보장. 지금 바로 오픈채팅 참여하세요. 링크: http://spam.example.com',
 0, 'ACTIVE', 0, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),

(9003, 9004, 'FREE', '홍보', '★★★무료 코인 지급★★★ 클릭필수',
 '저희 서비스에 가입하시면 무료 코인 5000개를 드립니다. 이벤트 기간 한정! 지금 바로 가입하세요.',
 0, 'ACTIVE', 0, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),

(9004, 9004, 'FREE', '홍보', '개인정보 팝니다 DB 저렴하게',
 '각종 개인정보 DB 보유 중. 마케팅 용도로 저렴하게 판매합니다. 비밀 연락 주세요.',
 0, 'ACTIVE', 0, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),

(9005, 9004, 'FREE', '홍보', '도박 사이트 초대 링크 공유합니다',
 '합법 사이트에요. 안전합니다. 첫 충전 보너스 100%. 지금 바로 링크 타고 가입하세요.',
 0, 'ACTIVE', 0, NOW(), NOW());

-- ============================================================
-- 6. 빌런 댓글 10개 (자신의 게시글에 도배)
-- ============================================================
INSERT IGNORE INTO comments
    (comment_id, post_id, author_id, parent_id, content,
     is_accepted, is_deleted, status, accept_count, image_url, created_at, updated_at)
VALUES
(9001, 9001, 9004, NULL, '지금 바로 연락주세요! 010-xxxx-xxxx 할인 마감 임박!',
 0, 0, 'ACTIVE', 0, NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9002, 9001, 9004, NULL, '★오늘만 특가★ 절대 후회 없는 선택입니다',
 0, 0, 'ACTIVE', 0, NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9003, 9002, 9004, NULL, '진짜 돈 됩니다. 저도 하루 20만원 벌었어요',
 0, 0, 'ACTIVE', 0, NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9004, 9002, 9004, NULL, '의심하지 마세요 100% 합법 알바입니다',
 0, 0, 'ACTIVE', 0, NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9005, 9003, 9004, NULL, '이벤트 끝나기 전에 빨리 가입하세요!',
 0, 0, 'ACTIVE', 0, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9006, 9003, 9004, NULL, '저 어제 가입해서 코인 받았습니다 ㅋㅋ',
 0, 0, 'ACTIVE', 0, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9007, 9004, 9004, NULL, '연락 주시면 샘플 무료로 드립니다',
 0, 0, 'ACTIVE', 0, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9008, 9004, 9004, NULL, '빠른 납기 가능 지금 바로 DM 주세요',
 0, 0, 'ACTIVE', 0, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9009, 9005, 9004, NULL, '첫 충전 100% 보너스 오늘만!!!!',
 0, 0, 'ACTIVE', 0, NULL, NOW(), NOW()),
(9010, 9005, 9004, NULL, '가입코드 VILLAIN 입력하시면 추가 보너스',
 0, 0, 'ACTIVE', 0, NULL, NOW(), NOW());

-- ============================================================
-- 7. 신고 49개 사전 주입
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
-- 9. AUTO_INCREMENT 충돌 방지 (9200 이후로 이동)
-- ============================================================
ALTER TABLE members         AUTO_INCREMENT = 9200;
ALTER TABLE course          AUTO_INCREMENT = 9200;
ALTER TABLE course_section  AUTO_INCREMENT = 9200;
ALTER TABLE lesson          AUTO_INCREMENT = 9200;
ALTER TABLE posts           AUTO_INCREMENT = 9200;
ALTER TABLE comments        AUTO_INCREMENT = 9200;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 검증 쿼리 (실행 후 아래 SELECT로 확인)
-- ============================================================
-- SELECT COUNT(*) AS member_count   FROM members        WHERE member_id BETWEEN 9001 AND 9200;  -- 예상: 64
-- SELECT COUNT(*) AS enroll_count   FROM enrollment     WHERE course_id = 9001;                  -- 예상: 61
-- SELECT COUNT(*) AS post_count     FROM posts          WHERE author_id = 9004;                  -- 예상: 5
-- SELECT COUNT(*) AS comment_count  FROM comments       WHERE author_id = 9004;                  -- 예상: 10
-- SELECT COUNT(*) AS report_count   FROM reports        WHERE reported_member_id = 9004;          -- 예상: 49
-- SELECT status FROM members WHERE member_id = 9004;  -- 신고 전 ACTIVE, 50번째 신고 후 SUSPENDED
