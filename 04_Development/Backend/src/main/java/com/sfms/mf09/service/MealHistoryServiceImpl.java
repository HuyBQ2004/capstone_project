package com.sfms.mf09.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.MealType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.common.reference.ReferenceResolver.DishNutrition;
import com.sfms.common.reference.ReferenceResolver.StudentRef;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf09.domain.ServingDish;
import com.sfms.mf09.domain.StudentDistribution;
import com.sfms.mf09.dto.DistributionDtos.ServingDishView;
import com.sfms.mf09.dto.MealHistoryDtos.MealHistoryEntryResponse;
import com.sfms.mf09.dto.MealHistoryDtos.NutritionDayResponse;
import com.sfms.mf09.dto.MealHistoryDtos.NutritionHistoryResponse;
import com.sfms.mf09.dto.MealHistoryDtos.NutritionTargetResponse;
import com.sfms.mf09.error.SrvExceptions;
import com.sfms.mf09.repository.MealConsumptionRecordRepository;
import com.sfms.mf09.repository.ServingDishRepository;
import com.sfms.mf09.repository.StudentDistributionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Hiện thực {@link MealHistoryService}. M09-D07 — controller RIÊNG dưới {@code /students/{code}},
 * KHÔNG sửa {@code StudentController}/{@code StudentHealthFileController} của MF03.
 */
@Service
public class MealHistoryServiceImpl implements MealHistoryService {

    private final StudentDistributionRepository distributionRepository;
    private final ServingDishRepository servingDishRepository;
    private final MealConsumptionRecordRepository mealRecordRepository;
    private final ReferenceResolver referenceResolver;

