package com.wanted.backend.domain.report_moderation.application.policy;

import com.wanted.backend.domain.community.domain.model.TargetType;
import org.springframework.stereotype.Component;

@Component
public class AdminContentDisplayPolicy {

    private static final String ACTIVE = "ACTIVE";
    private static final String ADMIN_DELETED = "ADMIN_DELETED";

    private static final String DELETED_POST_CONTENT = "삭제된 게시글입니다.";
    private static final String ADMIN_DELETED_POST_CONTENT = "관리자에 의해 삭제된 게시글입니다.";
    private static final String DELETED_COMMENT_CONTENT = "삭제된 댓글입니다.";
    private static final String ADMIN_DELETED_COMMENT_CONTENT = "관리자에 의해 삭제된 댓글입니다.";
    private static final String DELETED_REVIEW_CONTENT = "삭제된 리뷰입니다.";
    private static final String ADMIN_DELETED_REVIEW_CONTENT = "관리자에 의해 삭제된 리뷰입니다.";

    public String resolveContent(TargetType contentType, String status, String rawContent) {
        if (ACTIVE.equals(status)) {
            return rawContent;
        }

        if (ADMIN_DELETED.equals(status)) {
            return switch (contentType) {
                case POST -> ADMIN_DELETED_POST_CONTENT;
                case COMMENT -> ADMIN_DELETED_COMMENT_CONTENT;
                case REVIEW -> ADMIN_DELETED_REVIEW_CONTENT;
            };
        }

        return switch (contentType) {
            case POST -> DELETED_POST_CONTENT;
            case COMMENT -> DELETED_COMMENT_CONTENT;
            case REVIEW -> DELETED_REVIEW_CONTENT;
        };
    }
}
