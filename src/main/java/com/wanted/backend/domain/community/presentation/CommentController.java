package com.wanted.backend.domain.community.presentation;

import com.wanted.backend.domain.community.application.command.AcceptCommentCommand;
import com.wanted.backend.domain.community.application.command.CreateCommentCommand;
import com.wanted.backend.domain.community.application.command.DeleteCommentCommand;
import com.wanted.backend.domain.community.application.command.UpdateCommentCommand;
import com.wanted.backend.domain.community.application.usecase.CommentCommandUseCase;
import com.wanted.backend.domain.community.application.usecase.CommentQueryUseCase;
import com.wanted.backend.domain.community.presentation.request.CreateCommentRequest;
import com.wanted.backend.domain.community.presentation.request.UpdateCommentRequest;
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

    @PatchMapping(value = "/comments/{commentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> updateComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId,
            @RequestPart("data") @Valid UpdateCommentRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        commentCommandUseCase.update(new UpdateCommentCommand(
                userDetails.getMemberId(),
                commentId,
                request.content(),
                file
        ));

        return ApiResponse.successNoContent("댓글이 수정되었습니다.");
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId) {

        commentCommandUseCase.delete(new DeleteCommentCommand(
                userDetails.getMemberId(),
                commentId
        ));

        return ApiResponse.successNoContent("댓글이 삭제되었습니다.");
    }
}