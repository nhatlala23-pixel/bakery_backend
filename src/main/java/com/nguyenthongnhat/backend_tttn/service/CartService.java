package com.nguyenthongnhat.backend_tttn.service;

import com.nguyenthongnhat.backend_tttn.dto.CartItemResponse;
import java.util.List;

public interface CartService {
    CartItemResponse addToCart(Long userId, Long variantId, Integer quantity);
    CartItemResponse addProductToCart(Long userId, Long productId, Integer quantity);
    List<CartItemResponse> getCart(Long userId);
    void removeFromCart(Long cartItemId);
    CartItemResponse updateQuantity(Long cartItemId, Integer quantity);
    void clearCart(Long userId);
}
