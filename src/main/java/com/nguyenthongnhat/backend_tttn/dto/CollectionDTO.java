package com.nguyenthongnhat.backend_tttn.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionDTO {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String thumbnail;
    private Boolean active;
    private List<Long> productIds;
    private List<ProductResponse> products;
}
