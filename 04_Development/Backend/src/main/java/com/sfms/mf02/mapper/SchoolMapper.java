package com.sfms.mf02.mapper;

import com.sfms.mf02.domain.School;
import com.sfms.mf02.dto.SchoolDtos.SchoolResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct — {@link School} → DTO.
 *
 * <p>{@code classes}/{@code staff}/{@code students} KHÔNG map từ entity (entity không có
 * field đó — ADR-M02-2); service truyền vào giá trị đếm runtime.
 */
@Mapper(componentModel = "spring")
public interface SchoolMapper {

    @Mapping(target = "id", source = "school.code")
    @Mapping(target = "classes", source = "classes")
    @Mapping(target = "staff", source = "staff")
    @Mapping(target = "students", source = "students")
    @Mapping(target = "registrantEmailVerified", source = "registrantEmailVerified")
    SchoolResponse toResponse(School school, long classes, long staff, long students, boolean registrantEmailVerified);
}
