package com.nguyenthongnhat.backend_tttn.mapper;

import com.nguyenthongnhat.backend_tttn.dto.OrderResponse;
import com.nguyenthongnhat.backend_tttn.entity.Order;
import com.nguyenthongnhat.backend_tttn.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    @Mapping(target = "items", source = "orderItems")
    @Mapping(target = "paymentMethod", source = "payment.paymentMethod")
    @Mapping(target = "paymentStatus", source = "payment.paymentStatus")
    OrderResponse toResponse(Order order);

    @Mapping(target = "variantSku", source = "variant.sku")
    OrderResponse.OrderItemResponse toItemResponse(OrderItem item);
}
