package com.nguyenthongnhat.backend_tttn.service.impl;

import com.nguyenthongnhat.backend_tttn.dto.ProductReviewDTO;
import com.nguyenthongnhat.backend_tttn.dto.ProductReviewRequest;
import com.nguyenthongnhat.backend_tttn.entity.Product;
import com.nguyenthongnhat.backend_tttn.entity.ProductReview;
import com.nguyenthongnhat.backend_tttn.entity.User;
import com.nguyenthongnhat.backend_tttn.repository.ProductRepository;
import com.nguyenthongnhat.backend_tttn.repository.ProductReviewRepository;
import com.nguyenthongnhat.backend_tttn.repository.UserRepository;
import com.nguyenthongnhat.backend_tttn.service.ProductReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductReviewServiceImpl implements ProductReviewService {

    private final ProductReviewRepository productReviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductReviewDTO> getReviewsByProduct(Long productId) {
        return productReviewRepository.findByProductId(productId).stream()
                .filter(review -> review.getStatus() == null || review.getStatus() == 1)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductReviewDTO createReview(ProductReviewRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

        ProductReview review = ProductReview.builder()
                .product(product)
                .user(user)
                .rating(request.getRating())
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .status(1) // Visible by default
                .build();

        ProductReview savedReview = productReviewRepository.save(review);
        return mapToDTO(savedReview);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        productReviewRepository.deleteById(reviewId);
    }

    private ProductReviewDTO mapToDTO(ProductReview review) {
        return ProductReviewDTO.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFullName() != null ? review.getUser().getFullName() : review.getUser().getEmail())
                .rating(review.getRating())
                .content(review.getContent())
                .imageUrl(review.getImageUrl())
                .createdAt(review.getCreatedAt() != null ? review.getCreatedAt().toString() : null)
                .build();
    }
}
