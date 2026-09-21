package com.nguyenthongnhat.backend_tttn.service.impl;

import com.nguyenthongnhat.backend_tttn.dto.ProductResponse;
import com.nguyenthongnhat.backend_tttn.entity.Product;
import com.nguyenthongnhat.backend_tttn.entity.User;
import com.nguyenthongnhat.backend_tttn.entity.Wishlist;
import com.nguyenthongnhat.backend_tttn.mapper.ProductMapper;
import com.nguyenthongnhat.backend_tttn.repository.ProductRepository;
import com.nguyenthongnhat.backend_tttn.repository.UserRepository;
import com.nguyenthongnhat.backend_tttn.repository.WishlistRepository;
import com.nguyenthongnhat.backend_tttn.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public void addToWishlist(Long userId, Long productId) {
        Optional<Wishlist> existing = wishlistRepository.findByUserIdAndProductId(userId, productId);
        if (existing.isPresent()) {
            return; // Already in wishlist
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .product(product)
                .build();

        wishlistRepository.save(wishlist);
    }

    @Override
    @Transactional
    public void removeFromWishlist(Long userId, Long productId) {
        wishlistRepository.findByUserIdAndProductId(userId, productId)
                .ifPresent(wishlistRepository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getWishlistByUser(Long userId) {
        return wishlistRepository.findByUserId(userId).stream()
                .map(wishlist -> productMapper.toResponse(wishlist.getProduct()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isInWishlist(Long userId, Long productId) {
        return wishlistRepository.findByUserIdAndProductId(userId, productId).isPresent();
    }
}
