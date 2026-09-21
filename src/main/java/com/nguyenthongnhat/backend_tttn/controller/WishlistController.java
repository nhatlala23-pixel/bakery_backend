package com.nguyenthongnhat.backend_tttn.controller;

import com.nguyenthongnhat.backend_tttn.dto.ProductResponse;
import com.nguyenthongnhat.backend_tttn.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "Quản lý danh sách yêu thích")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping("/{userId}")
    @Operation(summary = "Lấy danh sách yêu thích của người dùng")
    public ResponseEntity<List<ProductResponse>> getWishlist(@PathVariable Long userId) {
        return ResponseEntity.ok(wishlistService.getWishlistByUser(userId));
    }

    @PostMapping("/add")
    @Operation(summary = "Thêm sản phẩm vào danh sách yêu thích")
    public ResponseEntity<Map<String, String>> addToWishlist(
            @RequestParam Long userId,
            @RequestParam Long productId) {
        wishlistService.addToWishlist(userId, productId);
        return ResponseEntity.ok(Map.of("message", "Đã thêm vào danh sách yêu thích!"));
    }

    @DeleteMapping("/remove")
    @Operation(summary = "Xóa sản phẩm khỏi danh sách yêu thích")
    public ResponseEntity<Map<String, String>> removeFromWishlist(
            @RequestParam Long userId,
            @RequestParam Long productId) {
        wishlistService.removeFromWishlist(userId, productId);
        return ResponseEntity.ok(Map.of("message", "Đã xóa khỏi danh sách yêu thích!"));
    }

    @GetMapping("/check")
    @Operation(summary = "Kiểm tra sản phẩm có trong danh sách yêu thích không")
    public ResponseEntity<Map<String, Boolean>> checkWishlist(
            @RequestParam Long userId,
            @RequestParam Long productId) {
        boolean result = wishlistService.isInWishlist(userId, productId);
        return ResponseEntity.ok(Map.of("inWishlist", result));
    }
}
