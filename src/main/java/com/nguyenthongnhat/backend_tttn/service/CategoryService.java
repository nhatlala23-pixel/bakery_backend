package com.nguyenthongnhat.backend_tttn.service;

import com.nguyenthongnhat.backend_tttn.dto.CategoryDTO;
import com.nguyenthongnhat.backend_tttn.dto.CategoryRequest;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    List<CategoryDTO> getAllCategories();
    Page<CategoryDTO> getAllCategories(Pageable pageable);
    CategoryDTO getBySlug(String slug);
    List<CategoryDTO> getByParent(Long parentId);
    CategoryDTO createCategory(CategoryRequest request);
    CategoryDTO updateCategory(Long id, CategoryRequest request);
    void deleteCategory(Long id);
}
