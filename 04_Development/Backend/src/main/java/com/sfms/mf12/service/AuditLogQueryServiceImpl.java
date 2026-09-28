package com.sfms.mf12.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.iam.domain.AuditLog;
import com.sfms.iam.repository.AuditLogRepository;
import com.sfms.iam.repository.UserAccountRepository;
import com.sfms.iam.service.AuditService;
import com.sfms.mf12.dto.AuditLogDtos.AuditLogResponse;
import com.sfms.mf12.dto.AuditLogDtos.IntegrityCheckResponse;
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

/** UC-106 — đọc {@code audit_logs} (bảng MF01), tính toàn vẹn qua {@link AuditService}. */
@Service
public class AuditLogQueryServiceImpl implements AuditLogQueryService {

    private static final List<String> SEARCH_FIELDS = List.of("actor", "action", "resource", "ip");
    private static final Map<String, String> JSON_TO_COLUMN = Map.of(
            "actor", "actor", "action", "action", "resource", "resource",
            "ip", "ip", "result", "result");

    private final AuditLogRepository auditLogRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuditService auditService;

    public AuditLogQueryServiceImpl(AuditLogRepository auditLogRepository,
                                     UserAccountRepository userAccountRepository, AuditService auditService) {
        this.auditLogRepository = auditLogRepository;
        this.userAccountRepository = userAccountRepository;
        this.auditService = auditService;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> list(ListQuery query, UUID schoolScope) {
        List<UUID> schoolUserIds = schoolScope == null ? null
                : userAccountRepository.findAllBySchoolId(schoolScope).stream().map(u -> u.getId()).toList();

        Specification<AuditLog> spec = (root, cq, cb) -> {
            List<Predicate> predicates = SpecSupport.newPredicateList();
            SpecSupport.applyExactFilters(root, cb, query.filters(), JSON_TO_COLUMN, predicates);
            if (query.hasSearch()) {
                predicates.add(SpecSupport.searchPredicate(root, cb, query.search(), SEARCH_FIELDS));
            }
            if (schoolUserIds != null) {
                predicates.add(schoolUserIds.isEmpty()
                        ? cb.disjunction()
                        : root.get("actorUserId").in(schoolUserIds));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<AuditLog> page = auditLogRepository.findAll(spec, query.toPageable(Sort.by(Sort.Direction.DESC, "timestamp")));
        List<AuditLogResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuditLogResponse> detail(String code) {
        return auditLogRepository.findByCode(code).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public IntegrityCheckResponse verifyIntegrity() {
        AuditService.IntegrityCheckResult result = auditService.verifyChain();
        return new IntegrityCheckResponse(result.valid(), result.brokenAt());
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(log.getCode(), log.getTimestamp(), log.getActor(), log.getIp(),
                log.getAction(), log.getResource(), log.getResult());
    }
}
