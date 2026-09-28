package com.sfms.mf09.fixture;

import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Trợ giúp seed dữ liệu test MF09 qua JDBC thô — {@code V900} không seed sẵn học sinh (MF03),
 * món/thực đơn (MF05), phiên kiểm thực (MF07) hay bất kỳ bản ghi {@code student_distributions}
 * nào; mỗi TC Integration/Contract/E2E tự dựng đúng dữ liệu cần, cách ly hoàn toàn theo
 * {@code @BeforeEach clean+migrate} của {@code AbstractIamIntegrationTest}.
 *
 * <p>Theo khuôn {@code com.sfms.mf05.fixture.Mf05Fixtures} / {@code com.sfms.mf08.fixture.Mf08Fixtures}
 * (đọc trước khi sửa). Với dị ứng/liên kết phụ huynh và phiên kiểm thực — vốn thuộc MF03/MF07 —
 * cố tình KHÔNG chép lại logic, mà gọi thẳng {@code Mf05Fixtures}/{@code Mf08Fixtures} (public,
 * cùng schema, cùng test scope) để một chỗ duy nhất giữ đúng SQL khi migration đổi.
 *
 * <p>{@code SCHOOL_CODE}/{@code CLASSROOM_CODE} khớp đúng seed thật của {@code V900}: lớp
 * {@code 3A} ở {@code SCH-HN-0042} có GVCN {@code STF-001} ≡ {@code U-0004} (role TEACHER) — xem
 * {@code RoleMatrixIT}/{@code ConfirmDistributionTest} đã dùng đúng cặp này.
 */
public final class Mf09Fixtures {

    public static final String SCHOOL_CODE = "SCH-HN-0042";
    public static final String OTHER_SCHOOL_CODE = "SCH-HN-0051";
    public static final String CLASSROOM_CODE = "3A";               // GVCN = STF-001 ≡ U-0004 (TEACHER)
    public static final String OTHER_CLASSROOM_CODE = "9Z";         // trường khác — IDOR cross-school
    public static final String CATERER_PARTNER_CODE = "CAT-001";

    private Mf09Fixtures() {
    }

    // ───────────────────────── Học sinh (MF03) ─────────────────────────

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

    public static UUID classroomIdOf(JdbcTemplate jdbc, String classroomCode, String schoolCode) {
        return jdbc.queryForObject("""
                SELECT c.id FROM classrooms c JOIN schools s ON s.id = c.school_id
                WHERE c.code = ? AND s.code = ?
                """, UUID.class, classroomCode, schoolCode);
    }

    /** Dị ứng ĐÃ XÁC MINH (status=VERIFIED) — cổng BR-065. Uỷ lại {@code Mf05Fixtures}. */
    public static void insertVerifiedAllergy(JdbcTemplate jdbc, UUID studentId, String allergen) {
        com.sfms.mf05.fixture.Mf05Fixtures.insertVerifiedAllergy(jdbc, studentId, allergen);
    }

    public static void linkParentToStudent(JdbcTemplate jdbc, String parentUserCode, UUID studentId) {
        com.sfms.mf05.fixture.Mf05Fixtures.linkParentToStudent(jdbc, parentUserCode, studentId);
    }

    // ───────────────────────── Món / thực đơn (MF05) ─────────────────────────

