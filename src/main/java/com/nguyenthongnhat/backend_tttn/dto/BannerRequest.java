package com.nguyenthongnhat.backend_tttn.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannerRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    private String subtitle;
    private String description;

    @NotBlank(message = "Hình ảnh không được để trống")
    private String imageUrl;

    private String buttonText;
    private String buttonLink;
    private Integer sortOrder;
    private Boolean active;
}
