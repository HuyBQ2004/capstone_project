package com.sfms.common.reference;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tra cứu <b>mã nghiệp vụ</b> và nhãn của khóa ngoại thuộc module khác.
 *
 * <p>Lý do tồn tại: <b>D1</b> quy định khóa ngoại trong DB là UUID nhưng khóa ngoại
 * <b>trong JSON</b> là {@code code} của bảng đích ({@code "schoolId": "SCH-HN-0042"},
 * {@code "partnerId": "SUP-001"}). Mọi mapper phải dịch qua lại, và cả hai chiều đều là
 * điểm dễ sai — D1 nêu rõ rủi ro: <i>"Đổi sang UUID mà sót một chỗ là rò dữ liệu chéo giữa
 * các NCC"</i>.
 *
 * <p>Bảng {@code schools}/{@code partners} thuộc MF02/MF04; ở đây chỉ đọc bằng native query
 * để không phụ thuộc entity của module khác (chúng chưa được implement).
 *
 * <p>Cache trong-request là đủ: danh mục trường/đối tác gần như tĩnh, và một response danh
 * sách 8 user dễ tra cùng một {@code schoolId} 8 lần.
 */
@Component
public class ReferenceResolver {

    @PersistenceContext
    private EntityManager em;

    private final Map<UUID, String[]> schoolCache = new ConcurrentHashMap<>();
    private final Map<UUID, String[]> partnerCache = new ConcurrentHashMap<>();

    /** @return mã nghiệp vụ của trường ({@code SCH-HN-0042}), hoặc {@code null}. */
    @Transactional(readOnly = true)
    public String schoolCode(UUID schoolId) {
        return schoolId == null ? null : school(schoolId).map(r -> r[0]).orElse(null);
    }

    /** @return nhãn hiển thị của trường ({@code TH Đoàn Thị Điểm}), hoặc {@code null}. */
    @Transactional(readOnly = true)
    public String schoolName(UUID schoolId) {
        return schoolId == null ? null : school(schoolId).map(r -> r[1]).orElse(null);
    }

    /**
     * @return địa chỉ trường ({@code schools.address}), hoặc {@code null} nếu chưa khai.
     *
     * Dùng cho phần "Địa điểm kiểm tra" in trên đầu Mẫu 1/2/3 (QĐ 1246/QĐ-BYT). Trước
     * 08/08/2026 chỗ đó là chuỗi {@code "Bếp trung tâm"} in cứng trong Java — sai với mọi
     * trường, và là chữ trên biểu mẫu pháp lý nên không được bịa.
     */
    @Transactional(readOnly = true)
    public String schoolAddress(UUID schoolId) {
        return schoolId == null ? null : school(schoolId).map(r -> r[2]).orElse(null);
    }

    /**
     * @return {@code schools.status} thô (PENDING/ACTIVE/INACTIVE, {@code SchoolStatus} MF02),
     *         hoặc {@code null} nếu {@code schoolId == null}/không tìm thấy.
     *
     * <p>Thêm 11/08/2026 — {@code AuthService.completeLogin()} cần biết trạng thái DUYỆT của
     * trường (không phải trạng thái user) để chặn đăng nhập khi trường chưa được System Admin
     * duyệt (UC-070) hoặc đã bị vô hiệu hóa (UC-072) — trước đó không có tầng nào trong luồng
     * đăng nhập tham chiếu tới {@code School}, nên user vừa activate email là login được ngay
     * dù trường vẫn PENDING.
     */
    @Transactional(readOnly = true)
    public String schoolStatus(UUID schoolId) {
        if (schoolId == null) {
            return null;
        }
        var results = em.createNativeQuery("select status from schools where id = :id")
                .setParameter("id", schoolId).getResultList();
        return results.isEmpty() ? null : String.valueOf(results.get(0));
    }

    /** @return mã nghiệp vụ của đối tác ({@code SUP-001}), hoặc {@code null}. */
    @Transactional(readOnly = true)
    public String partnerCode(UUID partnerId) {
        return partnerId == null ? null : partner(partnerId).map(r -> r[0]).orElse(null);
    }

    /** @return nhãn hiển thị của đối tác ({@code "Đơn vị suất ăn An Bình"}), hoặc {@code null}. MF05. */
    @Transactional(readOnly = true)
    public String partnerName(UUID partnerId) {
        return partnerId == null ? null : partner(partnerId).map(r -> r[1]).orElse(null);
    }

