package com.sfms.mf09.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf09.domain.MealConsumptionRecord;
import com.sfms.mf09.dto.MealRecordDtos.BatchConsumptionRequest;
import com.sfms.mf09.dto.MealRecordDtos.MealRecordResponse;
import com.sfms.mf09.dto.MealRecordDtos.UpdateConsumptionRequest;
import com.sfms.mf09.error.SrvExceptions;
import com.sfms.mf09.repository.MealConsumptionRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Hiện thực {@link MealConsumptionService}. BR-121/BR-125/BR-133. */
@Service
public class MealConsumptionServiceImpl implements MealConsumptionService {

    private final MealConsumptionRecordRepository repository;
    private final ReferenceResolver referenceResolver;
    private final Clock clock;

    public MealConsumptionServiceImpl(MealConsumptionRecordRepository repository,
                                      ReferenceResolver referenceResolver, Clock clock) {
        this.repository = repository;
        this.referenceResolver = referenceResolver;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MealRecordResponse> list(ListQuery query, AuthUser actor) {
        UUID schoolId = resolveSchoolId(actor);
        Page<MealConsumptionRecord> page = repository.findAll(MealRecordSpec.build(query, schoolId),
                query.toPageable(Sort.by(Sort.Direction.DESC, "date")));
        List<MealRecordResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Override
    @Transactional
    public MealRecordResponse update(String code, UpdateConsumptionRequest req, AuthUser actor) {
        requireTeacher(actor);
        MealConsumptionRecord record = loadInScope(code, actor);
        // BR-121/BR-133 — khoá sửa sau ngày phục vụ (đã vào tóm tắt gửi phụ huynh).
        if (!record.getDate().equals(LocalDate.now(clock))) {
            throw SrvExceptions.consumptionLocked();
        }
        record.setConsumption(req.consumption());
        record.setSpecialMealServed(req.specialMealServed());
        record.setRecordedAt(clock.instant());
        record.setRecordedByUserId(actor.userId());
        repository.saveAndFlush(record);
        return toResponse(record);
    }

    @Override
    @Transactional
    public List<MealRecordResponse> updateBatch(BatchConsumptionRequest req, AuthUser actor) {
        requireTeacher(actor);
        List<MealRecordResponse> out = new ArrayList<>();
        for (var item : req.items()) {
            MealConsumptionRecord record = loadInScope(item.id(), actor);
            if (!record.getDate().equals(LocalDate.now(clock))) {
                throw SrvExceptions.consumptionLocked();
            }
            record.setConsumption(item.consumption());
            record.setSpecialMealServed(item.specialMealServed());
            record.setRecordedAt(clock.instant());
            record.setRecordedByUserId(actor.userId());
            repository.saveAndFlush(record);
            out.add(toResponse(record));
        }
        return out;
    }

    private MealRecordResponse toResponse(MealConsumptionRecord r) {
        return new MealRecordResponse(r.getCode(), r.getDate(), r.getMealType().name(),
                referenceResolver.studentCode(r.getStudentId()), r.getStudentName(),
                r.getClassName(), r.getDishesLabel(), r.getSpecialDiet(), r.getSpecialMealServed(),
                r.getConsumption().name(), r.getRecordedAt());
    }

    private MealConsumptionRecord loadInScope(String code, AuthUser actor) {
        UUID schoolId = resolveSchoolId(actor);
        return repository.findBySchoolIdAndCode(schoolId, code).orElseThrow(SrvExceptions::notFound);
    }

    private void requireTeacher(AuthUser actor) {
        if (actor.role() != RoleKey.TEACHER) {
            throw SrvExceptions.accessDenied();
        }
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
