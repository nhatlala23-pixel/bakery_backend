package com.nguyenthongnhat.backend_tttn.service.impl;

import com.nguyenthongnhat.backend_tttn.dto.PromotionRequest;
import com.nguyenthongnhat.backend_tttn.dto.PromotionResponse;
import com.nguyenthongnhat.backend_tttn.entity.Promotion;
import com.nguyenthongnhat.backend_tttn.repository.PromotionRepository;
import com.nguyenthongnhat.backend_tttn.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nguyenthongnhat.backend_tttn.entity.Product;
import com.nguyenthongnhat.backend_tttn.repository.ProductRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;

    @Override
    public List<PromotionResponse> getAllPromotions() {
        return promotionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PromotionResponse createPromotion(PromotionRequest request) {
        Promotion promotion = Promotion.builder()
                .name(request.getName())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus() != null ? request.getStatus() : 1)
                .build();
        
        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            promotion.setProducts(new HashSet<>(productRepository.findAllById(request.getProductIds())));
        }
        
        Promotion savedPromotion = promotionRepository.save(promotion);
        applyPromotionToProducts(savedPromotion);
        
        return mapToResponse(savedPromotion);
    }

    @Override
    @Transactional
    public PromotionResponse updatePromotion(Long id, PromotionRequest request) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi!"));
        
        promotion.setName(request.getName());
        promotion.setDiscountType(request.getDiscountType());
        promotion.setDiscountValue(request.getDiscountValue());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setStatus(request.getStatus());

        if (request.getProductIds() != null) {
            promotion.setProducts(new HashSet<>(productRepository.findAllById(request.getProductIds())));
        } else {
            promotion.getProducts().clear();
        }
        
        Promotion savedPromotion = promotionRepository.save(promotion);
        applyPromotionToProducts(savedPromotion);
        
        return mapToResponse(savedPromotion);
    }

    @Override
    @Transactional
    public void deletePromotion(Long id) {
        Promotion promotion = promotionRepository.findById(id).orElse(null);
        if (promotion != null) {
            Set<Product> products = new HashSet<>(promotion.getProducts());
            promotionRepository.delete(promotion);
            // Sau khi xóa, tính toán lại giá cho các sản phẩm liên quan
            products.forEach(this::recalculateProductPrice);
        }
    }

    private void applyPromotionToProducts(Promotion promotion) {
        if (promotion.getProducts() != null) {
            promotion.getProducts().forEach(this::recalculateProductPrice);
        }
    }

    private void recalculateProductPrice(Product product) {
        BigDecimal originalPrice = product.getOriginalPrice();
        BigDecimal bestSalePrice = originalPrice;
        
        LocalDateTime now = LocalDateTime.now();
        
        if (product.getPromotions() != null) {
            for (Promotion p : product.getPromotions()) {
                // Kiểm tra promotion có đang active và trong thời hạn không
                if (p.getStatus() == 1 && !now.isBefore(p.getStartDate()) && !now.isAfter(p.getEndDate())) {
                    BigDecimal currentSalePrice = originalPrice;
                    if (p.getDiscountType().name().equals("PERCENTAGE")) {
                        BigDecimal discount = originalPrice.multiply(p.getDiscountValue()).divide(new BigDecimal(100));
                        currentSalePrice = originalPrice.subtract(discount);
                    } else {
                        currentSalePrice = originalPrice.subtract(p.getDiscountValue());
                    }
                    
                    if (currentSalePrice.compareTo(bestSalePrice) < 0) {
                        bestSalePrice = currentSalePrice;
                    }
                }
            }
        }
        
        if (bestSalePrice.compareTo(BigDecimal.ZERO) < 0) bestSalePrice = BigDecimal.ZERO;
        
        product.setSalePrice(bestSalePrice);
        productRepository.save(product);
    }

    private PromotionResponse mapToResponse(Promotion promotion) {
        return PromotionResponse.builder()
                .id(promotion.getId())
                .name(promotion.getName())
                .discountType(promotion.getDiscountType())
                .discountValue(promotion.getDiscountValue())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .status(promotion.getStatus())
                .productIds(promotion.getProducts() != null 
                    ? promotion.getProducts().stream().map(com.nguyenthongnhat.backend_tttn.entity.Product::getId).collect(Collectors.toList())
                    : null)
                .build();
    }
}
