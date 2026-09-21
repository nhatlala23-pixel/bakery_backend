package com.nguyenthongnhat.backend_tttn.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandDTO {
    private Long id;
    private String brandName;
    private String slug;
    private String country;
    private String logoUrl;
    private String description;
    private Integer status;
}
