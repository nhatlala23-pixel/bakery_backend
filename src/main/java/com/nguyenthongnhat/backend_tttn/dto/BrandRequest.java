package com.nguyenthongnhat.backend_tttn.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandRequest {

    @NotBlank(message = "Tên thương hiệu không được để trống")
    private String brandName;

    private String slug;

    private String country;

    private String logoUrl;

    private String description;

    @Builder.Default
    private Integer status = 1;
}
