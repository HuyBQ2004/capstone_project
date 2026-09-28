package com.sfms.mf09.service;

import com.sfms.common.allergen.AllergenSemantics;
import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.MealType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.common.enums.ConsumptionStatus;
import com.sfms.mf09.domain.DistributionErrorReport;
import com.sfms.mf09.domain.DistributionStatus;
import com.sfms.mf09.domain.MealConsumptionRecord;
import com.sfms.mf09.domain.ServingDish;
import com.sfms.mf09.domain.StudentDistribution;
import com.sfms.mf09.dto.DistributionDtos.ConfirmBatchRequest;
import com.sfms.mf09.dto.DistributionDtos.DistributionCatererResponse;
import com.sfms.mf09.dto.DistributionDtos.DistributionResponse;
import com.sfms.mf09.dto.DistributionDtos.DistributionSummaryResponse;
import com.sfms.mf09.dto.DistributionDtos.ErrorReportRequest;
import com.sfms.mf09.dto.DistributionDtos.PlannedDishRow;
import com.sfms.mf09.dto.DistributionDtos.PlannedDishSummary;
import com.sfms.mf09.dto.DistributionDtos.PlannedDishesResponse;
import com.sfms.mf09.dto.DistributionDtos.ServingDishView;
import com.sfms.mf09.error.SrvExceptions;
import com.sfms.mf09.port.MenuQueryPort;
import com.sfms.mf09.repository.DistributionErrorReportRepository;
import com.sfms.mf09.repository.MealConsumptionRecordRepository;
import com.sfms.mf09.repository.ServingDishRepository;
import com.sfms.mf09.repository.StudentDistributionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Hiện thực {@link StudentDistributionService}. TDS-MF09_Backend.md §8.1, ADR-M09-1..8
 * (M09-D08 — tham chiếu module khác qua {@link ReferenceResolver}, không dựng port riêng
 * ngoài {@link MenuQueryPort}).
 */
@Service
public class StudentDistributionServiceImpl implements StudentDistributionService {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter CODE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final StudentDistributionRepository distributionRepository;
    private final ServingDishRepository servingDishRepository;
    private final DistributionErrorReportRepository errorReportRepository;
    private final MealConsumptionRecordRepository mealConsumptionRecordRepository;
    private final ReferenceResolver referenceResolver;
    private final CodeGenerator codeGenerator;
    private final MenuQueryPort menuQueryPort;
    private final Clock clock;

    public StudentDistributionServiceImpl(StudentDistributionRepository distributionRepository,
                                          ServingDishRepository servingDishRepository,
                                          DistributionErrorReportRepository errorReportRepository,
                                          MealConsumptionRecordRepository mealConsumptionRecordRepository,
                                          ReferenceResolver referenceResolver,
                                          CodeGenerator codeGenerator,
                                          MenuQueryPort menuQueryPort,
                                          Clock clock) {
        this.distributionRepository = distributionRepository;
        this.servingDishRepository = servingDishRepository;
        this.errorReportRepository = errorReportRepository;
        this.mealConsumptionRecordRepository = mealConsumptionRecordRepository;
        this.referenceResolver = referenceResolver;
        this.codeGenerator = codeGenerator;
        this.menuQueryPort = menuQueryPort;
        this.clock = clock;
    }

