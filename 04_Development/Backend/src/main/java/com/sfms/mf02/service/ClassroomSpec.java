package com.sfms.mf02.service;

import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.mf02.domain.Classroom;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * {@code Specification} cho {@code GET /classrooms} (ADR-M02-1, TDS §17.1 C1/C2).
 *
 * <p>⚠️ <b>Lệch so với TDS §17.1</b> (khai {@code SEARCH_FIELDS = name, teacher}):
 * {@code teacher} là tên hiển thị của {@code homeroom_teacher_id} (UUID trỏ
 * {@code user_accounts.name}), và ADR-M02-5 CẤM import entity/repository của MF01 vào MF02.
 * Một {@code Specification} JPA Criteria không thể join sang một entity không được import.
 * SEARCH_FIELDS ở đây vì vậy chỉ còn {@code code}/{@code name} (đủ để tìm mờ theo mã lớp —
 * lưu ý L2: {@code code == name}); tìm theo tên GVCN cần Nam/Mạnh quyết định một cơ chế khác
 * (VD materialized view, hoặc native query nối {@code user_accounts}) — KHÔNG tự quyết ở đây.
 */
public final class ClassroomSpec {

    public static final List<String> SEARCH_FIELDS = List.of("code", "name");

    private static final Map<String, String> FIELD_MAP = Map.of(
            "id", "code",
            "code", "code",
            "name", "name",
            "gradeId", "grade.id"
    );

    private ClassroomSpec() {
    }

    /** @param scopedAcademicYearId khi khác {@code null}, ép lọc theo đúng năm học đó. */
    public static Specification<Classroom> build(ListQuery query, UUID scopedSchoolId,
                                                  UUID scopedAcademicYearId) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = SpecSupport.newPredicateList();

            if (scopedSchoolId != null) {
                predicates.add(cb.equal(root.get("schoolId"), scopedSchoolId));
            }
            if (scopedAcademicYearId != null) {
                predicates.add(cb.equal(root.get("academicYearId"), scopedAcademicYearId));
            }

            SpecSupport.applyExactFilters(root, cb, query.filters(), FIELD_MAP, predicates);

            if (query.hasSearch()) {
                predicates.add(SpecSupport.searchPredicate(root, cb, query.search(), SEARCH_FIELDS));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
