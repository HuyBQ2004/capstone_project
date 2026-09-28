package com.sfms.mf02.mapper;

import com.sfms.mf02.domain.AcademicYear;
import com.sfms.mf02.dto.AcademicYearDtos.AcademicYearResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** MapStruct — {@link AcademicYear} → DTO. {@code classes}/{@code students} — ADR-M02-2. */
@Mapper(componentModel = "spring")
public interface AcademicYearMapper {

    @Mapping(target = "id", source = "academicYear.code")
    @Mapping(target = "classes", source = "classes")
    @Mapping(target = "students", source = "students")
    AcademicYearResponse toResponse(AcademicYear academicYear, long classes, long students);
}
