package com.nguyenthongnhat.backend_tttn.controller;

import com.nguyenthongnhat.backend_tttn.dto.BrandDTO;
import com.nguyenthongnhat.backend_tttn.dto.BrandRequest;
import com.nguyenthongnhat.backend_tttn.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
@Tag(name = "Brand", description = "Quản lý thương hiệu")
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    @Operation(summary = "Lấy danh sách thương hiệu (Phân trang)")
    public ResponseEntity<Page<BrandDTO>> getAllBrands(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(brandService.getAllBrands(pageable));
    }

    @GetMapping("/all")
    @Operation(summary = "Lấy toàn bộ danh sách thương hiệu (Không phân trang)")
    public ResponseEntity<List<BrandDTO>> getAllBrandsList() {
        return ResponseEntity.ok(brandService.getAllBrands());
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Lấy chi tiết thương hiệu theo slug")
    public ResponseEntity<BrandDTO> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(brandService.getBySlug(slug));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo thương hiệu mới (Admin only)")
    public ResponseEntity<BrandDTO> create(@Valid @RequestBody BrandRequest request) {
        return ResponseEntity.ok(brandService.createBrand(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật thương hiệu (Admin only)")
    public ResponseEntity<BrandDTO> update(@PathVariable Long id, @Valid @RequestBody BrandRequest request) {
        return ResponseEntity.ok(brandService.updateBrand(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa thương hiệu (Admin only)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }
}

