package com.sfms.iam.mapper;

import com.sfms.iam.domain.Permission;
import com.sfms.iam.domain.RoleRecord;
import com.sfms.iam.dto.RoleDtos.PermissionResponse;
import com.sfms.iam.dto.RoleDtos.RoleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct — {@link RoleRecord}/{@link Permission} → DTO.
 *
 * <p>{@code users} KHÔNG map từ entity (entity không có field đó — đúng ADR-M02-2);
 * service truyền vào giá trị đếm runtime.
 */
@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "id", source = "role.code")
    @Mapping(target = "users", source = "userCount")
    RoleResponse toResponse(RoleRecord role, long userCount);

    @Mapping(target = "id", source = "code")
    PermissionResponse toPermissionResponse(Permission permission);

    List<PermissionResponse> toPermissionResponseList(List<Permission> permissions);
}
