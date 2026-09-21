package com.nguyenthongnhat.backend_tttn.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private String orderCode;
    private String customerName;
    private String paymentMethod;
    private String paymentStatus;
    private BigDecimal amount;
    private String transactionNo;
    private String gatewayTransactionId;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
