package com.nguyenthongnhat.backend_tttn.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    @NotBlank(message = "Tên người nhận không được để trống")
    @Size(max = 100)
    private String receiverName;

    @NotBlank(message = "Số điện thoại người nhận không được để trống")
    @Size(max = 15)
    private String receiverPhone;

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    private String shippingAddress;

    @Size(max = 500)
    private String note;

    private String voucherCode; // Mã giảm giá (có thể null)

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod; // COD hoặc VNPAY

    @NotNull(message = "Đơn hàng phải có ít nhất 1 sản phẩm")
    private List<OrderItemRequest> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemRequest {
        @NotNull
        private Long variantId;

        @NotNull
        @Min(value = 1, message = "Số lượng phải lớn hơn 0")
        private Integer quantity;
    }
}
