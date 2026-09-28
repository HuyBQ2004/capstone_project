package com.sfms.common.query;

import com.sfms.common.dto.PagedResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Tham số danh sách dùng chung — dịch bảng query param của Team Plan <b>§2b-2</b>.
 *
 * <p>Các <b>key điều khiển</b> (không được dùng làm lọc field):
 * {@code page}, {@code pageSize}, {@code search}, {@code dateFrom}, {@code dateTo},
 * {@code mealPrefix}.
 *
 * <p>⚠️ {@code step}/{@code stepStatus} <b>KHÔNG hiện thực</b> — grep toàn FE ra 0 call-site
 * (MF07 CF-17, xác nhận lại 30/07/2026). Mock vẫn giữ 2 key đó trong {@code control} set;
 * backend không cần.
 *
 * @param page      1-indexed, clamp ≥ 1
 * @param pageSize  clamp ≥ 1, mặc định 8
 * @param search    tìm mờ trên {@code SEARCH_FIELDS} của module (ILIKE), rỗng ⇒ bỏ qua
 * @param filters   phần còn lại của query string: lọc exact + {@code not:<field>}
 */
public record ListQuery(
        int page,
        int pageSize,
        String search,
        LocalDate dateFrom,
        LocalDate dateTo,
        String mealPrefix,
        Map<String, String> filters
) {

    /** Khớp hằng {@code control} của {@code mock-server.ts}, trừ {@code step}/{@code stepStatus}. */
    public static final Set<String> CONTROL_KEYS =
            Set.of("page", "pageSize", "search", "dateFrom", "dateTo", "mealPrefix");

    /**
     * Trần {@code pageSize} (13/08/2026) — trước đây chỉ clamp cận DƯỚI ({@code max(1, …)}),
     * cận trên bỏ ngỏ nên {@code ?pageSize=100000} là một câu SELECT không giới hạn thật sự,
     * ai gọi cũng được. 500 chọn để KHÔNG phá client hiện có (chỗ lớn nhất đang dùng 300 —
     * {@code supplier-products}), chỉ chặn giá trị vô lý.
     *
     * <p>CLAMP chứ không ném 422: đây là hạ tầng dùng chung, ném lỗi sẽ làm gãy các màn đang
     * chạy nếu có chỗ nào lỡ truyền lớn hơn. Client cần nhiều hơn 500 bản ghi thì thứ họ thiếu
     * là một endpoint TỔNG HỢP, không phải một trang to hơn.
     */
    public static final int MAX_PAGE_SIZE = 500;

    /**
     * Dựng từ {@code Map<String,String> allParams} của controller.
     * Áp nguyên các quy tắc clamp của §2b-6.
     */
    public static ListQuery from(Map<String, String> allParams) {
        Map<String, String> params = allParams == null ? Map.of() : allParams;

        int page = Math.max(1, parseInt(params.get("page"), 1));
        int pageSize = Math.min(MAX_PAGE_SIZE,
                Math.max(1, parseInt(params.get("pageSize"), PagedResponse.DEFAULT_PAGE_SIZE)));
        String search = params.getOrDefault("search", "");
        LocalDate dateFrom = parseDate(params.get("dateFrom"));
        LocalDate dateTo = parseDate(params.get("dateTo"));
        String mealPrefix = params.get("mealPrefix");

        Map<String, String> filters = new LinkedHashMap<>();
        params.forEach((k, v) -> {
            if (!CONTROL_KEYS.contains(k) && v != null && !v.isBlank()) {
                filters.put(k, v);
            }
        });

        return new ListQuery(page, pageSize, search == null ? "" : search,
                dateFrom, dateTo, mealPrefix, Map.copyOf(filters));
    }

    /** Spring Data là 0-indexed ⇒ {@code page - 1} (checklist §2b-7). */
    public Pageable toPageable(Sort sort) {
        return PageRequest.of(page - 1, pageSize, sort);
    }

    /**
     * ⚠️ SHARED INFRASTRUCTURE — thay đổi hành vi 2026-08, CHƯA CÓ SIGN-OFF của Nam (chủ sở
     * hữu {@code common/}, §4b kế hoạch).
     *
     * <p><b>Cái gì đã đổi:</b> trước đây method này mặc định {@code Sort.by(Sort.Direction.ASC,
     * "code")}. Bản hiện tại trả {@code Sort.unsorted()}.
     *
     * <p><b>Vì sao:</b> nhiều entity của MF03 (ví dụ {@code HealthHistoryEvent},
     * {@code AllergyDeclaration}, {@code ChronicConditionDeclaration}, {@code SpecialMealRequest})
     * KHÔNG có property {@code code} — gọi {@code toPageable()} cũ ném
     * {@code PropertyReferenceException} ngay ở lần chạy đầu cho MỌI endpoint danh sách của các
     * entity đó (JPA Specification/Sort validate field name lúc build query). Đổi sang
     * {@code Sort.unsorted()} là cách nhanh nhất để các endpoint đó không vỡ.
     *
     * <p><b>Ảnh hưởng:</b> đây là {@code common/}, dùng chung bởi <b>mọi</b> module (MF01, MF02,
     * MF03, MF12, …). Endpoint nào gọi overload không tham số này (không tự truyền {@code Sort})
     * sẽ mất thứ tự sắp xếp mặc định theo {@code code} — có thể đổi thứ tự hiển thị list mà
     * client (FE) không ngờ tới, dù không phải lỗi 500.
     *
     * <p><b>✅ ĐÃ CHỐT 08/08/2026 (Nam, chủ sở hữu {@code common/}) — chọn (a) + kèm luật:</b>
     * giữ {@code Sort.unsorted()} làm mặc định toàn cục (khôi phục mặc định theo {@code code}
     * sẽ làm vỡ ngay các entity MF03 không có property đó), NHƯNG mọi danh sách mà người dùng
     * đọc theo THỜI GIAN phải tự truyền {@code toPageable(Sort)} ở call-site. Lý do chốt thế
     * chứ không để nguyên: đo thật ngày 08/08/2026 thấy {@code GET /incidents} trả thứ tự
     * 002 → 004 → 005 → 003 (thứ tự vật lý của Postgres) ⇒ màn Sự cố của BGH lộn xộn và widget
     * "Sự cố mới nhất" của giáo viên lấy {@code items[0]} nên hiện sai bản ghi. Call-site đầu
     * tiên áp luật này: {@code IncidentServiceImpl.list()}. Khi thêm endpoint danh sách mới,
     * hỏi "người dùng có kỳ vọng thứ tự nào không?" — có thì truyền {@code Sort} tường minh,
     * đừng dựa vào mặc định.
     *
     * <p><b>Trạng thái:</b> đây là thay đổi hành vi của infra dùng chung ⇒ theo
     * {@code 07_implement/00_Common_Conventions.md} §0-BIS mục "Cần thêm method vào common/" —
     * loại "đổi hành vi cái đã có" phải hỏi Nam TRƯỚC khi merge, không chỉ báo nhóm. Thay đổi
     * này ĐÃ NẰM TRONG code (cần thiết để MF03 chạy được) nhưng CHƯA XIN Ý KIẾN Nam — cố tình
     * KHÔNG revert (revert lại làm vỡ MF03 ngay) nhưng để lại comment này để không bị chôn vùi
     * âm thầm. Nếu bạn là Nam: xem có nên (a) giữ {@code unsorted()} làm mặc định toàn cục, hay
     * (b) đổi các entity không có {@code code} sang truyền tường minh {@code toPageable(Sort)} ở
     * call-site và khôi phục mặc định {@code code} cho các entity CÓ property đó.
     *
     * <p><b>Kiểm tra regression đã làm (không thay cho sign-off) — SỬA LẠI cho đúng thực tế
     * grep, đừng tin mô tả cũ ở đây nếu nó nói khác:</b> {@code grep -rn "toPageable("
     * src/main/java/com/sfms/{iam,mf02,mf12}} cho thấy MF01 ({@code UserService},
     * {@code StaffService}) VÀ MF02 ({@code AcademicYearService}, {@code ClassroomService},
     * {@code SchoolService}) đều gọi overload KHÔNG tham số này — tức bản thân MF01/MF02 CŨNG
     * bị đổi từ "sort theo {@code code}" sang "unsorted" bởi thay đổi này, không chỉ MF03. Chỉ
     * MF12 ({@code AuditLogQueryServiceImpl}/{@code ErrorLogQueryServiceImpl}/
     * {@code SentReportQueryServiceImpl}) tự truyền {@code Sort} tường minh nên KHÔNG bị ảnh
     * hưởng. Bộ test hiện có của cả 4 module (chạy trong {@code mvn verify} cùng đợt sửa này)
     * vẫn PASS sau khi đổi — không phát hiện assertion nào phụ thuộc thứ tự mặc định theo
     * {@code code} — nhưng đó là bằng chứng THỰC NGHIỆM (test hiện có không phủ hết), không
     * thay thế cho việc Nam xem lại quyết định. Vì MF01/MF02 cũng bị ảnh hưởng, mức độ khẩn của
     * việc xin sign-off CAO HƠN đánh giá ban đầu — không chỉ riêng MF03.
     */
    public Pageable toPageable() {
        return PageRequest.of(page - 1, pageSize, Sort.unsorted());
    }

    public boolean hasSearch() {
        return search != null && !search.isBlank();
    }

    private static int parseInt(String raw, int fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /** D4: {@code dateFrom}/{@code dateTo} là {@code yyyy-MM-dd}. Sai định dạng ⇒ bỏ qua. */
    private static LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
