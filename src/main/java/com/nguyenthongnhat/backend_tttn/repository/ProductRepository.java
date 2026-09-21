package com.nguyenthongnhat.backend_tttn.repository;

import com.nguyenthongnhat.backend_tttn.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySlug(String slug);
    Optional<Product> findBySlugAndStatus(String slug, Integer status);
    Optional<Product> findBySku(String sku);
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    Page<Product> findByCategoryIdAndStatus(Long categoryId, Integer status, Pageable pageable);
    Page<Product> findByBrandId(Long brandId, Pageable pageable);
    Page<Product> findByBrandIdAndStatus(Long brandId, Integer status, Pageable pageable);
    Page<Product> findByStatus(Integer status, Pageable pageable);
    Page<Product> findByCategorySlugAndStatus(String slug, Integer status, Pageable pageable);
    Page<Product> findByBrandSlugAndStatus(String slug, Integer status, Pageable pageable);

    // Basic search
    @Query("SELECT p FROM Product p WHERE " +
            "(LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.brand.brandName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.category.categoryName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND p.status = :status")
    Page<Product> searchProducts(@Param("keyword") String keyword,
                                 @Param("status") Integer status,
                                 Pageable pageable);

    // Filtered search with optional brandId, categoryId, price range
    @Query("SELECT p FROM Product p WHERE " +
            "(LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.brand.brandName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.category.categoryName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND p.status = 1 " +
            "AND (:brandId = -1 OR p.brand.id = :brandId) " +
            "AND (:categoryId = -1 OR p.category.id = :categoryId) " +
            "AND p.salePrice >= :minPrice " +
            "AND p.salePrice <= :maxPrice")
    Page<Product> searchProductsFiltered(
            @Param("keyword") String keyword,
            @Param("brandId") Long brandId,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    // Filtered search sorted by discount amount descending
    @Query(value = "SELECT p FROM Product p WHERE " +
            "(LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.brand.brandName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.category.categoryName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND p.status = 1 " +
            "AND (:brandId = -1 OR p.brand.id = :brandId) " +
            "AND (:categoryId = -1 OR p.category.id = :categoryId) " +
            "AND p.salePrice >= :minPrice " +
            "AND p.salePrice <= :maxPrice " +
            "ORDER BY (p.originalPrice - p.salePrice) DESC",
           countQuery = "SELECT COUNT(p) FROM Product p WHERE " +
            "(LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.brand.brandName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.category.categoryName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND p.status = 1 " +
            "AND (:brandId = -1 OR p.brand.id = :brandId) " +
            "AND (:categoryId = -1 OR p.category.id = :categoryId) " +
            "AND p.salePrice >= :minPrice " +
            "AND p.salePrice <= :maxPrice")
    Page<Product> searchProductsByDiscount(
            @Param("keyword") String keyword,
            @Param("brandId") Long brandId,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);
}
