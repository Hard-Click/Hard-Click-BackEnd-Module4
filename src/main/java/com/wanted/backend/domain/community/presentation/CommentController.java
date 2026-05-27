package com.wanted.backend.domain.community.presentation;

import com.wanted.backend.domain.community.application.command.AcceptCommentCommand;
import com.wanted.backend.domain.community.application.command.CreateCommentCommand;
import com.wanted.backend.domain.community.application.usecase.CommentCommandUseCase;
import com.wanted.backend.domain.community.application.usecase.CommentQueryUseCase;
import com.wanted.backend.domain.community.presentation.request.CreateCommentRequest;
import com.wanted.backend.domain.community.presentation.response.CommentListResponse;
import com.wanted.backend.domain.community.presentation.response.CreateCommentResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/*
 * [Presentation Layer - Controller]
 *
 * POST /api/comments                       댓글/대댓글 작성
 * GET  /api/posts/{postId}/comments        댓글 목록 조회
 * PATCH  /api/comments/{commentId}         댓글 수정 (추후 구현)
 * DELETE /api/comments/{commentId}         댓글 삭제 (추후 구현)
 * POST   /api/comments/{commentId}/accept  댓글 채택 (추후 구현)
 */
@RestController
@RequestMapping("/api")
public class CommentController {

    private final CommentCommandUseCase commentCommandUseCase;
    private final CommentQueryUseCase commentQueryUseCase;

    public CommentController(CommentCommandUseCase commentCommandUseCase, CommentQueryUseCase commentQueryUseCase) {
        this.commentCommandUseCase = commentCommandUseCase;
        this.commentQueryUseCase = commentQueryUseCase;
    }


    @PostMapping(value = "/comments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CreateCommentResponse>> createComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("data") @Valid CreateCommentRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        Long commentId = commentCommandUseCase.create(new CreateCommentCommand(
                userDetails.getMemberId(),
                request.postId(),
                request.parentId(),
                request.content(),
                file
        ));

        return ApiResponse.created("댓글이 등록되었습니다.", new CreateCommentResponse(commentId));
    }

    @PostMapping("/comments/{commentId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId) {

        commentCommandUseCase.accept(new AcceptCommentCommand(
                userDetails.getMemberId(),
                commentId
        ));

        return ApiResponse.successNoContent("댓글이 채택되었습니다.");
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentListResponse>> getComments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId) {

        CommentListResponse response = commentQueryUseCase.getComments(
                postId, userDetails.getMemberId());

        return ApiResponse.success("댓글 목록 조회 성공", response);
    }
}