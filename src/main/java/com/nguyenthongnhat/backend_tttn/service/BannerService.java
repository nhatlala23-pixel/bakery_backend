package com.nguyenthongnhat.backend_tttn.service;

import com.nguyenthongnhat.backend_tttn.dto.BannerRequest;
import com.nguyenthongnhat.backend_tttn.dto.BannerResponse;
import java.util.List;

public interface BannerService {
    List<BannerResponse> getAllBanners();
    BannerResponse createBanner(BannerRequest request);
    BannerResponse updateBanner(Long id, BannerRequest request);
    void deleteBanner(Long id);
}
