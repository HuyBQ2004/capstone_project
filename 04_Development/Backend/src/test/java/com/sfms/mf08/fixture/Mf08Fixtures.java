package com.sfms.mf08.fixture;

import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.UUID;

/**
 * Trợ giúp seed dữ liệu test MF08 qua JDBC thô — {@code V900} không seed sẵn món ăn/phiên kiểm
 * thực/sự cố nào; mỗi TC dựng đúng dữ liệu cần, cách ly hoàn toàn theo {@code @BeforeEach
 * clean+migrate} của {@code AbstractIamIntegrationTest}.
 *
 * <p>⚠️ Bài học từ {@code FoodSampleFlowIT} (đã có trước): nó giả định dish {@code "com-trang"}
 * tồn tại sẵn từ {@code V9__seed_mf05.sql} — grep xác nhận KHÔNG có seed dish tĩnh nào trong
 * migration, {@code dishes} chỉ có dữ liệu khi test tự chèn. Helper này chèn dish THẬT trước khi
 * dùng, không dựa vào giả định đó.
 */
public final class Mf08Fixtures {

    public static final String SCHOOL_CODE = "SCH-HN-0042";
    public static final String OTHER_SCHOOL_CODE = "SCH-HN-0051";
    public static final String CATERER_PARTNER_CODE = "CAT-001";

    private Mf08Fixtures() {
    }

    public static UUID insertDish(JdbcTemplate jdbc, String code, String name) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO dishes (id, code, name, category, kcal, protein, fat, carb, is_active)
                VALUES (?, ?, ?, 'MON_CHINH', 250.0, 20.0, 10.0, 15.0, TRUE)
                ON CONFLICT (code) DO NOTHING
                """, id, code, name);
        return id;
    }

    /** Phiên kiểm thực PASSED 3/3 (cổng BR-105). */
    public static void insertPassedInspection(JdbcTemplate jdbc, String code, String schoolCode,
                                              String inspectorUserCode, java.time.LocalDate date) {
        jdbc.update("""
                INSERT INTO inspection_sessions (id, code, school_id, partner_id, date, meal_type,
                                                  step1, step2, step3, overall, core_temp, inspector_user_id)
                SELECT gen_random_uuid(), ?, s.id, p.id, ?, 'LUNCH',
                       'PASSED', 'PASSED', 'PASSED', 'PASSED', 76.00, u.id
                FROM schools s, partners p, user_accounts u
                WHERE s.code = ? AND p.code = ? AND u.code = ? AND u.school_id = s.id
                ON CONFLICT (school_id, code) DO NOTHING
                """, code, date, schoolCode, CATERER_PARTNER_CODE, inspectorUserCode);
    }

    /** Phiên kiểm thực FAILED ở bước 2 (nhánh chặn BR-105). */
    public static void insertFailedInspection(JdbcTemplate jdbc, String code, String schoolCode,
                                               String inspectorUserCode, java.time.LocalDate date) {
        jdbc.update("""
                INSERT INTO inspection_sessions (id, code, school_id, partner_id, date, meal_type,
                                                  step1, step2, step3, overall, fail_step, inspector_user_id)
                SELECT gen_random_uuid(), ?, s.id, p.id, ?, 'LUNCH',
                       'PASSED', 'FAILED', 'PENDING', 'FAILED', 2, u.id
                FROM schools s, partners p, user_accounts u
                WHERE s.code = ? AND p.code = ? AND u.code = ? AND u.school_id = s.id
                ON CONFLICT (school_id, code) DO NOTHING
                """, code, date, schoolCode, CATERER_PARTNER_CODE, inspectorUserCode);
    }

    /** Sự cố OPEN (status=INVESTIGATING) — BR-112/114/149. Trả UUID {@code incidents.id}. */
    public static UUID insertOpenIncident(JdbcTemplate jdbc, String code, String schoolCode, Instant occurredAt) {
        UUID recordId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO incident_records (id, code, school_id, content, status, created_by_id, incident_id)
                SELECT ?, ?, s.id, 'Test incident record', 'OPENED', u.id, ?
                FROM schools s, user_accounts u WHERE s.code = ? AND u.code = 'U-0002'
                """, recordId, "REC-" + code, incidentId, schoolCode);
        jdbc.update("""
                INSERT INTO incidents (id, code, school_id, incident_record_id, title, reported_at, occurred_at,
                                        reported_by_label, severity, status)
                SELECT ?, ?, s.id, ?, 'Test incident', ?, ?, 'Y tế trường', 'MEDIUM', 'INVESTIGATING'
                FROM schools s WHERE s.code = ?
                """, incidentId, code, recordId, java.sql.Timestamp.from(occurredAt.plusSeconds(3600)),
                java.sql.Timestamp.from(occurredAt), schoolCode);
        return incidentId;
    }

    public static void closeIncident(JdbcTemplate jdbc, String code) {
        jdbc.update("""
                UPDATE incidents SET status = 'CLOSED', closed_at = now(),
                       closed_by_id = (SELECT id FROM user_accounts WHERE code = 'U-0001'),
                       closing_statement = 'Đã xử lý xong (test fixture)'
                WHERE code = ?
                """, code);
    }
}
