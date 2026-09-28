package com.sfms.mf08.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf08.domain.SampleLabelReservation;
import com.sfms.mf08.domain.SampleLabelReservationStatus;
import com.sfms.mf08.dto.SampleLabelReservationDtos.BulkCreateRequest;
import com.sfms.mf08.dto.SampleLabelReservationDtos.ReservationItem;
import com.sfms.mf08.dto.SampleLabelReservationDtos.ReservationLabelResponse;
import com.sfms.mf08.dto.SampleLabelReservationDtos.ReservationResponse;
import com.sfms.mf08.error.SmpExceptions;
import com.sfms.mf08.repository.SampleLabelReservationRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * In tem TRƯỚC cho nhiều ngày (quyết định của Nam, 10/08/2026) — xem javadoc lớp
 * {@link com.sfms.mf08.domain.SampleLabelReservation} và migration V63 để biết vì sao đây là
 * một bảng RIÊNG, không đụng {@code food_samples}.
 */
@Service
public class SampleLabelReservationServiceImpl implements SampleLabelReservationService {

    private static final DateTimeFormatter CODE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final SampleLabelReservationRepository repository;
    private final ReferenceResolver referenceResolver;
    private final CodeGenerator codeGenerator;
    private final Clock clock;

    public SampleLabelReservationServiceImpl(SampleLabelReservationRepository repository,
                                              ReferenceResolver referenceResolver, CodeGenerator codeGenerator,
                                              Clock clock) {
        this.repository = repository;
        this.referenceResolver = referenceResolver;
        this.codeGenerator = codeGenerator;
        this.clock = clock;
    }

    @Override
    @Transactional
    public List<ReservationResponse> bulkCreate(BulkCreateRequest request, AuthUser actor) {
        if (actor.role() != RoleKey.SERVICE_CATERERS) {
            throw SmpExceptions.accessDenied();
        }
        UUID schoolId = resolveSchoolId(actor);
        if (schoolId == null) {
            throw SmpExceptions.accessDenied();
        }
        List<ReservationResponse> created = new ArrayList<>();
        for (ReservationItem item : request.items()) {
            UUID dishId = referenceResolver.dishIdByCode(item.dishId())
                    .orElseThrow(() -> SmpExceptions.validationFailed("items[].dishId", "unknown dish: " + item.dishId()));

            SampleLabelReservation r = new SampleLabelReservation();
            // CÙNG scope bộ đếm với FoodSampleServiceImpl.resolveCreateCode() ("mf08_food_sample:"
            // + yyyyMMdd của NGÀY DỰ KIẾN lấy mẫu, không phải ngày đặt trước) — mã sinh ra ở đây
            // dùng được thẳng làm `id` của `POST /samples` khi lấy mẫu thật, không bao giờ trùng.
            String dateStr = CODE_DATE.format(item.collectDate());
            r.setCode(codeGenerator.next("mf08_food_sample:" + dateStr, "SMP-" + dateStr, 2));
            r.setSchoolId(schoolId);
            r.setCollectDate(item.collectDate());
            r.setMealType(item.mealType());
            r.setDishId(dishId);
            r.setDishName(referenceResolver.dishName(dishId));
            r.setStatus(SampleLabelReservationStatus.PENDING);
            repository.saveAndFlush(r);
            created.add(toResponse(r));
        }
        return created;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReservationResponse> list(ListQuery query, AuthUser actor) {
        UUID schoolId = resolveSchoolId(actor);
        Specification<SampleLabelReservation> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("schoolId"), schoolId));
            String status = query.filters().get("status");
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status").as(String.class), status));
            }
            if (query.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("collectDate"), query.dateFrom()));
            }
            if (query.dateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("collectDate"), query.dateTo()));
            }
            if (query.hasSearch()) {
                predicates.add(com.sfms.common.query.SpecSupport.searchPredicate(root, cb, query.search(),
                        List.of("code", "dishName")));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<SampleLabelReservation> page = repository.findAll(spec,
                query.toPageable(Sort.by(Sort.Direction.ASC, "collectDate")));
        List<ReservationResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationLabelResponse label(String code, AuthUser actor) {
        SampleLabelReservation r = loadInScope(code, actor);
        String facility = referenceResolver.schoolName(r.getSchoolId());
        return new ReservationLabelResponse(r.getCode(), facility, r.getDishName(),
                r.getMealType() == null ? null : r.getMealType().name(), r.getCollectDate());
    }

    @Override
    @Transactional
    public ReservationResponse cancel(String code, AuthUser actor) {
        if (actor.role() != RoleKey.SERVICE_CATERERS) {
            throw SmpExceptions.accessDenied();
        }
        SampleLabelReservation r = loadInScope(code, actor);
        if (r.getStatus() != SampleLabelReservationStatus.PENDING) {
            throw SmpExceptions.validationFailed("status", "only a PENDING reservation can be cancelled");
        }
        r.setStatus(SampleLabelReservationStatus.CANCELLED);
        repository.saveAndFlush(r);
        return toResponse(r);
    }

    private SampleLabelReservation loadInScope(String code, AuthUser actor) {
        UUID schoolId = resolveSchoolId(actor);
        return repository.findBySchoolIdAndCode(schoolId, code).orElseThrow(SmpExceptions::notFound);
    }

    private ReservationResponse toResponse(SampleLabelReservation r) {
        return new ReservationResponse(r.getCode(), referenceResolver.dishCode(r.getDishId()), r.getDishName(),
                r.getMealType() == null ? null : r.getMealType().name(), r.getCollectDate(), r.getStatus().name());
    }

    private UUID resolveSchoolId(AuthUser actor) {
        if (actor.schoolId() != null) {
            return actor.schoolId();
        }
        if (actor.partnerId() != null) {
            return referenceResolver.activeSchoolIdForPartner(actor.partnerId()).orElse(null);
        }
        return null;
    }
}
