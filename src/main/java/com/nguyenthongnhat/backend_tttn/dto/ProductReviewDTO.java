package com.nguyenthongnhat.backend_tttn.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductReviewDTO {
    private Long id;
    private Long productId;
    private Long userId;
    private String userName;
    private Integer rating;
    private String content;
    private String imageUrl;
    private String createdAt; // ISO string format
}
