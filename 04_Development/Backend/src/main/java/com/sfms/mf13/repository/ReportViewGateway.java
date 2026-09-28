package com.sfms.mf13.repository;

import com.sfms.mf13.domain.ReportSlug;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ADR-M13-1 — đường đọc DUY NHẤT tới dữ liệu module khác: native query trên 7 SQL view
 * {@code v_rpt_*} do MF13 sở hữu (V30). KHÔNG {@code @Entity} nào của MF13 trỏ vào bảng module
 * khác; KHÔNG {@code import com.sfms.mf0x.domain.*}.
 *
 * <p>{@code view}/{@code dateColumn}/{@code orderColumn} bên dưới là hằng số nội bộ (không đến
 * từ request) — nối chuỗi SQL an toàn, không phải injection surface.
 */
@Component
public class ReportViewGateway {

    private record ViewSpec(String view, String dateColumn, String orderColumn) {
    }

    private static final Map<ReportSlug, ViewSpec> SPECS = new EnumMap<>(ReportSlug.class);

    static {
        SPECS.put(ReportSlug.THREE_STEP, new ViewSpec("v_rpt_three_step", "date", "date"));
        SPECS.put(ReportSlug.SAMPLE_RETENTION, new ViewSpec("v_rpt_sample_retention", "collected_at", "collected_at"));
        SPECS.put(ReportSlug.RECEIVING, new ViewSpec("v_rpt_receiving", "delivery_date", "delivery_date"));
        // Không có cột kỳ báo cáo tự nhiên — chứng nhận NCC/mức sử dụng không theo khoảng ngày.
        SPECS.put(ReportSlug.SUPPLIER_CERTIFICATES, new ViewSpec("v_rpt_supplier_cert", null, "id"));
        SPECS.put(ReportSlug.HYGIENE, new ViewSpec("v_rpt_hygiene", "date", "date"));
        SPECS.put(ReportSlug.TRACEABILITY, new ViewSpec("v_rpt_traceability", "received_date", "received_date"));
        SPECS.put(ReportSlug.SYSTEM_USAGE, new ViewSpec("v_rpt_system_usage", null, "id"));
    }

    private final JdbcTemplate jdbc;

