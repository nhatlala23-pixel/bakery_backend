package com.nguyenthongnhat.backend_tttn.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageRequest {
    private String imageUrl;
    private Boolean isMain;
    private Integer sortOrder;
}
