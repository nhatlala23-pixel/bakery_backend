package com.nguyenthongnhat.backend_tttn.service.impl;

import com.nguyenthongnhat.backend_tttn.dto.BannerRequest;
import com.nguyenthongnhat.backend_tttn.dto.BannerResponse;
import com.nguyenthongnhat.backend_tttn.entity.Banner;
import com.nguyenthongnhat.backend_tttn.repository.BannerRepository;
import com.nguyenthongnhat.backend_tttn.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;

    @Override
    public List<BannerResponse> getAllBanners() {
        return bannerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BannerResponse createBanner(BannerRequest request) {
        Banner banner = Banner.builder()
                .title(request.getTitle())
                .subtitle(request.getSubtitle())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .buttonText(request.getButtonText())
                .buttonLink(request.getButtonLink())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .active(request.getActive() != null ? request.getActive() : true)
                .build();
        
        return mapToResponse(bannerRepository.save(banner));
    }

    @Override
    @Transactional
    public BannerResponse updateBanner(Long id, BannerRequest request) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Banner!"));
        
        banner.setTitle(request.getTitle());
        banner.setSubtitle(request.getSubtitle());
        banner.setDescription(request.getDescription());
        banner.setImageUrl(request.getImageUrl());
        banner.setButtonText(request.getButtonText());
        banner.setButtonLink(request.getButtonLink());
        banner.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        banner.setActive(request.getActive() != null ? request.getActive() : true);
        
        return mapToResponse(bannerRepository.save(banner));
    }

    @Override
    @Transactional
    public void deleteBanner(Long id) {
        bannerRepository.deleteById(id);
    }

    private BannerResponse mapToResponse(Banner banner) {
        return BannerResponse.builder()
                .id(banner.getId())
                .title(banner.getTitle())
                .subtitle(banner.getSubtitle())
                .description(banner.getDescription())
                .imageUrl(banner.getImageUrl())
                .buttonText(banner.getButtonText())
                .buttonLink(banner.getButtonLink())
                .sortOrder(banner.getSortOrder())
                .active(banner.getActive())
                .build();
    }
}
