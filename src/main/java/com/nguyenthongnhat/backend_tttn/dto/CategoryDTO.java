package com.nguyenthongnhat.backend_tttn.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {
    private Long id;
    private String categoryName;
    private String slug;
    private Long parentId;
    private String description;
    private String image;
    private Integer status;
}
