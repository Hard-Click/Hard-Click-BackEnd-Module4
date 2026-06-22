package com.wanted.backend.domain.cource.presentation.api.request;

import com.wanted.backend.domain.cource.application.query.CourseListQuery;
import com.wanted.backend.domain.cource.domain.model.CourseSortType;
import com.wanted.backend.global.domain.SubjectType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "강의 목록 조회 요청")
public class CourseListRequest {

    @Schema(description = "검색 키워드 (강의명)", example = "수학")
    private String keyword;

    @Schema(description = "과목명 필터", example = "MATH_1")
    private SubjectType subject;

    @Schema(description = "강사명 필터", example = "박지훈")
    private String instructorName;

    @Schema(description = "정렬 기준 (LATEST/POPULAR/RATING)", example = "LATEST")
    private CourseSortType sort = CourseSortType.LATEST;

    @Schema(description = "페이지 번호 (0-based)", example = "0")
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
    private int page = 0;

    @Schema(description = "페이지당 항목 수", example = "12")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다.")
    private int size = 12;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public SubjectType getSubject() { return subject; }
    public void setSubject(SubjectType subject) { this.subject = subject; }

    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }

    public CourseSortType getSort() { return sort; }
    public void setSort(CourseSortType sort) { this.sort = sort; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public CourseListQuery toQuery() {
        return new CourseListQuery(keyword, subject != null ? subject.name() : null, instructorName, sort, page, size);
    }
}
