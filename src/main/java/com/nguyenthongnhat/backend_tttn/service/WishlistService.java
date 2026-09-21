package com.nguyenthongnhat.backend_tttn.service;

import com.nguyenthongnhat.backend_tttn.dto.ProductResponse;
import java.util.List;

public interface WishlistService {
    void addToWishlist(Long userId, Long productId);
    void removeFromWishlist(Long userId, Long productId);
    List<ProductResponse> getWishlistByUser(Long userId);
    boolean isInWishlist(Long userId, Long productId);
}
