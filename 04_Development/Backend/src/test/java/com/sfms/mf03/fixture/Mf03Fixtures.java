package com.sfms.mf03.fixture;

import com.sfms.common.enums.AllergySeverity;
import com.sfms.mf03.domain.AllergyDeclaration;
import com.sfms.mf03.domain.DeclarationStatus;
import com.sfms.mf03.domain.Student;
import com.sfms.mf03.domain.StudentStatus;

import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * CASE 2.0 — Props Isolation (TestSpec-MF03 §4.0). Mỗi test tự dựng dữ liệu qua factory;
 * KHÔNG shared mutable state (chống AP-AI-002). Giá trị baseline khớp §3 TDS-05 (FX-001..).
 */
public final class Mf03Fixtures {

    public static final UUID SCHOOL_ID = UUID.randomUUID();
    public static final UUID CLASS_3A = UUID.randomUUID();
    public static final UUID HS_3A_002 = UUID.randomUUID();
    public static final UUID HS_3A_009 = UUID.randomUUID();

    private Mf03Fixtures() {
    }

    /** FX-001 — HS baseline hợp lệ. dob là field MỚI so với mock (L12). */
    public static Student student(Consumer<Student> customize) {
        Student s = new Student();
        s.setId(UUID.randomUUID());
        s.setCode("HS-3A-001");
        s.setRegistrationNo("DTD-2026-0001");
        s.setName("Nguyễn Minh Khang (synthetic)");
        s.setDob(LocalDate.parse("2018-03-12"));       // D4: ISO, KHÔNG "12/03/2018"
        s.setSchoolId(SCHOOL_ID);
        s.setClassroomId(CLASS_3A);
        s.setStatus(StudentStatus.ACTIVE);
        customize.accept(s);
        return s;
    }

    /** FX-006 — SEVERE thiếu tài liệu (fixture MỚI, xem L6). */
    public static AllergyDeclaration severeWithoutProof(UUID studentId) {
        AllergyDeclaration d = new AllergyDeclaration();
        d.setId(java.util.UUID.randomUUID());
        d.setCode("ALG-2026-0001");                     // D1 — `id` của JSON là mã nghiệp vụ
        d.setStudentId(studentId);
        d.setCategory("Hải sản (tôm, cua, sò, ốc)");
        d.setAllergen("Tôm");
        d.setSeverity(AllergySeverity.SEVERE);          // D6: SCREAMING_SNAKE, KHÔNG "Nặng"/"NANG"/"Severe"
        d.setStatus(DeclarationStatus.PENDING);
        d.setProofFileId(null);
        return d;
    }

    /** FX-005 — MODERATE không tài liệu, RESET về PENDING (seed gốc đã VERIFIED — L7). */
    public static AllergyDeclaration moderateWithoutProof(UUID studentId) {
        AllergyDeclaration d = severeWithoutProof(studentId);
        d.setId(UUID.randomUUID());
        d.setAllergen("Trứng");
        d.setSeverity(AllergySeverity.MODERATE);
        return d;
    }
}
