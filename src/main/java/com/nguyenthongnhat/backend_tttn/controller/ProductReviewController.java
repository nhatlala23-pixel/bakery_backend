package com.nguyenthongnhat.backend_tttn.controller;

import com.nguyenthongnhat.backend_tttn.dto.ProductReviewDTO;
import com.nguyenthongnhat.backend_tttn.dto.ProductReviewRequest;
import com.nguyenthongnhat.backend_tttn.service.ProductReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Product Reviews", description = "Quản lý đánh giá và bình luận sản phẩm")
public class ProductReviewController {

    private final ProductReviewService productReviewService;

    @GetMapping("/product/{productId}")
    @Operation(summary = "Lấy danh sách đánh giá của một sản phẩm")
    public ResponseEntity<List<ProductReviewDTO>> getReviewsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(productReviewService.getReviewsByProduct(productId));
    }

    @PostMapping("/add")
    @Operation(summary = "Thêm đánh giá mới cho sản phẩm")
    public ResponseEntity<ProductReviewDTO> createReview(@RequestBody ProductReviewRequest request) {
        return ResponseEntity.ok(productReviewService.createReview(request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa đánh giá theo ID")
    public ResponseEntity<Map<String, String>> deleteReview(@PathVariable Long id) {
        productReviewService.deleteReview(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa đánh giá thành công!"));
    }
}
