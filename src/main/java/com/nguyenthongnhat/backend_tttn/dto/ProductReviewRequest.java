package com.nguyenthongnhat.backend_tttn.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductReviewRequest {
    private Long productId;
    private Long userId;
    private Integer rating;
    private String content;
    private String imageUrl;
}
