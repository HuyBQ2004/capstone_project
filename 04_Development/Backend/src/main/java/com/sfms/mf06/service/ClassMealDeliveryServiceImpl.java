package com.sfms.mf06.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.MealType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf06.domain.ClassMealDelivery;
import com.sfms.mf06.domain.ClassMealDeliveryStatus;
import com.sfms.mf06.dto.ClassMealDeliveryDtos.ClassMealDeliveryResponse;
import com.sfms.mf06.dto.ClassMealDeliveryDtos.CreateDeliveryRequest;
import com.sfms.mf06.error.IngExceptions;
import com.sfms.mf06.repository.ClassMealDeliveryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.UUID;

/** Hiện thực {@link ClassMealDeliveryService}. Phase 2 mục (1)+(4). */
@Service
public class ClassMealDeliveryServiceImpl implements ClassMealDeliveryService {

    private final ClassMealDeliveryRepository repository;
    private final ReferenceResolver referenceResolver;
    private final CodeGenerator codeGenerator;
    private final Clock clock;

    public ClassMealDeliveryServiceImpl(ClassMealDeliveryRepository repository,
                                        ReferenceResolver referenceResolver,
                                        CodeGenerator codeGenerator,
                                        Clock clock) {
        this.repository = repository;
        this.referenceResolver = referenceResolver;
        this.codeGenerator = codeGenerator;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassMealDeliveryResponse> list(LocalDate date, String mealTypeRaw, String classroomCode,
                                                 AuthUser actor) {
        UUID schoolId = Mf06Scope.requireSchoolId(actor, referenceResolver);
        if (schoolId == null) {
            throw IngExceptions.accessDenied();
        }
        MealType mealType = parseMealType(mealTypeRaw);

        if (actor.role() == RoleKey.TEACHER) {
            List<UUID> ownClassrooms = referenceResolver.classroomIdsOfHomeroomTeacher(actor.userId());
            if (classroomCode != null && !classroomCode.isBlank()) {
                UUID classroomId = referenceResolver.classroomIdByCode(classroomCode, schoolId)
                        .orElseThrow(IngExceptions::notFound);
                if (!ownClassrooms.contains(classroomId)) {
                    throw IngExceptions.accessDenied();
                }
                return repository.findBySchoolIdAndClassroomIdAndDateAndMealTypeOrderByClassNameAsc(
                        schoolId, classroomId, date, mealType).stream().map(this::toResponse).toList();
            }
            return repository.findBySchoolIdAndDateAndMealTypeOrderByClassNameAsc(schoolId, date, mealType).stream()
                    .filter(d -> ownClassrooms.contains(d.getClassroomId()))
                    .map(this::toResponse).toList();
        }

        if (classroomCode != null && !classroomCode.isBlank()) {
            UUID classroomId = referenceResolver.classroomIdByCode(classroomCode, schoolId)
                    .orElseThrow(IngExceptions::notFound);
            return repository.findBySchoolIdAndClassroomIdAndDateAndMealTypeOrderByClassNameAsc(
                    schoolId, classroomId, date, mealType).stream().map(this::toResponse).toList();
        }
        return repository.findBySchoolIdAndDateAndMealTypeOrderByClassNameAsc(schoolId, date, mealType).stream()
                .map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public ClassMealDeliveryResponse deliver(CreateDeliveryRequest req, AuthUser actor) {
        if (actor.role() != RoleKey.SERVICE_CATERERS) {
            throw IngExceptions.accessDenied();
        }
        UUID schoolId = Mf06Scope.requireSchoolId(actor, referenceResolver);
        if (schoolId == null) {
            throw IngExceptions.accessDenied();
        }
        MealType mealType = parseMealType(req.mealType());
        UUID classroomId = referenceResolver.classroomIdByCode(req.classroomId(), schoolId)
                .orElseThrow(IngExceptions::notFound);

        /*
         * BR-094 (10/08/2026, cùng cổng chặn `StudentDistributionServiceImpl.doServe()` dùng
         * cho mf09) — trước đây chỉ chặn bằng nút disable ở FE
         * (`service-caterers/class-deliveries/page.tsx`), server không kiểm lại gì. Phát hiện
         * thật: bản ghi "Đã giao" tồn tại cho bữa trưa dù kiểm thực 3 bước ngày/bữa đó CHƯA
         * Đạt — vì FE chỉ tính lại `inspectionPassed` để hiện banner, không có gì ngăn request
         * POST thật sự lọt qua (kể cả khi banner đỏ, gọi thẳng API vẫn thành công).
         */
        var gate = referenceResolver.inspectionGate(req.date(), mealType.name(), schoolId);
        if (gate.isPresent() && gate.get().blocked()) {
            String stepName = gate.get().failedSteps().isEmpty()
                    ? gate.get().reasonCode()
                    : gate.get().failedSteps().get(0);
            throw IngExceptions.inspectionGateBlocked(stepName);
        }
        /*
         * BR-105 mở rộng (10/08/2026, Nam) — "chưa lưu mẫu chưa được phân phát": kiểm thực
         * Đạt thôi chưa đủ, phải lưu mẫu đủ MỌI món của bữa đó (QĐ 1246 Điều 7) trước khi bếp
         * được đánh dấu "Đã giao" tới lớp. Cùng vị trí gate với BR-094 ở trên, KHÔNG chặn khi
         * không tìm được phiên (đã bị `inspectionGate` chặn từ trước với NO_SESSION).
         */
        var sampleGate = referenceResolver.sampleRetentionGate(req.date(), mealType.name(), schoolId);
        if (sampleGate.isPresent() && sampleGate.get().blocked()) {
            throw IngExceptions.sampleRetentionIncomplete(sampleGate.get().missingDishNames());
        }

        if (repository.findBySchoolIdAndDateAndMealTypeAndClassroomId(schoolId, req.date(), mealType, classroomId)
                .isPresent()) {
            throw IngExceptions.conflict(
                    "A delivery already exists for this (date, meal, class) — use adjustment note, not a new record.");
        }

        ClassMealDelivery d = new ClassMealDelivery();
        d.setCode(codeGenerator.next("mf06_class_meal_delivery:" + Year.now(clock).getValue(),
                "CMDLV-" + Year.now(clock).getValue(), 4));
        d.setSchoolId(schoolId);
        d.setDate(req.date());
        d.setMealType(mealType);
        d.setClassroomId(classroomId);
        d.setClassName(referenceResolver.classroomCode(classroomId));
        d.setStatus(ClassMealDeliveryStatus.DELIVERED);
        d.setDeliveredByUserId(actor.userId());
        d.setDeliveredAt(clock.instant());
        d.setNote(req.note());
        repository.saveAndFlush(d);
        return toResponse(d);
    }

    @Override
    @Transactional
    public ClassMealDeliveryResponse receive(String code, AuthUser actor) {
        if (actor.role() != RoleKey.TEACHER) {
            throw IngExceptions.accessDenied();
        }
        UUID schoolId = Mf06Scope.requireSchoolId(actor, referenceResolver);
        if (schoolId == null) {
            throw IngExceptions.accessDenied();
        }
        ClassMealDelivery d = repository.findBySchoolIdAndCode(schoolId, code).orElseThrow(IngExceptions::notFound);
        if (!referenceResolver.isHomeroomTeacher(d.getClassroomId(), actor.userId())) {
            throw IngExceptions.accessDenied();
        }
        if (d.getStatus() == ClassMealDeliveryStatus.RECEIVED) {
            return toResponse(d); // idempotent — đã nhận rồi thì thôi, không báo lỗi
        }
        d.setStatus(ClassMealDeliveryStatus.RECEIVED);
        d.setReceivedByUserId(actor.userId());
        d.setReceivedAt(clock.instant());
        repository.saveAndFlush(d);
        return toResponse(d);
    }

    private ClassMealDeliveryResponse toResponse(ClassMealDelivery d) {
        return new ClassMealDeliveryResponse(d.getCode(), d.getDate(), d.getMealType().name(),
                d.getClassName(), d.getStatus().name(),
                userDisplayName(d.getDeliveredByUserId()), d.getDeliveredAt(),
                d.getReceivedByUserId() == null ? null : userDisplayName(d.getReceivedByUserId()), d.getReceivedAt(),
                d.getNote());
    }

    private String userDisplayName(UUID userId) {
        if (userId == null) {
            return null;
        }
        String[] user = referenceResolver.userById(userId);
        return user == null ? null : user[2];
    }

    private static MealType parseMealType(String raw) {
        try {
            return MealType.valueOf(raw);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw IngExceptions.validationFailed("mealType", "must be one of BREAKFAST, LUNCH, SNACK");
        }
    }
}