    // ═════════════════════════ Đọc (D11 — Service Caterers nhận shape RÚT GỌN) ═════════════════════════

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<?> list(ListQuery query, AuthUser actor) {
        UUID schoolId = resolveSchoolId(actor);
        UUID studentIdFilter = resolveStudentIdFilter(query, schoolId);
        List<UUID> restrictClassrooms = actor.role() == RoleKey.TEACHER
                ? referenceResolver.classroomIdsOfHomeroomTeacher(actor.userId())
                : null;

        var spec = DistributionSpec.build(query, schoolId, studentIdFilter, restrictClassrooms);
        Page<StudentDistribution> page = distributionRepository.findAll(spec,
                query.toPageable(Sort.by(Sort.Direction.DESC, "date")));

        if (actor.role() == RoleKey.SERVICE_CATERERS) {
            List<DistributionCatererResponse> items = page.getContent().stream()
                    .map(this::toCatererResponse).toList();
            return PagedResponse.of(page, items, query.page(), query.pageSize());
        }
        List<DistributionResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public DistributionResponse getByCode(String code, AuthUser actor) {
        StudentDistribution dist = loadInScope(code, actor);
        guardReadScope(dist, actor);
        return toResponse(dist);
    }

    // ═════════════════════════ Xác nhận phát suất (UC-016) ═════════════════════════

    @Override
    @Transactional
    public DistributionResponse serve(String code, AuthUser actor) {
        UUID schoolId = requireTeacher(actor);
        StudentDistribution dist = distributionRepository.findBySchoolIdAndCodeForUpdate(schoolId, code)
                .orElseThrow(SrvExceptions::notFound);
        guardHomeroom(dist, actor);
        doServe(dist, actor);
        distributionRepository.saveAndFlush(dist);
        return toResponse(dist);
    }

    @Override
    @Transactional
    public List<DistributionResponse> confirmBatch(ConfirmBatchRequest req, AuthUser actor) {
        UUID schoolId = requireTeacher(actor);
        List<DistributionResponse> out = new ArrayList<>();
        for (String code : req.distributionIds()) {
            StudentDistribution dist = distributionRepository.findBySchoolIdAndCodeForUpdate(schoolId, code)
                    .orElseThrow(SrvExceptions::notFound);
            guardHomeroom(dist, actor);
            // AF1 UC-016 — SRV-023: còn báo lỗi chưa xử lý của chính phiên này thì không được hoàn tất.
            if (errorReportRepository.existsByDistributionIdAndResolvedFalse(dist.getId())) {
                throw SrvExceptions.unresolvedErrorReport();
            }
            if (dist.getStatus() != DistributionStatus.SERVED) {
                doServe(dist, actor);
            }
            distributionRepository.saveAndFlush(dist);
            out.add(toResponse(dist));
        }
        return out;
    }

    /** C4/BR-130/BR-094/ADR-M09-1 — hiện thực dùng chung cho {@code serve} và {@code confirmBatch}. */
    private void doServe(StudentDistribution dist, AuthUser actor) {
        if (dist.getStatus() == DistributionStatus.SERVED) {
            throw SrvExceptions.duplicateDistribution();
        }

        /*
         * BR-094 (MF07) qua ReferenceResolver — M09-D08, KHÔNG InspectionGatePort.
         *
         * SIẾT LẠI 07/08/2026 (Nam chốt): chặn cả ba trường hợp NO_SESSION / IN_PROGRESS /
         * INSPECTION_FAILED, khớp đúng `GET /inspections/serving-gate`. Trước đó chỉ chặn
         * khi có bước FAILED, nên **chưa mở phiên kiểm thực hoặc đang kiểm dở vẫn phát được
         * suất** — cổng fail-open, và FE đã phải né theo đường lỏng hơn.
         *
         * `gate.isEmpty()` nay CHỈ xảy ra khi tham số thiếu (date/meal/school null), không
         * còn mang nghĩa "không có phiên".
         */
        var gate = referenceResolver.inspectionGate(dist.getDate(), dist.getMealType().name(), dist.getSchoolId());
        if (gate.isPresent() && gate.get().blocked()) {
            String stepName = gate.get().failedSteps().isEmpty()
                    ? gate.get().reasonCode()
                    : gate.get().failedSteps().get(0);
            throw SrvExceptions.inspectionGateBlocked(stepName);
        }
        /*
         * BR-105 mở rộng (10/08/2026, Nam) — "chưa lưu mẫu chưa được phân phát": kiểm thực
         * Đạt thôi chưa đủ, QĐ 1246 Điều 7 bắt buộc lưu mẫu MỌI món trước khi phục vụ. Cùng
         * vị trí với gate BR-094 ở trên — chặn ngay tại `doServe()` (dùng chung cho cả
         * `serve()` lẫn `confirmBatch()`, ADR-M09-1) để không có đường phát suất nào né được.
         */
        var sampleGate = referenceResolver.sampleRetentionGate(dist.getDate(), dist.getMealType().name(), dist.getSchoolId());
        if (sampleGate.isPresent() && sampleGate.get().blocked()) {
            throw SrvExceptions.sampleRetentionIncomplete(sampleGate.get().missingDishNames());
        }

        List<PlannedDish> planned = planDishes(dist);
        if (planned.isEmpty()) {
            throw SrvExceptions.noMenu(dist.getMealType().name(), dist.getDate().toString(), null);
        }
        // BR-065, vá 05/08/2026 — dị ứng trùng món mà KHÔNG có món thay thế cấu hình sẵn: CHẶN
        // phát, không âm thầm ghi nhận đã ăn đúng món gây dị ứng (xem
        // 04_Meal_Substitution_Portion_Pipeline_Design.md — bug gốc + quyết định người dùng).
        PlannedDish blocked = planned.stream().filter(PlannedDish::blocked).findFirst().orElse(null);
        if (blocked != null) {
            throw SrvExceptions.allergySubstituteMissing(blocked.dishName());
        }

        List<ServingDish> toSave = new ArrayList<>();
        for (PlannedDish p : planned) {
            ServingDish sd = new ServingDish();
            sd.setCode(codeGenerator.next("mf09_serving_dish:" + CODE_DATE.format(dist.getDate()),
                    "SDSH-" + CODE_DATE.format(dist.getDate()), 3));
            sd.setSchoolId(dist.getSchoolId());
            sd.setDistribution(dist);
            sd.setDishId(p.dishId());
            sd.setSubstitute(p.isSubstitute());
            sd.setDishName(p.dishName());
            if (p.isSubstitute()) {
                sd.setSubstitutedForDishName(p.substitutedForDishName());
                sd.setReason(p.reason());
            }
            toSave.add(sd);
        }
        servingDishRepository.saveAll(toSave);

        /*
         * BUG tìm thấy 11/08/2026 (Nam) — `meal_consumption_records` (V16) có sẵn schema +
         * ràng buộc `uq_mcr_student_date_meal` đúng ý "mỗi học sinh 1 dòng/ngày/bữa, consumption
         * mặc định PENDING", nhưng KHÔNG CÓ dòng code nào từng INSERT vào bảng này —
         * `MealConsumptionServiceImpl` chỉ có `update()`/`updateBatch()`, không có `create()`.
         * Hệ quả thật: `/teacher/meal-records` LUÔN rỗng trên môi trường thật (không phải do
         * seed/mock như lúc dev), vì phát suất (`doServe`) ghi `ServingDish` (món đã phát) mà
         * không hề tạo dòng "cần ghi nhận đã ăn hết/một phần/không ăn" (BR-121) cho GVCN xử lý
         * sau đó. Tạo NGAY ở đây — đúng chỗ duy nhất biết đủ dữ liệu (học sinh, món, suất thay
         * thế) tại thời điểm phát suất — mỗi học sinh 1 dòng PENDING/ngày/bữa.
         */
        if (!mealConsumptionRecordRepository.existsByStudentIdAndDateAndMealType(
                dist.getStudentId(), dist.getDate(), dist.getMealType())) {
            MealConsumptionRecord mcr = new MealConsumptionRecord();
            mcr.setCode(codeGenerator.next("mf09_meal_record:" + CODE_DATE.format(dist.getDate()),
                    "MR-" + CODE_DATE.format(dist.getDate()), 3));
            mcr.setSchoolId(dist.getSchoolId());
            mcr.setDate(dist.getDate());
            mcr.setMealType(dist.getMealType());
            mcr.setStudentId(dist.getStudentId());
            mcr.setStudentName(dist.getStudentName());
            mcr.setClassName(dist.getClassName());
            mcr.setDishesLabel(planned.stream().map(PlannedDish::dishName).distinct()
                    .reduce((a, b) -> a + ", " + b).orElse(null));
            mcr.setSpecialDiet(referenceResolver.specialDietOfStudent(dist.getStudentId()).orElse(null));
            // BR-125 — chỉ ghi khi CÓ thay món (dị ứng); không thay thì để trống, không phải "—".
            String substituted = planned.stream().filter(PlannedDish::isSubstitute)
                    .map(p -> p.dishName() + " (thay " + p.substitutedForDishName() + ")")
                    .distinct().reduce((a, b) -> a + "; " + b).orElse(null);
            mcr.setSpecialMealServed(substituted);
            mcr.setConsumption(ConsumptionStatus.PENDING);
            mealConsumptionRecordRepository.saveAndFlush(mcr);
        }

        dist.setStatus(DistributionStatus.SERVED);
        dist.setServedAt(clock.instant());
        dist.setServedByUserId(actor.userId());
    }

    /**
     * 04_Meal_Substitution_Portion_Pipeline_Design.md Phase 1/2 — hàm THUẦN, không side-effect,
     * dùng LẠI y nguyên luật thay món (BR-065) của {@code doServe()}. Tách ra để phục vụ CẢ
     * ghi (`doServe`) LẪN xem trước (`plannedDishes`) — không viết 2 bản luật lệch nhau.
     * Trả rỗng khi {@code dist.getMenuId() == null} hoặc thực đơn không có món nào.
     *
     * <p>Vá 05/08/2026 (bug BR-065 phát hiện khi đối chiếu `TestSpec-MF09` FX-009): TRƯỚC ĐÂY
     * chỉ đánh cờ {@code isSubstitute}, KHÔNG tra {@code Dish.defaultSubstituteId} — món ghi
     * nhận vẫn là món gây dị ứng. NAY: có {@code defaultSubstituteId} thì món thực tế đổi sang
     * món thay thế ({@code dishId`/`dishCode`/`dishName` là của MÓN THAY THẾ}); KHÔNG có thì
     * đánh dấu {@code blocked=true} để {@code doServe()} chặn phát (không đoán, không tự bịa
     * món thay thế).
     */
    private List<PlannedDish> planDishes(StudentDistribution dist) {
        if (dist.getMenuId() == null) {
            return List.of();
        }
        List<String> verifiedAllergens = referenceResolver.verifiedAllergensOfStudent(dist.getStudentId());
        var menuDishes = menuQueryPort.dishesOfMenu(dist.getMenuId());
        List<PlannedDish> out = new ArrayList<>();
        for (var dishView : menuDishes) {
            /*
             * 13/08/2026 (Nam) — trước đây là `verifiedAllergens.stream().anyMatch(allergens::contains)`,
             * tức so khớp CHUỖI CHÍNH XÁC. Bỏ lọt quan hệ Ô (BR-063): em khai "Tôm" vẫn được phát
             * món chỉ tag chung "Hải sản", và ngược lại. MF05 đã dùng AllergenSemantics từ cùng
             * ngày; MF09 là chỗ THẬT SỰ đưa thức ăn tới miệng học sinh nên càng không được lỏng
             * hơn. Nay hai module dùng chung một luật (lớp đã chuyển sang common/).
             */
            boolean conflicts = verifiedAllergens.stream()
                    .anyMatch(sa -> dishView.allergens().stream()
                            .anyMatch(da -> AllergenSemantics.touches(sa, da)));
            if (!conflicts) {
                out.add(new PlannedDish(dishView.dishId(), dishView.dishCode(), dishView.name(),
                        false, null, null, false));
                continue;
            }
            String reason = "Dị ứng " + String.join(", ", verifiedAllergens);
            if (dishView.defaultSubstituteId() != null) {
                out.add(new PlannedDish(dishView.defaultSubstituteId(), dishView.defaultSubstituteCode(),
                        dishView.defaultSubstituteName(), true, dishView.name(), reason, false));
            } else {
                out.add(new PlannedDish(dishView.dishId(), dishView.dishCode(), dishView.name(),
                        true, dishView.name(), reason + " — CHƯA có món thay thế cấu hình (BR-065)", true));
            }
        }
        return out;
    }

    /**
     * Kết quả thuần của {@link #planDishes}, dùng nội bộ trước khi ánh xạ ra entity/DTO.
     * {@code dishId}/{@code dishCode}/{@code dishName} là của MÓN THỰC TẾ sẽ phát (món thay thế
     * nếu {@code isSubstitute}, món gốc nếu không) — {@code substitutedForDishName} giữ tên món
     * GỐC bị dị ứng. {@code blocked=true} khi cần thay món nhưng chưa có món thay thế cấu hình.
     */
    private record PlannedDish(UUID dishId, String dishCode, String dishName, boolean isSubstitute,
                                String substitutedForDishName, String reason, boolean blocked) {}

    // ═════════════════════════ Preview (Phase 1 — KHÔNG ghi gì) ═════════════════════════

    @Override
    @Transactional(readOnly = true)
    public PlannedDishesResponse plannedDishes(LocalDate date, String className, MealType mealType, AuthUser actor) {
        UUID schoolId = resolveSchoolId(actor);
        if (schoolId == null) {
            throw SrvExceptions.accessDenied();
        }
        // Phase 2 — Teacher LUÔN phải chỉ rõ lớp (BR-120, chỉ xem lớp mình); School Admin/Service
        // Caterers bỏ trống `className` để gộp TOÀN TRƯỜNG theo món (đếm suất bếp cần nấu).
        if (actor.role() == RoleKey.TEACHER && (className == null || className.isBlank())) {
            throw SrvExceptions.validationFailed("class", "required for TEACHER role");
        }
        List<StudentDistribution> rows = (className == null || className.isBlank())
                ? distributionRepository.findBySchoolIdAndDateAndMealType(schoolId, date, mealType)
                : distributionRepository.findBySchoolIdAndDateAndClassNameAndMealType(schoolId, date, className, mealType);
        if (actor.role() == RoleKey.TEACHER) {
            List<UUID> ownClassrooms = referenceResolver.classroomIdsOfHomeroomTeacher(actor.userId());
            rows = rows.stream().filter(d -> ownClassrooms.contains(d.getClassroomId())).toList();
        }
        rows = rows.stream().filter(d -> d.getStatus() == DistributionStatus.PENDING).toList();

        List<PlannedDishRow> perStudent = new ArrayList<>();
        Map<String, PlannedDishSummary> byDish = new LinkedHashMap<>();
        // Audience tra 1 lần/thực đơn: một bữa toàn trường có hàng trăm bản ghi phân phát nhưng
        // chỉ vài menuId khác nhau (1 thực đơn chung mỗi khối + thực đơn riêng của từng nhóm).
        Map<UUID, String> audienceByMenu = new HashMap<>();
        for (StudentDistribution d : rows) {
            String menuAudience = d.getMenuId() == null ? null
                    : audienceByMenu.computeIfAbsent(d.getMenuId(), referenceResolver::menuAudience);
            for (PlannedDish p : planDishes(d)) {
                perStudent.add(new PlannedDishRow(d.getCode(), referenceResolver.studentCode(d.getStudentId()),
                        d.getStudentName(), d.getClassName(), p.dishCode(), p.dishName(), p.isSubstitute(),
                        p.substitutedForDishName(), p.reason(), p.blocked(), menuAudience));
                PlannedDishSummary cur = byDish.get(p.dishCode());
                int count = (cur == null ? 0 : cur.count()) + 1;
                int subCount = (cur == null ? 0 : cur.substituteCount()) + (p.isSubstitute() ? 1 : 0);
                int blockedCount = (cur == null ? 0 : cur.blockedCount()) + (p.blocked() ? 1 : 0);
                byDish.put(p.dishCode(), new PlannedDishSummary(p.dishCode(), p.dishName(), count, subCount, blockedCount));
            }
        }
        return new PlannedDishesResponse(perStudent, new ArrayList<>(byDish.values()));
    }

    // ═════════════════════════ Hủy đánh dấu (ADR-M09-3) ═════════════════════════

    @Override
    @Transactional
    public DistributionResponse revoke(String code, AuthUser actor) {
        UUID schoolId = requireTeacher(actor);
        StudentDistribution dist = distributionRepository.findBySchoolIdAndCodeForUpdate(schoolId, code)
                .orElseThrow(SrvExceptions::notFound);
        guardHomeroom(dist, actor);
        if (dist.getStatus() != DistributionStatus.SERVED) {
            return toResponse(dist);   // idempotent — đã PENDING thì thôi
        }
        LocalDate servedDay = dist.getServedAt().atZone(VN_ZONE).toLocalDate();
        if (!servedDay.equals(LocalDate.now(clock))) {
            throw SrvExceptions.revokeTooLate();
        }
        // ADR-M09-1 — serving_dishes KHÔNG bị xoá (RULE append-only ở DB chặn DELETE dù có gọi).
        dist.setStatus(DistributionStatus.PENDING);
        dist.setServedAt(null);
        dist.setServedByUserId(null);
        distributionRepository.saveAndFlush(dist);
        return toResponse(dist);
    }

    // ═════════════════════════ Báo lỗi phân phát (UC-016 AF1) ═════════════════════════

    @Override
    @Transactional
    public DistributionResponse reportError(String code, ErrorReportRequest req, AuthUser actor) {
        UUID schoolId = requireTeacher(actor);
        StudentDistribution dist = distributionRepository.findBySchoolIdAndCode(schoolId, code)
                .orElseThrow(SrvExceptions::notFound);
        guardHomeroom(dist, actor);

        DistributionErrorReport report = new DistributionErrorReport();
        report.setCode(codeGenerator.next("mf09_error_report:" + CODE_DATE.format(dist.getDate()),
                "DERR-" + CODE_DATE.format(dist.getDate()), 3));
        report.setSchoolId(schoolId);
        report.setDistributionId(dist.getId());
        report.setReportedByUserId(actor.userId());
        report.setReason(req.reason());
        errorReportRepository.save(report);
        return toResponse(dist);
    }

    // ═════════════════════════ Tổng hợp theo lớp (UC-132) ═════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<DistributionSummaryResponse> summary(LocalDate date, AuthUser actor) {
        UUID schoolId = resolveSchoolId(actor);
        LocalDate targetDate = date != null ? date : LocalDate.now(clock);
        // Nhóm theo class tại chỗ (dữ liệu 1 ngày luôn nhỏ) — tính runtime, KHÔNG bảng tổng hợp (ADR-M09-2).
        List<StudentDistribution> rows = distributionRepository.findAll((root, cq, cb) ->
                cb.and(cb.equal(root.get("schoolId"), schoolId), cb.equal(root.get("date"), targetDate)));

        Map<String, List<StudentDistribution>> byClass = new LinkedHashMap<>();
        for (StudentDistribution d : rows) {
            byClass.computeIfAbsent(d.getClassName(), k -> new ArrayList<>()).add(d);
        }
        List<DistributionSummaryResponse> out = new ArrayList<>();
        for (var entry : byClass.entrySet()) {
            List<StudentDistribution> list = entry.getValue();
            long served = list.stream().filter(d -> d.getStatus() == DistributionStatus.SERVED).count();
            int specialCount = (int) list.stream()
                    .filter(d -> referenceResolver.specialDietOfStudent(d.getStudentId()).isPresent())
                    .count();
            String status = served == 0 ? "PENDING" : served == list.size() ? "COMPLETED" : "IN_PROGRESS";
            out.add(new DistributionSummaryResponse(entry.getKey(), list.size(), specialCount, status));
        }
        return out;
    }

