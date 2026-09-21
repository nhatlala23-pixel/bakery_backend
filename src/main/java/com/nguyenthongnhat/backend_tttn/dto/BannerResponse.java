package com.nguyenthongnhat.backend_tttn.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannerResponse {
    private Long id;
    private String title;
    private String subtitle;
    private String description;
    private String imageUrl;
    private String buttonText;
    private String buttonLink;
    private Integer sortOrder;
    private Boolean active;
}
