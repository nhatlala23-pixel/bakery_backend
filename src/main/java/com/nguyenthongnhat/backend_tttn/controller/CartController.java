package com.nguyenthongnhat.backend_tttn.controller;

import com.nguyenthongnhat.backend_tttn.dto.CartItemResponse;
import com.nguyenthongnhat.backend_tttn.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Quản lý giỏ hàng")
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    @Operation(summary = "Thêm sản phẩm vào giỏ hàng theo variantId")
    public ResponseEntity<CartItemResponse> addToCart(
            @RequestParam Long userId,
            @RequestParam Long variantId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        return ResponseEntity.ok(cartService.addToCart(userId, variantId, quantity));
    }

    @PostMapping("/add-by-product")
    @Operation(summary = "Thêm sản phẩm vào giỏ hàng theo productId (tự chọn variant đầu tiên)")
    public ResponseEntity<CartItemResponse> addByProduct(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        return ResponseEntity.ok(cartService.addProductToCart(userId, productId, quantity));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Lấy danh sách sản phẩm trong giỏ hàng của người dùng")
    public ResponseEntity<List<CartItemResponse>> getCart(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @DeleteMapping("/item/{cartItemId}")
    @Operation(summary = "Xóa sản phẩm khỏi giỏ hàng")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long cartItemId) {
        cartService.removeFromCart(cartItemId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/item/{cartItemId}")
    @Operation(summary = "Cập nhật số lượng sản phẩm trong giỏ hàng")
    public ResponseEntity<CartItemResponse> updateQuantity(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.updateQuantity(cartItemId, quantity));
    }

    @DeleteMapping("/clear/{userId}")
    @Operation(summary = "Làm trống giỏ hàng")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok().build();
    }
}