    public MealHistoryServiceImpl(StudentDistributionRepository distributionRepository,
                                  ServingDishRepository servingDishRepository,
                                  MealConsumptionRecordRepository mealRecordRepository,
                                  ReferenceResolver referenceResolver) {
        this.distributionRepository = distributionRepository;
        this.servingDishRepository = servingDishRepository;
        this.mealRecordRepository = mealRecordRepository;
        this.referenceResolver = referenceResolver;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MealHistoryEntryResponse> list(String studentCode, ListQuery query, AuthUser actor) {
        StudentRef student = resolveAndGuard(studentCode, actor);
        LocalDate from = query.dateFrom() != null ? query.dateFrom() : LocalDate.of(2000, 1, 1);
        LocalDate to = query.dateTo() != null ? query.dateTo() : LocalDate.of(2100, 1, 1);
        return paginate(loadEntries(student.id(), from, to), query);
    }

    @Override
    @Transactional(readOnly = true)
    public MealHistoryEntryResponse detail(String studentCode, String distributionCode, AuthUser actor) {
        StudentRef student = resolveAndGuard(studentCode, actor);
        StudentDistribution dist = distributionRepository.findBySchoolIdAndCode(student.schoolId(), distributionCode)
                .filter(d -> d.getStudentId().equals(student.id()))
                .orElseThrow(SrvExceptions::notFound);
        return toEntry(dist);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MealHistoryEntryResponse> specialMealRecords(String studentCode, ListQuery query,
                                                                       AuthUser actor) {
        StudentRef student = resolveAndGuard(studentCode, actor);
        if (referenceResolver.specialDietOfStudent(student.id()).isEmpty()) {
            return PagedResponse.empty(query.page(), query.pageSize());
        }
        LocalDate from = query.dateFrom() != null ? query.dateFrom() : LocalDate.of(2000, 1, 1);
        LocalDate to = query.dateTo() != null ? query.dateTo() : LocalDate.of(2100, 1, 1);
        return paginate(loadEntries(student.id(), from, to), query);
    }

    /*
     * BR-062 — chuẩn dinh dưỡng KHÔNG còn là hằng số ở đây (sửa 07/08/2026).
     *
     * Trước đây chỗ này là `private static final NutritionTargetResponse NUTRITION_TARGET =
     * new NutritionTargetResponse(715, 28, 22, 95)` — một con số duy nhất cho MỌI trường,
     * MỌI khối, và không sửa được nếu không build lại. Trong khi `menus` đã gắn `grade_id`
     * từ V44 nên thực đơn Khối 1 và Khối 5 bị chấm cùng một thước ±10%.
     *
     * Nay tra theo khối của chính học sinh đó (V54, `nutrition_targets`); trường chưa cấu
     * hình thì `ReferenceResolver.DEFAULT_NUTRITION_TARGET` giữ nguyên đúng 4 con số cũ nên
     * hành vi không đổi với dữ liệu hiện có.
     */

    @Override
    @Transactional(readOnly = true)
    public NutritionHistoryResponse nutritionHistory(String studentCode, LocalDate from, LocalDate to,
                                                       AuthUser actor) {
        StudentRef student = resolveAndGuard(studentCode, actor);
        LocalDate rangeFrom = from != null ? from : LocalDate.now().minusDays(29);
        LocalDate rangeTo = to != null ? to : LocalDate.now();

        List<NutritionDayResponse> days = distributionRepository
                .findByStudentIdAndDateBetweenOrderByDateDesc(student.id(), rangeFrom, rangeTo).stream()
                .filter(d -> d.getMealType() == MealType.LUNCH)
                .map(this::toNutritionDay)
                .sorted(java.util.Comparator.comparing(NutritionDayResponse::date))
                .toList();
        var target = referenceResolver.nutritionTargetOfStudent(student.id());
        return new NutritionHistoryResponse(days, new NutritionTargetResponse(
                target.kcal(), target.protein(), target.fat(), target.carb()));
    }

    private NutritionDayResponse toNutritionDay(StudentDistribution d) {
        BigDecimal kcal = BigDecimal.ZERO;
        BigDecimal protein = BigDecimal.ZERO;
        BigDecimal fat = BigDecimal.ZERO;
        BigDecimal carb = BigDecimal.ZERO;
        for (ServingDish sd : servingDishRepository.findByDistributionId(d.getId())) {
            DishNutrition n = referenceResolver.dishNutrition(sd.getDishId());
            if (n == null) continue;
            kcal = kcal.add(n.kcal());
            protein = protein.add(n.protein());
            fat = fat.add(n.fat());
            carb = carb.add(n.carb());
        }
        return new NutritionDayResponse(d.getDate(), kcal, protein, fat, carb);
    }

    private List<MealHistoryEntryResponse> loadEntries(UUID studentId, LocalDate from, LocalDate to) {
        return distributionRepository.findByStudentIdAndDateBetweenOrderByDateDesc(studentId, from, to).stream()
                .map(this::toEntry)
                .toList();
    }

    private MealHistoryEntryResponse toEntry(StudentDistribution d) {
        List<ServingDishView> dishes = servingDishRepository.findByDistributionId(d.getId()).stream()
                .map(sd -> new ServingDishView(referenceResolver.dishCode(sd.getDishId()), sd.getDishName(),
                        sd.isSubstitute(), sd.getSubstitutedForDishName(), sd.getReason()))
                .toList();
        String consumption = mealRecordRepository
                .findByStudentIdAndDateBetweenOrderByDateDesc(d.getStudentId(), d.getDate(), d.getDate()).stream()
                .filter(r -> r.getMealType() == d.getMealType())
                .findFirst()
                .map(r -> r.getConsumption().name())
                .orElse(null);
        return new MealHistoryEntryResponse(d.getCode(), d.getDate(), d.getMealType().name(),
                d.getStatus().name(), d.getCatererName(), dishes, consumption);
    }

    private static PagedResponse<MealHistoryEntryResponse> paginate(List<MealHistoryEntryResponse> all,
                                                                     ListQuery query) {
        int pageSize = Math.max(1, query.pageSize());
        int pageCount = Math.max(1, (int) Math.ceil(all.size() / (double) pageSize));
        int page = Math.min(Math.max(1, query.page()), pageCount);
        int from = Math.min((page - 1) * pageSize, all.size());
        int to = Math.min(from + pageSize, all.size());
        return new PagedResponse<>(all.subList(from, to), all.size(), page, pageSize, pageCount);
    }

    /**
     * BR-201 (Parent — con đã liên kết) / BR-120 (Teacher — lớp mình chủ nhiệm).
     *
     * <p>MEDICAL_STAFF (mở 08/08/2026, backlog B4): chỉ chặn theo PHẠM VI TRƯỜNG, không giới hạn
     * lớp — y tế điều tra sự cố ATTP phải tra được suất ăn của bất kỳ học sinh nào bị ảnh hưởng
     * trong trường, mà các em đó thường rải khắp nhiều lớp. Đây là nhánh `else` sẵn có
     * ({@code studentByCode(code, actor.schoolId())}), không cần thêm điều kiện gì.
     */
    private StudentRef resolveAndGuard(String studentCode, AuthUser actor) {
        StudentRef student = actor.role() == RoleKey.PARENT
                ? referenceResolver.studentByCodeAnySchool(studentCode).orElseThrow(SrvExceptions::notFound)
                : referenceResolver.studentByCode(studentCode, actor.schoolId()).orElseThrow(SrvExceptions::notFound);

        if (actor.role() == RoleKey.PARENT
                && !referenceResolver.isParentLinkedToStudent(actor.userId(), student.id())) {
            throw SrvExceptions.accessDenied();
        }
        if (actor.role() == RoleKey.TEACHER
                && !referenceResolver.isHomeroomTeacher(student.classroomId(), actor.userId())) {
            throw SrvExceptions.accessDenied();
        }
        return student;
    }
}