    /** Chiều ngược: mã nghiệp vụ → UUID nội bộ. Dùng khi nhận {@code schoolId} từ request. */
    @Transactional(readOnly = true)
    public Optional<UUID> schoolIdByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return single("select id from schools where code = :code", code)
                .map(o -> (UUID) o);
    }

    @Transactional(readOnly = true)
    public Optional<UUID> partnerIdByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return single("select id from partners where code = :code", code)
                .map(o -> (UUID) o);
    }

    /**
     * Đối tác ĐƠN VỊ SUẤT ĂN đang hợp tác hiệu lực (APPROVED) với một trường — dùng để phân
     * quyền/gán {@code partnerId} cho actor vai trò {@code SERVICE_CATERERS}, vốn KHÔNG có
     * claim {@code partnerId} trong JWT (chỉ tài khoản {@code SUPPLIER} có — khác {@code
     * PartnerType.SERVICE_CATERER}, xem javadoc {@code RoleKey}). Một trường tại một thời
     * điểm chỉ hợp tác với đúng MỘT đơn vị suất ăn còn hiệu lực — lấy dòng mới nhất nếu seed
     * có nhiều hơn 1 (không nên xảy ra ở dữ liệu thật, DB không có UNIQUE ràng buộc điều này).
     *
     * <p><b>Sửa 08/08/2026 — thêm vế {@code until}.</b> Trước đó chỉ lọc {@code APPROVED},
     * nên hợp tác ĐÃ KẾT THÚC (until trong quá khứ) mà chưa đổi status vẫn được coi là hiệu
     * lực. Sau khi BR-EXCLUSIVE-CATERER được enforce, ĐỔI NHÀ THẦU là thao tác bình thường
     * (kết thúc hợp tác cũ rồi duyệt đơn vị mới) — thiếu vế này thì đơn vị CŨ vẫn resolve về
     * trường đó và ghi được dữ liệu mới, đồng thời trường có thể trả về nhầm nhà thầu cũ.
     * Cùng định nghĩa "đang hiệu lực" với {@code SupplierPartnershipRepository} và
     * {@code catererForSchoolOn(date)} bên dưới.
     */
    @Transactional(readOnly = true)
    public UUID approvedCatererPartnerId(UUID schoolId) {
        if (schoolId == null) {
            return null;
        }
        var results = em.createNativeQuery(
                        "select sp.partner_id from supplier_partnerships sp "
                                + "join partners p on p.id = sp.partner_id "
                                + "where sp.school_id = :schoolId and sp.status = 'APPROVED' "
                                + "and p.partner_type = 'SERVICE_CATERER' "
                                + "and (sp.until is null or sp.until >= current_date) "
                                + "order by sp.created_at desc")
                .setParameter("schoolId", schoolId)
                .setMaxResults(1)
                .getResultList();
        return results.isEmpty() ? null : (UUID) results.get(0);
    }

    private Optional<String[]> school(UUID id) {
        return Optional.ofNullable(schoolCache.computeIfAbsent(id,
                key -> row("select code, name, address from schools where id = :id", key)));
    }

    private Optional<String[]> partner(UUID id) {
        return Optional.ofNullable(partnerCache.computeIfAbsent(id,
                key -> row("select code, name from partners where id = :id", key)));
    }

    private String[] row(String sql, UUID id) {
        var results = em.createNativeQuery(sql).setParameter("id", id).getResultList();
        if (results.isEmpty()) {
            return null;
        }
        Object[] cols = (Object[]) results.get(0);
        // Ánh xạ theo ĐÚNG số cột của câu select, không cố định 2 — `school()` cần cột thứ ba.
        String[] out = new String[cols.length];
        for (int i = 0; i < cols.length; i++) {
            out[i] = (String) cols[i];
        }
        return out;
    }

    private Optional<Object> single(String sql, String code) {
        var results = em.createNativeQuery(sql).setParameter("code", code).getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /** Gọi khi MF02/MF04 sửa danh mục — tránh giữ nhãn cũ suốt vòng đời process. */
    public void evictAll() {
        schoolCache.clear();
        partnerCache.clear();
    }

    // ───────────────────────── MF02 ADR-M02-5 — GVCN (user_accounts) ─────────────────────────

    /**
     * Chiều {@code UUID → nhãn} cho {@code homeroom_teacher_id}. KHÔNG cache: danh mục nhân
     * sự đổi thường xuyên hơn trường/đối tác (assign/reassign GVCN, BR-020), và số dòng đọc
     * mỗi request nhỏ (≤ pageSize).
     *
     * @return {@code [code, staffCode(hoặc rỗng), name]}, hoặc {@code null} nếu không tồn tại.
     */
    @Transactional(readOnly = true)
    public String[] userById(UUID id) {
        if (id == null) {
            return null;
        }
        var results = em.createNativeQuery(
                        "select code, coalesce(staff_code, ''), name from user_accounts where id = :id")
                .setParameter("id", id)
                .getResultList();
        if (results.isEmpty()) {
            return null;
        }
        Object[] cols = (Object[]) results.get(0);
        return new String[]{(String) cols[0], (String) cols[1], (String) cols[2]};
    }

    /**
     * Chiều ngược — {@code code} <b>hoặc</b> {@code staff_code} → hồ sơ đủ để validate
     * BR-020 (role=Teacher, status=ACTIVE, cùng trường) mà KHÔNG import entity/repository
     * của MF01 (ADR-M02-5, AP-AI-005).
     */
    @Transactional(readOnly = true)
    public Optional<TeacherRef> teacherByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        var results = em.createNativeQuery("""
                select ua.id, ua.code, coalesce(ua.staff_code, ''), ua.name,
                       r.role_key, ua.status, ua.school_id
                from user_accounts ua
                join roles r on r.id = ua.role_id
                where ua.code = :code or ua.staff_code = :code
                """)
                .setParameter("code", code)
                .getResultList();
        if (results.isEmpty()) {
            return Optional.empty();
        }
        Object[] cols = (Object[]) results.get(0);
        return Optional.of(new TeacherRef((UUID) cols[0], (String) cols[1], (String) cols[2],
                (String) cols[3], (String) cols[4], (String) cols[5], (UUID) cols[6]));
    }

    /** Hồ sơ tối thiểu của {@code user_accounts} cần để enforce BR-020 (ADR-M02-5). */
    public record TeacherRef(UUID id, String code, String staffCode, String name,
                             String roleKey, String status, UUID schoolId) {

        /** {@code staff_code} nếu có, ngược lại {@code code} — dùng để hiển thị {@code teacherId}. */
        public String publicId() {
            return (staffCode == null || staffCode.isBlank()) ? code : staffCode;
        }
    }

    // ───────────────────────── MF03 CF-M03-13 — classrooms (MF02) ─────────────────────────
    // Bổ sung THUẦN (không đổi hành vi cái đã có) theo 00_Common_Conventions.md §0-BIS mục
    // "Cần thêm method vào common/?" — Huy tự viết, Mạnh review (07_implement/00_Common_
    // Conventions.md, việc tồn đọng #4). Cùng khuôn cached pair schoolCode/schoolIdByCode vì
    // danh mục lớp cũng gần như tĩnh trong một request/response.

    private final Map<UUID, String> classroomCodeCache = new ConcurrentHashMap<>();

    /** @return {@code classrooms.code} ({@code "3A"}), hoặc {@code null}. D1/CF-M03-13. */
    @Transactional(readOnly = true)
    public String classroomCode(UUID classroomId) {
        if (classroomId == null) {
            return null;
        }
        return classroomCodeCache.computeIfAbsent(classroomId, id -> {
            var results = em.createNativeQuery("select code from classrooms where id = :id")
                    .setParameter("id", id)
                    .getResultList();
            return results.isEmpty() ? null : (String) results.get(0);
        });
    }

    /** Chiều ngược — {@code code} lớp → UUID nội bộ. Dùng khi nhận {@code classroomId} từ request. */
    @Transactional(readOnly = true)
    public Optional<UUID> classroomIdByCode(String code, UUID schoolId) {
        if (code == null || code.isBlank() || schoolId == null) {
            return Optional.empty();
        }
        var results = em.createNativeQuery("select id from classrooms where code = :code and school_id = :schoolId")
                .setParameter("code", code)
                .setParameter("schoolId", schoolId)
                .getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of((UUID) results.get(0));
    }

    /**
     * 13/08/2026 (theo Nam) — mã lớp CÒN HOẠT ĐỘNG của trường, sắp theo tên — dùng dựng
     * dropdown chọn lớp trong template Excel nhập học sinh hàng loạt (`StudentImportServiceImpl
     * .downloadTemplate()`). Cùng nguyên tắc THUẦN như 2 method trên: MF03 không import trực
     * tiếp entity/repository của MF02.
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public java.util.List<String> classroomCodesOfSchool(UUID schoolId) {
        if (schoolId == null) {
            return java.util.List.of();
        }
        var results = em.createNativeQuery(
                "select code from classrooms where school_id = :schoolId order by code")
                .setParameter("schoolId", schoolId)
                .getResultList();
        return (java.util.List<String>) results;
    }

    // ───────────────────────── MF03 CF-M03-13 — user_accounts (mã HS-PH tổng quát) ─────────────────────────
    // KHÔNG cache: khác userById (MF02, dùng cho homeroom_teacher_id) — ở đây phục vụ resolve
    // parentId trong JSON của MF03, đổi trạng thái/số dòng thường hơn danh mục trường/lớp.

    /** @return {@code user_accounts.code} ({@code "U-0005"}), hoặc {@code null}. D1/CF-M03-13. */
    @Transactional(readOnly = true)
    public String userCode(UUID userId) {
        if (userId == null) {
            return null;
        }
        var results = em.createNativeQuery("select code from user_accounts where id = :id")
                .setParameter("id", userId)
                .getResultList();
        return results.isEmpty() ? null : (String) results.get(0);
    }

    /** Chiều ngược — {@code code} → UUID nội bộ của {@code user_accounts}. */
    @Transactional(readOnly = true)
    public Optional<UUID> userIdByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return single("select id from user_accounts where code = :code", code).map(o -> (UUID) o);
    }

    // ───────────────────────── MF11 — bổ sung THUẦN (§0-BIS "Cần thêm method vào common/?") ─────────────────────────
    // M11-D19: lập biên bản vi phạm (BR-183) cần chọn ĐÚNG MỘT tài khoản đại diện Nhà thầu
    // (user_accounts.role=SERVICE_CATERERS, không phải partner) — GET /partners không đủ (trả
    // đối tác, không trả người). Native query THUẦN, không import entity MF01/MF04.
    //
    // Sửa 13/08/2026 (theo Nam) — đo thật: tài khoản SERVICE_CATERERS do System Admin tạo trực
    // tiếp (System Admin › Người dùng › Tạo tài khoản) chỉ có `school_id` (liên kết THẲNG tới
    // trường), KHÔNG có `partner_id` — `CreateUserRequest` (MF01) không có field đó, chỉ tự
    // đăng ký qua `POST /partners/register/caterer` (UC-067) mới set `partner_id`. Trước đây
    // điều kiện chỉ lọc `partner_id` nên tài khoản kiểu này (dù ACTIVE) không bao giờ hiện ra
    // được ở đây — đúng hiện tượng "còn active mà vẫn không chọn được".
    //
    // Sửa lần 2, cùng ngày — bug THẬT SỰ khiến dropdown LUÔN rỗng bất kể 2 điều kiện trên:
    // `roles.role_key` lưu dạng kebab-case THẬT (`"service-caterers"`, xem `RoleKey.
    // SERVICE_CATERERS.key()`), không phải SCREAMING_SNAKE (`"SERVICE_CATERERS"`) như literal
    // native query dùng — never match, kiểm bằng query trực tiếp trên DB (`select role_key
    // from roles` → toàn kebab-case). Sửa (1) ở trên đúng nhưng vô nghĩa vì (2) chặn trước.

    /** Đại diện Nhà thầu (role SERVICE_CATERERS, ACTIVE) của các đối tác đang hợp tác với trường. */
    @Transactional(readOnly = true)
    public java.util.List<UserBrief> serviceCatererRepresentatives(UUID schoolId) {
        if (schoolId == null) {
            return java.util.List.of();
        }
        var results = em.createNativeQuery("""
                select ua.code, ua.name
                from user_accounts ua
                join roles r on r.id = ua.role_id
                where r.role_key = 'service-caterers'
                  and ua.status = 'ACTIVE'
                  and (
                      ua.school_id = :schoolId
                      or ua.partner_id in (
                          select partner_id from partnership_applications where school_id = :schoolId
                          union
                          select partner_id from supplier_partnerships where school_id = :schoolId
                      )
                  )
                order by ua.name
                """)
                .setParameter("schoolId", schoolId)
                .getResultList();
        return results.stream().map(r -> {
            Object[] cols = (Object[]) r;
            return new UserBrief((String) cols[0], (String) cols[1]);
        }).toList();
    }

    public record UserBrief(String code, String name) {}

    // ───────────────────────── MF05 — bổ sung THUẦN (§0-BIS "Cần thêm method vào common/?") ─────────────────────────
    // M05-D14 (PDPD): BR-063 cần đếm dị ứng theo khối nhưng KHÔNG BAO GIỜ trả studentId/tên —
    // chỉ số đếm. M05-D14 chốt: mở rộng ReferenceResolver bằng native query, KHÔNG import
    // entity/repository của MF03 (ADR-M02-5/AP-AI-005). Mạnh review theo quy trình additive.
    //
    // §2.5 mục 7 của TDS-MF05: SpecialMealLookupPort (mf03/port/) CHƯA được MF03 tạo — sanctioned
    // fallback là native query tạm ở đây, ghi nợ rõ ràng, thay bằng Port thật khi MF03 có.

    /** BR-080/M05-D09 — trần chi phí của trường, chép vào {@code menus.cost_ceiling} lúc TẠO. */
    @Transactional(readOnly = true)
    public java.math.BigDecimal schoolCostCeiling(UUID schoolId) {
        if (schoolId == null) {
            return null;
        }
        var results = em.createNativeQuery("select cost_ceiling from school_config where school_id = :schoolId")
                .setParameter("schoolId", schoolId)
                .getResultList();
        return results.isEmpty() ? null : (java.math.BigDecimal) results.get(0);
    }

    /**
     * Chuẩn dinh dưỡng bữa trưa (BR-062) — 4 chỉ số + cờ đã cấu hình hay chưa.
     * Xem {@code com.sfms.mf02.service.NutritionTargetServiceImpl} và migration V54.
     */
    public record NutritionTargetValues(java.math.BigDecimal kcal, java.math.BigDecimal protein,
                                         java.math.BigDecimal fat, java.math.BigDecimal carb,
                                         boolean configured) {}

    /**
     * Giá trị áp dụng khi trường CHƯA cấu hình chuẩn cho khối đó — nhóm tuổi 6–11 (tiểu học).
     *
     * <p><b>Đây là nơi khai DUY NHẤT trong toàn hệ thống.</b> Trước 07/08/2026 con số này nằm
     * cứng ở {@code MealHistoryServiceImpl} (MF09) và lặp lại ở
     * {@code Frontend/src/lib/mock-data.ts NUTRITION_TARGET} — hai bản sao không có cách nào
     * biết nhau đã lệch. Mọi nơi cần chuẩn phải đi qua {@link #nutritionTargetOfStudent(UUID)}
     * hoặc {@code GET /api/v1/nutrition-targets}, KHÔNG khai lại hằng số.
     */
    public static final NutritionTargetValues DEFAULT_NUTRITION_TARGET = new NutritionTargetValues(
            java.math.BigDecimal.valueOf(715), java.math.BigDecimal.valueOf(28),
            java.math.BigDecimal.valueOf(22), java.math.BigDecimal.valueOf(95), false);

    /**
     * Chuẩn dinh dưỡng ÁP DỤNG cho một học sinh, tra theo khối của lớp em đang học
     * ({@code students → classrooms.grade_id → nutrition_targets}). Khối chưa cấu hình ⇒
     * {@link #DEFAULT_NUTRITION_TARGET}.
     *
     * <p>Vì sao đặt ở đây mà không inject {@code NutritionTargetService} của MF02: không
     * module nào trong dự án inject service của module khác — cross-module read đi qua lớp
     * này bằng native query. Giữ nguyên quy ước đó.
     */
    @Transactional(readOnly = true)
    public NutritionTargetValues nutritionTargetOfStudent(UUID studentId) {
        if (studentId == null) {
            return DEFAULT_NUTRITION_TARGET;
        }
        var results = em.createNativeQuery("""
                select nt.kcal, nt.protein, nt.fat, nt.carb
                from students s
                join classrooms c on c.id = s.classroom_id
                join nutrition_targets nt on nt.grade_id = c.grade_id
                where s.id = :studentId
                """)
                .setParameter("studentId", studentId)
                .getResultList();
        if (results.isEmpty()) {
            return DEFAULT_NUTRITION_TARGET;
        }
        Object[] row = (Object[]) results.get(0);
        return new NutritionTargetValues((java.math.BigDecimal) row[0], (java.math.BigDecimal) row[1],
                (java.math.BigDecimal) row[2], (java.math.BigDecimal) row[3], true);
    }

    /** V44 — nhãn hiển thị của {@code grades.name} (VD "Khối 3"), hoặc {@code null}. */
    @Transactional(readOnly = true)
    public String gradeLabel(UUID gradeId) {
        if (gradeId == null) {
            return null;
        }
        var results = em.createNativeQuery("select name from grades where id = :id")
                .setParameter("id", gradeId).getResultList();
        return results.isEmpty() ? null : (String) results.get(0);
    }

    /**
     * BR-063 — đếm HS đã xác minh dị ứng ({@code allergy_declarations.status='VERIFIED'}) theo
     * từng allergen, trong PHẠM VI một khối của một trường. CHỈ trả {@code allergen → SỐ ĐẾM},
     * KHÔNG BAO GIỜ {@code studentId}/tên (PDPD, M05-D14). V44 — {@code gradeId} là FK UUID thật
     * ({@code classrooms.grade_id}), so sánh trực tiếp, không còn cần tách số bằng regex.
     */
    @Transactional(readOnly = true)
    public Map<String, Integer> verifiedAllergenCountsByGrade(UUID schoolId, UUID gradeId) {
        if (schoolId == null || gradeId == null) {
            return Map.of();
        }
        var rows = em.createNativeQuery("""
                select ad.allergen, count(distinct ad.student_id)
                from allergy_declarations ad
                join students s on s.id = ad.student_id
                join classrooms c on c.id = s.classroom_id
                where s.school_id = :schoolId and c.grade_id = :gradeId and ad.status = 'VERIFIED'
                group by ad.allergen
                """)
                .setParameter("schoolId", schoolId)
                .setParameter("gradeId", gradeId)
                .getResultList();
        Map<String, Integer> out = new java.util.LinkedHashMap<>();
        for (Object row : rows) {
            Object[] cols = (Object[]) row;
            out.put((String) cols[0], ((Number) cols[1]).intValue());
        }
        return out;
    }

    /**
     * MF09 {@code DistributionPlanHandler} — {@code DietGroup.code} (danh mục cố định MF05) của
     * chế độ ăn ĐÃ DUYỆT gần nhất của một HS, khớp qua {@code match_diet} (case-insensitive) với
     * {@code special_meal_requests.dietary_type} (tự do, BR-220). {@code Optional.empty()} = HS
     * ăn thực đơn chung (không có chế độ ăn đặc biệt đã duyệt, hoặc {@code dietary_type} không
     * khớp {@code DietGroup} nào trong danh mục — coi như KHÔNG có nhóm, không phải lỗi ở đây).
     */
    @Transactional(readOnly = true)
    public Optional<String> approvedDietGroupCodeOfStudent(UUID studentId) {
        if (studentId == null) {
            return Optional.empty();
        }
        var results = em.createNativeQuery("""
                select dg.code
                from special_meal_requests smr
                join diet_groups dg on lower(dg.match_diet) = lower(smr.dietary_type)
                where smr.student_id = :id and smr.status = 'APPROVED'
                order by smr.requested_at desc limit 1
                """)
                .setParameter("id", studentId)
                .getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of((String) results.get(0));
    }

    /**
     * MF09 {@code DistributionPlanHandler} + MF05 {@code DietCoverageValidator} — true nếu menu
     * "ALL" hiện có ở (schoolId, date, grade, mealType) KHÔNG chứa món nào vi phạm
     * {@code exclude_allergens}/{@code exclude_ingredient_keywords} của {@code dietGroupCode} →
     * nhóm này dùng chung được menu Tiêu chuẩn, KHÔNG cần menu riêng (VD bữa chỉ có dưa hấu, ai
     * ăn cũng được — tránh ép nhân bản menu vô ích). {@code false} nếu có món vi phạm HOẶC menu
     * "ALL" chưa tồn tại cho slot đó (chưa có gì để "dùng chung").
     */
    @Transactional(readOnly = true)
    public boolean standardMenuSatisfiesDietGroup(UUID schoolId, java.time.LocalDate date, UUID gradeId,
                                                    com.sfms.common.enums.MealType mealType, String dietGroupCode) {
        if (schoolId == null || date == null || gradeId == null || mealType == null || dietGroupCode == null) {
            return false;
        }
        var allMenuRows = em.createNativeQuery("""
                select id from menus
                where school_id = :schoolId and menu_date = :date and grade_id = :gradeId
                  and meal_type = :mealType and audience = 'ALL' and status <> 'REJECTED'
                limit 1
                """)
                .setParameter("schoolId", schoolId)
                .setParameter("date", date)
                .setParameter("gradeId", gradeId)
                .setParameter("mealType", mealType.name())
                .getResultList();
        if (allMenuRows.isEmpty()) {
            return false;
        }
        UUID allMenuId = (UUID) allMenuRows.get(0);
        /*
         * Sửa 13/08/2026 (Nam: "nhưng tiêu chuẩn đâu thay thế được chế độ ăn đặc biệt?").
         *
         * Bản trước chỉ soi HAI thứ: `d.manual_allergens` và TÊN MÓN. Cả hai đều lọt đúng ca
         * nguy hiểm nhất:
         *   · `manual_allergens` theo đúng định nghĩa của nó chỉ là phần KHAI THÊM BẰNG TAY cho
         *     nguyên liệu ngoài danh mục (BR-063 — tác nhân chính được DẪN XUẤT từ thành phần),
         *     nên món khai thành phần tử tế lại có mảng này RỖNG;
         *   · từ khoá chỉ khớp `d.name`, mà tên món tiếng Việt hiếm khi chứa tên nguyên liệu
         *     phải kiêng — "Canh xương hầm" không có chữ nào khớp "lợn"/"heo".
         * Kết quả: một thực đơn Tiêu chuẩn có thịt lợn trong thành phần vẫn được tuyên bố là
         * "HALAL ăn được", và BR-065 thôi đòi thực đơn riêng cho nhóm đó.
         *
         * Nay quét THÊM `dish_ingredients.name` — đây là nơi thành phần thật sự nằm.
         */
        var violationRows = em.createNativeQuery("""
                select exists (
                    select 1
                    from menu_dishes md
                    join dishes d on d.id = md.dish_id
                    join diet_groups dg on dg.code = :dietGroupCode
                    where md.menu_id = :menuId
                      and (
                        exists (
                            select 1 from jsonb_array_elements_text(d.manual_allergens) da
                            join jsonb_array_elements_text(dg.exclude_allergens) ea
                              on lower(da) = lower(ea)
                        )
                        or exists (
                            select 1 from jsonb_array_elements_text(dg.exclude_ingredient_keywords) kw
                            where lower(d.name) like '%' || lower(kw) || '%'
                        )
                        or exists (
                            select 1
                            from dish_ingredients di
                            join jsonb_array_elements_text(dg.exclude_ingredient_keywords) kw
                              on lower(di.name) like '%' || lower(kw) || '%'
                            where di.dish_id = d.id
                        )
                        /*
                         * V74 (13/08/2026) — tác nhân dị ứng khai ở CẤP NGUYÊN LIỆU.
                         *
                         * Ba nhánh trên đều là so khớp CHUỖI (tên món, tên nguyên liệu) nên chỉ
                         * đúng khi người đặt tên nghĩ giống người viết từ khoá. Nhánh này đối chiếu
                         * DỮ LIỆU đã khai: nhóm "Không lactose" khai exclude_allergens = ["Sữa"],
                         * nguyên liệu "Sữa đặc có đường" khai allergens = ["Sữa bò"] — hai bên gặp
                         * nhau ở đây mà không cần tên chứa từ khoá nào.
                         */
                        or exists (
                            select 1
                            from dish_ingredients di
                            join catalog_ingredients ci on ci.code = di.ingredient_code
                            where di.dish_id = d.id
                              and exists (
                                  select 1
                                  from jsonb_array_elements_text(ci.allergens) ia
                                  join jsonb_array_elements_text(dg.exclude_allergens) ea
                                    on lower(ia) = lower(ea)
                              )
                        )
                      )
                )
                """)
                .setParameter("dietGroupCode", dietGroupCode)
                .setParameter("menuId", allMenuId)
                .getResultList();
        boolean hasViolation = !violationRows.isEmpty() && Boolean.TRUE.equals(violationRows.get(0));
        return !hasViolation;
    }

    /**
     * BR-065 — nhóm chế độ ăn ({@code special_meal_requests.dietary_type}, tự do/BR-220) ĐÃ
     * DUYỆT ({@code status='APPROVED'}) của một khối, kèm số HS. Thay tạm cho
     * {@code SpecialMealLookupPort.approvedDietsByGrade()} (mf03/port/, CHƯA tồn tại — TDS
     * §2.5 mục 7). CHỈ trả {@code dietaryType → SỐ ĐẾM}, không trả studentId/tên (PDPD).
     *
     * <p>⚠️ NỢ TÀI LIỆU: khi MF03 tạo {@code SpecialMealLookupPort} thật, thay lời gọi
     * phương thức này bằng inject Port đó — xem TDS-MF05_Backend.md §2.5 mục 7 và M05-D04.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> approvedSpecialDietStudentCountsByGrade(UUID schoolId, UUID gradeId) {
        if (schoolId == null || gradeId == null) {
            return Map.of();
        }
        var rows = em.createNativeQuery("""
                select smr.dietary_type, count(distinct smr.student_id)
                from special_meal_requests smr
                join students s on s.id = smr.student_id
                join classrooms c on c.id = s.classroom_id
                where s.school_id = :schoolId and c.grade_id = :gradeId and smr.status = 'APPROVED'
                group by smr.dietary_type
                """)
                .setParameter("schoolId", schoolId)
                .setParameter("gradeId", gradeId)
                .getResultList();
        Map<String, Long> out = new java.util.LinkedHashMap<>();
        for (Object row : rows) {
            Object[] cols = (Object[]) row;
            out.put((String) cols[0], ((Number) cols[1]).longValue());
        }
        return out;
    }

    /**
     * Trường mà một ĐỐI TÁC đang hợp tác hiệu lực — dùng để phạm vi hoá khi JWT không mang
     * {@code schoolId}. <b>Tài khoản đối tác KHÔNG BAO GIỜ có {@code school_id}</b> (xem
     * javadoc {@code AuthUser}): liên kết đi vòng
     * {@code user_accounts.partner_id → partners → supplier_partnerships → school_id}.
     * KHÔNG BAO GIỜ nhận {@code schoolId} từ body/query (D1/BR-013).
     *
     * <p><b>Sửa 08/08/2026 — thêm vế {@code until}</b>, cùng lý do như
     * {@link #approvedCatererPartnerId(UUID)}: hợp tác đã kết thúc mà chưa đổi status thì
     * đối tác CŨ vẫn resolve về trường đó và ghi được dữ liệu mới. Sau khi
     * BR-EXCLUSIVE-CATERER được enforce, đổi nhà thầu là thao tác bình thường nên tình huống
     * này thành thường gặp.
     *
     * <p>⚠️ <b>Hệ quả cần biết:</b> hợp tác kết thúc ⇒ hàm này trả {@code empty()} ⇒ đối tác
     * mất luôn quyền ĐỌC dữ liệu giai đoạn mình từng phục vụ (thực đơn, kiểm thực, mẫu lưu).
     * Đây là lựa chọn AN TOÀN TRƯỚC: không để một nhà thầu đã chấm dứt ghi đè dữ liệu của
     * nhà thầu mới. Nếu nghiệp vụ cần cho họ xem lại lịch sử, phải làm bằng một cơ chế
     * TƯỜNG MINH (endpoint đọc lịch sử theo khoảng {@code since..until}), chứ không phải
     * bằng cách nới lỏng hàm phạm vi này. Xem 08_E2E_Findings_Backlog.md mục D3.
     */
    @Transactional(readOnly = true)
    public Optional<UUID> activeSchoolIdForPartner(UUID partnerId) {
        if (partnerId == null) {
            return Optional.empty();
        }
        var results = em.createNativeQuery("""
                select school_id from supplier_partnerships
                where partner_id = :partnerId and status = 'APPROVED'
                  and (until is null or until >= current_date)
                order by since desc
                """)
                .setParameter("partnerId", partnerId)
                .setMaxResults(1)
                .getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of((UUID) results.get(0));
    }

    /**
     * MF05 — tài khoản Parent KHÔNG mang {@code schoolId} trong JWT (liên kết gián tiếp qua
     * {@code student_parents} → {@code students.school_id}, MF03). Dùng để phạm vi hoá
     * {@code GET /menus} khi Parent gọi (D11 — TC-SEC-015). Không phải hành vi mới, chỉ đọc.
     */
    @Transactional(readOnly = true)
    public Optional<UUID> firstSchoolIdForParent(UUID parentUserId) {
        if (parentUserId == null) {
            return Optional.empty();
        }
        var results = em.createNativeQuery("""
                select s.school_id from student_parents sp
                join students s on s.id = sp.student_id
                where sp.parent_user_id = :parentUserId
                order by s.school_id
                """)
                .setParameter("parentUserId", parentUserId)
                .setMaxResults(1)
                .getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of((UUID) results.get(0));
    }

    // ───────────────────────── MF06 — bổ sung THUẦN (§0-BIS "Cần thêm method vào common/?") ─────────────────────────
    // TDS-MF06_Backend.md §2.5 mục 2: PartnerStatusPort CHƯA tồn tại — dùng ReferenceResolver
    // (tra cứu thuần, native query) cho `partners.status`, KHÔNG import entity/repository MF04.
    // Additive only — không đổi hành vi các method khác.

    /**
     * BR-076 — mã nguyên liệu có nằm trong danh mục System Admin cấu hình không.
     *
     * <p>Dùng để chặn khai món bằng nguyên liệu ngoài danh mục. Thêm 08/08/2026 sau khi đo
     * DB thật: 10/14 dòng {@code dish_ingredients} có {@code ingredient_code} NULL, và cả hai
     * màn tính nhu cầu đều BỎ QUA dòng không có mã ⇒ nguyên liệu đó không đặt mua, không xuất
     * kho, không truy xuất được lô — mà không cảnh báo gì. FE đã chặn từ trước, nhưng gọi API
     * trực tiếp thì lọt.
     */
    @Transactional(readOnly = true)
    public boolean ingredientCodeExists(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        var results = em.createNativeQuery("select 1 from catalog_ingredients where code = :code")
                .setParameter("code", code.trim())
                .setMaxResults(1)
                .getResultList();
        return !results.isEmpty();
    }

    /** D1 — mã nghiệp vụ của nguyên liệu ({@code catalog_ingredients.code}), hoặc {@code null}. */
    @Transactional(readOnly = true)
    public String ingredientCode(UUID ingredientId) {
        if (ingredientId == null) {
            return null;
        }
        var results = em.createNativeQuery("select code from catalog_ingredients where id = :id")
                .setParameter("id", ingredientId).getResultList();
        return results.isEmpty() ? null : (String) results.get(0);
    }

    /** PROPOSED (plan "Đơn vị đo lường") — đơn vị chuẩn (baseUnit) của nguyên liệu, hoặc {@code null}. */
    @Transactional(readOnly = true)
    public String ingredientUnit(UUID ingredientId) {
        if (ingredientId == null) {
            return null;
        }
        var results = em.createNativeQuery("select unit from catalog_ingredients where id = :id")
                .setParameter("id", ingredientId).getResultList();
        return results.isEmpty() ? null : (String) results.get(0);
    }

    /** Nhãn hiển thị của nguyên liệu, hoặc {@code null}. */
    @Transactional(readOnly = true)
    public String ingredientName(UUID ingredientId) {
        if (ingredientId == null) {
            return null;
        }
        var results = em.createNativeQuery("select name from catalog_ingredients where id = :id")
                .setParameter("id", ingredientId).getResultList();
        return results.isEmpty() ? null : (String) results.get(0);
    }

    /** Chiều ngược — {@code code} → UUID nội bộ của {@code catalog_ingredients}. */
    @Transactional(readOnly = true)
    public Optional<UUID> ingredientIdByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return single("select id from catalog_ingredients where code = :code", code).map(o -> (UUID) o);
    }

    /** D1 — mã nghiệp vụ (slug) của món ({@code dishes.code}), hoặc {@code null}. MF06 C6. */
    @Transactional(readOnly = true)
    public String dishCode(UUID dishId) {
        if (dishId == null) {
            return null;
        }
        var results = em.createNativeQuery("select code from dishes where id = :id")
                .setParameter("id", dishId).getResultList();
        return results.isEmpty() ? null : (String) results.get(0);
    }

    /** BR-062 — kcal/protein/fat/carb THẬT của món (MF05 {@code dishes}), dùng cho nutrition-history. */
    public record DishNutrition(java.math.BigDecimal kcal, java.math.BigDecimal protein,
                                 java.math.BigDecimal fat, java.math.BigDecimal carb) {}

    @Transactional(readOnly = true)
    public DishNutrition dishNutrition(UUID dishId) {
        if (dishId == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        java.util.List<Object[]> rows = em.createNativeQuery(
                "select kcal, protein, fat, carb from dishes where id = :id")
                .setParameter("id", dishId).getResultList();
        if (rows.isEmpty()) {
            return null;
        }
        Object[] row = rows.get(0);
        return new DishNutrition((java.math.BigDecimal) row[0], (java.math.BigDecimal) row[1],
                (java.math.BigDecimal) row[2], (java.math.BigDecimal) row[3]);
    }

    /**
     * Tra {@code incidents.code} (MF11) theo UUID — MF06 dùng để trả `quarantine.incidentId`
     * ở `InventoryBatchResponse` đúng D1 (mã nghiệp vụ, không phải UUID nội bộ). Tra cứu
     * thuần, một cột — không cần Port riêng (§0-BIS việc 6).
     */
    @Transactional(readOnly = true)
    public String incidentCode(UUID incidentId) {
        if (incidentId == null) {
            return null;
        }
        var results = em.createNativeQuery("select code from incidents where id = :id")
                .setParameter("id", incidentId).getResultList();
        return results.isEmpty() ? null : (String) results.get(0);
    }

    /** @return mã tài xế (MF04 {@code delivery_persons.code}, VD {@code DP-03}), hoặc {@code null}. */
    @Transactional(readOnly = true)
    public String deliveryPersonCode(UUID deliveryPersonId) {
        if (deliveryPersonId == null) {
            return null;
        }
        var results = em.createNativeQuery("select code from delivery_persons where id = :id")
                .setParameter("id", deliveryPersonId).getResultList();
        return results.isEmpty() ? null : (String) results.get(0);
    }

    /**
     * BR-050/BR-051 — faceId THẬT đã đăng ký của tài xế gán cho chuyến giao {@code deliveryCode}
     * (JOIN {@code deliveries.driver_id} → {@code delivery_persons.face_id}), dùng để gọi
     * {@code FaceRecognitionPort.verify} — trước đây {@code DeliveryController} tự bịa chuỗi
     * {@code "mock-uuid-driver-" + code} thay vì tra faceId thật.
     *
     * @return {@code null} nếu chuyến giao chưa gán tài xế, hoặc tài xế chưa đăng ký sinh trắc
     *         học ({@code face_id IS NULL}, {@code biometricStatus != REGISTERED}).
     */
    @Transactional(readOnly = true)
    public String driverFaceIdForDelivery(String deliveryCode) {
        var results = em.createNativeQuery(
                "select dp.face_id from deliveries d join delivery_persons dp on dp.id = d.driver_id "
                        + "where d.code = :code")
                .setParameter("code", deliveryCode).getResultList();
        return results.isEmpty() ? null : (String) results.get(0);
    }

    /**
     * Tra UUID tài xế theo mã, trong phạm vi ĐÚNG đối tác (Supplier chỉ gán tài xế của
     * chính mình) — MF06 `DeliveryService.create()` dùng khi đăng ký chuyến giao.
     */
    @Transactional(readOnly = true)
    public UUID deliveryPersonIdByCode(String code, UUID partnerId) {
        if (code == null || code.isBlank() || partnerId == null) {
            return null;
        }
        var results = em.createNativeQuery(
                        "select id from delivery_persons where code = :code and partner_id = :partnerId")
                .setParameter("code", code).setParameter("partnerId", partnerId).getResultList();
        return results.isEmpty() ? null : (UUID) results.get(0);
    }

    @Transactional(readOnly = true)
    public String dishName(UUID dishId) {
        if (dishId == null) {
            return null;
        }
        var results = em.createNativeQuery("select name from dishes where id = :id")
                .setParameter("id", dishId).getResultList();
        return results.isEmpty() ? null : (String) results.get(0);
    }

    @Transactional(readOnly = true)
    public Optional<UUID> dishIdByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return single("select id from dishes where code = :code", code).map(o -> (UUID) o);
    }

    /** D1 — mã nghiệp vụ của thực đơn ({@code menus.code}), hoặc {@code null}. */
    @Transactional(readOnly = true)
    public String menuCode(UUID menuId) {
        if (menuId == null) {
            return null;
        }
        var results = em.createNativeQuery("select code from menus where id = :id")
                .setParameter("id", menuId).getResultList();
        return results.isEmpty() ? null : (String) results.get(0);
    }

    /**
     * BR-063 (V74, 13/08/2026) — HỢP các tác nhân dị ứng của một tập mã nguyên liệu
     * ({@code catalog_ingredients.code}, khoá mà {@code dish_ingredients.ingredient_code} trỏ tới).
     *
     * <p>Đây là thứ cho phép server tự dẫn xuất allergen của MÓN thay vì chỉ đọc
     * {@code dishes.manual_allergens}. Mã không có trong danh mục (nguyên liệu gõ tay, ngoài
     * danh mục) đơn giản không đóng góp gì — đúng như frontend, và đúng cảnh báo "Ngoài danh
     * mục — không truy xuất được lô, chưa nhận diện allergen" mà form nhập món vẫn hiện.
     */
    @Transactional(readOnly = true)
    public java.util.Set<String> allergensOfIngredientCodes(java.util.Collection<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return java.util.Set.of();
        }
        var rows = em.createNativeQuery("""
                select distinct jsonb_array_elements_text(allergens)
                from catalog_ingredients
                where code in (:codes)
                """)
                .setParameter("codes", codes)
                .getResultList();
        java.util.Set<String> out = new java.util.LinkedHashSet<>();
        for (Object row : rows) {
            if (row != null) {
                out.add(String.valueOf(row));
            }
        }
        return out;
    }

    /**
     * {@code menus.audience} của một thực đơn — {@code "ALL"} = thực đơn chung, còn lại là mã
     * nhóm chế độ ăn ({@code diet_groups.code}). Thêm 13/08/2026 cho {@code plannedDishes()}.
     *
     * <p>Vì sao Bảng nấu ăn cần: BR-082 bắt nấu suất chế độ ăn riêng ở NỒI RIÊNG, DỤNG CỤ RIÊNG
     * để tránh nhiễm chéo. Trước đây {@code PlannedDishRow} chỉ có tên món, nên gộp theo món là
     * trộn chung suất từ thực đơn riêng vào nồi lớn — bếp không còn cách nào biết phần nào phải
     * tách. Lưu ý đây là audience của THỰC ĐƠN mà em đó thực sự được định tuyến tới, KHÁC chế độ
     * ăn của bản thân em: em có chế độ ăn riêng vẫn có thể nhận thực đơn chung khi thực đơn chung
     * không vi phạm giới hạn của nhóm (xem {@code DistributionPlanHandler} nhánh 2) — lúc đó em
     * ăn nồi lớn thật, và bảng nấu phải đếm em vào nồi lớn.
     */
    @Transactional(readOnly = true)
    public String menuAudience(UUID menuId) {
        if (menuId == null) {
            return null;
        }
        var results = em.createNativeQuery("select audience from menus where id = :id")
                .setParameter("id", menuId).getResultList();
        return results.isEmpty() ? null : (String) results.get(0);
    }

    @Transactional(readOnly = true)
    public Optional<UUID> menuIdByCode(UUID schoolId, String code) {
        if (code == null || code.isBlank() || schoolId == null) {
            return Optional.empty();
        }
        var results = em.createNativeQuery("select id from menus where code = :code and school_id = :schoolId")
                .setParameter("code", code).setParameter("schoolId", schoolId).getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of((UUID) results.get(0));
    }

    /** BR-070 — trạng thái THẬT của đối tác (Partner.status) tại thời điểm gọi. */
    @Transactional(readOnly = true)
    public Optional<String> partnerStatus(UUID partnerId) {
        if (partnerId == null) {
            return Optional.empty();
        }
        var results = em.createNativeQuery("select status from partners where id = :id")
                .setParameter("id", partnerId).getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of((String) results.get(0));
    }

    /**
     * PROPOSED (chưa có BR/UC/TDS chính thức — xem {@code Proposal-Partnership-Catalog-
     * Contract.md} §Phase 2) — quy cách đóng gói của dòng danh mục ĐÃ DUYỆT (mf04
     * {@code partnership_catalog_items}) cho một cặp (school, ingredient) của một NCC.
     * Tra cứu thuần, KHÔNG import entity/repository mf04 (cùng khuôn các method MF06 khác
     * ở trên — "PartnerStatusPort CHƯA tồn tại").
     */
    /**
     * {@code conversionToBase}/{@code purchaseUnitFamily} bổ sung theo plan "Đơn vị đo
     * lường" (bản sửa: quy đổi bó/thùng↔baseUnit là của NCC, KHÔNG phải của nguyên liệu).
     * {@code purchaseUnitFamily} = {@code family} của {@code purchaseUnit} trong
     * {@code measure_units} (WEIGHT/VOLUME/COUNT), hoặc {@code null} nếu đơn vị không nằm
     * trong bảng tra cứu — dùng để quyết định có bắt buộc làm tròn nguyên đơn vị hay không
     * (COUNT luôn phải nguyên, VD không mua được "1.67 bó").
     */
    public record CatalogPackaging(java.math.BigDecimal packagingSize, boolean allowPartialPackage,
                                    java.math.BigDecimal conversionToBase, String purchaseUnitFamily) {}

    /**
     * Giờ nhận hàng THẬT của một lô kho (`InventoryBatch.receivingSessionId` — BR-073, server
     * ghi tại `ReceivingSessionService.create()`) — thêm 10/08/2026 (theo yêu cầu Nam): Bước 1
     * kiểm thực trước đây chỉ có `InventoryBatch.receivedDate` (LocalDate, không giờ) nên buộc
     * người kiểm phải TỰ CHỌN giờ nhận, dù hệ thống đã có sẵn giờ thật lúc ghi phiên nhận hàng.
     * {@code null} nếu lô không gắn phiên nhận nào (dữ liệu cũ trước khi có {@code receiving_
     * sessions}, hoặc lô tạo tay/điều chỉnh) — Bước 1 vẫn phải chấp nhận trống, không suy đại.
     */
    @Transactional(readOnly = true)
    public java.time.Instant receivingSessionReceivedAt(UUID receivingSessionId) {
        if (receivingSessionId == null) {
            return null;
        }
        var results = em.createNativeQuery("select received_at from receiving_sessions where id = :id")
                .setParameter("id", receivingSessionId)
                .getResultList();
        return results.isEmpty() || results.get(0) == null ? null : toInstant(results.get(0));
    }

    @Transactional(readOnly = true)
    public Optional<CatalogPackaging> partnershipCatalogPackaging(UUID partnerId, UUID schoolId, UUID ingredientId) {
        if (partnerId == null || schoolId == null || ingredientId == null) {
            return Optional.empty();
        }
        var results = em.createNativeQuery("""
                select pci.packaging_size, pci.allow_partial_package, pci.conversion_to_base, mu.family
                from partnership_catalog_items pci
                left join measure_units mu on mu.code = pci.unit
                where pci.partner_id = :partnerId and pci.school_id = :schoolId
                  and pci.ingredient_id = :ingredientId and pci.status = 'ACTIVE'
                order by pci.created_at desc limit 1
                """)
                .setParameter("partnerId", partnerId)
                .setParameter("schoolId", schoolId)
                .setParameter("ingredientId", ingredientId)
                .getResultList();
        if (results.isEmpty()) {
            return Optional.empty();
        }
        Object[] cols = (Object[]) results.get(0);
        return Optional.of(new CatalogPackaging((java.math.BigDecimal) cols[0], Boolean.TRUE.equals(cols[1]),
                (java.math.BigDecimal) cols[2], (String) cols[3]));
    }

    /**
     * Đơn giá theo HỢP ĐỒNG (quyết định của Nam, 10/08/2026 — bỏ vòng "NCC báo giá / trường
     * chấp nhận giá" khỏi {@code PurchaseRequestService}: giá đã bị khoá bởi hợp đồng nên
     * không còn gì để đàm phán, đơn lấy giá tự động ngay lúc tạo).
     *
     * <p>Nguồn ưu tiên là dòng danh mục hợp đồng ĐÃ DUYỆT ({@code partnership_catalog_items.
     * approved_unit_price}, xem {@link #partnershipCatalogPackaging}); nếu NCC×trường chưa có
     * dòng danh mục nào (hợp đồng chưa khai nguyên liệu này), rơi về bảng giá chung của NCC
     * ({@code supplier_products.unit_price}) — vẫn tốt hơn để trống.
     */
    @Transactional(readOnly = true)
    public Optional<java.math.BigDecimal> contractUnitPrice(UUID partnerId, UUID schoolId, UUID ingredientId) {
        if (partnerId == null || schoolId == null || ingredientId == null) {
            return Optional.empty();
        }
        var catalogResults = em.createNativeQuery("""
                select approved_unit_price from partnership_catalog_items
                where partner_id = :partnerId and school_id = :schoolId
                  and ingredient_id = :ingredientId and status = 'ACTIVE'
                order by created_at desc limit 1
                """)
                .setParameter("partnerId", partnerId)
                .setParameter("schoolId", schoolId)
                .setParameter("ingredientId", ingredientId)
                .getResultList();
        if (!catalogResults.isEmpty()) {
            return Optional.of((java.math.BigDecimal) catalogResults.get(0));
        }
        var productResults = em.createNativeQuery("""
                select unit_price from supplier_products
                where partner_id = :partnerId and ingredient_id = :ingredientId
                  and active = true and unit_price is not null
                order by created_at desc limit 1
                """)
                .setParameter("partnerId", partnerId)
                .setParameter("ingredientId", ingredientId)
                .getResultList();
        if (productResults.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of((java.math.BigDecimal) productResults.get(0));
    }

    /**
     * MF04 — nguyên liệu này có nằm trong DANH MỤC ĐÃ DUYỆT của cặp (nhà cung cấp × trường) không?
     *
     * <p>Thêm 08/08/2026 theo quyết định của Nam: đơn mua hàng BẮT BUỘC nằm trong danh mục hợp
     * đồng đã được trường duyệt. Trước đó {@code PurchaseRequestService.create()} chỉ resolve
     * {@code ingredientId} trong danh mục nguyên liệu toàn hệ thống, nên đặt được BẤT KỲ nguyên
     * liệu nào từ BẤT KỲ nhà cung cấp nào — kể cả thứ họ chưa từng đăng ký bán, chưa ai duyệt
     * giá. Đo thật cùng ngày: {@code partnership_catalog_items} rỗng hoàn toàn mà vẫn tạo được
     * đơn và nhập kho trọn vẹn.
     *
     * <p>Nguồn sự thật là {@code partnership_catalog_items} (qua luồng
     * {@code catalog-change-request} → trường duyệt), KHÔNG phải {@code supplier_products} —
     * bảng đó là bảng giá chung do NCC tự khai, không có cổng duyệt nào.
     */
    @Transactional(readOnly = true)
    public boolean isIngredientInApprovedCatalog(UUID partnerId, UUID schoolId, UUID ingredientId) {
        if (partnerId == null || schoolId == null || ingredientId == null) {
            return false;
        }
        var results = em.createNativeQuery("""
                select 1 from partnership_catalog_items
                where partner_id = :partnerId and school_id = :schoolId
                  and ingredient_id = :ingredientId and status = 'ACTIVE'
                  and (effective_from is null or effective_from <= current_date)
                limit 1
                """)
                .setParameter("partnerId", partnerId)
                .setParameter("schoolId", schoolId)
                .setParameter("ingredientId", ingredientId)
                .getResultList();
        return !results.isEmpty();
    }

    /** MF02/BR-028 — toạ độ trường + bán kính geofence (ADR-M06-7), mặc định 200m nếu chưa cấu hình. */
    public record SchoolGeofence(java.math.BigDecimal lat, java.math.BigDecimal lng, int radiusMeters) {}

    @Transactional(readOnly = true)
    public Optional<SchoolGeofence> schoolGeofence(UUID schoolId) {
        if (schoolId == null) {
            return Optional.empty();
        }
        var results = em.createNativeQuery("""
                select s.lat, s.lng, coalesce(sc.geofence_radius, 200)
                from schools s left join school_config sc on sc.school_id = s.id
                where s.id = :id
                """).setParameter("id", schoolId).getResultList();
        if (results.isEmpty()) {
            return Optional.empty();
        }
        Object[] cols = (Object[]) results.get(0);
        java.math.BigDecimal lat = (java.math.BigDecimal) cols[0];
        java.math.BigDecimal lng = (java.math.BigDecimal) cols[1];
        int radius = ((Number) cols[2]).intValue();
        return Optional.of(new SchoolGeofence(lat, lng, radius));
    }

    // ───────────────────────── MF07 — bổ sung THUẦN (§0-BIS "Cần thêm method vào common/?") ─────────────────────────
    // BR-092 (INSP-092): Bước 1 dùng lô quá HSD phải bị chặn. Native query thay vì import
    // entity/repository của MF06 (ADR-M02-5/AP-AI-005). Additive only.

    /**
     * D1 — MÃ LÔ ({@code inventory_batches.code}, VD {@code "BATCH-GF-4412"}) → UUID nội bộ.
     *
     * <p>Thêm 08/08/2026 (backlog A1). Trước đó MF07 nhận {@code batchId} từ FE rồi
     * {@code UUID.fromString()} thẳng; FE luôn gửi MÃ nên parse luôn ném, và hàm parse lại NUỐT
     * lỗi trả {@code null} ⇒ {@code step1_*_rows.batch_id} NULL suốt ⇒ BR-092/093 (chặn nguyên
     * liệu hết hạn vào bếp) chưa từng chạy lần nào.
     *
     * <p>Chấp nhận cả UUID thô để không vỡ dữ liệu/lời gọi cũ.
     */
    @Transactional(readOnly = true)
    public java.util.Optional<UUID> batchIdByCode(String raw, UUID schoolId) {
        if (raw == null || raw.isBlank()) {
            return java.util.Optional.empty();
        }
        try {
            return java.util.Optional.of(UUID.fromString(raw));
        } catch (IllegalArgumentException ignored) {
            // Không phải UUID ⇒ coi là mã nghiệp vụ, tra thật.
        }
        var results = schoolId == null
                ? em.createNativeQuery("select id from inventory_batches where code = :code")
                        .setParameter("code", raw).getResultList()
                : em.createNativeQuery("select id from inventory_batches where code = :code and school_id = :schoolId")
                        .setParameter("code", raw).setParameter("schoolId", schoolId).getResultList();
        return results.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of((UUID) results.get(0));
    }

    /** @return {@code inventory_batches.expiry_date}, hoặc {@code null} nếu lô không tồn tại. */
    @Transactional(readOnly = true)
    public java.time.LocalDate batchExpiryDate(UUID batchId) {
        if (batchId == null) {
            return null;
        }
        var results = em.createNativeQuery("select expiry_date from inventory_batches where id = :id")
                .setParameter("id", batchId).getResultList();
        return results.isEmpty() ? null : toLocalDate(results.get(0));
    }

    // ───────────────────────── MF08 — bổ sung THUẦN (§0-BIS "Cần thêm method vào common/?") ─────────────────────────
    // M08-D07: BR-105 (MF08) chỉ cần TRA CỨU THUẦN "phiên kiểm thực đã PASSED cả 3 bước chưa" —
    // MF07 đã merge (V12/V13, com.sfms.mf07) nhưng KHÔNG expose port nào cho việc này. Theo
    // §2.1 (02_Module_Completion_Workflow.md): tra cứu thuần đi qua ReferenceResolver, KHÔNG
    // dựng InspectionGatePort riêng (viết lại hạ tầng có sẵn = AP-AI-016). Additive only.

    /**
     * D1 — mã nghiệp vụ của lô kho ({@code inventory_batches.code}), hoặc {@code null}.
     *
     * <p>Chiều ngược của {@link #batchIdByCode(String, UUID)}. Thiếu nó nên
     * {@code InspectionAssembler} phải trả thẳng UUID cho {@code batchId} của Mẫu số 1 —
     * đúng cái lỗi D1 mà bản vá 05/08/2026 đã sửa cho {@code dishId} của Mẫu 2/3 nhưng bỏ sót
     * Mẫu 1 (đo được 08/08/2026: đọc lại phiên vừa nộp thấy
     * {@code batchId = "680bc2a7-…"} thay vì {@code "SB-THIT-HEO"}).
     */
    @Transactional(readOnly = true)
    public String batchCode(UUID batchId) {
        if (batchId == null) {
            return null;
        }
        var results = em.createNativeQuery("select code from inventory_batches where id = :id")
                .setParameter("id", batchId).getResultList();
        return results.isEmpty() ? null : (String) results.get(0);
    }

    /** D1 — mã nghiệp vụ của phiên kiểm thực ({@code inspection_sessions.code}), hoặc {@code null}. */
    @Transactional(readOnly = true)
    public String inspectionCode(UUID inspectionId) {
        if (inspectionId == null) {
            return null;
        }
        var results = em.createNativeQuery("select code from inspection_sessions where id = :id")
                .setParameter("id", inspectionId).getResultList();
        return results.isEmpty() ? null : (String) results.get(0);
    }

    /** Chiều ngược — {@code code} → UUID nội bộ của {@code inspection_sessions}, trong phạm vi trường. */
    @Transactional(readOnly = true)
    public Optional<UUID> inspectionIdByCode(String code, UUID schoolId) {
        if (code == null || code.isBlank() || schoolId == null) {
            return Optional.empty();
        }
        var results = em.createNativeQuery(
                        "select id from inspection_sessions where code = :code and school_id = :schoolId")
                .setParameter("code", code).setParameter("schoolId", schoolId).getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of((UUID) results.get(0));
    }

    /**
     * BR-105 (MF08 — cổng đăng ký mẫu lưu) — {@code true} khi phiên tồn tại, cùng trường,
     * VÀ cả 3 bước ({@code step1/step2/step3}) đều {@code PASSED} ({@code overall='PASSED'},
     * xem ADR-M07-02: {@code overall} là cột chốt một lần). Phiên không tồn tại hoặc khác
     * trường ⇒ {@code false} (MF08 tự map sang lỗi {@code SMP-105}/MSG-119).
     */
    @Transactional(readOnly = true)
    public boolean inspectionPassedAllSteps(UUID inspectionId, UUID schoolId) {
        if (inspectionId == null || schoolId == null) {
            return false;
        }
        var results = em.createNativeQuery(
                        "select overall from inspection_sessions where id = :id and school_id = :schoolId")
                .setParameter("id", inspectionId).setParameter("schoolId", schoolId).getResultList();
        return !results.isEmpty() && "PASSED".equals(results.get(0));
    }

    // ───────────────────────── MF09 (M09-D08) — bổ sung THUẦN, 02_Module_Completion_Workflow.md §7 ─────────────────────────
    // 5 port của TDS-MF09 §8.3 v1.0 (InspectionGatePort/StudentProfilePort/ClassroomPort/
    // ParentStudentLinkPort/CatererAssignmentPort) đều là tra cứu thuần -> gỡ, thêm 9 method
    // dưới đây (7 dự kiến trong TDS + classroomGrade + studentByCode, cần thêm để resolve
    // studentId/grade). MF07/MF02/MF03/MF04 đã merge -> KHÔNG stub, đọc bảng thật.

    /**
     * BR-094 (MF07) — cổng kiểm thực ba bước cho một (ngày, bữa, trường).
     *
     * @param blocked      không được phép phục vụ
     * @param reasonCode   {@code NO_SESSION} | {@code IN_PROGRESS} | {@code INSPECTION_FAILED},
     *                     {@code null} khi cho phép — khớp 1-1 với
     *                     {@code ServingGateResponse.reasonCode} của {@code GET /inspections/serving-gate}
     * @param failedSteps  tên bước FAILED (rỗng nếu chưa có bước nào hỏng)
     */
    public record InspectionGate(boolean blocked, String reasonCode, java.util.List<String> failedSteps,
                                  UUID sessionId, String sessionCode) {}

    /**
     * BR-094 — cổng chặn phục vụ, dùng chung cho MF09 ({@code doServe}/{@code confirm}).
     *
     * <p><b>Siết lại 07/08/2026 (Nam chốt).</b> Trước đó hàm này chỉ chặn khi có bước
     * {@code FAILED}; không có phiên ({@code Optional.empty()}) hoặc phiên đang dở
     * ({@code overall} chưa {@code PASSED}) đều **KHÔNG chặn** — tức là chưa kiểm thực xong
     * vẫn phát được suất, trong khi {@code GET /inspections/serving-gate} lại trả
     * {@code allowed:false} cho cả ba. Hai đường cùng mang danh "BR-094" mà nói ngược nhau,
     * và FE đã phải né theo đường lỏng hơn.
     *
     * <p>Nay ngữ nghĩa khớp ĐÚNG {@code InspectionServiceImpl.checkGate()}: chỉ
     * {@code overall = PASSED} mới cho phục vụ. Sửa {@code checkGate} thì phải sửa cả đây.
     *
     * <p>Trả {@link Optional#empty()} chỉ khi tham số thiếu — KHÔNG dùng {@code empty()} để
     * diễn đạt "không có phiên" nữa (đó chính là chỗ fail-open cũ); không có phiên là
     * {@code blocked=true, reasonCode=NO_SESSION}.
     */
    @Transactional(readOnly = true)
    public Optional<InspectionGate> inspectionGate(java.time.LocalDate date, String mealType, UUID schoolId) {
        if (date == null || mealType == null || schoolId == null) {
            return Optional.empty();
        }
        // ORDER BY created_at DESC — phiên MỚI NHẤT, khớp `checkGate`. Trước đây không sắp
        // xếp nên trường có ≥2 nhà thầu, hoặc có phiên tái kiểm (`reinspect` tạo phiên MỚI),
        // sẽ lấy phiên NGẪU NHIÊN và hai đường đọc ra hai kết quả khác nhau.
        var results = em.createNativeQuery("""
                select id, code, step1, step2, step3, overall
                from inspection_sessions
                where school_id = :schoolId and date = :date and meal_type = :mealType
                order by created_at desc
                """)
                .setParameter("schoolId", schoolId)
                .setParameter("date", date)
                .setParameter("mealType", mealType)
                .getResultList();
        if (results.isEmpty()) {
            return Optional.of(new InspectionGate(true, "NO_SESSION", java.util.List.of(), null, null));
        }
        Object[] cols = (Object[]) results.get(0);
        java.util.List<String> failed = new java.util.ArrayList<>();
        if ("FAILED".equals(cols[2])) {
            failed.add("Bước 1");
        }
        if ("FAILED".equals(cols[3])) {
            failed.add("Bước 2");
        }
        if ("FAILED".equals(cols[4])) {
            failed.add("Bước 3");
        }
        UUID sessionId = (UUID) cols[0];
        String sessionCode = (String) cols[1];
        if ("PASSED".equals(cols[5])) {
            return Optional.of(new InspectionGate(false, null, failed, sessionId, sessionCode));
        }
        String reason = "FAILED".equals(cols[5]) || !failed.isEmpty() ? "INSPECTION_FAILED" : "IN_PROGRESS";
        return Optional.of(new InspectionGate(true, reason, failed, sessionId, sessionCode));
    }

    /**
     * BR-105 mở rộng (10/08/2026, Nam) — "1 bữa chỉ được phát khi hoàn tất kiểm thực + lưu
     * mẫu đầy đủ, chưa lưu mẫu chưa được phân phát". Trước đây chỉ chặn theo kết quả kiểm
     * thực ({@link #inspectionGate}) — bếp giao suất/giáo viên phát suất được ngay cả khi
     * CHƯA lưu mẫu món nào, dù QĐ 1246 (BR-105/107) bắt buộc lưu mẫu trước khi phục vụ.
     *
     * <p>Danh sách món "cần lưu mẫu" của phiên lấy từ {@code step3_rows} (Mẫu số 3 — đúng
     * món thực tế được chia), lùi về {@code step2_rows} nếu Bước 3 chưa có dòng nào — CÙNG
     * logic {@code dishChoicesOf()} phía frontend (samples/register), để hai bên không lệch
     * danh sách "món cần mẫu".
     *
     * @return {@link Optional#empty()} chỉ khi không tìm được phiên kiểm thực khớp (ngày/bữa/
     *         trường) — khi đó {@link #inspectionGate} cũng đã chặn từ trước, không cần chặn
     *         kép. {@code blocked=true} kèm danh sách món còn thiếu mẫu khi có phiên nhưng
     *         chưa lưu đủ.
     */
    public record SampleRetentionGate(boolean blocked, java.util.List<String> missingDishNames) {}

    @Transactional(readOnly = true)
    public Optional<SampleRetentionGate> sampleRetentionGate(java.time.LocalDate date, String mealType, UUID schoolId) {
        if (date == null || mealType == null || schoolId == null) {
            return Optional.empty();
        }
        var sessionRows = em.createNativeQuery("""
                select id from inspection_sessions
                where school_id = :schoolId and date = :date and meal_type = :mealType
                order by created_at desc
                """)
                .setParameter("schoolId", schoolId)
                .setParameter("date", date)
                .setParameter("mealType", mealType)
                .getResultList();
        if (sessionRows.isEmpty()) {
            return Optional.empty();
        }
        UUID sessionId = (UUID) sessionRows.get(0);

        var step3 = em.createNativeQuery("""
                select distinct r.dish_id, d.name from step3_rows r join dishes d on d.id = r.dish_id
                where r.session_id = :sessionId and r.dish_id is not null
                """).setParameter("sessionId", sessionId).getResultList();
        var requiredRows = !step3.isEmpty() ? step3 : em.createNativeQuery("""
                select distinct r.dish_id, d.name from step2_rows r join dishes d on d.id = r.dish_id
                where r.session_id = :sessionId and r.dish_id is not null
                """).setParameter("sessionId", sessionId).getResultList();
        if (requiredRows.isEmpty()) {
            return Optional.of(new SampleRetentionGate(false, java.util.List.of()));
        }

        var sampledIds = em.createNativeQuery("select distinct dish_id from food_samples where inspection_id = :sessionId")
                .setParameter("sessionId", sessionId).getResultList();
        java.util.Set<UUID> sampled = new java.util.HashSet<>();
        for (Object o : sampledIds) sampled.add((UUID) o);

        java.util.List<String> missing = new java.util.ArrayList<>();
        for (Object row : requiredRows) {
            Object[] cols = (Object[]) row;
            UUID dishId = (UUID) cols[0];
            if (!sampled.contains(dishId)) {
                missing.add((String) cols[1]);
            }
        }
        return Optional.of(new SampleRetentionGate(!missing.isEmpty(), missing));
    }

    /** BR-031/BR-123 (MF03) — allergen ĐÃ XÁC MINH của một học sinh. */
    @Transactional(readOnly = true)
    public java.util.List<String> verifiedAllergensOfStudent(UUID studentId) {
        if (studentId == null) {
            return java.util.List.of();
        }
        var results = em.createNativeQuery(
                        "select allergen from allergy_declarations where student_id = :id and status = 'VERIFIED'")
                .setParameter("id", studentId)
                .getResultList();
        java.util.List<String> out = new java.util.ArrayList<>();
        for (Object r : results) {
            out.add((String) r);
        }
        return out;
    }

    /** BR-066 (MF03) — chế độ ăn đặc biệt ĐÃ DUYỆT gần nhất của một học sinh, nếu có. */
    @Transactional(readOnly = true)
    public Optional<String> specialDietOfStudent(UUID studentId) {
        if (studentId == null) {
            return Optional.empty();
        }
        // V4__mf03_student_parent.sql: bảng KHÔNG có cột `created_at` (không extends BaseEntity,
        // D9 chỉ áp cho entity JPA thường) — cột mốc thời gian thật là `requested_at`. Bug phát
        // hiện 05/08/2026 khi nối FE `student-distributions`: mọi lời gọi `list()` từng luôn
        // 500 SRV-500 do query này (chạy cho MỌI dòng trả về, không phải case hiếm).
        var results = em.createNativeQuery("""
                select dietary_type from special_meal_requests
                where student_id = :id and status = 'APPROVED'
                order by requested_at desc limit 1
                """)
                .setParameter("id", studentId)
                .getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of((String) results.get(0));
    }

    /** BR-120 (MF02) — {@code teacherUserId} có phải GVCN của {@code classroomId} không. */
    @Transactional(readOnly = true)
    public boolean isHomeroomTeacher(UUID classroomId, UUID teacherUserId) {
        if (classroomId == null || teacherUserId == null) {
            return false;
        }
        var results = em.createNativeQuery(
                        "select 1 from classrooms where id = :classroomId and homeroom_teacher_id = :teacherId")
                .setParameter("classroomId", classroomId)
                .setParameter("teacherId", teacherUserId)
                .getResultList();
        return !results.isEmpty();
    }

    /** V44 — {@code classrooms.grade_id} (UUID, MF02) của một lớp — khớp trực tiếp với
     *  {@code menus.grade_id}, không còn cần tách số bằng regex. */
    @Transactional(readOnly = true)
    public UUID classroomGrade(UUID classroomId) {
        if (classroomId == null) {
            return null;
        }
        var results = em.createNativeQuery("select grade_id from classrooms where id = :id")
                .setParameter("id", classroomId)
                .getResultList();
        return results.isEmpty() ? null : (UUID) results.get(0);
    }

    /** BR-201 (MF03) — {@code parentUserId} đã liên kết với {@code studentId} chưa. */
    @Transactional(readOnly = true)
    public boolean isParentLinkedToStudent(UUID parentUserId, UUID studentId) {
        if (parentUserId == null || studentId == null) {
            return false;
        }
        var results = em.createNativeQuery(
                        "select 1 from student_parents where parent_user_id = :parentId and student_id = :studentId")
                .setParameter("parentId", parentUserId)
                .setParameter("studentId", studentId)
                .getResultList();
        return !results.isEmpty();
    }

    /** BR-201 (MF03) — toàn bộ HS đã liên kết với một tài khoản phụ huynh (≤5). */
    @Transactional(readOnly = true)
    public java.util.List<UUID> linkedStudentIds(UUID parentUserId) {
        if (parentUserId == null) {
            return java.util.List.of();
        }
        var results = em.createNativeQuery("select student_id from student_parents where parent_user_id = :id")
                .setParameter("id", parentUserId)
                .getResultList();
        java.util.List<UUID> out = new java.util.ArrayList<>();
        for (Object r : results) {
            out.add((UUID) r);
        }
        return out;
    }

    /** D1 (MF03) — hồ sơ tối thiểu để snapshot lúc tạo phiên phát suất (ADR-M09-1). */
    public record StudentRef(UUID id, String code, String name, UUID classroomId, UUID schoolId) {}

    /**
     * Tra {@code students} theo mã, KHÔNG giới hạn trường — dùng cho Parent (account
     * {@code school_id IS NULL}, không có phạm vi trường sẵn để lọc trước). Giới hạn đã biết:
     * {@code code} chỉ UNIQUE trong phạm vi {@code (school_id, code)} ở DB, hàm này lấy dòng
     * đầu tiên khớp — chấp nhận được vì HS trùng {@code code} khác trường là biến cố hiếm và
     * bước sau vẫn kiểm {@code isParentLinkedToStudent} trước khi trả dữ liệu.
     */
    @Transactional(readOnly = true)
    public Optional<StudentRef> studentByCodeAnySchool(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        var results = em.createNativeQuery(
                        "select id, code, name, classroom_id, school_id from students where code = :code limit 1")
                .setParameter("code", code)
                .getResultList();
        if (results.isEmpty()) {
            return Optional.empty();
        }
        Object[] cols = (Object[]) results.get(0);
        return Optional.of(new StudentRef((UUID) cols[0], (String) cols[1], (String) cols[2],
                (UUID) cols[3], (UUID) cols[4]));
    }

    /** Chiều {@code UUID -> students.code} — dùng khi trả JSON {@code studentId} (D1). */
    @Transactional(readOnly = true)
    public String studentCode(UUID studentId) {
        if (studentId == null) {
            return null;
        }
        var results = em.createNativeQuery("select code from students where id = :id")
                .setParameter("id", studentId)
                .getResultList();
        return results.isEmpty() ? null : (String) results.get(0);
    }

    /** Tra {@code students} theo mã nghiệp vụ (JSON gửi lên là {@code code}, D1), trong phạm vi trường. */
    @Transactional(readOnly = true)
    public Optional<StudentRef> studentByCode(String code, UUID schoolId) {
        if (code == null || code.isBlank() || schoolId == null) {
            return Optional.empty();
        }
        var results = em.createNativeQuery(
                        "select id, code, name, classroom_id from students where code = :code and school_id = :schoolId")
                .setParameter("code", code)
                .setParameter("schoolId", schoolId)
                .getResultList();
        if (results.isEmpty()) {
            return Optional.empty();
        }
        Object[] cols = (Object[]) results.get(0);
        return Optional.of(new StudentRef((UUID) cols[0], (String) cols[1], (String) cols[2],
                (UUID) cols[3], schoolId));
    }

    /** BR-120 — các lớp mà một GV đang làm GVCN (thường đúng 1, BR-020 partial unique index). */
    @Transactional(readOnly = true)
    public java.util.List<UUID> classroomIdsOfHomeroomTeacher(UUID teacherUserId) {
        if (teacherUserId == null) {
            return java.util.List.of();
        }
        var results = em.createNativeQuery("select id from classrooms where homeroom_teacher_id = :teacherId")
                .setParameter("teacherId", teacherUserId)
                .getResultList();
        java.util.List<UUID> out = new java.util.ArrayList<>();
        for (Object r : results) {
            out.add((UUID) r);
        }
        return out;
    }

    /** UC-103 (BR-123) — toàn bộ HS ACTIVE của một lớp, dùng để quét cảnh báo dị ứng theo lớp. */
    @Transactional(readOnly = true)
    public java.util.List<StudentRef> studentsOfClassroom(UUID classroomId) {
        if (classroomId == null) {
            return java.util.List.of();
        }
        var results = em.createNativeQuery("""
                select id, code, name, classroom_id, school_id from students
                where classroom_id = :classroomId and status = 'ACTIVE'
                """)
                .setParameter("classroomId", classroomId)
                .getResultList();
        java.util.List<StudentRef> out = new java.util.ArrayList<>();
        for (Object row : results) {
            Object[] cols = (Object[]) row;
            out.add(new StudentRef((UUID) cols[0], (String) cols[1], (String) cols[2],
                    (UUID) cols[3], (UUID) cols[4]));
        }
        return out;
    }

    /** ADR-M09-8 (MF04) — NCC suất ăn đang có hợp đồng phục vụ trường tại một ngày. */
    public record CatererRef(UUID partnerId, String code, String name) {}

    @Transactional(readOnly = true)
    public Optional<CatererRef> currentCatererOf(UUID schoolId, java.time.LocalDate date) {
        if (schoolId == null || date == null) {
            return Optional.empty();
        }
        var results = em.createNativeQuery("""
                select p.id, p.code, p.name
                from supplier_partnerships sp
                join partners p on p.id = sp.partner_id
                where sp.school_id = :schoolId and sp.status = 'APPROVED'
                  and sp.since <= :date and (sp.until is null or sp.until >= :date)
                order by sp.since desc limit 1
                """)
                .setParameter("schoolId", schoolId)
                .setParameter("date", date)
                .getResultList();
        if (results.isEmpty()) {
            return Optional.empty();
        }
        Object[] cols = (Object[]) results.get(0);
        return Optional.of(new CatererRef((UUID) cols[0], (String) cols[1], (String) cols[2]));
    }

    /**
     * MF09 {@code DistributionPlanHandler} — toàn bộ lớp của một khối trong một trường, dùng
     * để sinh phiên phát suất {@code PENDING} khi MF05 phát {@code MenuPublished}. V44 —
     * {@code gradeId} là FK UUID thật, so sánh trực tiếp. Additive-only.
     */
    @Transactional(readOnly = true)
    public java.util.List<UUID> classroomIdsOfGrade(UUID schoolId, UUID gradeId) {
        if (schoolId == null || gradeId == null) {
            return java.util.List.of();
        }
        var results = em.createNativeQuery(
                        "select id from classrooms where school_id = :schoolId and grade_id = :gradeId")
                .setParameter("schoolId", schoolId)
                .setParameter("gradeId", gradeId)
                .getResultList();
        java.util.List<UUID> out = new java.util.ArrayList<>();
        for (Object r : results) {
            out.add((UUID) r);
        }
        return out;
    }

    /** MF05 {@code MenuServiceImpl} — tổng số HS ACTIVE của một khối, dùng để suy `servings`
     *  của menu "ALL" (grade total − nhóm chế độ ăn không dùng chung được ALL). */
    @Transactional(readOnly = true)
    public long gradeHeadcount(UUID schoolId, UUID gradeId) {
        if (schoolId == null || gradeId == null) {
            return 0;
        }
        var results = em.createNativeQuery("""
                select count(*) from students s join classrooms c on c.id = s.classroom_id
                where s.school_id = :schoolId and c.grade_id = :gradeId and s.status = 'ACTIVE'
                """)
                .setParameter("schoolId", schoolId)
                .setParameter("gradeId", gradeId)
                .getResultList();
        return results.isEmpty() ? 0 : ((Number) results.get(0)).longValue();
    }

    // ───────────────────────── MF10 (M10-D06) — bổ sung THUẦN, 02_Module_Completion_Workflow.md §7 ─────────────────────────
    // `MealServingPort` của TDS-MF10 v1.0 là TRA CỨU THUẦN trên student_distributions (MF09,
    // đã merge V16/V17) — gỡ port, thêm 2 method dưới đây (tiền lệ M08-D08/M09-D08). Additive only.

    /** MF03 (MF10 D12/L9-15) — tên hiển thị của HS theo UUID, hoặc {@code null}. */
    @Transactional(readOnly = true)
    public String studentName(UUID studentId) {
        if (studentId == null) {
            return null;
        }
        var results = em.createNativeQuery("select name from students where id = :id")
                .setParameter("id", studentId).getResultList();
        return results.isEmpty() ? null : (String) results.get(0);
    }

    /** D1 (MF10) — {@code code} của {@code student_distributions} → UUID nội bộ, trong phạm vi trường. */
    @Transactional(readOnly = true)
    public Optional<UUID> distributionIdByCode(String code, UUID schoolId) {
        if (code == null || code.isBlank() || schoolId == null) {
            return Optional.empty();
        }
        var results = em.createNativeQuery(
                        "select id from student_distributions where code = :code and school_id = :schoolId")
                .setParameter("code", code).setParameter("schoolId", schoolId).getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of((UUID) results.get(0));
    }

    /** D1 (MF10) — {@code student_distributions.code} của một UUID nội bộ, hoặc {@code null}. */
    @Transactional(readOnly = true)
    public String distributionCode(UUID distributionId) {
        if (distributionId == null) {
            return null;
        }
        var results = em.createNativeQuery("select code from student_distributions where id = :id")
                .setParameter("id", distributionId).getResultList();
        return results.isEmpty() ? null : (String) results.get(0);
    }

    /** BR-139 (MF10) — mốc phục vụ của một phiên phát suất; empty nếu chưa phục vụ (status≠SERVED). */
    @Transactional(readOnly = true)
    public Optional<java.time.Instant> servedAtOf(UUID distributionId) {
        if (distributionId == null) {
            return Optional.empty();
        }
        var results = em.createNativeQuery("select served_at from student_distributions where id = :id")
                .setParameter("id", distributionId)
                .getResultList();
        if (results.isEmpty() || results.get(0) == null) {
            return Optional.empty();
        }
        return Optional.of(toInstant(results.get(0)));
    }

    /** Hibernate 7/Postgres driver trả {@code TIMESTAMPTZ} dưới dạng {@link Instant} hoặc
     *  {@link java.sql.Timestamp} tuỳ đường truy vấn — chấp nhận cả hai, KHÔNG ép cứng 1 kiểu. */
    private static java.time.Instant toInstant(Object raw) {
        return raw instanceof java.time.Instant instant ? instant : ((java.sql.Timestamp) raw).toInstant();
    }

    /**
     * Native query trả cột {@code date} về kiểu nào là do driver/Hibernate quyết định —
     * PostgreSQL JDBC hiện trả thẳng {@link java.time.LocalDate}, KHÔNG phải
     * {@link java.sql.Date}. Ép cứng một kiểu là {@code ClassCastException} lúc chạy.
     *
     * <p><b>Đo được 08/08/2026:</b> {@code batchExpiryDate()} ép {@code (java.sql.Date)} nên
     * {@code POST /inspections/{code}/steps/1} ném <b>500</b> ngay khi Bước 1 khai một
     * {@code batchId} CÓ THẬT — tức đúng con đường BR-092/093 vừa mở. Lô không tồn tại thì
     * {@code batchId = null} nên không ai chạm vào lỗi này ⇒ nó ẩn suốt.
     *
     * <p>Cùng cách xử lý đã có sẵn ở {@code mf11/port/InventoryBatchPortImpl.toLocalDate()}.
     */
    private static java.time.LocalDate toLocalDate(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof java.time.LocalDate d) {
            return d;
        }
        if (raw instanceof java.sql.Date d) {
            return d.toLocalDate();
        }
        return toInstant(raw).atZone(java.time.ZoneOffset.UTC).toLocalDate();
    }

    /** BR-139 (MF10) — hồ sơ tối thiểu của một phiên phát suất ĐÃ phục vụ, còn trong cửa sổ phản hồi. */
    public record ServedMealRef(UUID id, String code, java.time.LocalDate date, String mealType,
                                java.time.Instant servedAt, UUID menuId) {}

    /** BR-139 (MF10) — các bữa ĐÃ phục vụ của một HS còn trong cửa sổ {@code window} tính từ {@code served_at}. */
    @Transactional(readOnly = true)
    public java.util.List<ServedMealRef> servedMealsWithin(UUID studentId, java.time.Duration window) {
        if (studentId == null || window == null) {
            return java.util.List.of();
        }
        java.time.Instant cutoff = java.time.Instant.now().minus(window);
        var results = em.createNativeQuery("""
                select id, code, date, meal_type, served_at, menu_id
                from student_distributions
                where student_id = :studentId and status = 'SERVED' and served_at >= :cutoff
                order by served_at desc
                """)
                .setParameter("studentId", studentId)
                .setParameter("cutoff", java.sql.Timestamp.from(cutoff))
                .getResultList();
        java.util.List<ServedMealRef> out = new java.util.ArrayList<>();
        for (Object row : results) {
            Object[] cols = (Object[]) row;
            out.add(new ServedMealRef((UUID) cols[0], (String) cols[1],
                    toLocalDate(cols[2]), (String) cols[3],
                    toInstant(cols[4]), (UUID) cols[5]));
        }
        return out;
    }

    // ───────────────────────── MF13 (M13-D07a) — bổ sung THUẦN, 02_Module_Completion_Workflow.md §7 ─────────────────────────
    // SchoolConfigPort của TDS-MF13 v1.0 là TRA CỨU THUẦN trên school_config.report_emails
    // (MF02, đã merge V3) -> gỡ port, thêm 1 method dưới đây (tiền lệ M08-D08/M09-D08/M10-D06).
    // Additive only — không đổi hành vi các method khác.

    /**
     * BR-179 — người nhận email của lịch báo cáo định kỳ, đọc {@code school_config.report_emails}
     * (JSONB mảng chuỗi). Rỗng nếu trường chưa cấu hình. §4.3 Email egress: người nhận CHỈ đến
     * từ đây, KHÔNG BAO GIỜ từ request.
     */
    @Transactional(readOnly = true)
    public java.util.List<String> reportEmails(UUID schoolId) {
        if (schoolId == null) {
            return java.util.List.of();
        }
        var results = em.createNativeQuery(
                        "select report_emails::text from school_config where school_id = :schoolId")
                .setParameter("schoolId", schoolId)
                .getResultList();
        if (results.isEmpty() || results.get(0) == null) {
            return java.util.List.of();
        }
        String json = (String) results.get(0);
        java.util.List<String> emails = new java.util.ArrayList<>();
        try {
            com.fasterxml.jackson.databind.JsonNode node =
                    new com.fasterxml.jackson.databind.ObjectMapper().readTree(json);
            node.forEach(n -> emails.add(n.asText()));
        } catch (Exception e) {
            return java.util.List.of();
        }
        return emails;
    }
}
