package com.nguyenthongnhat.backend_tttn.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// DTO dùng để hiển thị trong danh sách sản phẩm (không cần chi tiết)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String sku;
    private String productName;
    private String slug;
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
    private Integer stock;
    private String thumbnail;
    private String shortDescription;
    private String categoryName;
    private Long categoryId;
    private String brandName;
    private Long brandId;
    private Integer status;
    private Boolean isBestseller;
    private Boolean isFeatured;
    private BigDecimal rating;
    private LocalDateTime createdAt;
}
