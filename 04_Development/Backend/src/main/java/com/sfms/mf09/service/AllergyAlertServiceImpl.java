package com.sfms.mf09.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.MealType;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf09.dto.AllergyAlertDtos.AllergyAlertResponse;
import com.sfms.mf09.error.SrvExceptions;
import com.sfms.mf09.port.MenuQueryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * UC-103 — cảnh báo dị ứng: giao giữa allergen ĐÃ XÁC MINH của từng HS trong lớp và allergen
 * của thực đơn hôm nay. Dẫn xuất tại chỗ (CF-M09 §9.1), KHÔNG bảng riêng. L20 — response CHỈ
 * cờ {@code conflictWithTodayMenu}, KHÔNG mang mã MSG-051 (BR-100 ngoài phạm vi MF09).
 */
@Service
public class AllergyAlertServiceImpl implements AllergyAlertService {

    private final ReferenceResolver referenceResolver;
    private final MenuQueryPort menuQueryPort;

    public AllergyAlertServiceImpl(ReferenceResolver referenceResolver, MenuQueryPort menuQueryPort) {
        this.referenceResolver = referenceResolver;
        this.menuQueryPort = menuQueryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AllergyAlertResponse> list(ListQuery query, AuthUser actor) {
        UUID schoolId = resolveSchoolId(actor);
        String classCode = query.filters().get("class");
        String mealTypeRaw = query.filters().get("mealType");
        LocalDate date = parseDate(query.filters().get("date"));
        if (classCode == null || classCode.isBlank() || mealTypeRaw == null || date == null) {
            throw SrvExceptions.validationFailed("class/mealType/date", "all three are required");
        }
        MealType mealType = MealType.valueOf(mealTypeRaw);

        UUID classroomId = referenceResolver.classroomIdByCode(classCode, schoolId)
                .orElseThrow(SrvExceptions::notFound);
        UUID gradeId = referenceResolver.classroomGrade(classroomId);
        var block = gradeId == null ? java.util.Optional.<MenuQueryPort.MenuBlockView>empty()
                : menuQueryPort.publishedBlock(date, gradeId, mealType, schoolId);
        List<String> menuAllergens = block.map(MenuQueryPort.MenuBlockView::menuId)
                .map(menuQueryPort::allergensOfMenu).orElse(List.of());

        List<AllergyAlertResponse> alerts = new ArrayList<>();
        if (!menuAllergens.isEmpty()) {
            for (var student : referenceResolver.studentsOfClassroom(classroomId)) {
                List<String> verified = referenceResolver.verifiedAllergensOfStudent(student.id());
                List<String> hit = verified.stream().filter(menuAllergens::contains).toList();
                if (!hit.isEmpty()) {
                    alerts.add(new AllergyAlertResponse(student.code(), student.name(), classCode, hit, true));
                }
            }
        }

        int page = Math.max(1, query.page());
        int pageSize = Math.max(1, query.pageSize());
        int pageCount = Math.max(1, (int) Math.ceil(alerts.size() / (double) pageSize));
        int safePage = Math.min(page, pageCount);
        int from = Math.min((safePage - 1) * pageSize, alerts.size());
        int to = Math.min(from + pageSize, alerts.size());
        return new PagedResponse<>(alerts.subList(from, to), alerts.size(), safePage, pageSize, pageCount);
    }

    private static LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw.trim());
        } catch (Exception e) {
            return null;
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
