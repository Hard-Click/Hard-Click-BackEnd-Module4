package com.wanted.backend.domain.quiz.presentation;

import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class QuizMockController {

    @GetMapping("/members/me/quizzes")
    public ResponseEntity<ApiResponse<MyQuizListResponse>> getMyQuizzes(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        MyQuizListResponse response = new MyQuizListResponse(
                List.of(
                        new MyQuizListResponse.MyQuizItem(
                                90L,
                                "React 기초 개념 퀴즈",
                                "React 완벽 가이드",
                                "섹션 1: React 기초",
                                2,
                                true,
                                80,
                                OffsetDateTime.parse("2026-05-10T15:30:00+09:00")
                        ),
                        new MyQuizListResponse.MyQuizItem(
                                91L,
                                "Hooks 활용 퀴즈",
                                "React 완벽 가이드",
                                "섹션 2: Hooks",
                                1,
                                false,
                                null,
                                null
                        )
                ),
                2,
                page == null ? 0 : page,
                size == null ? 10 : size
        );

        return ApiResponse.success("내 퀴즈 목록을 조회했습니다.", response);
    }

    @GetMapping("/quizzes/{quizId}")
    public ResponseEntity<ApiResponse<StudentQuizDetailResponse>> getStudentQuizDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long quizId
    ) {
        StudentQuizDetailResponse response = new StudentQuizDetailResponse(
                quizId,
                "React 기초 개념 퀴즈",
                "React 완벽 가이드",
                "섹션 1: React 기초",
                2,
                1,
                false,
                List.of(
                        new StudentQuizDetailResponse.Question(
                                1L,
                                1,
                                "React의 가상 DOM이란 무엇인가요?",
                                List.of(
                                        new StudentQuizDetailResponse.Option(1L, 1, "실제 DOM의 복사본"),
                                        new StudentQuizDetailResponse.Option(2L, 2, "메모리에 존재하는 DOM의 표현"),
                                        new StudentQuizDetailResponse.Option(3L, 3, "HTML 파일"),
                                        new StudentQuizDetailResponse.Option(4L, 4, "CSS 스타일시트")
                                )
                        ),
                        new StudentQuizDetailResponse.Question(
                                2L,
                                2,
                                "JSX는 무엇의 약자인가요?",
                                List.of(
                                        new StudentQuizDetailResponse.Option(5L, 1, "JavaScript XML"),
                                        new StudentQuizDetailResponse.Option(6L, 2, "Java Syntax Extension"),
                                        new StudentQuizDetailResponse.Option(7L, 3, "JSON XML"),
                                        new StudentQuizDetailResponse.Option(8L, 4, "JavaScript Extension")
                                )
                        )
                )
        );

        return ApiResponse.success("퀴즈 상세 정보를 조회했습니다.", response);
    }

    @PostMapping("/quizzes/{quizId}/submissions")
    public ResponseEntity<ApiResponse<QuizSubmissionResponse>> submitQuiz(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long quizId,
            @RequestBody(required = false) QuizSubmissionRequest request
    ) {
        QuizSubmissionResponse response = new QuizSubmissionResponse(
                55L,
                quizId,
                80,
                2,
                1,
                1,
                OffsetDateTime.parse("2026-05-10T15:30:00+09:00")
        );

        return ApiResponse.success("퀴즈가 제출되었습니다.", response);
    }

    @GetMapping("/quizzes/{quizId}/reports/me")
    public ResponseEntity<ApiResponse<QuizReportResponse>> getMyQuizReport(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long quizId
    ) {
        QuizReportResponse response = new QuizReportResponse(
                quizId,
                "React 기초 개념 퀴즈",
                "React 완벽 가이드",
                "섹션 1: React 기초",
                80,
                2,
                1,
                1,
                OffsetDateTime.parse("2026-05-10T15:30:00+09:00"),
                List.of(
                        new QuizReportResponse.QuestionResult(
                                1L,
                                1,
                                "React의 가상 DOM이란 무엇인가요?",
                                2L,
                                2L,
                                true,
                                "가상 DOM은 메모리에 존재하는 UI 표현이며 실제 DOM 변경을 효율화합니다.",
                                List.of(
                                        new QuizReportResponse.Option(1L, 1, "실제 DOM의 복사본"),
                                        new QuizReportResponse.Option(2L, 2, "메모리에 존재하는 DOM의 표현"),
                                        new QuizReportResponse.Option(3L, 3, "HTML 파일"),
                                        new QuizReportResponse.Option(4L, 4, "CSS 스타일시트")
                                )
                        ),
                        new QuizReportResponse.QuestionResult(
                                2L,
                                2,
                                "JSX는 무엇의 약자인가요?",
                                6L,
                                5L,
                                false,
                                "JSX는 JavaScript XML의 약자로 JavaScript 안에서 UI 구조를 표현합니다.",
                                List.of(
                                        new QuizReportResponse.Option(5L, 1, "JavaScript XML"),
                                        new QuizReportResponse.Option(6L, 2, "Java Syntax Extension"),
                                        new QuizReportResponse.Option(7L, 3, "JSON XML"),
                                        new QuizReportResponse.Option(8L, 4, "JavaScript Extension")
                                )
                        )
                )
        );

        return ApiResponse.success("오답노트 및 리포트를 조회했습니다.", response);
    }

    @GetMapping("/instructor/quizzes")
    public ResponseEntity<ApiResponse<InstructorQuizListResponse>> getInstructorQuizzes(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long sectionId
    ) {
        InstructorQuizListResponse response = new InstructorQuizListResponse(
                courseId,
                sectionId,
                List.of(
                        new InstructorQuizListResponse.InstructorQuizItem(
                                90L,
                                "React 기초 개념 퀴즈",
                                "React 완벽 가이드",
                                "섹션 1: React 기초",
                                2,
                                OffsetDateTime.parse("2026-05-10T15:30:00+09:00")
                        ),
                        new InstructorQuizListResponse.InstructorQuizItem(
                                91L,
                                "Hooks 활용 퀴즈",
                                "React 완벽 가이드",
                                "섹션 2: Hooks",
                                1,
                                OffsetDateTime.parse("2026-05-11T15:30:00+09:00")
                        )
                )
        );

        return ApiResponse.success("강사 퀴즈 목록을 조회했습니다.", response);
    }

    @GetMapping("/instructor/quizzes/{quizId}")
    public ResponseEntity<ApiResponse<InstructorQuizDetailResponse>> getInstructorQuizDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long quizId
    ) {
        return ApiResponse.success("강사 퀴즈 상세 정보를 조회했습니다.", instructorQuizDetail(quizId));
    }

    @PostMapping("/instructor/quizzes")
    public ResponseEntity<ApiResponse<InstructorQuizMutationResponse>> createInstructorQuiz(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody(required = false) InstructorQuizRequest request
    ) {
        InstructorQuizMutationResponse response = new InstructorQuizMutationResponse(
                90L,
                request == null || request.quizTitle() == null ? "React 기초 개념 퀴즈" : request.quizTitle(),
                request == null || request.questions() == null ? 2 : request.questions().size(),
                OffsetDateTime.parse("2026-06-11T16:00:00+09:00")
        );

        return ApiResponse.created("퀴즈가 등록되었습니다.", response);
    }

    @PutMapping("/instructor/quizzes/{quizId}")
    public ResponseEntity<ApiResponse<InstructorQuizMutationResponse>> updateInstructorQuiz(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long quizId,
            @RequestBody(required = false) InstructorQuizRequest request
    ) {
        InstructorQuizMutationResponse response = new InstructorQuizMutationResponse(
                quizId,
                request == null || request.quizTitle() == null ? "React 기초 개념 퀴즈" : request.quizTitle(),
                request == null || request.questions() == null ? 2 : request.questions().size(),
                OffsetDateTime.parse("2026-06-11T16:00:00+09:00")
        );

        return ApiResponse.success("퀴즈가 수정되었습니다.", response);
    }

    @DeleteMapping("/instructor/quizzes/{quizId}")
    public ResponseEntity<ApiResponse<InstructorQuizDeleteResponse>> deleteInstructorQuiz(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long quizId
    ) {
        InstructorQuizDeleteResponse response = new InstructorQuizDeleteResponse(
                quizId,
                "DELETED",
                OffsetDateTime.parse("2026-06-11T16:00:00+09:00")
        );

        return ApiResponse.success("퀴즈가 삭제되었습니다.", response);
    }

    @GetMapping("/instructors/me/quizzes/{quizId}/statistics")
    public ResponseEntity<ApiResponse<InstructorQuizStatisticsResponse>> getInstructorQuizStatistics(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long quizId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        InstructorQuizStatisticsResponse response = new InstructorQuizStatisticsResponse(
                "수1 푸시기",
                "2주차: 미적분 기본기 다지기",
                "2주차 퀴즈",
                new InstructorQuizStatisticsResponse.Summary(5, 1, 80),
                List.of(
                        new InstructorQuizStatisticsResponse.ScoreDistribution("90~100", 2, 40),
                        new InstructorQuizStatisticsResponse.ScoreDistribution("70~89", 2, 40),
                        new InstructorQuizStatisticsResponse.ScoreDistribution("50~69", 1, 20),
                        new InstructorQuizStatisticsResponse.ScoreDistribution("0~49", 0, 0)
                ),
                List.of(
                        new InstructorQuizStatisticsResponse.StudentScore("@choiaa2026", "최*아", true, 100, "2026-05-10"),
                        new InstructorQuizStatisticsResponse.StudentScore("@kimsu92", "김*수", true, 90, "2026-05-12"),
                        new InstructorQuizStatisticsResponse.StudentScore("@hanyeong3", "한*영", false, null, null)
                )
        );

        return ApiResponse.success("퀴즈 점수 현황을 조회했습니다.", response);
    }

    private InstructorQuizDetailResponse instructorQuizDetail(Long quizId) {
        return new InstructorQuizDetailResponse(
                quizId,
                "React 기초 개념 퀴즈",
                1L,
                "React 완벽 가이드",
                1L,
                "섹션 1: React 기초",
                2,
                OffsetDateTime.parse("2026-05-10T15:30:00+09:00"),
                List.of(
                        new InstructorQuizDetailResponse.Question(
                                1L,
                                1,
                                "React의 가상 DOM이란 무엇인가요?",
                                2L,
                                "가상 DOM은 메모리에 존재하는 UI 표현이며 실제 DOM 변경을 효율화합니다.",
                                List.of(
                                        new InstructorQuizDetailResponse.Option(1L, 1, "실제 DOM의 복사본", false),
                                        new InstructorQuizDetailResponse.Option(2L, 2, "메모리에 존재하는 DOM의 표현", true),
                                        new InstructorQuizDetailResponse.Option(3L, 3, "HTML 파일", false),
                                        new InstructorQuizDetailResponse.Option(4L, 4, "CSS 스타일시트", false)
                                )
                        ),
                        new InstructorQuizDetailResponse.Question(
                                2L,
                                2,
                                "JSX는 무엇의 약자인가요?",
                                5L,
                                "JSX는 JavaScript XML의 약자로 JavaScript 안에서 UI 구조를 표현합니다.",
                                List.of(
                                        new InstructorQuizDetailResponse.Option(5L, 1, "JavaScript XML", true),
                                        new InstructorQuizDetailResponse.Option(6L, 2, "Java Syntax Extension", false),
                                        new InstructorQuizDetailResponse.Option(7L, 3, "JSON XML", false),
                                        new InstructorQuizDetailResponse.Option(8L, 4, "JavaScript Extension", false)
                                )
                        )
                )
        );
    }

    public record MyQuizListResponse(
            List<MyQuizItem> quizzes,
            int totalCount,
            int page,
            int size
    ) {
        public record MyQuizItem(
                Long quizId,
                String quizTitle,
                String courseTitle,
                String sectionTitle,
                int questionCount,
                boolean submitted,
                Integer score,
                OffsetDateTime submittedAt
        ) {
        }
    }

    public record StudentQuizDetailResponse(
            Long quizId,
            String quizTitle,
            String courseTitle,
            String sectionTitle,
            int totalQuestionCount,
            int answeredCount,
            boolean submitted,
            List<Question> questions
    ) {
        public record Question(Long questionId, int questionNumber, String questionText, List<Option> options) {
        }

        public record Option(Long optionId, int optionNumber, String optionText) {
        }
    }

    public record QuizSubmissionRequest(List<Answer> answers) {
        public record Answer(Long questionId, Long selectedOptionId) {
        }
    }

    public record QuizSubmissionResponse(
            Long submissionId,
            Long quizId,
            int score,
            int totalQuestionCount,
            int correctCount,
            int incorrectCount,
            OffsetDateTime submittedAt
    ) {
    }

    public record QuizReportResponse(
            Long quizId,
            String quizTitle,
            String courseTitle,
            String sectionTitle,
            int score,
            int totalQuestionCount,
            int correctCount,
            int incorrectCount,
            OffsetDateTime submittedAt,
            List<QuestionResult> questions
    ) {
        public record QuestionResult(
                Long questionId,
                int questionNumber,
                String questionText,
                Long correctOptionId,
                Long selectedOptionId,
                boolean correct,
                String explanation,
                List<Option> options
        ) {
        }

        public record Option(Long optionId, int optionNumber, String optionText) {
        }
    }

    public record InstructorQuizListResponse(
            Long courseId,
            Long sectionId,
            List<InstructorQuizItem> quizzes
    ) {
        public record InstructorQuizItem(
                Long quizId,
                String quizTitle,
                String courseTitle,
                String sectionTitle,
                int questionCount,
                OffsetDateTime createdAt
        ) {
        }
    }

    public record InstructorQuizDetailResponse(
            Long quizId,
            String quizTitle,
            Long courseId,
            String courseTitle,
            Long sectionId,
            String sectionTitle,
            int questionCount,
            OffsetDateTime createdAt,
            List<Question> questions
    ) {
        public record Question(
                Long questionId,
                int questionNumber,
                String questionText,
                Long correctOptionId,
                String explanation,
                List<Option> options
        ) {
        }

        public record Option(Long optionId, int optionNumber, String optionText, boolean correct) {
        }
    }

    public record InstructorQuizRequest(
            String quizTitle,
            Long courseId,
            Long sectionId,
            List<Question> questions
    ) {
        public record Question(
                Long questionId,
                String questionText,
                Long correctOptionId,
                String explanation,
                List<Option> options
        ) {
        }

        public record Option(Long optionId, String optionText) {
        }
    }

    public record InstructorQuizMutationResponse(
            Long quizId,
            String quizTitle,
            int questionCount,
            OffsetDateTime updatedAt
    ) {
    }

    public record InstructorQuizDeleteResponse(
            Long quizId,
            String status,
            OffsetDateTime deletedAt
    ) {
    }

    public record InstructorQuizStatisticsResponse(
            String courseTitle,
            String sectionTitle,
            String quizTitle,
            Summary summary,
            List<ScoreDistribution> scoreDistribution,
            List<StudentScore> students
    ) {
        public record Summary(int submittedCount, int notSubmittedCount, int averageScore) {
        }

        public record ScoreDistribution(String range, int count, int percentage) {
        }

        public record StudentScore(
                String userId,
                String name,
                boolean submitted,
                Integer score,
                String submittedAt
        ) {
        }
    }
}
