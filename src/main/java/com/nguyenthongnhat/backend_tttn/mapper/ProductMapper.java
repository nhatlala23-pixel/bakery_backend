package com.nguyenthongnhat.backend_tttn.mapper;

import com.nguyenthongnhat.backend_tttn.dto.ProductDetailResponse;
import com.nguyenthongnhat.backend_tttn.dto.ProductResponse;
import com.nguyenthongnhat.backend_tttn.dto.ProductVariantResponse;
import com.nguyenthongnhat.backend_tttn.entity.Product;
import com.nguyenthongnhat.backend_tttn.entity.ProductImage;
import com.nguyenthongnhat.backend_tttn.entity.ProductSpecification;
import com.nguyenthongnhat.backend_tttn.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, 
        uses = {CategoryMapper.class, BrandMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "categoryName", source = "category.categoryName")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "brandName", source = "brand.brandName")
    @Mapping(target = "brandId", source = "brand.id")
    @Mapping(target = "thumbnail", source = "thumbnail", qualifiedByName = "fixUrl")
    ProductResponse toResponse(Product product);

    @Mapping(target = "category", source = "category")
    @Mapping(target = "brand", source = "brand")
    @Mapping(target = "thumbnail", source = "thumbnail", qualifiedByName = "fixUrl")
    @Mapping(target = "images", source = "images")
    ProductDetailResponse toDetailResponse(Product product);

    @Mapping(target = "imageUrl", source = "imageUrl", qualifiedByName = "fixUrl")
    ProductDetailResponse.ProductImageDTO toImageResponse(ProductImage image);

    @Mapping(target = "attributeValues", expression = "java(mapColorAndSize(variant))")
    @Mapping(target = "thumbnailUrl", source = "thumbnailUrl", qualifiedByName = "fixUrl")
    ProductVariantResponse toVariantResponse(ProductVariant variant);

    ProductDetailResponse.ProductSpecificationDTO toSpecResponse(ProductSpecification spec);

    default List<String> mapColorAndSize(ProductVariant variant) {
        List<String> values = new java.util.ArrayList<>();
        if (variant.getColorCode() != null && !variant.getColorCode().trim().isEmpty()) {
            values.add(variant.getColorCode().trim());
        }
        if (variant.getSize() != null && !variant.getSize().trim().isEmpty()) {
            values.add(variant.getSize().trim());
        }
        return values;
    }

    @Named("fixUrl")
    default String fixUrl(String url) {
        if (url == null || url.isEmpty()) return url;
        if (url.startsWith("http")) return url;
        // Tự động thêm domain backend nếu là đường dẫn tương đối
        return "http://localhost:8080" + (url.startsWith("/") ? "" : "/") + url;
    }
}
