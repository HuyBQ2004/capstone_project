package com.sfms.mf02.service;

import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf02.domain.Grade;
import com.sfms.mf02.domain.NutritionTarget;
import com.sfms.mf02.dto.NutritionTargetDtos.NutritionTargetResponse;
import com.sfms.mf02.dto.NutritionTargetDtos.UpdateNutritionTargetRequest;
import com.sfms.mf02.error.SchExceptions;
import com.sfms.mf02.repository.GradeRepository;
import com.sfms.mf02.repository.NutritionTargetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Chuẩn dinh dưỡng bữa trưa theo TRƯỜNG × KHỐI (BR-062) — xem migration V54.
 *
 * <p>Cùng khuôn school-scope của {@link GradeServiceImpl}: phạm vi lấy từ
 * {@code actor.schoolId()}, ra ngoài phạm vi ⇒ 404 (không 403, BR-022).
 */
@Service
public class NutritionTargetServiceImpl implements NutritionTargetService {

    /**
     * Giá trị áp dụng cho trường CHƯA cấu hình — khai DUY NHẤT ở
     * {@link ReferenceResolver#DEFAULT_NUTRITION_TARGET} (common), vì MF09 cũng cần đúng
     * con số đó mà không được inject service của module khác. Đừng khai lại ở đây.
     */
    private static final BigDecimal DEFAULT_KCAL = ReferenceResolver.DEFAULT_NUTRITION_TARGET.kcal();
    private static final BigDecimal DEFAULT_PROTEIN = ReferenceResolver.DEFAULT_NUTRITION_TARGET.protein();
    private static final BigDecimal DEFAULT_FAT = ReferenceResolver.DEFAULT_NUTRITION_TARGET.fat();
    private static final BigDecimal DEFAULT_CARB = ReferenceResolver.DEFAULT_NUTRITION_TARGET.carb();

    private final NutritionTargetRepository repository;
    private final GradeRepository gradeRepository;

    public NutritionTargetServiceImpl(NutritionTargetRepository repository, GradeRepository gradeRepository) {
        this.repository = repository;
        this.gradeRepository = gradeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NutritionTargetResponse> list(AuthUser actor) {
        UUID schoolId = actor.schoolId();
        if (schoolId == null) {
            // SYSTEM_ADMIN không gắn trường: không có "trường của tôi" để liệt kê.
            return List.of();
        }
        Map<UUID, NutritionTarget> configured = repository.findBySchoolId(schoolId).stream()
                .collect(Collectors.toMap(NutritionTarget::getGradeId, Function.identity()));
        return gradeRepository.findBySchoolIdAndActiveTrueOrderBySortOrder(schoolId).stream()
                .map(g -> toResponse(g, Optional.ofNullable(configured.get(g.getId()))))
                .toList();
    }

    @Override
    @Transactional
    public NutritionTargetResponse update(UUID gradeId, UpdateNutritionTargetRequest request, AuthUser actor) {
        Grade grade = requireInScope(gradeId, actor);
        NutritionTarget target = repository
                .findBySchoolIdAndGradeId(grade.getSchoolId(), gradeId)
                .orElseGet(() -> {
                    // Lần khai đầu tiên: field không gửi lấy mặc định hệ thống, KHÔNG để null
                    // (DB khai NOT NULL cả 4 cột — "chưa khai" là không có dòng, không phải null).
                    NutritionTarget fresh = new NutritionTarget();
                    fresh.setSchoolId(grade.getSchoolId());
                    fresh.setGradeId(gradeId);
                    fresh.setKcal(DEFAULT_KCAL);
                    fresh.setProtein(DEFAULT_PROTEIN);
                    fresh.setFat(DEFAULT_FAT);
                    fresh.setCarb(DEFAULT_CARB);
                    return fresh;
                });
        if (request.kcal() != null) {
            target.setKcal(request.kcal());
        }
        if (request.protein() != null) {
            target.setProtein(request.protein());
        }
        if (request.fat() != null) {
            target.setFat(request.fat());
        }
        if (request.carb() != null) {
            target.setCarb(request.carb());
        }
        repository.saveAndFlush(target);
        return toResponse(grade, Optional.of(target));
    }

    @Override
    @Transactional(readOnly = true)
    public NutritionTargetResponse effectiveForGrade(UUID gradeId) {
        if (gradeId == null) {
            return defaultFor(null, null, 0);
        }
        Optional<NutritionTarget> configured = repository.findByGradeId(gradeId);
        Optional<Grade> grade = gradeRepository.findById(gradeId);
        return grade.map(g -> toResponse(g, configured))
                .orElseGet(() -> defaultFor(gradeId, null, 0));
    }

    /** BR-022 — ngoài phạm vi trường của JWT ⇒ 404, cùng quy ước {@link GradeServiceImpl}. */
    private Grade requireInScope(UUID gradeId, AuthUser actor) {
        Grade grade = gradeRepository.findById(gradeId).orElseThrow(SchExceptions::notFound);
        if (!actor.isSystemAdmin() && !grade.getSchoolId().equals(actor.schoolId())) {
            throw SchExceptions.notFound();
        }
        return grade;
    }

    private static NutritionTargetResponse toResponse(Grade grade, Optional<NutritionTarget> target) {
        return target
                .map(t -> new NutritionTargetResponse(grade.getId(), grade.getName(), grade.getSortOrder(),
                        true, t.getKcal(), t.getProtein(), t.getFat(), t.getCarb()))
                .orElseGet(() -> defaultFor(grade.getId(), grade.getName(), grade.getSortOrder()));
    }

    private static NutritionTargetResponse defaultFor(UUID gradeId, String gradeName, int sortOrder) {
        return new NutritionTargetResponse(gradeId, gradeName, sortOrder, false,
                DEFAULT_KCAL, DEFAULT_PROTEIN, DEFAULT_FAT, DEFAULT_CARB);
    }
}