    // ═════════════════════════ Mapping ═════════════════════════

    private DistributionResponse toResponse(StudentDistribution d) {
        List<String> verified = referenceResolver.verifiedAllergensOfStudent(d.getStudentId());
        List<String> allergyWarning = d.getMenuId() == null ? List.of()
                : verified.stream().filter(menuQueryPort.allergensOfMenu(d.getMenuId())::contains).toList();
        String specialDiet = referenceResolver.specialDietOfStudent(d.getStudentId()).orElse(null);

        List<ServingDishView> servedDishes = servingDishRepository.findByDistributionId(d.getId()).stream()
                .map(sd -> new ServingDishView(referenceResolver.dishCode(sd.getDishId()), sd.getDishName(),
                        sd.isSubstitute(), sd.getSubstitutedForDishName(), sd.getReason()))
                .toList();

        return new DistributionResponse(d.getCode(), d.getDate(), d.getClassName(), d.getMealType().name(),
                referenceResolver.studentCode(d.getStudentId()), d.getStudentName(),
                d.getStatus().name(), d.getServedAt(), d.getCatererName(), allergyWarning, specialDiet,
                specialDiet != null ? "yes" : "no", servedDishes);
    }

    private DistributionCatererResponse toCatererResponse(StudentDistribution d) {
        String isSpecial = referenceResolver.specialDietOfStudent(d.getStudentId()).isPresent() ? "yes" : "no";
        return new DistributionCatererResponse(d.getCode(), d.getDate(), d.getClassName(),
                d.getMealType().name(), d.getStatus().name(), isSpecial);
    }

