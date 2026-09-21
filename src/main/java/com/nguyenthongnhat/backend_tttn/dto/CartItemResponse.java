package com.nguyenthongnhat.backend_tttn.dto;

import lombok.*;
import java.math.BigDecimal;

// DTO hiển thị giỏ hàng - trả về thông tin đầy đủ của variant + sản phẩm
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {
    private Long cartItemId;
    private Long variantId;
    private String variantSku;
    private Long productId;
    private String productName;
    private String productSlug;
    private String productThumbnail;
    private String variantAttributes; // Ví dụ: "Đen - 128GB"
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
