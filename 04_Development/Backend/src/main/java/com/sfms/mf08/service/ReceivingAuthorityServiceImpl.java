package com.sfms.mf08.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.mf08.domain.ReceivingAuthority;
import com.sfms.mf08.dto.ReceivingAuthorityDtos.ReceivingAuthorityResponse;
import com.sfms.mf08.repository.ReceivingAuthorityRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ReceivingAuthorityServiceImpl implements ReceivingAuthorityService {

    private static final List<String> SEARCH_FIELDS = List.of("code", "name");
    private static final Map<String, String> FIELD_MAP = Map.of(
            "id", "code", "type", "type", "isActive", "isActive");

    private final ReceivingAuthorityRepository repository;

    public ReceivingAuthorityServiceImpl(ReceivingAuthorityRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReceivingAuthorityResponse> list(ListQuery query) {
        Specification<ReceivingAuthority> spec = (root, cq, cb) -> {
            List<Predicate> predicates = SpecSupport.newPredicateList();
            SpecSupport.applyExactFilters(root, cb, query.filters(), FIELD_MAP, predicates);
            if (query.hasSearch()) {
                predicates.add(SpecSupport.searchPredicate(root, cb, query.search(), SEARCH_FIELDS));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<ReceivingAuthority> page = repository.findAll(spec, query.toPageable(Sort.by("name")));
        List<ReceivingAuthorityResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    private ReceivingAuthorityResponse toResponse(ReceivingAuthority a) {
        return new ReceivingAuthorityResponse(a.getCode(), a.getName(),
                a.getType() == null ? null : a.getType().name(), a.getContact(), a.getAddress(),
                a.isActive(), a.getNote());
    }
}
