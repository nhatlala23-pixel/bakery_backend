package com.nguyenthongnhat.backend_tttn.service;

import com.nguyenthongnhat.backend_tttn.dto.GalleryDTO;
import java.util.List;

public interface GalleryService {
    List<GalleryDTO> getAllActiveGalleries();
    List<GalleryDTO> getActiveGalleriesByCategory(String category);
    
    // Admin CRUD
    List<GalleryDTO> getAllGalleriesForAdmin();
    GalleryDTO createGallery(GalleryDTO dto);
    GalleryDTO updateGallery(Long id, GalleryDTO dto);
    void deleteGallery(Long id);
}
