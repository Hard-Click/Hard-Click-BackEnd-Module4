ALTER TABLE course_curriculum
    ADD COLUMN order_index INT NOT NULL DEFAULT 0;

DROP TABLE IF EXISTS videos;
DROP TABLE IF EXISTS lessons;
DROP TABLE IF EXISTS course_sections;