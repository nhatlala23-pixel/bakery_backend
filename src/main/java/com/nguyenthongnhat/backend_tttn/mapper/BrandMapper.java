package com.nguyenthongnhat.backend_tttn.mapper;

import com.nguyenthongnhat.backend_tttn.dto.BrandDTO;
import com.nguyenthongnhat.backend_tttn.entity.Brand;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BrandMapper {
    BrandDTO toDTO(Brand brand);
    Brand toEntity(BrandDTO dto);
}
