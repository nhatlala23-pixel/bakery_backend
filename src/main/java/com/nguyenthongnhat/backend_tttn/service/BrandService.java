package com.nguyenthongnhat.backend_tttn.service;

import com.nguyenthongnhat.backend_tttn.dto.BrandDTO;
import com.nguyenthongnhat.backend_tttn.dto.BrandRequest;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BrandService {
    List<BrandDTO> getAllBrands();
    Page<BrandDTO> getAllBrands(Pageable pageable);
    BrandDTO getBySlug(String slug);
    BrandDTO createBrand(BrandRequest request);
    BrandDTO updateBrand(Long id, BrandRequest request);
    void deleteBrand(Long id);
}

