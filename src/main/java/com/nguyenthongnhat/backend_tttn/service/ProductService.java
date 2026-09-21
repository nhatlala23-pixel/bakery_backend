package com.nguyenthongnhat.backend_tttn.service;

import com.nguyenthongnhat.backend_tttn.dto.ProductDetailResponse;
import com.nguyenthongnhat.backend_tttn.dto.ProductRequest;
import com.nguyenthongnhat.backend_tttn.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;

public interface ProductService {
    Page<ProductResponse> getAllProducts(Pageable pageable);
    ProductDetailResponse getProductDetail(String slug);
    Page<ProductResponse> getByCategoryId(Long categoryId, Pageable pageable);
    Page<ProductResponse> getByBrandId(Long brandId, Pageable pageable);
    Page<ProductResponse> getByCategorySlug(String slug, Pageable pageable);
    Page<ProductResponse> getByBrandSlug(String slug, Pageable pageable);
    ProductDetailResponse createProduct(ProductRequest request);
    ProductDetailResponse updateProduct(Long id, ProductRequest request);
    void deleteProduct(Long id);

    // Basic search
    Page<ProductResponse> searchProducts(String keyword, Pageable pageable);

    // Filtered search with sort, brand, category, price range
    Page<ProductResponse> searchProductsFiltered(
            String keyword,
            Long brandId,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sort,
            int page,
            int size);

    ProductResponse updateStock(Long id, Integer stock);
}
