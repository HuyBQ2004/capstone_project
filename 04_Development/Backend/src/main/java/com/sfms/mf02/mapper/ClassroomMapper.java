package com.sfms.mf02.mapper;

import com.sfms.mf02.domain.Classroom;
import com.sfms.mf02.dto.ClassroomDtos.ClassroomDetailResponse;
import com.sfms.mf02.dto.ClassroomDtos.ClassroomResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct — {@link Classroom} → DTO.
 *
 * <p>{@code students}/{@code allergies} KHÔNG map từ entity (ADR-M02-2). {@code teacher}/
 * {@code teacherId}/{@code academicYearId} là mã nghiệp vụ tra qua
 * {@code common/reference/ReferenceResolver} ở service — truyền vào đây dưới dạng tham số vì
 * MapStruct KHÔNG được phép import repository/entity của module khác (D1, AP-AI-005).
 */
@Mapper(componentModel = "spring")
public interface ClassroomMapper {

    @Mapping(target = "id", source = "classroom.code")
    @Mapping(target = "grade", source = "gradeName")
    @Mapping(target = "academicYearId", source = "academicYearCode")
    @Mapping(target = "students", source = "students")
    @Mapping(target = "allergies", source = "allergies")
    @Mapping(target = "teacher", source = "teacherName")
    @Mapping(target = "teacherId", source = "teacherPublicId")
    ClassroomResponse toResponse(Classroom classroom, String gradeName, String academicYearCode,
                                 long students, long allergies, String teacherName, String teacherPublicId);

    @Mapping(target = "id", source = "classroom.code")
    @Mapping(target = "grade", source = "gradeName")
    @Mapping(target = "academicYearId", source = "academicYearCode")
    @Mapping(target = "students", source = "students")
    @Mapping(target = "allergies", source = "allergies")
    @Mapping(target = "teacher", source = "teacherName")
    @Mapping(target = "teacherId", source = "teacherPublicId")
    @Mapping(target = "schoolId", source = "schoolCode")
    ClassroomDetailResponse toDetailResponse(Classroom classroom, String gradeName,
                                             String academicYearCode, long students, long allergies,
                                             String teacherName, String teacherPublicId, String schoolCode);
}
