package com.nguyenthongnhat.backend_tttn.service;

import java.math.BigDecimal;

public interface MomoService {
    String createPaymentUrl(String orderId, String orderInfo, BigDecimal amount);
}
