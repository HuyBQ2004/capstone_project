package com.sfms.mf09.service;

import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.mf09.domain.StudentDistribution;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * {@code Specification} cho {@code GET /student-distributions}. TDS-MF09_Backend.md §9.1.
 *
 * <p>{@code SEARCH_FIELDS} = {@code studentName, className, studentId} ở tầng JPA/DB — JSON
 * response giữ nguyên {@code name}/{@code class} qua {@code @JsonProperty} (CF-M09-6,
 * ADR-M09-5 phần tên field vẫn hiệu lực, chỉ định dạng ngày bị D4/M09-D04 ghi đè).
 *
 * <p>⚠️ {@code date} KHÔNG đi qua {@link SpecSupport#applyExactFilters} (khác các field khác)
 * — phát hiện 05/08/2026 khi nối FE: cơ chế đó ép `Path.as(String.class)` rồi so bằng chuỗi,
 * Postgres báo lỗi {@code operator does not exist: date = character varying} cho cột kiểu
 * {@code date} (khác {@code varchar}/{@code uuid}, tự cast được). Parse tay thành
 * {@link LocalDate} rồi so trực tiếp — tránh đụng {@code SpecSupport} dùng chung (blast radius
 * nhiều module khác nếu sửa generic).
 */
public final class DistributionSpec {

    public static final List<String> SEARCH_FIELDS = List.of("studentName", "className", "studentId");

    /** JSON field -> thuộc tính JPA. {@code studentId} JSON là CODE của HS, resolve ở service. */
    private static final Map<String, String> FIELD_MAP = Map.ofEntries(
            Map.entry("class", "className"),
            Map.entry("status", "status"),
            Map.entry("mealType", "mealType")
            // ⚠️ isSpecial/studentId (mã) KHÔNG có cột trực tiếp -> resolve ở service trước khi build spec.
            // ⚠️ date: xem Javadoc lớp — xử lý riêng bên dưới, KHÔNG qua applyExactFilters.
    );

    private DistributionSpec() {
    }

    /**
     * @param studentIdFilter    UUID đã resolve từ query param {@code studentId} (mã students.code), hoặc {@code null}.
     * @param restrictClassrooms khác {@code null} ⇒ chỉ trả bản ghi thuộc các lớp này (BR-120 — Teacher
     *                           chỉ đọc lớp mình chủ nhiệm); {@code null} = không giới hạn (School Admin/Medical/Caterers).
     */
    public static Specification<StudentDistribution> build(ListQuery query, UUID schoolId, UUID studentIdFilter,
                                                             List<UUID> restrictClassrooms) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = SpecSupport.newPredicateList();
            predicates.add(cb.equal(root.get("schoolId"), schoolId));
            if (studentIdFilter != null) {
                predicates.add(cb.equal(root.get("studentId"), studentIdFilter));
            }
            if (restrictClassrooms != null) {
                predicates.add(restrictClassrooms.isEmpty()
                        ? cb.disjunction()
                        : root.get("classroomId").in(restrictClassrooms));
            }

            SpecSupport.applyExactFilters(root, cb, query.filters(), FIELD_MAP, predicates);

            String dateParam = query.filters().get("date");
            if (dateParam != null && !dateParam.isBlank()) {
                predicates.add(cb.equal(root.get("date"), LocalDate.parse(dateParam)));
            }

            if (query.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), query.dateFrom()));
            }
            if (query.dateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), query.dateTo()));
            }
            if (query.hasSearch()) {
                predicates.add(SpecSupport.searchPredicate(root, cb, query.search(), SEARCH_FIELDS));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
