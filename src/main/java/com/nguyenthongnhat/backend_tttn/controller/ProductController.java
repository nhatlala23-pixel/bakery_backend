package com.nguyenthongnhat.backend_tttn.controller;

import com.nguyenthongnhat.backend_tttn.dto.ProductDetailResponse;
import com.nguyenthongnhat.backend_tttn.dto.ProductRequest;
import com.nguyenthongnhat.backend_tttn.dto.ProductResponse;
import com.nguyenthongnhat.backend_tttn.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "Quản lý sản phẩm & chi tiết")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm sản phẩm theo từ khóa")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.searchProducts(keyword, PageRequest.of(page, size)));
    }

    @GetMapping("/search/filter")
    @Operation(summary = "Tìm kiếm có bộ lọc: brand, category, giá, sắp xếp")
    public ResponseEntity<Page<ProductResponse>> searchProductsFiltered(
            @RequestParam String keyword,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(defaultValue = "default") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(productService.searchProductsFiltered(
                keyword, brandId, categoryId, minPrice, maxPrice, sort, page, size));
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách sản phẩm có phân trang")
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {
        
        String[] sortParams = sort.split(",");
        Sort sortObj = Sort.by(sortParams[1].equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC, sortParams[0]);
        
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Lấy chi tiết sản phẩm (bao gồm variants, specs, images) theo slug")
    public ResponseEntity<ProductDetailResponse> getProductDetail(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getProductDetail(slug));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Lấy danh sách sản phẩm theo danh mục")
    public ResponseEntity<Page<ProductResponse>> getByCategoryId(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.getByCategoryId(categoryId, PageRequest.of(page, size)));
    }

    @GetMapping("/brand/{brandId}")
    @Operation(summary = "Lấy danh sách sản phẩm theo thương hiệu")
    public ResponseEntity<Page<ProductResponse>> getByBrandId(
            @PathVariable Long brandId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.getByBrandId(brandId, PageRequest.of(page, size)));
    }

    @GetMapping("/category/slug/{slug}")
    @Operation(summary = "Lấy danh sách sản phẩm theo slug danh mục")
    public ResponseEntity<Page<ProductResponse>> getByCategorySlug(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.getByCategorySlug(slug, PageRequest.of(page, size)));
    }

    @GetMapping("/brand/slug/{slug}")
    @Operation(summary = "Lấy danh sách sản phẩm theo slug thương hiệu")
    public ResponseEntity<Page<ProductResponse>> getByBrandSlug(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.getByBrandSlug(slug, PageRequest.of(page, size)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo sản phẩm mới (Admin only)")
    public ResponseEntity<ProductDetailResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.createProduct(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật sản phẩm (Admin only)")
    public ResponseEntity<ProductDetailResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @PutMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Cập nhật số lượng tồn kho (Admin & Staff)")
    public ResponseEntity<ProductResponse> updateStock(
            @PathVariable Long id,
            @RequestParam Integer stock) {
        return ResponseEntity.ok(productService.updateStock(id, stock));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa sản phẩm (Admin only)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }
}
