package com.sfms.mf04.repository;

import com.sfms.mf04.domain.SupplierPartnership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupplierPartnershipRepository extends JpaRepository<SupplierPartnership, UUID>,
        JpaSpecificationExecutor<SupplierPartnership> {

    Optional<SupplierPartnership> findByPartnerIdAndSchoolId(UUID partnerId, UUID schoolId);

    List<SupplierPartnership> findBySchoolId(UUID schoolId);

    List<SupplierPartnership> findByPartnerId(UUID partnerId);

    boolean existsByCode(String code);

    Optional<SupplierPartnership> findByCode(String code);

    /*
     * ── BR-EXCLUSIVE-CATERER (chốt 08/08/2026, Nam) ────────────────────────────────
     * Một ĐƠN VỊ SUẤT ĂN chỉ hợp tác với ĐÚNG MỘT trường, và một trường chỉ có ĐÚNG MỘT
     * đơn vị suất ăn TẠI MỘT THỜI ĐIỂM — quan hệ 1-1. Nhà cung cấp thực phẩm thì KHÔNG
     * bị ràng buộc này (nhiều-nhiều là bình thường).
     *
     * "Đang hiệu lực" = status APPROVED (PENDING chưa tính, SUSPENDED coi như tạm dừng)
     * và chưa tới hạn kết thúc (`until` null hoặc còn ở tương lai) — nhờ vế `until` mà
     * trường đổi nhà thầu theo năm học vẫn giữ được lịch sử hợp tác cũ.
     *
     * Lọc theo `partners.partner_type` nên phải JOIN — không đặt được thành partial
     * unique index ở DB (Postgres không cho subquery trong index predicate). Ràng buộc
     * vì thế nằm ở tầng service; xem ghi chú "nợ" ở PartnerApprovalServiceImpl.
     */
    @org.springframework.data.jpa.repository.Query(value = """
            select count(*) > 0 from supplier_partnerships sp
            join partners p on p.id = sp.partner_id
            where p.partner_type = 'SERVICE_CATERER'
              and sp.status = 'APPROVED'
              and (sp.until is null or sp.until >= current_date)
              and sp.partner_id = :partnerId
              and (:excludeSchoolId is null or sp.school_id <> :excludeSchoolId)
            """, nativeQuery = true)
    boolean catererHasActivePartnership(@org.springframework.data.repository.query.Param("partnerId") UUID partnerId,
                                         @org.springframework.data.repository.query.Param("excludeSchoolId") UUID excludeSchoolId);

    @org.springframework.data.jpa.repository.Query(value = """
            select count(*) > 0 from supplier_partnerships sp
            join partners p on p.id = sp.partner_id
            where p.partner_type = 'SERVICE_CATERER'
              and sp.status = 'APPROVED'
              and (sp.until is null or sp.until >= current_date)
              and sp.school_id = :schoolId
              and (:excludePartnerId is null or sp.partner_id <> :excludePartnerId)
            """, nativeQuery = true)
    boolean schoolHasActiveCaterer(@org.springframework.data.repository.query.Param("schoolId") UUID schoolId,
                                    @org.springframework.data.repository.query.Param("excludePartnerId") UUID excludePartnerId);
}
