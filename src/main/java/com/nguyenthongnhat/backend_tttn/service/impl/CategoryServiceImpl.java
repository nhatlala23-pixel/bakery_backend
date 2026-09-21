package com.nguyenthongnhat.backend_tttn.service.impl;

import com.nguyenthongnhat.backend_tttn.dto.CategoryDTO;
import com.nguyenthongnhat.backend_tttn.dto.CategoryRequest;
import com.nguyenthongnhat.backend_tttn.entity.Category;
import com.nguyenthongnhat.backend_tttn.mapper.CategoryMapper;
import com.nguyenthongnhat.backend_tttn.repository.CategoryRepository;
import com.nguyenthongnhat.backend_tttn.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findByStatus(1).stream()
                .map(categoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CategoryDTO> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(categoryMapper::toDTO);
    }

    @Override
    public CategoryDTO getBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .map(categoryMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục: " + slug));
    }

    @Override
    public List<CategoryDTO> getByParent(Long parentId) {
        return categoryRepository.findByParentId(parentId).stream()
                .map(categoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryDTO createCategory(CategoryRequest request) {
        Category category = new Category();
        category.setCategoryName(request.getCategoryName());
        category.setDescription(request.getDescription());
        category.setImage(request.getImage());
        category.setStatus(request.getStatus());
        
        String slug = (request.getSlug() != null && !request.getSlug().isEmpty()) 
                ? request.getSlug() 
                : generateSlug(request.getCategoryName());
        category.setSlug(slug);

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục cha!"));
            category.setParent(parent);
        }

        return categoryMapper.toDTO(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với id: " + id));
        
        category.setCategoryName(request.getCategoryName());
        category.setDescription(request.getDescription());
        category.setImage(request.getImage());
        category.setStatus(request.getStatus());
        
        String slug = (request.getSlug() != null && !request.getSlug().isEmpty()) 
                ? request.getSlug() 
                : generateSlug(request.getCategoryName());
        category.setSlug(slug);

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục cha!"));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        return categoryMapper.toDTO(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với id: " + id));
        
        // Kiểm tra xem có danh mục con không
        List<Category> children = categoryRepository.findByParentId(id);
        if (!children.isEmpty()) {
            throw new RuntimeException("Không thể xóa danh mục này vì vẫn còn danh mục con!");
        }
        
        categoryRepository.delete(category);
    }

    private String generateSlug(String input) {
        if (input == null || input.isEmpty()) return "";
        String nowhitespace = Pattern.compile("\\s+").matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = Pattern.compile("[^\\w-]").matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }
}
