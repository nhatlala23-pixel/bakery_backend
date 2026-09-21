package com.nguyenthongnhat.backend_tttn.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantResponse {
    private Long id;
    private String sku;
    private BigDecimal variantPrice;
    private Integer stock;
    private String thumbnailUrl;
    private List<String> attributeValues; // Vẫn giữ lại cho tương thích ngược
    private String colorCode;
    private String size;
}
