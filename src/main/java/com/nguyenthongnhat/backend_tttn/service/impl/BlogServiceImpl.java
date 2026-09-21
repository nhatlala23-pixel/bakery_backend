package com.nguyenthongnhat.backend_tttn.service.impl;

import com.nguyenthongnhat.backend_tttn.dto.BlogDTO;
import com.nguyenthongnhat.backend_tttn.entity.Blog;
import com.nguyenthongnhat.backend_tttn.entity.User;
import com.nguyenthongnhat.backend_tttn.repository.BlogRepository;
import com.nguyenthongnhat.backend_tttn.repository.UserRepository;
import com.nguyenthongnhat.backend_tttn.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final UserRepository userRepository;

    @Override
    public Page<BlogDTO> getPublishedBlogs(Pageable pageable) {
        return blogRepository.findByStatus("PUBLISHED", pageable)
                .map(this::mapToDTO);
    }

    @Override
    public BlogDTO getPublishedBlogBySlug(String slug) {
        return blogRepository.findBySlugAndStatus(slug, "PUBLISHED")
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết hoặc bài viết chưa được xuất bản!"));
    }

    @Override
    public Page<BlogDTO> getAllBlogsForAdmin(Pageable pageable) {
        return blogRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    @Override
    public BlogDTO getBlogById(Long id) {
        return blogRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết!"));
    }

    @Override
    @Transactional
    public BlogDTO createBlog(BlogDTO dto, String authorEmail) {
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin tác giả!"));

        Blog blog = Blog.builder()
                .title(dto.getTitle())
                .slug(dto.getSlug() != null && !dto.getSlug().isEmpty() ? dto.getSlug() : generateSlug(dto.getTitle()))
                .thumbnail(dto.getThumbnail())
                .excerpt(dto.getExcerpt())
                .content(dto.getContent())
                .author(author)
                .status(dto.getStatus() != null ? dto.getStatus() : "DRAFT")
                .publishedDate("PUBLISHED".equalsIgnoreCase(dto.getStatus()) ? LocalDateTime.now() : null)
                .seoTitle(dto.getSeoTitle())
                .seoDescription(dto.getSeoDescription())
                .build();

        return mapToDTO(blogRepository.save(blog));
    }

    @Override
    @Transactional
    public BlogDTO updateBlog(Long id, BlogDTO dto) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết để cập nhật!"));

        blog.setTitle(dto.getTitle());
        blog.setSlug(dto.getSlug() != null && !dto.getSlug().isEmpty() ? dto.getSlug() : generateSlug(dto.getTitle()));
        blog.setThumbnail(dto.getThumbnail());
        blog.setExcerpt(dto.getExcerpt());
        blog.setContent(dto.getContent());
        
        if ("PUBLISHED".equalsIgnoreCase(dto.getStatus()) && !"PUBLISHED".equalsIgnoreCase(blog.getStatus())) {
            blog.setPublishedDate(LocalDateTime.now());
        }
        blog.setStatus(dto.getStatus());
        blog.setSeoTitle(dto.getSeoTitle());
        blog.setSeoDescription(dto.getSeoDescription());

        return mapToDTO(blogRepository.save(blog));
    }

    @Override
    @Transactional
    public void deleteBlog(Long id) {
        if (!blogRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy bài viết để xóa!");
        }
        blogRepository.deleteById(id);
    }

    private String generateSlug(String input) {
        if (input == null || input.isEmpty()) return "";
        String nowhitespace = Pattern.compile("\\s+").matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = Pattern.compile("[^\\w-]").matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }

    private BlogDTO mapToDTO(Blog blog) {
        String fixedUrl = blog.getThumbnail();
        if (fixedUrl != null && !fixedUrl.isEmpty() && !fixedUrl.startsWith("http")) {
            fixedUrl = "http://localhost:8080" + (fixedUrl.startsWith("/") ? "" : "/") + fixedUrl;
        }

        return BlogDTO.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .slug(blog.getSlug())
                .thumbnail(fixedUrl)
                .excerpt(blog.getExcerpt())
                .content(blog.getContent())
                .authorName(blog.getAuthor().getFullName())
                .authorId(blog.getAuthor().getId())
                .status(blog.getStatus())
                .publishedDate(blog.getPublishedDate())
                .seoTitle(blog.getSeoTitle())
                .seoDescription(blog.getSeoDescription())
                .createdAt(blog.getCreatedAt())
                .build();
    }
}
