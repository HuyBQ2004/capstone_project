package com.sfms.mf03.mapper;

import com.sfms.mf03.domain.Student;
import com.sfms.mf03.dto.StudentDtos.ParentSummary;
import com.sfms.mf03.dto.StudentDtos.StudentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct — {@link Student} → {@link StudentResponse}. Field dẫn xuất (ADR-M03-3) KHÔNG map
 * từ entity — truyền vào dưới dạng tham số đã tính sẵn ở service (khuôn giống hệt
 * {@code mf02.mapper.ClassroomMapper}), vì mapper KHÔNG được import repository của module khác.
 */
@Mapper(componentModel = "spring")
public interface StudentMapper {

    @Mapping(target = "id", source = "student.code")
    @Mapping(target = "studentCode", source = "student.code")
    @Mapping(target = "classroomId", source = "classroomCode")
    @Mapping(target = "clazz", source = "className")
    @Mapping(target = "allergies", source = "allergies")
    @Mapping(target = "specialDiet", source = "specialDiet")
    @Mapping(target = "healthStatus", source = "healthStatus")
    @Mapping(target = "allergySeverity", source = "allergySeverity")
    @Mapping(target = "parent", source = "parentName")
    @Mapping(target = "parentId", source = "parentPublicId")
    @Mapping(target = "parents", source = "parents")
    @Mapping(target = "hasAllergy", source = "hasAllergy")
    @Mapping(target = "hasSpecialDiet", source = "hasSpecialDiet")
    @Mapping(target = "parentAccountCreated", source = "parentAccountCreated")
    StudentResponse toResponse(Student student, String classroomCode, String className,
                               List<String> allergies, String specialDiet, String healthStatus,
                               String allergySeverity, String parentName, String parentPublicId,
                               List<ParentSummary> parents, String hasAllergy, String hasSpecialDiet,
                               Boolean parentAccountCreated);
}
