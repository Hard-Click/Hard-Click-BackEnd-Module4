package com.wanted.backend.domain.community.presentation;

import com.wanted.backend.domain.community.application.command.CreatePostCommand;
import com.wanted.backend.domain.community.application.usecase.PostCommandUseCase;
import com.wanted.backend.domain.community.presentation.request.CreatePostRequest;
import com.wanted.backend.domain.community.presentation.response.CreatePostResponse;
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

    public PostController(PostCommandUseCase postCommandUseCase) {
        this.postCommandUseCase = postCommandUseCase;
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
}