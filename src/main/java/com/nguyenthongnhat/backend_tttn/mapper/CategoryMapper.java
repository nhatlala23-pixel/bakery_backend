package com.nguyenthongnhat.backend_tttn.mapper;

import com.nguyenthongnhat.backend_tttn.dto.CategoryDTO;
import com.nguyenthongnhat.backend_tttn.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    @Mapping(target = "parentId", source = "parent.id")
    CategoryDTO toDTO(Category category);

    @Mapping(target = "parent", ignore = true)
    Category toEntity(CategoryDTO dto);
}
