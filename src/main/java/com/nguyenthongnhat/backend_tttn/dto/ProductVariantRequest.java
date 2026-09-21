package com.nguyenthongnhat.backend_tttn.dto;

import jakarta.validation.constraints.Min;

import lombok.*;

import java.math.BigDecimal;
    
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantRequest {

    private String sku;

    private String colorCode;

    private String size;

    private String thumbnailUrl;

    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    private Integer stockQuantity;

    @Min(value = 0, message = "Giá không được âm")
    private BigDecimal price;

    @Builder.Default
    private Integer status = 1;
}