    public static UUID insertDish(JdbcTemplate jdbc, String code, String name) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO dishes (id, code, name, category, kcal, protein, fat, carb, is_active)
                VALUES (?, ?, ?, 'MON_CHINH', 250.0, 20.0, 10.0, 15.0, TRUE)
                ON CONFLICT (code) DO NOTHING
                """, id, code, name);
        return id;
    }

    /**
     * Thực đơn ĐÃ DUYỆT (status=APPROVED) cho 1 (trường, ngày, buổi, khối) — điều kiện phát suất (C3).
     * {@code grade} là số khối (VD "3" cho lớp 3A) — V44 đổi cột {@code menus.grade} (VARCHAR) thành
     * {@code grade_id} (FK {@code grades(id)}, seed sẵn "Khối 1".."Khối 12" cho mọi trường ở V900).
     */
    public static UUID insertApprovedMenu(JdbcTemplate jdbc, String code, String schoolCode,
                                           LocalDate date, String mealType, String grade) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO menus (id, code, school_id, week_label, name, grade_id, menu_date, meal_type,
                                    status, audience, approved_by_user_id, approved_at)
                SELECT ?, ?, s.id, 'Tuần test', 'Thực đơn test ' || ?, g.id, ?, ?,
                       'APPROVED', 'ALL', (SELECT id FROM user_accounts WHERE code = 'U-0001'), now()
                FROM schools s JOIN grades g ON g.school_id = s.id AND g.name = 'Khối ' || ?
                WHERE s.code = ?
                """, id, code, code, date, mealType, grade, schoolCode);
        return id;
    }

    public static void addDishToMenu(JdbcTemplate jdbc, UUID menuId, UUID dishId) {
        jdbc.update("""
                INSERT INTO menu_dishes (id, menu_id, dish_id, serving_order, portions)
                VALUES (gen_random_uuid(), ?, ?, 1, 100)
                ON CONFLICT (menu_id, dish_id) DO NOTHING
                """, menuId, dishId);
    }

    // ───────────────────────── Phiên kiểm thực (MF07) — uỷ lại Mf08Fixtures ─────────────────────────

    /** Phiên kiểm thực PASSED 3/3 — mở khoá cổng BR-094/BR-105. */
    public static void insertPassedInspection(JdbcTemplate jdbc, String code, String schoolCode,
                                               String inspectorUserCode, LocalDate date) {
        com.sfms.mf08.fixture.Mf08Fixtures.insertPassedInspection(jdbc, code, schoolCode, inspectorUserCode, date);
    }

    /** Phiên kiểm thực FAILED bước 2 — nhánh chặn cổng BR-094. */
    public static void insertFailedInspection(JdbcTemplate jdbc, String code, String schoolCode,
                                               String inspectorUserCode, LocalDate date) {
        com.sfms.mf08.fixture.Mf08Fixtures.insertFailedInspection(jdbc, code, schoolCode, inspectorUserCode, date);
    }

    // ───────────────────────── Phân phát (MF09) ─────────────────────────

    /** Bản ghi {@code student_distributions} PENDING, sẵn sàng gọi {@code POST .../serve}. */
    public static UUID insertPendingDistribution(JdbcTemplate jdbc, String code, String schoolCode,
                                                  UUID studentId, String studentName, String classroomCode,
                                                  UUID menuId, LocalDate date, String mealType) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO student_distributions
                    (id, code, school_id, date, meal_type, student_id, student_name, class_name,
                     classroom_id, menu_id, caterer_partner_id, caterer_name, status)
                SELECT ?, ?, s.id, ?, ?, ?, ?, c.code, c.id, ?, p.id, p.name, 'PENDING'
                FROM schools s
                JOIN classrooms c ON c.school_id = s.id AND c.code = ?
                LEFT JOIN partners p ON p.code = ?
                WHERE s.code = ?
                """, id, code, date, mealType, studentId, studentName, menuId, classroomCode,
                CATERER_PARTNER_CODE, schoolCode);
        return id;
    }

    /** Bản ghi {@code student_distributions} đã SERVED — dùng cho GET/summary/history. */
    public static UUID insertServedDistribution(JdbcTemplate jdbc, String code, String schoolCode,
                                                 UUID studentId, String studentName, String classroomCode,
                                                 UUID menuId, LocalDate date, String mealType,
                                                 String servedByUserCode, Instant servedAt) {
        UUID id = insertPendingDistribution(jdbc, code, schoolCode, studentId, studentName, classroomCode,
                menuId, date, mealType);
        jdbc.update("""
                UPDATE student_distributions
                SET status = 'SERVED', served_at = ?,
                    served_by_user_id = (SELECT id FROM user_accounts WHERE code = ?)
                WHERE id = ?
                """, java.sql.Timestamp.from(servedAt), servedByUserCode, id);
        return id;
    }

    public static UUID insertServingDish(JdbcTemplate jdbc, String code, String schoolCode, UUID distributionId,
                                          UUID dishId, String dishName) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO serving_dishes (id, code, school_id, distribution_id, dish_id, dish_name, is_substitute)
                SELECT ?, ?, s.id, ?, ?, ?, FALSE
                FROM schools s WHERE s.code = ?
                """, id, code, distributionId, dishId, dishName, schoolCode);
        return id;
    }
}
