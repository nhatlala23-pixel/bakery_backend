package com.nguyenthongnhat.backend_tttn.controller;

import com.nguyenthongnhat.backend_tttn.dto.GalleryDTO;
import com.nguyenthongnhat.backend_tttn.service.GalleryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/galleries")
@RequiredArgsConstructor
@Tag(name = "Gallery", description = "Quản lý hình ảnh và bộ sưu tập ảnh thương hiệu")
public class GalleryController {

    private final GalleryService galleryService;

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả hình ảnh đang kích hoạt (Public)")
    public ResponseEntity<List<GalleryDTO>> getActiveGalleries(@RequestParam(required = false) String category) {
        if (category != null && !category.trim().isEmpty()) {
            return ResponseEntity.ok(galleryService.getActiveGalleriesByCategory(category.trim().toUpperCase()));
        }
        return ResponseEntity.ok(galleryService.getAllActiveGalleries());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Lấy danh sách tất cả hình ảnh (Admin/Staff)")
    public ResponseEntity<List<GalleryDTO>> getAllGalleriesForAdmin() {
        return ResponseEntity.ok(galleryService.getAllGalleriesForAdmin());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Thêm hình ảnh mới vào thư viện (Admin/Staff)")
    public ResponseEntity<GalleryDTO> createGallery(@RequestBody GalleryDTO dto) {
        return ResponseEntity.ok(galleryService.createGallery(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Cập nhật thông tin hình ảnh (Admin/Staff)")
    public ResponseEntity<GalleryDTO> updateGallery(@PathVariable Long id, @RequestBody GalleryDTO dto) {
        return ResponseEntity.ok(galleryService.updateGallery(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Xóa hình ảnh khỏi thư viện (Admin/Staff)")
    public ResponseEntity<Void> deleteGallery(@PathVariable Long id) {
        galleryService.deleteGallery(id);
        return ResponseEntity.ok().build();
    }
}
