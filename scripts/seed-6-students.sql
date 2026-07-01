-- 학생 테스트 계정 6개 (student1~6 / 비밀번호 Test1234! / BCrypt)
-- role=STUDENT, status=ACTIVE. 중복 실행 안전(username 기준 무시).
INSERT IGNORE INTO members
  (username, email, password, name, role, status, created_at,
   is_locked, is_password_change_required, login_fail_count, optional_terms_agreed)
VALUES
  ('student1', 'student1@test.com', '$2a$10$r5oXdfQSR9aIhYCWlCz29OxoqA9VVcv6DsmJSKEWKqcotfasmmOVO', '테스트학생1', 'STUDENT', 'ACTIVE', NOW(6), 0, 0, 0, 1),
  ('student2', 'student2@test.com', '$2a$10$lWNECLy.CYw1XNeIYxmxt.1/20hwg0TNBRKB52PXTfVIFCTutWRDm', '테스트학생2', 'STUDENT', 'ACTIVE', NOW(6), 0, 0, 0, 1),
  ('student3', 'student3@test.com', '$2a$10$WQQ7SiHy0AbQpyDHewctU.H9C/8IdnQOHpnw13Sc.AlH5E0Uu7RIm', '테스트학생3', 'STUDENT', 'ACTIVE', NOW(6), 0, 0, 0, 1),
  ('student4', 'student4@test.com', '$2a$10$0D5RzG9RGJqD1tmdPRuWmusuDidwT1Z94g5j6Wl83UjEkCXkAIFnq', '테스트학생4', 'STUDENT', 'ACTIVE', NOW(6), 0, 0, 0, 1),
  ('student5', 'student5@test.com', '$2a$10$W/C57LSOulvqJRbsJp0N0u6owmTmqBueSoZ9qpuUhBJD61Gt5nayq', '테스트학생5', 'STUDENT', 'ACTIVE', NOW(6), 0, 0, 0, 1),
  ('student6', 'student6@test.com', '$2a$10$He8wKA5jGtcxjbAOvX1rTeyknWMgkDLhQoSxlxOwt89H4amHVeoRi', '테스트학생6', 'STUDENT', 'ACTIVE', NOW(6), 0, 0, 0, 1);

SELECT member_id, username, email, role, status FROM members WHERE username IN
  ('student1','student2','student3','student4','student5','student6');
