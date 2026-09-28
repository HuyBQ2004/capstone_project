package com.sfms.mf04.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf04.domain.ApplicationStatus;
import com.sfms.mf04.domain.PartnerDocument;
import com.sfms.mf04.domain.PartnershipApplication;
import com.sfms.mf04.dto.ApplicationDtos.ApplicationPortalState;
import com.sfms.mf04.dto.ApplicationDtos.ApplicationResponse;
import com.sfms.mf04.dto.ApplicationDtos.SubmitApplicationRequest;
import com.sfms.mf04.error.SupExceptions;
import com.sfms.mf04.mapper.ApplicationMapper;
import com.sfms.mf04.repository.PartnerDocumentRepository;
import com.sfms.mf04.repository.PartnershipApplicationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/** UC-152/155/169/170 · UC-156/157/158/159 — BR-040/041, ADR-M04-2/M04-3, CF-M04-9. */
@Service
public class PartnershipApplicationServiceImpl implements PartnershipApplicationService {

    private static final Set<ApplicationStatus> OPEN = Set.of(ApplicationStatus.PENDING,
            ApplicationStatus.ACTION_REQUIRED);
    private static final Set<ApplicationStatus> RESUBMITTABLE = Set.of(ApplicationStatus.REJECTED,
            ApplicationStatus.ACTION_REQUIRED);
    /** Khớp {@code TDS-MF04 §9.2}: {@code ^\d{10}(-\d{3})?$} (⚠️ ngoài §1.4, SUP-007). */
    private static final Pattern TAX_ID_PATTERN = Pattern.compile("^\\d{10}(-\\d{3})?$");

    private final PartnershipApplicationRepository applicationRepository;
    private final PartnerDocumentRepository documentRepository;
    /** BR-EXCLUSIVE-CATERER — kiểm quan hệ hợp tác ĐANG HIỆU LỰC khi nộp đơn. */
    private final com.sfms.mf04.repository.SupplierPartnershipRepository partnershipRepository;
    private final ApplicationMapper applicationMapper;
    private final ReferenceResolver referenceResolver;
    private final CodeGenerator codeGenerator;
    private final Clock clock;

    @PersistenceContext
    private EntityManager em;

    public PartnershipApplicationServiceImpl(PartnershipApplicationRepository applicationRepository,
                                             PartnerDocumentRepository documentRepository,
                                             com.sfms.mf04.repository.SupplierPartnershipRepository partnershipRepository,
                                             ApplicationMapper applicationMapper,
                                             ReferenceResolver referenceResolver,
                                             CodeGenerator codeGenerator,
                                             Clock clock) {
        this.applicationRepository = applicationRepository;
        this.documentRepository = documentRepository;
        this.partnershipRepository = partnershipRepository;
        this.applicationMapper = applicationMapper;
        this.referenceResolver = referenceResolver;
        this.codeGenerator = codeGenerator;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ApplicationResponse> list(ListQuery query, AuthUser actor) {
        var spec = switch (actor.role()) {
            case SUPPLIER, SERVICE_CATERERS -> ApplicationSpec.build(query, actor.partnerId(), null);
            case SCHOOL_ADMIN -> ApplicationSpec.build(query, null, actor.schoolId());
            default -> ApplicationSpec.build(query, null, null);   // System Admin — All
        };
        Page<PartnershipApplication> page = applicationRepository.findAll(spec, query.toPageable());
        List<ApplicationResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationPortalState portalState(AuthUser actor) {
        List<PartnershipApplication> candidates = applicationRepository.findAll(
                (root, cq, cb) -> cb.and(cb.equal(root.get("partnerId"), actor.partnerId()),
                        root.get("status").in(Set.of(ApplicationStatus.PENDING, ApplicationStatus.ACTION_REQUIRED,
                                ApplicationStatus.REJECTED))),
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "submittedAt"))).getContent();

        if (candidates.isEmpty()) {
            return new ApplicationPortalState("NO_APPLICATION", null, null, List.of("SUBMIT"));
        }
        PartnershipApplication latest = candidates.get(0);
        String schoolCode = referenceResolver.schoolCode(latest.getSchoolId());
        if (latest.getStatus() == ApplicationStatus.PENDING) {
            return new ApplicationPortalState("TRACKING", latest.getCode(), schoolCode, List.of("TRACK"));
        }
        return new ApplicationPortalState("CAN_RESUBMIT", latest.getCode(), schoolCode,
                List.of("RESUBMIT", "TRACK"));
    }

