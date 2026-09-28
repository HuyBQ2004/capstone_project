package com.sfms.mf03.repository;

import com.sfms.mf03.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID>,
        JpaSpecificationExecutor<Student> {

    Optional<Student> findByIdAndSchoolId(UUID id, UUID schoolId);

    Optional<Student> findByCode(String code);
 
    Optional<Student> findByCodeAndSchoolId(String code, UUID schoolId);

    boolean existsByCodeAndSchoolId(String code, UUID schoolId);

    boolean existsByRegistrationNo(String registrationNo);

    long countByClassroomId(UUID classroomId);

    /**
     * 13/08/2026 — chống nhập trùng cho bulk import (BR-032). Khoá trùng = tên (không phân biệt
     * hoa/thường) + ngày sinh + lớp, ĐÚNG khoá mà {@code StudentImportServiceImpl.validate()}
     * đang dùng để dò trùng trong nội bộ file; trước đây nó chỉ dò trong file nên upload lại
     * cùng một file là tạo mới toàn bộ học sinh.
     */
    boolean existsByNameIgnoreCaseAndDobAndClassroomId(String name, LocalDate dob, UUID classroomId);

    List<Student> findByClassroomId(UUID classroomId);
}
