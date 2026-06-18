package com.wanted.backend.global.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * EC2/로컬 서버 최초 기동 시 프론트엔드 연동용 테스트 계정과 시드 데이터를 자동 생성한다.
 * 이미 존재하는 데이터는 건너뛰므로 재기동해도 중복 생성되지 않는다.
 *
 * ─────────────────────────────────────────────
 *  역할        아이디          이메일                    비밀번호
 * ─────────────────────────────────────────────
 *  관리자      hc_admin       hcadmin@gmail.com        HardClick1!
 *  강사        hc_prof        hcprof@gmail.com         HardClick1!
 *  학생        hc_student     hcstudent@gmail.com      HardClick1!
 * ─────────────────────────────────────────────
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TestDataInitializer {

    private static final String PASSWORD = "HardClick1!";

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        log.info("[TestDataInitializer] 테스트 데이터 초기화 시작");

        Long adminId = createMemberIfAbsent("hc_admin", "hcadmin@gmail.com", "관리자계정", "ADMIN");
        Long profId = createMemberIfAbsent("hc_prof", "hcprof@gmail.com", "강사계정", "INSTRUCTOR");
        Long studentId = createMemberIfAbsent("hc_student", "hcstudent@gmail.com", "학생계정", "STUDENT");

        Long course1 = createCourseIfAbsent(profId, "Spring Boot 마스터클래스",
                "백엔드", "Spring Boot 3.x 기반 REST API 설계부터 배포까지", 99000);
        Long course2 = createCourseIfAbsent(profId, "Java 알고리즘 완성",
                "알고리즘", "코딩테스트를 위한 Java 자료구조와 알고리즘 총정리", 79000);
        Long course3 = createCourseIfAbsent(profId, "React + TypeScript 실전",
                "프론트엔드", "실무 프로젝트로 배우는 React 18 + TypeScript 완성", 89000);

        createSubjectsIfAbsent();

        enrollIfAbsent(studentId, course1);
        enrollIfAbsent(studentId, course2);
        enrollIfAbsent(studentId, course3);

        log.info("[TestDataInitializer] 완료 — admin:{} / prof:{} / student:{}", adminId, profId, studentId);
    }

    private Long createMemberIfAbsent(String username, String email, String name, String role) {
        Long existing = jdbc.query(
                "SELECT member_id FROM members WHERE username = ?",
                (rs, n) -> rs.getLong("member_id"),
                username
        ).stream().findFirst().orElse(null);

        if (existing != null) {
            log.info("[TestDataInitializer] 이미 존재하는 계정 건너뜀: {}", username);
            return existing;
        }

        String encoded = passwordEncoder.encode(PASSWORD);
        jdbc.update("""
                INSERT INTO members
                  (username, email, password, name, gender, birth_date, phone_number,
                   role, status, is_password_change_required, login_fail_count,
                   is_locked, optional_terms_agreed, created_at, updated_at)
                VALUES (?, ?, ?, ?, 'MALE', ?, '010-0000-0000',
                        ?, 'ACTIVE', false, 0,
                        false, true, NOW(), NOW())
                """,
                username, email, encoded, name,
                LocalDate.of(1995, 1, 1),
                role
        );

        Long id = jdbc.queryForObject(
                "SELECT member_id FROM members WHERE username = ?", Long.class, username);
        log.info("[TestDataInitializer] 계정 생성: {} (id={})", username, id);
        return id;
    }

    private Long createCourseIfAbsent(Long authorId, String title, String subject, String description, int price) {
        Long existing = jdbc.query(
                "SELECT course_id FROM course WHERE title = ?",
                (rs, n) -> rs.getLong("course_id"),
                title
        ).stream().findFirst().orElse(null);

        if (existing != null) {
            return existing;
        }

        jdbc.update("""
                INSERT INTO course
                  (author_id, title, subject, description, thumbnail_url,
                   price_type, price, status, created_at,
                   learning_objectives, target_audience, tech_tags, level)
                VALUES (?, ?, ?, ?, NULL,
                        'PAID', ?, 'PUBLISHED', NOW(),
                        '실무 역량 향상', '개발자 지망생, 현직 개발자', 'Java, Spring', '중급')
                """,
                authorId, title, subject, description, price
        );

        Long id = jdbc.queryForObject(
                "SELECT course_id FROM course WHERE title = ?", Long.class, title);
        log.info("[TestDataInitializer] 강의 생성: {} (id={})", title, id);
        return id;
    }

    private void createSubjectsIfAbsent() {
        String[] subjects = {"백엔드", "프론트엔드", "알고리즘", "데이터베이스", "CS기초", "인공지능", "모바일"};
        for (String s : subjects) {
            Integer count = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM subjects WHERE name = ?", Integer.class, s);
            if (count == null || count == 0) {
                jdbc.update("INSERT INTO subjects (name) VALUES (?)", s);
            }
        }
    }

    private void enrollIfAbsent(Long memberId, Long courseId) {
        if (memberId == null || courseId == null) return;
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM enrollment WHERE member_id = ? AND course_id = ?",
                Integer.class, memberId, courseId);
        if (count != null && count > 0) return;

        jdbc.update("""
                INSERT INTO enrollment (member_id, course_id, enrolled_at, status)
                VALUES (?, ?, NOW(), 'IN_PROGRESS')
                """, memberId, courseId);
        log.info("[TestDataInitializer] 수강 등록: memberId={}, courseId={}", memberId, courseId);
    }
}
