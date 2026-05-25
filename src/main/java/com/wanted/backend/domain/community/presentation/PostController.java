package com.wanted.backend.domain.community.presentation;

import com.wanted.backend.domain.community.application.command.CreatePostCommand;
import com.wanted.backend.domain.community.application.command.DeletePostCommand;
import com.wanted.backend.domain.community.application.command.UpdatePostCommand;
import com.wanted.backend.domain.community.application.usecase.PostCommandUseCase;
import com.wanted.backend.domain.community.application.usecase.PostQueryUseCase;
import com.wanted.backend.domain.community.domain.model.BoardType;
import com.wanted.backend.domain.community.domain.model.PostSortType;
import com.wanted.backend.domain.community.presentation.request.CreatePostRequest;
import com.wanted.backend.domain.community.presentation.request.UpdatePostRequest;
import com.wanted.backend.domain.community.presentation.response.CreatePostResponse;
import com.wanted.backend.domain.community.presentation.response.PostDetailResponse;
import com.wanted.backend.domain.community.presentation.response.PostListResponse;
import com.wanted.backend.domain.community.presentation.response.UpdatePostResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostCommandUseCase postCommandUseCase;
    private final PostQueryUseCase postQueryUseCase;

    public PostController(PostCommandUseCase postCommandUseCase, PostQueryUseCase postQueryUseCase) {
        this.postCommandUseCase = postCommandUseCase;
        this.postQueryUseCase = postQueryUseCase;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CreatePostResponse>> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("data") @Valid CreatePostRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        Long postId = postCommandUseCase.create(new CreatePostCommand(
                userDetails.getMemberId(),
                request.boardType(),
                request.subjectId(),
                request.title(),
                request.content(),
                files
        ));

        return ApiResponse.created("게시글이 등록되었습니다.", new CreatePostResponse(postId));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PostListResponse>> getPostList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) BoardType boardType,
            @RequestParam(defaultValue = "latest") PostSortType sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page) {

        PostListResponse response = postQueryUseCase.getList(boardType, sort, keyword, page);
        return ApiResponse.success("게시글 목록 조회 성공", response);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPostDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId) {

        PostDetailResponse response = postQueryUseCase.getDetail(
                postId, userDetails.getMemberId());

        return ApiResponse.success("게시글 상세 조회 성공", response);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId) {

        postCommandUseCase.delete(new DeletePostCommand(
                userDetails.getMemberId(), postId));

        return ApiResponse.successNoContent("게시글이 삭제되었습니다.");
    }

    @PatchMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UpdatePostResponse>> updatePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @RequestPart("data") @Valid UpdatePostRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        Long updatedPostId = postCommandUseCase.update(new UpdatePostCommand(
                userDetails.getMemberId(),
                postId,
                request.subjectId(),
                request.title(),
                request.content(),
                files
        ));

        return ApiResponse.success("게시글이 수정되었습니다.", new UpdatePostResponse(updatedPostId));
    }


}