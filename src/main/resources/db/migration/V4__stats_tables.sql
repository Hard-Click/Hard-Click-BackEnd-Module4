CREATE TABLE study_timer_sessions (
    study_timer_session_id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    course_id BIGINT NULL,
    lesson_id BIGINT NULL,
    started_at DATETIME(6) NOT NULL,
    ended_at DATETIME(6) NULL,
    elapsed_seconds INT NOT NULL DEFAULT 0,
    status ENUM('RUNNING','ENDED','CANCELED') NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (study_timer_session_id),
    KEY idx_study_timer_sessions_member_started_at (member_id, started_at),
    KEY idx_study_timer_sessions_member_status (member_id, status),
    KEY idx_study_timer_sessions_course_member (course_id, member_id),
    KEY idx_study_timer_sessions_lesson_id (lesson_id),
    CONSTRAINT fk_study_timer_sessions_member
        FOREIGN KEY (member_id) REFERENCES members (member_id),
    CONSTRAINT fk_study_timer_sessions_course
        FOREIGN KEY (course_id) REFERENCES course (course_id),
    CONSTRAINT fk_study_timer_sessions_lesson
        FOREIGN KEY (lesson_id) REFERENCES lesson (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE daily_study_stats (
    daily_study_stat_id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    stat_date DATE NOT NULL,
    study_time_seconds INT NOT NULL DEFAULT 0,
    watched_lesson_count INT NOT NULL DEFAULT 0,
    completed_lesson_count INT NOT NULL DEFAULT 0,
    last_activity_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (daily_study_stat_id),
    UNIQUE KEY uk_daily_study_stats_member_date (member_id, stat_date),
    KEY idx_daily_study_stats_stat_date (stat_date),
    CONSTRAINT fk_daily_study_stats_member
        FOREIGN KEY (member_id) REFERENCES members (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ranking_sync_logs (
    ranking_sync_log_id BIGINT NOT NULL AUTO_INCREMENT,
    ranking_type ENUM('STUDY_TIME','LESSON','ACCEPTED_COMMENT') NOT NULL,
    period_type ENUM('DAILY','WEEKLY','MONTHLY') NOT NULL,
    period_key VARCHAR(30) NOT NULL,
    status ENUM('SUCCESS','FAILED') NOT NULL,
    target_count INT NOT NULL DEFAULT 0,
    started_at DATETIME(6) NOT NULL,
    ended_at DATETIME(6) NULL,
    error_message TEXT NULL,
    PRIMARY KEY (ranking_sync_log_id),
    KEY idx_ranking_sync_logs_type_period_key (ranking_type, period_type, period_key),
    KEY idx_ranking_sync_logs_started_at (started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE quizzes (
    quiz_id BIGINT NOT NULL AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    section_id BIGINT NULL,
    instructor_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NULL,
    status ENUM('DRAFT','PUBLISHED','DELETED') NOT NULL DEFAULT 'PUBLISHED',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,
    deleted_at DATETIME(6) NULL,
    PRIMARY KEY (quiz_id),
    KEY idx_quizzes_course_section (course_id, section_id),
    KEY idx_quizzes_instructor_id (instructor_id),
    KEY idx_quizzes_status (status),
    CONSTRAINT fk_quizzes_course
        FOREIGN KEY (course_id) REFERENCES course (course_id),
    CONSTRAINT fk_quizzes_section
        FOREIGN KEY (section_id) REFERENCES course_section (id),
    CONSTRAINT fk_quizzes_instructor
        FOREIGN KEY (instructor_id) REFERENCES members (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE quiz_questions (
    question_id BIGINT NOT NULL AUTO_INCREMENT,
    quiz_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    explanation TEXT NULL,
    question_order INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (question_id),
    KEY idx_quiz_questions_quiz_order (quiz_id, question_order),
    CONSTRAINT fk_quiz_questions_quiz
        FOREIGN KEY (quiz_id) REFERENCES quizzes (quiz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE quiz_options (
    option_id BIGINT NOT NULL AUTO_INCREMENT,
    question_id BIGINT NOT NULL,
    option_text VARCHAR(500) NOT NULL,
    option_order INT NOT NULL,
    is_correct BIT(1) NOT NULL DEFAULT b'0',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (option_id),
    KEY idx_quiz_options_question_order (question_id, option_order),
    CONSTRAINT fk_quiz_options_question
        FOREIGN KEY (question_id) REFERENCES quiz_questions (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE quiz_submissions (
    submission_id BIGINT NOT NULL AUTO_INCREMENT,
    quiz_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    question_count INT NOT NULL,
    correct_count INT NOT NULL,
    wrong_count INT NOT NULL,
    score INT NOT NULL,
    submitted_at DATETIME(6) NOT NULL,
    PRIMARY KEY (submission_id),
    UNIQUE KEY uk_quiz_submissions_quiz_member (quiz_id, member_id),
    KEY idx_quiz_submissions_member_submitted_at (member_id, submitted_at),
    CONSTRAINT fk_quiz_submissions_quiz
        FOREIGN KEY (quiz_id) REFERENCES quizzes (quiz_id),
    CONSTRAINT fk_quiz_submissions_member
        FOREIGN KEY (member_id) REFERENCES members (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE quiz_submission_answers (
    submission_answer_id BIGINT NOT NULL AUTO_INCREMENT,
    submission_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    selected_option_id BIGINT NULL,
    is_correct BIT(1) NOT NULL,
    question_text_snapshot TEXT NULL,
    selected_option_text_snapshot VARCHAR(500) NULL,
    correct_option_text_snapshot VARCHAR(500) NULL,
    explanation_snapshot TEXT NULL,
    PRIMARY KEY (submission_answer_id),
    KEY idx_quiz_submission_answers_submission_id (submission_id),
    KEY idx_quiz_submission_answers_question_id (question_id),
    KEY idx_quiz_submission_answers_selected_option_id (selected_option_id),
    CONSTRAINT fk_quiz_submission_answers_submission
        FOREIGN KEY (submission_id) REFERENCES quiz_submissions (submission_id),
    CONSTRAINT fk_quiz_submission_answers_question
        FOREIGN KEY (question_id) REFERENCES quiz_questions (question_id),
    CONSTRAINT fk_quiz_submission_answers_selected_option
        FOREIGN KEY (selected_option_id) REFERENCES quiz_options (option_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
