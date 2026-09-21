package com.nguyenthongnhat.backend_tttn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String productName;

    @NotNull(message = "ID danh mục không được để trống")
    private Long categoryId;

    @NotNull(message = "ID thương hiệu không được để trống")
    private Long brandId;

    private String description;

    private String shortDescription;

    private String thumbnail;

    @Builder.Default
    private Integer status = 1;

    @JsonProperty("sku")
    @NotBlank(message = "SKU không được để trống")
    private String sku;

    @JsonProperty("originalPrice")
    @NotNull(message = "Giá gốc không được để trống")
    @Min(value = 0, message = "Giá gốc không được âm")
    private BigDecimal originalPrice;

    @JsonProperty("salePrice")
    @NotNull(message = "Giá bán không được để trống")
    @Min(value = 0, message = "Giá bán không được âm")
    private BigDecimal salePrice;

    @JsonProperty("stock")
    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    private Integer stock;

    private String ingredients;
    private String size;
    private String preservation;
    
    private Boolean isBestseller;
    private Boolean isFeatured;
    private BigDecimal rating;

    private List<ProductVariantRequest> variants;

    private List<ProductImageRequest> images;
}
