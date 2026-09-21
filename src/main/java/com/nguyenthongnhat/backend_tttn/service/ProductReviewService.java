package com.nguyenthongnhat.backend_tttn.service;

import com.nguyenthongnhat.backend_tttn.dto.ProductReviewDTO;
import com.nguyenthongnhat.backend_tttn.dto.ProductReviewRequest;

import java.util.List;

public interface ProductReviewService {
    List<ProductReviewDTO> getReviewsByProduct(Long productId);
    ProductReviewDTO createReview(ProductReviewRequest request);
    void deleteReview(Long reviewId);
}
