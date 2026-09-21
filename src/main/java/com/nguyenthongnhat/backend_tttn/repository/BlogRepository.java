package com.nguyenthongnhat.backend_tttn.repository;

import com.nguyenthongnhat.backend_tttn.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
    Optional<Blog> findBySlugAndStatus(String slug, String status);
    Page<Blog> findByStatus(String status, Pageable pageable);
    Optional<Blog> findBySlug(String slug);
}
