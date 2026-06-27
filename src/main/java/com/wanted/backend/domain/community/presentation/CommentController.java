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
import com.wanted.backend.domain.community.presentation.response.UpdateCommentResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(
            summary = "댓글 작성",
            description = """
                게시글에 댓글 또는 대댓글을 작성합니다.
                - 로그인한 회원만 작성할 수 있습니다.
                - parentId를 함께 전달하면 대댓글로 등록됩니다. (선택사항)
                - 댓글 내용은 300자 이하여야 합니다.
                - 이미지 파일 첨부는 선택사항이며 jpg, jpeg, png만 허용합니다.
                - 요청 타입은 multipart/form-data 입니다.
                """
    )
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
    @Operation(
            summary = "댓글 채택",
            description = """
                질문게시판 게시글 작성자가 댓글을 채택합니다.
                - 로그인한 회원만 채택할 수 있습니다.
                - 질문게시판 게시글만 채택이 가능합니다.
                - 본인이 작성한 게시글의 댓글만 채택할 수 있습니다.
                - 대댓글은 채택할 수 없습니다.
                - 게시글당 하나의 댓글만 채택 가능하며, 채택 취소는 불가능합니다.
                """
    )
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
    @Operation(
            summary = "댓글 목록 조회",
            description = """
            게시글의 댓글 목록을 조회합니다.
            - 로그인한 회원만 조회 가능합니다.
            - 댓글에 대댓글이 있을 경우 replies 필드에 중첩하여 반환합니다.
            - 본인이 작성한 댓글은 isMine: true로 표시됩니다.
            - 채택된 댓글은 isAccepted: true로 표시됩니다.
            - 삭제된 댓글은 isDeleted: true로 표시됩니다.
            """
    )
    public ResponseEntity<ApiResponse<CommentListResponse>> getComments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId) {

        boolean isAdmin = "ADMIN".equals(userDetails.getRole());
        CommentListResponse response = commentQueryUseCase.getComments(
                postId, userDetails.getMemberId(), isAdmin);

        return ApiResponse.success("댓글 목록 조회 성공", response);
    }

    @PatchMapping(value = "/comments/{commentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "댓글 수정",
            description = """
                본인이 작성한 댓글을 수정합니다.
                - 로그인한 회원만 수정할 수 있습니다.
                - 본인이 작성한 댓글인지 검증 후 수정합니다.
                - 채택된 댓글은 수정할 수 없습니다.
                - 댓글 내용은 300자 이하여야 합니다.
                - 이미지 파일 첨부는 선택사항이며 jpg, jpeg, png만 허용합니다.
                - 기존 첨부 이미지는 새 파일로 교체되며, 파일 미전송 시 이미지가 제거됩니다.
                - 요청 타입은 multipart/form-data 입니다.
                """
    )
    public ResponseEntity<ApiResponse<UpdateCommentResponse>> updateComment(
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

        return ApiResponse.success("댓글이 수정되었습니다.", new UpdateCommentResponse(commentId));
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(
            summary = "댓글 삭제",
            description = """
            댓글을 삭제합니다.
            - 로그인한 회원만 삭제할 수 있습니다.
            - 본인이 작성한 댓글인지 검증 후 삭제합니다.
            - 채택된 댓글은 삭제할 수 없습니다.
            - 대댓글이 존재하는 댓글은 즉시 삭제되지 않고 삭제 상태로 표시됩니다.
            - 대댓글이 없는 댓글은 즉시 삭제됩니다.
            - ADMIN은 모든 댓글을 삭제할 수 있습니다.
            """
    )
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId) {

        commentCommandUseCase.delete(new DeleteCommentCommand(
                userDetails.getMemberId(),
                commentId,
                "ADMIN".equals(userDetails.getRole())));

        return ApiResponse.successNoContent("댓글이 삭제되었습니다.");
    }
}