    // ═════════════════════════ Guards / hỗ trợ ═════════════════════════

    private void guardHomeroom(StudentDistribution dist, AuthUser actor) {
        if (!referenceResolver.isHomeroomTeacher(dist.getClassroomId(), actor.userId())) {
            throw SrvExceptions.accessDenied();
        }
    }

    /** Teacher đọc: chỉ lớp mình chủ nhiệm. Vai trò khác (School Admin/Medical/Caterers): toàn trường. */
    private void guardReadScope(StudentDistribution dist, AuthUser actor) {
        if (actor.role() == RoleKey.TEACHER
                && !referenceResolver.isHomeroomTeacher(dist.getClassroomId(), actor.userId())) {
            throw SrvExceptions.accessDenied();
        }
    }

    private UUID requireTeacher(AuthUser actor) {
        if (actor.role() != RoleKey.TEACHER) {
            throw SrvExceptions.accessDenied();
        }
        UUID schoolId = resolveSchoolId(actor);
        if (schoolId == null) {
            throw SrvExceptions.accessDenied();
        }
        return schoolId;
    }

    private StudentDistribution loadInScope(String code, AuthUser actor) {
        UUID schoolId = resolveSchoolId(actor);
        return distributionRepository.findBySchoolIdAndCode(schoolId, code).orElseThrow(SrvExceptions::notFound);
    }

    private UUID resolveStudentIdFilter(ListQuery query, UUID schoolId) {
        String raw = query.filters().get("studentId");
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return referenceResolver.studentByCode(raw, schoolId).map(ReferenceResolver.StudentRef::id).orElse(null);
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
