package com.sfms.mf12.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.mf12.domain.ErrorLog;
import com.sfms.mf12.dto.ErrorLogDtos.ErrorLogDetail;
import com.sfms.mf12.dto.ErrorLogDtos.ErrorLogSummary;
import com.sfms.mf12.repository.ErrorLogRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** UC-111 — SEARCH_FIELDS = {message, module, path} (`00_` §2b-3). CF-18: list trả đủ field nặng. */
@Service
public class ErrorLogQueryServiceImpl implements ErrorLogQueryService {

    private static final List<String> SEARCH_FIELDS = List.of("message", "module", "path");
    private static final Map<String, String> JSON_TO_COLUMN = Map.of(
            "message", "message", "module", "module", "path", "path", "severity", "severity");

    private final ErrorLogRepository repository;

    public ErrorLogQueryServiceImpl(ErrorLogRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ErrorLogSummary> list(ListQuery query) {
        Specification<ErrorLog> spec = (root, cq, cb) -> {
            List<Predicate> predicates = SpecSupport.newPredicateList();
            SpecSupport.applyExactFilters(root, cb, query.filters(), JSON_TO_COLUMN, predicates);
            if (query.hasSearch()) {
                predicates.add(SpecSupport.searchPredicate(root, cb, query.search(), SEARCH_FIELDS));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<ErrorLog> page = repository.findAll(spec,
                query.toPageable(Sort.by(Sort.Direction.DESC, "severity").and(Sort.by(Sort.Direction.DESC, "timestamp"))));
        List<ErrorLogSummary> items = page.getContent().stream().map(this::toSummary).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ErrorLogDetail> detail(String code) {
        return repository.findByCode(code).map(e -> new ErrorLogDetail(e.getCode(), e.getTimestamp(),
                e.getMessage(), e.getSeverity() == null ? null : e.getSeverity().name(), e.getModule(),
                e.getPath(), e.getStackTrace(), e.getSessionInfo()));
    }

    private ErrorLogSummary toSummary(ErrorLog e) {
        return new ErrorLogSummary(e.getCode(), e.getTimestamp(), e.getMessage(),
                e.getSeverity() == null ? null : e.getSeverity().name(), e.getModule(), e.getPath(),
                e.getStackTrace(), e.getSessionInfo());
    }
}
