package com.sfms.mf12.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.repository.UserAccountRepository;
import com.sfms.iam.service.AuditService;
import com.sfms.mf12.domain.SentReport;
import com.sfms.mf12.dto.SentReportDtos.SentReportResponse;
import com.sfms.mf12.error.AisExceptions;
import com.sfms.mf12.repository.SentReportRepository;
import com.sfms.mf12.service.ReportExporter.ExportedFile;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** UC-190 — BR-176 lọc theo DÒNG (schoolId), không cần {@code oneOf} (TC-CON-007). */
@Service
public class SentReportQueryServiceImpl implements SentReportQueryService {

    private static final List<String> SEARCH_FIELDS = List.of("rangeLabel", "notes");
    private static final Map<String, String> JSON_TO_COLUMN = Map.of(
            "reportType", "reportType", "period", "period");

    private final SentReportRepository repository;
    private final UserAccountRepository userAccountRepository;
    private final ReportExporter reportExporter;
    private final AuditService auditService;
    private final ReferenceResolver referenceResolver;

    public SentReportQueryServiceImpl(SentReportRepository repository, UserAccountRepository userAccountRepository,
                                       ReportExporter reportExporter, AuditService auditService,
                                       ReferenceResolver referenceResolver) {
        this.repository = repository;
        this.userAccountRepository = userAccountRepository;
        this.reportExporter = reportExporter;
        this.auditService = auditService;
        this.referenceResolver = referenceResolver;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<SentReportResponse> list(ListQuery query, UUID schoolScope) {
        Specification<SentReport> spec = (root, cq, cb) -> {
            List<Predicate> predicates = SpecSupport.newPredicateList();
            SpecSupport.applyExactFilters(root, cb, query.filters(), JSON_TO_COLUMN, predicates);
            if (query.hasSearch()) {
                predicates.add(SpecSupport.searchPredicate(root, cb, query.search(), SEARCH_FIELDS));
            }
            if (schoolScope != null) {
                predicates.add(cb.equal(root.get("schoolId"), schoolScope));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<SentReport> page = repository.findAll(spec, query.toPageable(Sort.by(Sort.Direction.DESC, "sentAt")));
        List<SentReportResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SentReportResponse> detail(String code, UUID schoolScope) {
        return resolve(code, schoolScope).map(this::toResponse);
    }

    @Override
    @Transactional
    public ExportedFile download(String code, String format, UUID schoolScope, String actorEmail, String ip) {
        if (!"pdf".equalsIgnoreCase(format) && !"xlsx".equalsIgnoreCase(format)) {
            // BR-178 — chỉ hỗ trợ đúng 2 định dạng.
            throw AisExceptions.validationFailed("format", "must be one of pdf, xlsx");
        }
        SentReport report = resolve(code, schoolScope).orElseThrow(AisExceptions::notFound);
        String schoolName = referenceResolver.schoolName(report.getSchoolId());
        ExportedFile file = "xlsx".equalsIgnoreCase(format)
                ? reportExporter.renderXlsx(report, schoolName) : reportExporter.renderPdf(report, schoolName);
        // BR-177 — mỗi lần xuất báo cáo ghi audit trail (ai, khi nào, loại).
        auditService.success(actorEmail, ip, "DOWNLOAD sent-report=" + code + " format=" + format, "Sent Reports");
        return file;
    }

    private Optional<SentReport> resolve(String code, UUID schoolScope) {
        if (schoolScope != null) {
            return repository.findBySchoolIdAndCode(schoolScope, code);
        }
        return repository.findByCode(code).stream().findFirst();
    }

    private SentReportResponse toResponse(SentReport r) {
        String sentBy = userAccountRepository.findById(r.getSentByUserId()).map(u -> u.getEmail()).orElse(null);
        return new SentReportResponse(r.getCode(), r.getReportType().name(), r.getPeriod().name(),
                r.getRangeLabel(), r.getRangeFrom(), r.getRangeTo(), r.getSentAt(), sentBy, r.getRecipient(),
                r.getNotes(), r.isConfidential(), r.getFilePdfUrl() != null, r.getFileXlsxUrl() != null,
                r.getPayload());
    }
}
