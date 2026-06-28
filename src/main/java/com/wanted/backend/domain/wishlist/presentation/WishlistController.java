package com.wanted.backend.domain.wishlist.presentation;

import com.wanted.backend.domain.wishlist.application.usecase.AddWishlistItemUseCase;
import com.wanted.backend.domain.wishlist.application.usecase.GetWishlistUseCase;
import com.wanted.backend.domain.wishlist.application.usecase.RemoveWishlistItemUseCase;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishlist")
@Tag(name = "Wishlist", description = "찜하기 API")
public class WishlistController {

    private final GetWishlistUseCase getWishlistUseCase;
    private final AddWishlistItemUseCase addWishlistItemUseCase;
    private final RemoveWishlistItemUseCase removeWishlistItemUseCase;

    @Operation(summary = "찜 목록 조회", description = "로그인 수강생의 찜한 강의 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "찜 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<WishlistResponse>> getWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<GetWishlistUseCase.Item> items = getWishlistUseCase.handle(userDetails.getMemberId());
        return ApiResponse.success("찜 목록을 조회했습니다.", WishlistResponse.from(items));
    }

    @Operation(summary = "강의 찜하기", description = "강의를 찜 목록에 추가합니다. 이미 찜한 강의는 409를 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "찜하기 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "강의를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 찜한 강의")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addWishlistItem(
            @Valid @RequestBody AddWishlistItemRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        addWishlistItemUseCase.handle(userDetails.getMemberId(), request.courseId());
        return ApiResponse.successNoContent("찜 목록에 추가되었습니다.");
    }

    @Operation(summary = "찜하기 취소", description = "찜 목록에서 특정 강의를 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "찜하기 취소 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "찜 목록에 해당 강의가 없음")
    })
    @DeleteMapping("/{courseId}")
    public ResponseEntity<ApiResponse<Void>> removeWishlistItem(
            @Parameter(description = "찜하기를 취소할 강의 ID", example = "1")
            @PathVariable Long courseId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        removeWishlistItemUseCase.handle(userDetails.getMemberId(), courseId);
        return ApiResponse.successNoContent("찜 목록에서 삭제되었습니다.");
    }
}
