package com.nguyenthongnhat.backend_tttn.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// DTO dùng cho trang chi tiết sản phẩm - bao gồm variants, ảnh, thông số
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailResponse {
    private Long id;
    private String sku;
    private String productName;
    private String slug;
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
    private Integer stock;
    private String thumbnail;
    private String shortDescription;
    private String description;
    private CategoryDTO category;
    private BrandDTO brand;
    private Integer status;
    private String ingredients;
    private String size;
    private String preservation;
    private Boolean isBestseller;
    private Boolean isFeatured;
    private BigDecimal rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Chi tiết
    private List<ProductImageDTO> images;
    private List<ProductSpecificationDTO> specifications;
    private List<ProductVariantResponse> variants;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductImageDTO {
        private Long id;
        private String imageUrl;
        private Boolean isMain;
        private Integer sortOrder;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductSpecificationDTO {
        private Long id;
        private String specKey;
        private String specValue;
        private Integer sortOrder;
    }
}
