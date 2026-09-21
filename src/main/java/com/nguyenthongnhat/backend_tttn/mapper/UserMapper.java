package com.nguyenthongnhat.backend_tttn.mapper;

import com.nguyenthongnhat.backend_tttn.dto.UserRequest;
import com.nguyenthongnhat.backend_tttn.dto.UserResponse;
import com.nguyenthongnhat.backend_tttn.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "roleName", source = "role.roleName")
    UserResponse toResponse(User user);

    @Mapping(target = "role", ignore = true) // Sẽ thủ công map RoleId trong Service
    User toEntity(UserRequest request);
}
