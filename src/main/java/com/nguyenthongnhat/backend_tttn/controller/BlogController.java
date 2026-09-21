package com.nguyenthongnhat.backend_tttn.controller;

import com.nguyenthongnhat.backend_tttn.dto.BlogDTO;
import com.nguyenthongnhat.backend_tttn.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
@Tag(name = "Blog", description = "Quản lý bài viết và tin tức cửa hàng")
public class BlogController {

    private final BlogService blogService;

    @GetMapping
    @Operation(summary = "Lấy danh sách bài viết đã xuất bản (Public)")
    public ResponseEntity<Page<BlogDTO>> getPublishedBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedDate").descending());
        return ResponseEntity.ok(blogService.getPublishedBlogs(pageable));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Chi tiết bài viết bằng slug (Public)")
    public ResponseEntity<BlogDTO> getBlogBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(blogService.getPublishedBlogBySlug(slug));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Lấy tất cả bài viết (Admin/Staff)")
    public ResponseEntity<Page<BlogDTO>> getAllBlogsForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(blogService.getAllBlogsForAdmin(pageable));
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Lấy chi tiết bài viết bằng ID (Admin/Staff)")
    public ResponseEntity<BlogDTO> getBlogById(@PathVariable Long id) {
        return ResponseEntity.ok(blogService.getBlogById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Tạo bài viết mới (Admin/Staff)")
    public ResponseEntity<BlogDTO> createBlog(@RequestBody BlogDTO dto, Principal principal) {
        String email = principal != null ? principal.getName() : "admin@gaubakery.com";
        return ResponseEntity.ok(blogService.createBlog(dto, email));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Cập nhật bài viết (Admin/Staff)")
    public ResponseEntity<BlogDTO> updateBlog(@PathVariable Long id, @RequestBody BlogDTO dto) {
        return ResponseEntity.ok(blogService.updateBlog(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Xóa bài viết (Admin/Staff)")
    public ResponseEntity<Void> deleteBlog(@PathVariable Long id) {
        blogService.deleteBlog(id);
        return ResponseEntity.ok().build();
    }
}
