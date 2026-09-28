package com.sfms.mf04.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * §16 ghi chú — School Administration chỉ đọc dữ liệu của đối tác ĐÃ/ĐANG hợp tác với trường
 * mình (đơn nộp vào trường, hoặc quan hệ hợp tác đang hiệu lực), không phải toàn bộ đối tác
 * nền tảng. Dùng chung cho {@code PartnerService}/{@code ComplianceDocumentService} — tránh
 * lặp lại native query ở nhiều nơi.
 */
@Component
public class SchoolPartnerLinkResolver {

    @PersistenceContext
    private EntityManager em;

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<UUID> partnerIdsConnectedToSchool(UUID schoolId) {
        if (schoolId == null) {
            return List.of();
        }
        return em.createNativeQuery("""
                SELECT DISTINCT partner_id FROM partnership_applications WHERE school_id = :schoolId
                UNION
                SELECT DISTINCT partner_id FROM supplier_partnerships WHERE school_id = :schoolId
                """)
                .setParameter("schoolId", schoolId)
                .getResultList();
    }
}
