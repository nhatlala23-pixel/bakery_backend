package com.nguyenthongnhat.backend_tttn.service;

import com.nguyenthongnhat.backend_tttn.dto.PromotionRequest;
import com.nguyenthongnhat.backend_tttn.dto.PromotionResponse;
import java.util.List;

public interface PromotionService {
    List<PromotionResponse> getAllPromotions();
    PromotionResponse createPromotion(PromotionRequest request);
    PromotionResponse updatePromotion(Long id, PromotionRequest request);
    void deletePromotion(Long id);
}
