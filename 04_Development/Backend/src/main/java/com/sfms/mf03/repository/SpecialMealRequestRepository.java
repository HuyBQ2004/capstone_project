package com.sfms.mf03.repository;

import com.sfms.mf03.domain.RequestStatus;
import com.sfms.mf03.domain.SpecialMealRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpecialMealRequestRepository extends JpaRepository<SpecialMealRequest, UUID>,
        JpaSpecificationExecutor<SpecialMealRequest> {

    /** D1 — tra cứu theo MÃ NGHIỆP VỤ (`id` của JSON), không phải khoá UUID. */
    Optional<SpecialMealRequest> findByCode(String code);

    /**
     * ⚠️ Cố ý dùng {@code findBy...} trả {@code List} + {@link Pageable} thay vì
     * {@code findFirstBy...OrderBy...Desc} trả {@code Optional} trực tiếp — bản dẫn xuất kia
     * (giữ nguyên chữ ký ban đầu) từng ném {@code org.hibernate.NonUniqueResultException:
     * Query did not return a unique result: 2 results were returned} khi chạy thật (Testcontainers
     * Postgres 16, Spring Data JPA 4.1.0 / Hibernate 7.4.1 — tổ hợp bản rất mới ở pom.xml), dù
     * WHERE clause chỉ khớp đúng {@code studentId} của MỘT học sinh. {@code Pageable} ép LIMIT ở
     * tầng JPQL trước khi Hibernate chạy unique-check, né hẳn lỗi thay vì cố tìm nguyên nhân sâu
     * trong bản Spring Data/Hibernate pre-release (phát hiện thực nghiệm khi chạy
     * StudentCapacityIT/AllergyVerifyAlertIT thật).
     */
    List<SpecialMealRequest> findByStudentIdAndStatusOrderByRequestedAtDesc(
            UUID studentId, RequestStatus status, Pageable pageable);

    default Optional<SpecialMealRequest> findFirstByStudentIdAndStatusOrderByRequestedAtDesc(
            UUID studentId, RequestStatus status) {
        List<SpecialMealRequest> results = findByStudentIdAndStatusOrderByRequestedAtDesc(
                studentId, status, org.springframework.data.domain.PageRequest.of(0, 1));
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    List<SpecialMealRequest> findByStudentId(UUID studentId);

    boolean existsByStudentIdAndStatus(UUID studentId, RequestStatus status);
}
