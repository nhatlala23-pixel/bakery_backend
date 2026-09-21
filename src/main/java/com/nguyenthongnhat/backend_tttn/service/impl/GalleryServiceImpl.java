package com.nguyenthongnhat.backend_tttn.service.impl;

import com.nguyenthongnhat.backend_tttn.dto.GalleryDTO;
import com.nguyenthongnhat.backend_tttn.entity.Gallery;
import com.nguyenthongnhat.backend_tttn.repository.GalleryRepository;
import com.nguyenthongnhat.backend_tttn.service.GalleryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GalleryServiceImpl implements GalleryService {

    private final GalleryRepository galleryRepository;

    @Override
    public List<GalleryDTO> getAllActiveGalleries() {
        return galleryRepository.findByActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<GalleryDTO> getActiveGalleriesByCategory(String category) {
        return galleryRepository.findByCategoryAndActiveTrueOrderByDisplayOrderAsc(category).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<GalleryDTO> getAllGalleriesForAdmin() {
        return galleryRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GalleryDTO createGallery(GalleryDTO dto) {
        Gallery gallery = Gallery.builder()
                .title(dto.getTitle())
                .imageUrl(dto.getImageUrl())
                .category(dto.getCategory())
                .displayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0)
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();
        return mapToDTO(galleryRepository.save(gallery));
    }

    @Override
    @Transactional
    public GalleryDTO updateGallery(Long id, GalleryDTO dto) {
        Gallery gallery = galleryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hình ảnh trong thư viện!"));
        
        gallery.setTitle(dto.getTitle());
        gallery.setImageUrl(dto.getImageUrl());
        gallery.setCategory(dto.getCategory());
        gallery.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0);
        gallery.setActive(dto.getActive() != null ? dto.getActive() : true);

        return mapToDTO(galleryRepository.save(gallery));
    }

    @Override
    @Transactional
    public void deleteGallery(Long id) {
        if (!galleryRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy hình ảnh để xóa!");
        }
        galleryRepository.deleteById(id);
    }

    private GalleryDTO mapToDTO(Gallery gallery) {
        String fixedUrl = gallery.getImageUrl();
        if (fixedUrl != null && !fixedUrl.isEmpty() && !fixedUrl.startsWith("http")) {
            fixedUrl = "http://localhost:8080" + (fixedUrl.startsWith("/") ? "" : "/") + fixedUrl;
        }

        return GalleryDTO.builder()
                .id(gallery.getId())
                .title(gallery.getTitle())
                .imageUrl(fixedUrl)
                .category(gallery.getCategory())
                .displayOrder(gallery.getDisplayOrder())
                .active(gallery.getActive())
                .build();
    }
}
