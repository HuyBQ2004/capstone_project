package com.sfms.mf05.service;

import com.sfms.common.enums.MealType;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.mf05.domain.DietGroup;
import com.sfms.mf05.domain.MenuStatus;
import com.sfms.mf05.repository.DietGroupRepository;
import com.sfms.mf05.repository.MenuRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * BR-065 — mọi {@link DietGroup} có HS trong khối (đã DUYỆT special-meal-request, BR-220)
 * phải "có gì ăn": tồn tại một {@code Menu} cùng (ngày, khối, buổi) với {@code audience} =
 * mã nhóm đó. Port từ luật {@code semantic-audit.ts} {@code NOP_DUYET_MA_NHOM_CHE_DO_AN_CHUA_CO_GI_AN}
 * / {@code SUAT_RIENG_KHONG_CO_NGUOI_AN}.
 *
 * <p>M05-D04 — đọc "đã duyệt" qua {@link ReferenceResolver#approvedSpecialDietStudentCountsByGrade}
 * (native-query tạm thay {@code SpecialMealLookupPort} của MF03, TDS §2.5 mục 7 — NỢ TÀI LIỆU).
 */
@Component
public class DietCoverageValidator {

    private final ReferenceResolver referenceResolver;
    private final DietGroupRepository dietGroupRepository;
    private final MenuRepository menuRepository;

    public DietCoverageValidator(ReferenceResolver referenceResolver,
                                  DietGroupRepository dietGroupRepository,
                                  MenuRepository menuRepository) {
        this.referenceResolver = referenceResolver;
        this.dietGroupRepository = dietGroupRepository;
        this.menuRepository = menuRepository;
    }

    public List<DietGap> findGaps(UUID schoolId, UUID gradeId, LocalDate menuDate, MealType mealType) {
        return coverage(schoolId, gradeId, menuDate, mealType).stream()
                .filter(c -> !c.covered())
                .map(c -> new DietGap(c.dietGroupCode(), c.dietGroupLabel(), mealType.name(), c.students()))
                .toList();
    }

    /**
     * Như {@link #findGaps} nhưng trả CẢ nhóm đã có menu (không chỉ nhóm thiếu) — dùng cho màn
     * lập thực đơn hiển thị checklist "cần menu cho những nhóm nào" (04_Meal.../05_...design.md
     * mục 2) TRƯỚC khi bấm nộp duyệt, thay vì chỉ biết khi submit() ném lỗi.
     */
    public List<CoverageEntry> coverage(UUID schoolId, UUID gradeId, LocalDate menuDate, MealType mealType) {
        Map<String, Long> approvedCounts = referenceResolver.approvedSpecialDietStudentCountsByGrade(schoolId, gradeId);
        if (approvedCounts.isEmpty()) {
            return List.of();
        }
        List<CoverageEntry> out = new ArrayList<>();
        for (DietGroup group : dietGroupRepository.findAll()) {
            if (group.getMatchDiet() == null) {
                continue;
            }
            Long studentCount = approvedCounts.entrySet().stream()
                    .filter(e -> e.getKey().equalsIgnoreCase(group.getMatchDiet()))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
            // Nhóm không có HS nào cần -> không hiện (luật SUAT_RIENG_KHONG_CO_NGUOI_AN).
            if (studentCount == null || studentCount == 0) {
                continue;
            }
            boolean hasPrivateMenu = menuRepository
                    .findBySchoolIdAndMenuDateAndGradeIdAndAudienceAndStatusNot(
                            schoolId, menuDate, gradeId, group.getCode(), MenuStatus.REJECTED)
                    .stream()
                    .anyMatch(m -> m.getMealType() == mealType);
            // Quyết định 06/08/2026 — nhóm cũng "có gì ăn" nếu thực đơn CHUNG của đúng slot không
            // vi phạm giới hạn của nhóm (VD bữa chỉ có dưa hấu, ai ăn cũng được) — tránh ép Caterer
            // nhân bản thực đơn giống hệt chỉ để "có" một bản audience riêng.
            boolean covered = hasPrivateMenu
                    || referenceResolver.standardMenuSatisfiesDietGroup(schoolId, menuDate, gradeId, mealType,
                            group.getCode());
            out.add(new CoverageEntry(group.getCode(), group.getLabel(), studentCount, covered));
        }
        return out;
    }

    public record DietGap(String dietGroupCode, String dietGroupLabel, String mealType, long students) {}

    public record CoverageEntry(String dietGroupCode, String dietGroupLabel, long students, boolean covered) {}
}
