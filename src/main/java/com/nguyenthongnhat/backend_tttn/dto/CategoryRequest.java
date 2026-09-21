package com.nguyenthongnhat.backend_tttn.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {

    @NotBlank(message = "Tên danh mục không được để trống")
    private String categoryName;

    private String slug;

    private Long parentId;

    private String description;

    private String image;

    @Builder.Default
    private Integer status = 1;
}