    public ReportViewGateway(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * BR-176: {@code schoolId} luôn tham số hoá, không nội suy vào SQL. {@code schoolId == null}
     * ⇒ KHÔNG lọc trường — CHỈ dùng cho System Admin {@code scope=all} (đã kiểm quyền ở tầng
     * service trước khi gọi tới đây).
     */
    public List<Map<String, Object>> rows(ReportSlug slug, UUID schoolId, LocalDate from, LocalDate to,
                                          int limit, int offset) {
        ViewSpec spec = SPECS.get(slug);
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(spec.view());
        List<Object> args = new java.util.ArrayList<>();
        appendSchoolFilter(sql, args, schoolId);
        appendDateFilter(sql, args, spec, from, to);
        sql.append(" ORDER BY ").append(spec.orderColumn()).append(" DESC LIMIT ? OFFSET ?");
        args.add(limit);
        args.add(offset);
        List<Map<String, Object>> raw = jdbc.queryForList(sql.toString(), args.toArray());
        return raw.stream().map(ReportViewGateway::normalizeTemporalValues).toList();
    }

    /**
     * D4 — JDBC trả {@code java.sql.Date}/{@code java.sql.Timestamp} cho DATE/TIMESTAMPTZ; Jackson
     * mặc định serialize chúng dạng epoch/không nhất quán. Chuẩn hoá về {@link LocalDate}/
     * {@link java.time.Instant} TRƯỚC khi rời tầng đọc, để {@code GET /reports/{slug}/rows} luôn
     * trả ISO-8601 dù đi qua {@code Map<String,Object>} thay vì DTO có kiểu tường minh.
     */
    private static Map<String, Object> normalizeTemporalValues(Map<String, Object> row) {
        Map<String, Object> out = new java.util.LinkedHashMap<>();
        row.forEach((k, v) -> out.put(k, normalize(v)));
        return out;
    }

    private static Object normalize(Object value) {
        if (value instanceof java.sql.Timestamp ts) {
            return ts.toInstant();
        }
        if (value instanceof java.sql.Date d) {
            return d.toLocalDate();
        }
        if (value instanceof java.sql.Time t) {
            return t.toLocalTime();
        }
        return value;
    }

    public long count(ReportSlug slug, UUID schoolId, LocalDate from, LocalDate to) {
        ViewSpec spec = SPECS.get(slug);
        StringBuilder sql = new StringBuilder("SELECT count(*) FROM ").append(spec.view());
        List<Object> args = new java.util.ArrayList<>();
        appendSchoolFilter(sql, args, schoolId);
        appendDateFilter(sql, args, spec, from, to);
        Long total = jdbc.queryForObject(sql.toString(), Long.class, args.toArray());
        return total == null ? 0L : total;
    }

    private static void appendSchoolFilter(StringBuilder sql, List<Object> args, UUID schoolId) {
        if (schoolId == null) {
            sql.append(" WHERE 1=1");
            return;
        }
        sql.append(" WHERE school_id = ?");
        args.add(schoolId);
    }

    /**
     * Chặn trên của kỳ báo cáo — dùng {@code < (to + 1 ngày)} chứ KHÔNG dùng {@code <= to}.
     *
     * <p>Vì sao (sửa 08/08/2026): một số view có cột kỳ kiểu {@code timestamptz}
     * ({@code v_rpt_sample_retention.collected_at}), số khác kiểu {@code date}. Với cột
     * timestamp, Postgres nâng {@code date} bên phải thành {@code 00:00:00} nên {@code <= to}
     * LOẠI SẠCH mọi bản ghi phát sinh sau nửa đêm của NGÀY CUỐI KỲ — tức gần như toàn bộ.
     *
     * <p>Đo thật: kỳ 09/07→08/08/2026 có 2 mẫu lưu hợp lệ thu lúc 11:05 ngày 08/08.
     * {@code collected_at <= '2026-08-08'} đếm được <b>0</b>; {@code < '2026-08-08'::date + 1}
     * đếm được <b>2</b>. Báo cáo tuân thủ của BGH vì thế hiện "Mẫu lưu đúng quy định 0%".
     *
     * <p>{@code < to + 1} đúng cho CẢ HAI kiểu cột nên không phải phân nhánh theo view. Cùng
     * luật này áp ở {@code aggregate()} bên dưới.
     */
    private void appendDateFilter(StringBuilder sql, List<Object> args, ViewSpec spec, LocalDate from,
                                  LocalDate to) {
        if (spec.dateColumn() == null) {
            return;
        }
        if (from != null) {
            sql.append(" AND ").append(spec.dateColumn()).append(" >= ?");
            args.add(from);
        }
        if (to != null) {
            sql.append(" AND ").append(spec.dateColumn()).append(" < (?::date + 1)");
            args.add(to);
        }
    }

    /** Tổng hợp gọn cho {@code GET /compliance-report} — BR-182: KHÔNG hằng số. */
    public record ModuleAggregate(int total, int alerts) {
        public int rate() {
            return total == 0 ? 0 : (int) Math.round((total - alerts) * 100.0 / total);
        }
    }

    /**
     * UC-171 — 5 nhóm tuân thủ (three-step, sample-retention, receiving, supplier-certificates,
     * hygiene). traceability/system-usage KHÔNG thuộc compliance-report (chỉ ở {@code GET /reports}).
     */
    public ModuleAggregate threeStepAggregate(UUID schoolId, LocalDate from, LocalDate to) {
        return aggregate("v_rpt_three_step", "date", schoolId, from, to, "overall = 'FAILED'");
    }

    public ModuleAggregate sampleRetentionAggregate(UUID schoolId, LocalDate from, LocalDate to) {
        return aggregate("v_rpt_sample_retention", "collected_at", schoolId, from, to, "status = 'OVERDUE'");
    }

    public ModuleAggregate receivingAggregate(UUID schoolId, LocalDate from, LocalDate to) {
        return aggregate("v_rpt_receiving", "delivery_date", schoolId, from, to, "status = 'REJECTED'");
    }

    public ModuleAggregate supplierCertAggregate(UUID schoolId, LocalDate from, LocalDate to) {
        // Không có cột kỳ báo cáo — chứng nhận là trạng thái hiện tại, không lọc theo period.
        return aggregate("v_rpt_supplier_cert", null, schoolId, null, null,
                "cert_status IN ('EXPIRED','MISSING')");
    }

    public ModuleAggregate hygieneAggregate(UUID schoolId, LocalDate from, LocalDate to) {
        return aggregate("v_rpt_hygiene", "date", schoolId, from, to, "result = 'FAILED'");
    }

    private ModuleAggregate aggregate(String view, String dateColumn, UUID schoolId, LocalDate from, LocalDate to,
                                      String alertPredicate) {
        StringBuilder sql = new StringBuilder("SELECT count(*) AS total, ")
                .append("count(*) FILTER (WHERE ").append(alertPredicate).append(") AS alerts FROM ")
                .append(view);
        List<Object> args = new java.util.ArrayList<>();
        appendSchoolFilter(sql, args, schoolId);
        if (dateColumn != null) {
            if (from != null) {
                sql.append(" AND ").append(dateColumn).append(" >= ?");
                args.add(from);
            }
            if (to != null) {
                // Cùng luật chặn trên với `appendDateFilter` — xem Javadoc ở đó.
                sql.append(" AND ").append(dateColumn).append(" < (?::date + 1)");
                args.add(to);
            }
        }
        Map<String, Object> row = jdbc.queryForMap(sql.toString(), args.toArray());
        int total = ((Number) row.get("total")).intValue();
        int alerts = ((Number) row.get("alerts")).intValue();
        return new ModuleAggregate(total, alerts);
    }
}
