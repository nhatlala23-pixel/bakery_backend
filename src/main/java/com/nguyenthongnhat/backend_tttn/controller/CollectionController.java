package com.nguyenthongnhat.backend_tttn.controller;

import com.nguyenthongnhat.backend_tttn.dto.CollectionDTO;
import com.nguyenthongnhat.backend_tttn.service.CollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
@Tag(name = "Collection", description = "Quản lý bộ sưu tập bánh kem/bánh ngọt Gấu")
public class CollectionController {

    private final CollectionService collectionService;

    @GetMapping
    @Operation(summary = "Lấy tất cả bộ sưu tập đang kích hoạt (Public)")
    public ResponseEntity<List<CollectionDTO>> getActiveCollections() {
        return ResponseEntity.ok(collectionService.getActiveCollections());
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Chi tiết bộ sưu tập bằng slug (Public)")
    public ResponseEntity<CollectionDTO> getCollectionBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(collectionService.getCollectionBySlug(slug));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Lấy tất cả bộ sưu tập kể cả chưa kích hoạt (Admin/Staff)")
    public ResponseEntity<List<CollectionDTO>> getAllCollectionsForAdmin() {
        return ResponseEntity.ok(collectionService.getAllCollectionsForAdmin());
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Lấy chi tiết bộ sưu tập bằng ID (Admin/Staff)")
    public ResponseEntity<CollectionDTO> getCollectionById(@PathVariable Long id) {
        return ResponseEntity.ok(collectionService.getCollectionById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Tạo bộ sưu tập mới (Admin/Staff)")
    public ResponseEntity<CollectionDTO> createCollection(@RequestBody CollectionDTO dto) {
        return ResponseEntity.ok(collectionService.createCollection(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Cập nhật bộ sưu tập (Admin/Staff)")
    public ResponseEntity<CollectionDTO> updateCollection(@PathVariable Long id, @RequestBody CollectionDTO dto) {
        return ResponseEntity.ok(collectionService.updateCollection(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Xóa bộ sưu tập (Admin/Staff)")
    public ResponseEntity<Void> deleteCollection(@PathVariable Long id) {
        collectionService.deleteCollection(id);
        return ResponseEntity.ok().build();
    }
}
