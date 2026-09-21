package com.nguyenthongnhat.backend_tttn.service.impl;

import com.nguyenthongnhat.backend_tttn.dto.CollectionDTO;
import com.nguyenthongnhat.backend_tttn.entity.Collection;
import com.nguyenthongnhat.backend_tttn.entity.Product;
import com.nguyenthongnhat.backend_tttn.mapper.ProductMapper;
import com.nguyenthongnhat.backend_tttn.repository.CollectionRepository;
import com.nguyenthongnhat.backend_tttn.repository.ProductRepository;
import com.nguyenthongnhat.backend_tttn.service.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {

    private final CollectionRepository collectionRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public List<CollectionDTO> getActiveCollections() {
        return collectionRepository.findByActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CollectionDTO getCollectionBySlug(String slug) {
        return collectionRepository.findBySlugAndActiveTrue(slug)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bộ sưu tập!"));
    }

    @Override
    public List<CollectionDTO> getAllCollectionsForAdmin() {
        return collectionRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CollectionDTO getCollectionById(Long id) {
        return collectionRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bộ sưu tập!"));
    }

    @Override
    @Transactional
    public CollectionDTO createCollection(CollectionDTO dto) {
        Collection collection = Collection.builder()
                .name(dto.getName())
                .slug(dto.getSlug() != null && !dto.getSlug().isEmpty() ? dto.getSlug() : generateSlug(dto.getName()))
                .description(dto.getDescription())
                .thumbnail(dto.getThumbnail())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();

        if (dto.getProductIds() != null && !dto.getProductIds().isEmpty()) {
            List<Product> products = productRepository.findAllById(dto.getProductIds());
            collection.setProducts(new HashSet<>(products));
        }

        return mapToDTO(collectionRepository.save(collection));
    }

    @Override
    @Transactional
    public CollectionDTO updateCollection(Long id, CollectionDTO dto) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bộ sưu tập để cập nhật!"));

        collection.setName(dto.getName());
        collection.setSlug(dto.getSlug() != null && !dto.getSlug().isEmpty() ? dto.getSlug() : generateSlug(dto.getName()));
        collection.setDescription(dto.getDescription());
        collection.setThumbnail(dto.getThumbnail());
        collection.setActive(dto.getActive() != null ? dto.getActive() : true);

        if (dto.getProductIds() != null) {
            List<Product> products = productRepository.findAllById(dto.getProductIds());
            collection.setProducts(new HashSet<>(products));
        } else {
            collection.setProducts(new HashSet<>());
        }

        return mapToDTO(collectionRepository.save(collection));
    }

    @Override
    @Transactional
    public void deleteCollection(Long id) {
        if (!collectionRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy bộ sưu tập để xóa!");
        }
        collectionRepository.deleteById(id);
    }

    private String generateSlug(String input) {
        if (input == null || input.isEmpty()) return "";
        String nowhitespace = Pattern.compile("\\s+").matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = Pattern.compile("[^\\w-]").matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }

    private CollectionDTO mapToDTO(Collection collection) {
        String fixedUrl = collection.getThumbnail();
        if (fixedUrl != null && !fixedUrl.isEmpty() && !fixedUrl.startsWith("http")) {
            fixedUrl = "http://localhost:8080" + (fixedUrl.startsWith("/") ? "" : "/") + fixedUrl;
        }

        List<Long> productIds = null;
        List<com.nguyenthongnhat.backend_tttn.dto.ProductResponse> productResponses = null;

        if (collection.getProducts() != null) {
            productIds = collection.getProducts().stream()
                    .map(Product::getId)
                    .collect(Collectors.toList());
            productResponses = collection.getProducts().stream()
                    .map(productMapper::toResponse)
                    .collect(Collectors.toList());
        }

        return CollectionDTO.builder()
                .id(collection.getId())
                .name(collection.getName())
                .slug(collection.getSlug())
                .description(collection.getDescription())
                .thumbnail(fixedUrl)
                .active(collection.getActive())
                .productIds(productIds)
                .products(productResponses)
                .build();
    }
}
