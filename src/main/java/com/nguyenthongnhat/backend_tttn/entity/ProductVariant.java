package com.nguyenthongnhat.backend_tttn.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(name = "variant_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal variantPrice;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer stock;

    @Column(name = "thumbnail_url", length = 255)
    private String thumbnailUrl;

    @Column(name = "color_code", length = 50)
    private String colorCode;

    @Column(name = "size", length = 50)
    private String size;

    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer status;
}
