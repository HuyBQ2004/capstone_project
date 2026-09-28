package com.sfms.mf06.repository;

import com.sfms.mf06.domain.PurchaseRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, UUID>,
        JpaSpecificationExecutor<PurchaseRequest> {

    Optional<PurchaseRequest> findBySchoolIdAndCode(UUID schoolId, String code);

    boolean existsBySchoolIdAndCode(UUID schoolId, String code);

    /**
     * Không lọc theo trường — dùng khi actor KHÔNG có {@code schoolId} riêng (SUPPLIER,
     * D9: `AuthUser.schoolId()` luôn null với tài khoản đối tác). An toàn vì mã sinh bởi
     * `codeGenerator.next("mf06_purchase_requests:" + year, ...)` — counter DÙNG CHUNG
     * mọi trường trong năm đó, không theo school_id, nên mã là duy nhất toàn hệ thống dù
     * ràng buộc UNIQUE ở DB chỉ khai theo (school_id, code).
     */
    Optional<PurchaseRequest> findByCode(String code);
}