    @Override
    @Transactional
    public ApplicationResponse submit(SubmitApplicationRequest req, AuthUser actor) {
        if (!TAX_ID_PATTERN.matcher(req.taxId()).matches()) {
            throw SupExceptions.invalidTaxId();                                 // ⚠️ ngoài §1.4
        }
        UUID schoolId = referenceResolver.schoolIdByCode(req.schoolId()).orElseThrow(SupExceptions::notFound);

        if (applicationRepository.existsByPartnerIdAndSchoolIdAndStatusIn(actor.partnerId(), schoolId, OPEN)) {
            throw SupExceptions.businessConflict();                             // UC-152/156 Precondition
        }

        /*
         * BR-EXCLUSIVE-CATERER (chốt 08/08/2026, Nam) — đơn vị suất ăn hợp tác với ĐÚNG MỘT
         * trường, và một trường có ĐÚNG MỘT đơn vị suất ăn tại một thời điểm. Nhà cung cấp
         * thực phẩm KHÔNG bị ràng buộc (nhiều-nhiều là bình thường).
         *
         * Chặn ngay ở bước NỘP để không ai mất công chuẩn bị hồ sơ rồi bị từ chối ở bước
         * duyệt. Nhưng chặn ở đây là CHƯA ĐỦ: hai đơn khác nhau có thể cùng đang chờ cho một
         * trường, nên `PartnerApprovalServiceImpl.approve()` kiểm lại lần nữa — đó mới là
         * chỗ tạo ra quan hệ hợp tác.
         */
        if (actor.role() == RoleKey.SERVICE_CATERERS) {
            if (partnershipRepository.catererHasActivePartnership(actor.partnerId(), schoolId)) {
                throw SupExceptions.catererExclusivity(
                        "Đơn vị suất ăn chỉ được hợp tác với MỘT trường tại một thời điểm — "
                        + "hồ sơ hợp tác hiện tại phải kết thúc trước khi nộp cho trường khác.");
            }
            if (partnershipRepository.schoolHasActiveCaterer(schoolId, actor.partnerId())) {
                throw SupExceptions.catererExclusivity(
                        "Trường này đã có đơn vị suất ăn đang hợp tác — mỗi trường chỉ hợp tác "
                        + "với MỘT đơn vị suất ăn tại một thời điểm.");
            }
        }

        List<PartnerDocument> attachments = resolveAttachments(req.certificateDocumentIds(), actor.partnerId());

        String code;
        if (req.id() != null && !req.id().isBlank()) {
            if (applicationRepository.existsByCode(req.id())) {
                throw SupExceptions.duplicateValue("id", req.id());             // D5
            }
            code = req.id();
        } else {
            code = codeGenerator.next("partnership_applications", "APP", 3);
        }

        PartnershipApplication application = new PartnershipApplication();
        application.setCode(code);
        application.setPartnerId(actor.partnerId());
        application.setSchoolId(schoolId);
        application.setCompanyName(req.companyName());
        application.setTaxId(req.taxId());
        application.setAddress(req.address());
        application.setSubmittedAt(clock.instant());
        application.setStatus(ApplicationStatus.PENDING);
        applicationRepository.saveAndFlush(application);

        attach(application.getId(), attachments);

        return toResponse(application);
    }

    @Override
    @Transactional
    public ApplicationResponse resubmit(String code, SubmitApplicationRequest req, AuthUser actor) {
        PartnershipApplication application = applicationRepository.findByCodeAndPartnerId(code, actor.partnerId())
                .orElseThrow(SupExceptions::notFound);
        if (!RESUBMITTABLE.contains(application.getStatus())) {
            throw SupExceptions.businessConflict();                             // UC-170 Precondition
        }
        if (!TAX_ID_PATTERN.matcher(req.taxId()).matches()) {
            throw SupExceptions.invalidTaxId();
        }

        List<PartnerDocument> attachments = resolveAttachments(req.certificateDocumentIds(), actor.partnerId());

        application.setCompanyName(req.companyName());
        application.setTaxId(req.taxId());
        application.setAddress(req.address());
        application.setSubmittedAt(clock.instant());
        application.setStatus(ApplicationStatus.PENDING);                       // UC-170/158 — giữ nguyên mã đơn
        application.setComment(null);
        application.setReviewedBy(null);
        application.setReviewedAt(null);
        applicationRepository.saveAndFlush(application);

        em.createNativeQuery("DELETE FROM partnership_application_documents WHERE application_id = :id")
                .setParameter("id", application.getId())
                .executeUpdate();
        attach(application.getId(), attachments);

        return toResponse(application);
    }

    // ───────────────────────── Tiện ích ─────────────────────────

    private List<PartnerDocument> resolveAttachments(List<String> documentCodes, UUID partnerId) {
        if (documentCodes == null || documentCodes.isEmpty()) {
            return List.of();
        }
        LocalDate today = LocalDate.now(clock);
        return documentCodes.stream().map(docCode -> {
            PartnerDocument doc = documentRepository.findByCodeAndPartnerId(docCode, partnerId)
                    .orElseThrow(SupExceptions::notFound);
            if (DocumentStatusCalculator.statusOf(doc, today) == DocumentStatus.EXPIRED) {
                throw SupExceptions.certExpired();                              // UC-152 AF1
            }
            return doc;
        }).toList();
    }

    private void attach(UUID applicationId, List<PartnerDocument> attachments) {
        for (PartnerDocument doc : attachments) {
            em.createNativeQuery("""
                    INSERT INTO partnership_application_documents (application_id, document_id)
                    VALUES (:appId, :docId) ON CONFLICT DO NOTHING
                    """)
                    .setParameter("appId", applicationId)
                    .setParameter("docId", doc.getId())
                    .executeUpdate();
        }
    }

    private ApplicationResponse toResponse(PartnershipApplication application) {
        String partnerCode = referenceResolver.partnerCode(application.getPartnerId());
        String schoolCode = referenceResolver.schoolCode(application.getSchoolId());
        String schoolName = referenceResolver.schoolName(application.getSchoolId());
        return applicationMapper.toResponse(application, partnerCode, schoolCode, schoolName);
    }
}
