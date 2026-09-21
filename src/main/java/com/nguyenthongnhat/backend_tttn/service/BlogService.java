package com.nguyenthongnhat.backend_tttn.service;

import com.nguyenthongnhat.backend_tttn.dto.BlogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BlogService {
    Page<BlogDTO> getPublishedBlogs(Pageable pageable);
    BlogDTO getPublishedBlogBySlug(String slug);
    
    // Admin CRUD
    Page<BlogDTO> getAllBlogsForAdmin(Pageable pageable);
    BlogDTO getBlogById(Long id);
    BlogDTO createBlog(BlogDTO dto, String authorEmail);
    BlogDTO updateBlog(Long id, BlogDTO dto);
    void deleteBlog(Long id);
}
