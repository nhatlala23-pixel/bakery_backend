package com.nguyenthongnhat.backend_tttn.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GalleryDTO {
    private Long id;
    private String title;
    private String imageUrl;
    private String category; // PRODUCT, STORE, KITCHEN, EVENT, CUSTOMER
    private Integer displayOrder;
    private Boolean active;
}
