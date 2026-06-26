package com.wanted.backend.domain.cart.presentation;

import com.wanted.backend.domain.cart.application.usecase.AddCartItemUseCase;
import com.wanted.backend.domain.cart.application.usecase.GetCartUseCase;
import com.wanted.backend.domain.cart.application.usecase.RemoveAllCartItemsUseCase;
import com.wanted.backend.domain.cart.application.usecase.RemoveCartItemUseCase;
import com.wanted.backend.domain.cart.presentation.request.AddCartItemRequest;
import com.wanted.backend.domain.cart.presentation.response.CartResponse;
import com.wanted.backend.global.common.ApiResponse;
import com.wanted.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/cart")
@Tag(name = "Cart", description = "장바구니 API")
public class CartController {

    private final GetCartUseCase getCartUseCase;
    private final AddCartItemUseCase addCartItemUseCase;
    private final RemoveCartItemUseCase removeCartItemUseCase;
    private final RemoveAllCartItemsUseCase removeAllCartItemsUseCase;

    @Operation(summary = "장바구니 조회", description = "로그인 사용자의 장바구니 목록과 결제 예정 금액을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        GetCartUseCase.Result result = getCartUseCase.handle(userDetails.getMemberId());
        return ApiResponse.success("장바구니를 조회했습니다.", CartResponse.from(result));
    }

    @Operation(summary = "장바구니 강의 추가", description = "강의를 장바구니에 추가합니다. 이미 담긴 강의는 409를 반환합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addCartItem(
            @Valid @RequestBody AddCartItemRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        addCartItemUseCase.handle(userDetails.getMemberId(), request.courseId());
        return ApiResponse.successNoContent("장바구니에 추가되었습니다.");
    }

    @Operation(summary = "장바구니 강의 단건 삭제", description = "장바구니에서 특정 강의를 삭제합니다.")
    @DeleteMapping("/{courseId}")
    public ResponseEntity<ApiResponse<Void>> removeCartItem(
            @PathVariable Long courseId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        removeCartItemUseCase.handle(userDetails.getMemberId(), courseId);
        return ApiResponse.successNoContent("장바구니에서 삭제되었습니다.");
    }

    @Operation(summary = "장바구니 전체 삭제", description = "장바구니를 비웁니다.")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> removeAllCartItems(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        removeAllCartItemsUseCase.handle(userDetails.getMemberId());
        return ApiResponse.successNoContent("장바구니를 비웠습니다.");
    }
}
