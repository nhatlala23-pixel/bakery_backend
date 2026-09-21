package com.nguyenthongnhat.backend_tttn.mapper;

import com.nguyenthongnhat.backend_tttn.dto.CartItemResponse;
import com.nguyenthongnhat.backend_tttn.entity.CartItem;
import com.nguyenthongnhat.backend_tttn.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartMapper {

    @Mapping(target = "cartItemId", source = "id")
    @Mapping(target = "variantId", source = "variant.id")
    @Mapping(target = "variantSku", source = "variant.sku")
    @Mapping(target = "productId", source = "variant.product.id")
    @Mapping(target = "productName", source = "variant.product.productName")
    @Mapping(target = "productSlug", source = "variant.product.slug")
    @Mapping(target = "productThumbnail", source = "variant", qualifiedByName = "mapThumbnail")
    @Mapping(target = "variantAttributes", source = "variant", qualifiedByName = "mapVariantLabel")
    @Mapping(target = "subtotal", source = ".", qualifiedByName = "calcSubtotal")
    CartItemResponse toItemResponse(CartItem cartItem);

    @Named("calcSubtotal")
    default java.math.BigDecimal calcSubtotal(CartItem item) {
        if (item == null || item.getUnitPrice() == null || item.getQuantity() == null) return java.math.BigDecimal.ZERO;
        return item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity()));
    }

    // Ảnh: ưu tiên ảnh phiên bản, fallback về ảnh chính của sản phẩm
    @Named("mapThumbnail")
    default String mapThumbnail(ProductVariant variant) {
        if (variant == null) return null;
        if (variant.getThumbnailUrl() != null && !variant.getThumbnailUrl().isBlank()) {
            return variant.getThumbnailUrl();
        }
        if (variant.getProduct() != null) {
            return variant.getProduct().getThumbnail();
        }
        return null;
    }

    // Nhãn phiên bản: colorCode + size
    @Named("mapVariantLabel")
    default String mapVariantLabel(ProductVariant variant) {
        if (variant == null) return "";
        StringBuilder sb = new StringBuilder();
        if (variant.getColorCode() != null && !variant.getColorCode().isBlank()) {
            sb.append(variant.getColorCode());
        }
        if (variant.getSize() != null && !variant.getSize().isBlank()) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(variant.getSize());
        }
        return sb.toString();
    }
}

