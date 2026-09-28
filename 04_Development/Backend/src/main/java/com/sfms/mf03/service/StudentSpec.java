package com.sfms.mf03.service;

import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.mf03.domain.AllergyDeclaration;
import com.sfms.mf03.domain.ChronicConditionDeclaration;
import com.sfms.mf03.domain.DeclarationStatus;
import com.sfms.mf03.domain.RequestStatus;
import com.sfms.mf03.domain.SpecialMealRequest;
import com.sfms.mf03.domain.Student;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

/**
 * C1/§2b — SEARCH_FIELDS = {@code students.name, students.code, classrooms.name}. {@code name}/
 * {@code code} chạy trực tiếp trên {@code students}; {@code classrooms.name} chạy qua tập
 * {@code classroomId} khớp tên lớp đã tra TRƯỚC bằng native query ở service (tham số
 * {@code classroomIdsMatchingSearch}) — StudentSpec không import entity {@code Classroom} của
 * MF02 (ADR-M02-5), nên không thể JOIN trực tiếp trong Criteria API.
 *
 * <p>ADR-M03-3 — {@code healthStatus}/{@code hasAllergy}/{@code hasSpecialDiet} lọc bằng
 * {@code EXISTS} subquery trên {@code allergy_declarations}/{@code chronic_condition_declarations}/
 * {@code special_meal_requests}, KHÔNG trên cột (không tồn tại).
 */
public final class StudentSpec {

    private static final List<String> SEARCH_FIELDS = List.of("name", "code");

    private StudentSpec() {
    }

    public static Specification<Student> build(ListQuery query, UUID schoolId, UUID classroomIdScope,
                                                UUID parentUserIdScope) {
        return build(query, schoolId, classroomIdScope, parentUserIdScope, List.of());
    }

    public static Specification<Student> build(ListQuery query, UUID schoolId, UUID classroomIdScope,
                                                UUID parentUserIdScope,
                                                List<UUID> classroomIdsMatchingSearch) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = SpecSupport.newPredicateList();

            predicates.add(cb.equal(root.get("schoolId"), schoolId));
            if (classroomIdScope != null) {
                predicates.add(cb.equal(root.get("classroomId"), classroomIdScope));
            }

            // classroomId đã áp qua classroomIdScope ở trên (resolve code->UUID ở service) —
            // KHÔNG áp lại qua SpecSupport.applyExactFilters ở đây: giá trị filter là CODE
            // (chuỗi, VD "3A") trong khi cột students.classroom_id là UUID — so sánh string thô
            // với UUID qua path.as(String.class) khiến Postgres ném "operator does not exist:
            // uuid = character varying" (bug tìm thấy khi chạy thật MF03-TC-INT-009/010/011).

            String hasAllergy = query.filters().get("hasAllergy");
            if ("yes".equalsIgnoreCase(hasAllergy)) {
                predicates.add(cb.exists(verifiedAllergyExists(root, cq, cb)));
            } else if ("no".equalsIgnoreCase(hasAllergy)) {
                predicates.add(cb.not(cb.exists(verifiedAllergyExists(root, cq, cb))));
            }

            String hasSpecialDiet = query.filters().get("hasSpecialDiet");
            if ("yes".equalsIgnoreCase(hasSpecialDiet)) {
                predicates.add(cb.exists(approvedDietExists(root, cq, cb)));
            } else if ("no".equalsIgnoreCase(hasSpecialDiet)) {
                predicates.add(cb.not(cb.exists(approvedDietExists(root, cq, cb))));
            }

            String healthStatus = query.filters().get("healthStatus");
            if (healthStatus != null && !healthStatus.isBlank()) {
                predicates.add(healthStatusPredicate(root, cq, cb, healthStatus));
            }
            String notHealthStatus = query.filters().get("not:healthStatus");
            if (notHealthStatus != null && !notHealthStatus.isBlank()) {
                predicates.add(cb.not(healthStatusPredicate(root, cq, cb, notHealthStatus)));
            }

            if (parentUserIdScope != null) {
                // Own scope (Parent) — chỉ HS của chính mình, join student_parents.
                Subquery<UUID> sp = cq.subquery(UUID.class);
                var spRoot = sp.from(com.sfms.mf03.domain.StudentParent.class);
                sp.select(spRoot.get("studentId"))
                        .where(cb.equal(spRoot.get("parentUserId"), parentUserIdScope));
                predicates.add(root.get("id").in(sp));
            }

