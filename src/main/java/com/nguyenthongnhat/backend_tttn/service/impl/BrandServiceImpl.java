package com.nguyenthongnhat.backend_tttn.service.impl;

import com.nguyenthongnhat.backend_tttn.dto.BrandDTO;
import com.nguyenthongnhat.backend_tttn.dto.BrandRequest;
import com.nguyenthongnhat.backend_tttn.entity.Brand;
import com.nguyenthongnhat.backend_tttn.mapper.BrandMapper;
import com.nguyenthongnhat.backend_tttn.repository.BrandRepository;
import com.nguyenthongnhat.backend_tttn.service.BrandService;
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
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    @Override
    public List<BrandDTO> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(brandMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<BrandDTO> getAllBrands(Pageable pageable) {
        return brandRepository.findAll(pageable)
                .map(brandMapper::toDTO);
    }

    @Override
    public BrandDTO getBySlug(String slug) {
        return brandRepository.findBySlug(slug)
                .map(brandMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu: " + slug));
    }

    @Override
    @Transactional
    public BrandDTO createBrand(BrandRequest request) {
        Brand brand = new Brand();
        brand.setBrandName(request.getBrandName());
        brand.setDescription(request.getDescription());
        brand.setCountry(request.getCountry());
        brand.setLogoUrl(request.getLogoUrl());
        brand.setStatus(request.getStatus());
        
        String slug = (request.getSlug() != null && !request.getSlug().isEmpty()) 
                ? request.getSlug() 
                : generateSlug(request.getBrandName());
        brand.setSlug(slug);

        return brandMapper.toDTO(brandRepository.save(brand));
    }

    @Override
    @Transactional
    public BrandDTO updateBrand(Long id, BrandRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với id: " + id));
        
        brand.setBrandName(request.getBrandName());
        brand.setDescription(request.getDescription());
        brand.setCountry(request.getCountry());
        brand.setLogoUrl(request.getLogoUrl());
        brand.setStatus(request.getStatus());
        
        String slug = (request.getSlug() != null && !request.getSlug().isEmpty()) 
                ? request.getSlug() 
                : generateSlug(request.getBrandName());
        brand.setSlug(slug);

        return brandMapper.toDTO(brandRepository.save(brand));
    }

    @Override
    @Transactional
    public void deleteBrand(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với id: " + id));
        brandRepository.delete(brand);
    }

    private String generateSlug(String input) {
        if (input == null || input.isEmpty()) return "";
        String nowhitespace = Pattern.compile("\\s+").matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = Pattern.compile("[^\\w-]").matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }
}

