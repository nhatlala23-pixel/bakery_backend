package com.nguyenthongnhat.backend_tttn.service;

import com.nguyenthongnhat.backend_tttn.dto.CollectionDTO;
import java.util.List;

public interface CollectionService {
    List<CollectionDTO> getActiveCollections();
    CollectionDTO getCollectionBySlug(String slug);
    
    // Admin CRUD
    List<CollectionDTO> getAllCollectionsForAdmin();
    CollectionDTO getCollectionById(Long id);
    CollectionDTO createCollection(CollectionDTO dto);
    CollectionDTO updateCollection(Long id, CollectionDTO dto);
    void deleteCollection(Long id);
}
