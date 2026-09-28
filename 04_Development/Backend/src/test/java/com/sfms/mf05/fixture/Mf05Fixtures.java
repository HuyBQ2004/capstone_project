package com.sfms.mf05.fixture;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

/**
 * Trợ giúp seed dữ liệu test MF05 qua JDBC thô — V900 KHÔNG có sẵn học sinh/dị ứng (MF03) hay
 * liên kết {@code supplier_partnerships} cho {@code CAT-001} (chỉ MF04 seed đối tác, không seed
 * quan hệ hợp tác cho CAT-001). Mỗi test tự dựng dữ liệu cần, cách ly hoàn toàn theo
 * {@code @BeforeEach clean+migrate} của {@code AbstractIamIntegrationTest} (L28/M05-D12).
 *
 * <p>KHÔNG set field audit ({@code created_at}/{@code created_by}...) — BaseEntity/AuditorAware
 * lo (L22). Fixture ở đây ghi thẳng SQL nên audit cột để {@code NULL} — chấp nhận được vì test
 * không assert trên các cột đó.
 */
public final class Mf05Fixtures {

    public static final String SCHOOL_CODE = "SCH-HN-0042";
    public static final String CATERER_PARTNER_CODE = "CAT-001";
    public static final String CATERER_USER_CODE = "U-0022";       // partner_id=CAT-001 (M05-D10)
    public static final String OTHER_SCHOOL_CODE = "SCH-HN-0051";

    private Mf05Fixtures() {
    }

    /** U-0022 (partner_id=CAT-001) không có schoolId trong JWT — cần liên kết hợp tác để resolveSchoolId() chạy. */
    public static void linkCatererToSchool(JdbcTemplate jdbc, String schoolCode) {
        jdbc.update("""
                INSERT INTO supplier_partnerships (id, code, partner_id, school_id, delivery_address, since, status)
                SELECT gen_random_uuid(), 'PTN-MF05-' || substr(gen_random_uuid()::text, 1, 8),
                       p.id, s.id, 'Test address', DATE '2026-01-01', 'APPROVED'
                FROM partners p, schools s WHERE p.code = ? AND s.code = ?
                """, CATERER_PARTNER_CODE, schoolCode);
    }

    /** Học sinh mới trong lớp (grade suy từ classroom), trả về UUID học sinh vừa tạo. */
    public static UUID insertStudent(JdbcTemplate jdbc, String studentCode, String classroomCode, String schoolCode) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO students (id, code, registration_no, name, dob, school_id, classroom_id, status)
                SELECT ?, ?, ?, 'HS Test ' || ?, DATE '2018-01-01', s.id, c.id, 'ACTIVE'
                FROM schools s JOIN classrooms c ON c.school_id = s.id AND c.code = ?
                WHERE s.code = ?
                """, id, studentCode, "REG-" + studentCode, studentCode, classroomCode, schoolCode);
        return id;
    }

    /** Dị ứng ĐÃ XÁC MINH (status=VERIFIED) — oracle của BR-063. */
    public static void insertVerifiedAllergy(JdbcTemplate jdbc, UUID studentId, String allergen) {
        jdbc.update("""
                INSERT INTO allergy_declarations
                    (id, code, student_id, category, allergen, severity, declared_by_user_id, status)
                VALUES (gen_random_uuid(), 'ALG-MF05-' || substr(gen_random_uuid()::text, 1, 8),
                        ?, 'FOOD', ?, 'MODERATE',
                        (SELECT id FROM user_accounts WHERE code = 'U-0002'), 'VERIFIED')
                """, studentId, allergen);
    }

    public static void linkParentToStudent(JdbcTemplate jdbc, String parentUserCode, UUID studentId) {
        jdbc.update("""
                INSERT INTO student_parents (id, student_id, parent_user_id, relationship)
                SELECT gen_random_uuid(), ?, u.id, 'Mother'
                FROM user_accounts u WHERE u.code = ?
                """, studentId, parentUserCode);
    }
}
