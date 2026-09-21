package com.nguyenthongnhat.backend_tttn.service.impl;

import com.nguyenthongnhat.backend_tttn.dto.CartItemResponse;
import com.nguyenthongnhat.backend_tttn.entity.Cart;
import com.nguyenthongnhat.backend_tttn.entity.CartItem;
import com.nguyenthongnhat.backend_tttn.entity.ProductVariant;
import com.nguyenthongnhat.backend_tttn.entity.User;
import com.nguyenthongnhat.backend_tttn.mapper.CartMapper;
import com.nguyenthongnhat.backend_tttn.repository.CartItemRepository;
import com.nguyenthongnhat.backend_tttn.repository.CartRepository;
import com.nguyenthongnhat.backend_tttn.repository.ProductRepository;
import com.nguyenthongnhat.backend_tttn.repository.ProductVariantRepository;
import com.nguyenthongnhat.backend_tttn.repository.UserRepository;
import com.nguyenthongnhat.backend_tttn.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional
    public CartItemResponse addToCart(Long userId, Long variantId, Integer quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
                    Cart newCart = Cart.builder().user(user).build();
                    return cartRepository.save(newCart);
                });

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiên bản sản phẩm!"));

        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        CartItem existingItem = items.stream()
                .filter(item -> item.getVariant().getId().equals(variantId))
                .findFirst().orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            return cartMapper.toItemResponse(cartItemRepository.save(existingItem));
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .variant(variant)
                    .quantity(quantity)
                    .unitPrice(variant.getVariantPrice())
                    .build();
            return cartMapper.toItemResponse(cartItemRepository.save(newItem));
        }
    }

    @Override
    @Transactional
    public CartItemResponse addProductToCart(Long userId, Long productId, Integer quantity) {
        // Find first variant of this product
        List<ProductVariant> variants = productVariantRepository.findByProductId(productId);
        if (variants.isEmpty()) {
            // Auto-create a default variant if none exists
            com.nguyenthongnhat.backend_tttn.entity.Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));
            
            ProductVariant defaultVariant = new ProductVariant();
            defaultVariant.setProduct(product);
            defaultVariant.setSku(product.getSku() != null ? product.getSku() + "-DEF" : "DEF-" + product.getId());
            defaultVariant.setVariantPrice(product.getSalePrice() != null ? product.getSalePrice() : java.math.BigDecimal.ZERO);
            defaultVariant.setStock(product.getStock() != null ? product.getStock() : 0);
            defaultVariant.setStatus(1);
            
            defaultVariant = productVariantRepository.save(defaultVariant);
            return addToCart(userId, defaultVariant.getId(), quantity);
        }
        ProductVariant variant = variants.get(0);
        return addToCart(userId, variant.getId(), quantity);
    }

    @Override
    public List<CartItemResponse> getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng trống!"));
        return cartItemRepository.findByCartId(cart.getId()).stream()
                .map(cartMapper::toItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeFromCart(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    @Override
    @Transactional
    public CartItemResponse updateQuantity(Long cartItemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ!"));
        item.setQuantity(quantity);
        return cartMapper.toItemResponse(cartItemRepository.save(item));
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> 
            cartItemRepository.deleteByCartId(cart.getId())
        );
    }
}