            if (query.hasSearch()) {
                Predicate onNameOrCode = SpecSupport.searchPredicate(root, cb, query.search(), SEARCH_FIELDS);
                if (classroomIdsMatchingSearch.isEmpty()) {
                    predicates.add(onNameOrCode);
                } else {
                    // SEARCH_FIELDS bao gồm classrooms.name (C1) — OR thêm khớp theo classroomId
                    // đã tra sẵn ở service (không JOIN trực tiếp entity của MF02, ADR-M02-5).
                    predicates.add(cb.or(onNameOrCode, root.get("classroomId").in(classroomIdsMatchingSearch)));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Subquery<UUID> verifiedAllergyExists(jakarta.persistence.criteria.Root<Student> root,
                                                         jakarta.persistence.criteria.CommonAbstractCriteria cq,
                                                         jakarta.persistence.criteria.CriteriaBuilder cb) {
        Subquery<UUID> sub = ((jakarta.persistence.criteria.AbstractQuery<?>) cq).subquery(UUID.class);
        var r = sub.from(AllergyDeclaration.class);
        sub.select(r.get("id")).where(cb.equal(r.get("studentId"), root.get("id")),
                cb.equal(r.get("status"), DeclarationStatus.VERIFIED));
        return sub;
    }

    private static Subquery<UUID> approvedDietExists(jakarta.persistence.criteria.Root<Student> root,
                                                      jakarta.persistence.criteria.CommonAbstractCriteria cq,
                                                      jakarta.persistence.criteria.CriteriaBuilder cb) {
        Subquery<UUID> sub = ((jakarta.persistence.criteria.AbstractQuery<?>) cq).subquery(UUID.class);
        var r = sub.from(SpecialMealRequest.class);
        sub.select(r.get("id")).where(cb.equal(r.get("studentId"), root.get("id")),
                cb.equal(r.get("status"), RequestStatus.APPROVED));
        return sub;
    }

    /** ADR-M03-3 — công thức NEEDS_INFO > PENDING > VERIFIED, tập rỗng ⇒ VERIFIED. */
    private static Predicate healthStatusPredicate(jakarta.persistence.criteria.Root<Student> root,
                                                    jakarta.persistence.criteria.CommonAbstractCriteria cq,
                                                    jakarta.persistence.criteria.CriteriaBuilder cb,
                                                    String status) {
        Subquery<UUID> needsInfoAllergy = declStatusExists(root, cq, cb, AllergyDeclaration.class,
                DeclarationStatus.NEEDS_INFO);
        Subquery<UUID> needsInfoChronic = declStatusExists(root, cq, cb, ChronicConditionDeclaration.class,
                DeclarationStatus.NEEDS_INFO);
        Predicate anyNeedsInfo = cb.or(cb.exists(needsInfoAllergy), cb.exists(needsInfoChronic));

        Subquery<UUID> pendingAllergy = declStatusExists(root, cq, cb, AllergyDeclaration.class,
                DeclarationStatus.PENDING);
        Subquery<UUID> pendingChronic = declStatusExists(root, cq, cb, ChronicConditionDeclaration.class,
                DeclarationStatus.PENDING);
        Predicate anyPending = cb.or(cb.exists(pendingAllergy), cb.exists(pendingChronic));

        return switch (status) {
            case "NEEDS_INFO" -> anyNeedsInfo;
            case "PENDING" -> cb.and(cb.not(anyNeedsInfo), anyPending);
            case "VERIFIED" -> cb.and(cb.not(anyNeedsInfo), cb.not(anyPending));
            default -> cb.disjunction();
        };
    }

    private static <T> Subquery<UUID> declStatusExists(jakarta.persistence.criteria.Root<Student> root,
                                                        jakarta.persistence.criteria.CommonAbstractCriteria cq,
                                                        jakarta.persistence.criteria.CriteriaBuilder cb,
                                                        Class<T> entityClass, DeclarationStatus status) {
        Subquery<UUID> sub = ((jakarta.persistence.criteria.AbstractQuery<?>) cq).subquery(UUID.class);
        var r = sub.from(entityClass);
        sub.select(r.get("id")).where(cb.equal(r.get("studentId"), root.get("id")),
                cb.equal(r.get("status"), status));
        return sub;
    }
}
