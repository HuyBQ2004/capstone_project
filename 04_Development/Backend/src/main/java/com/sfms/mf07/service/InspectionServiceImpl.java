package com.sfms.mf07.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.MealType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf07.domain.InspectionSession;
import com.sfms.mf07.domain.Step1DryRow;
import com.sfms.mf07.domain.Step1FreshRow;
import com.sfms.mf07.domain.Step2Row;
import com.sfms.mf07.domain.Step3Row;
import com.sfms.mf07.domain.StepStatus;
import com.sfms.mf07.dto.InspectionDtos.CoSignRequest;
import com.sfms.mf07.dto.InspectionDtos.InspectionSessionResponse;
import com.sfms.mf07.dto.InspectionDtos.MedicalReviewRequest;
import com.sfms.mf07.dto.InspectionDtos.OpenSessionRequest;
import com.sfms.mf07.dto.InspectionDtos.ServingGateResponse;
import com.sfms.mf07.dto.InspectionDtos.Step1DryRowDto;
import com.sfms.mf07.dto.InspectionDtos.Step1FreshRowDto;
import com.sfms.mf07.dto.InspectionDtos.Step2RowDto;
import com.sfms.mf07.dto.InspectionDtos.Step3RowDto;
import com.sfms.mf07.dto.InspectionDtos.SubmitStepRequest;
import com.sfms.mf07.error.InspExceptions;
import com.sfms.mf07.repository.InspectionSessionRepository;
import com.sfms.mf10.dto.NotificationDtos.NotificationCommand;
import com.sfms.mf10.port.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Hiện thực {@link InspectionService}.
 *
 * <p>⚠️ Simplification biết trước (Bước 2, ghi lại để nợ tài liệu rõ ràng thay vì im lặng):
 * <ul>
 *   <li>{@code GET /inspections/print} (endpoint #29, dựng bản in server-side) CHƯA hiện thực —
 *   FE hiện tự render tại chỗ từ chính {@code InspectionSessionResponse}, không có call-site
 *   nào phụ thuộc endpoint này (§9.1).</li>
 * </ul>
 *
 * <p>BR-095 "hệ thống tự động tạo CAPA" khi một bước trượt — {@code CapaPort} của MF11 đã nối
 * (04/08/2026, {@code M11-D08}). {@code corrective_actions} vẫn do MF11 sở hữu (M07-D08); gọi
 * qua port trong CÙNG transaction ghi bước trượt (CF-25).
 */
@Service
public class InspectionServiceImpl implements InspectionService {

    private static final Logger log = LoggerFactory.getLogger(InspectionServiceImpl.class);
    private static final DateTimeFormatter CODE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final InspectionSessionRepository sessionRepository;
    private final InspectionAssembler assembler;
    private final ReferenceResolver referenceResolver;
    private final CodeGenerator codeGenerator;
    private final com.sfms.mf11.port.CapaPort capaPort;
    private final NotificationPort notificationPort;
    private final Clock clock;

    public InspectionServiceImpl(InspectionSessionRepository sessionRepository, InspectionAssembler assembler,
                                  ReferenceResolver referenceResolver, CodeGenerator codeGenerator,
                                  com.sfms.mf11.port.CapaPort capaPort, NotificationPort notificationPort, Clock clock) {
        this.sessionRepository = sessionRepository;
        this.assembler = assembler;
        this.referenceResolver = referenceResolver;
        this.codeGenerator = codeGenerator;
        this.capaPort = capaPort;
        this.notificationPort = notificationPort;
        this.clock = clock;
    }

    // ═════════════════════════ Đọc (D11 — không lệch shape theo role, chỉ lệch TẬP DÒNG) ═════════════════════════

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<InspectionSessionResponse> list(ListQuery query, AuthUser actor) {
        UUID schoolId = resolveSchoolId(actor);
        UUID scopePartnerId = actor.role() == RoleKey.SERVICE_CATERERS
                ? resolveCatererPartnerId(actor, schoolId) : null;
        Page<InspectionSession> page = sessionRepository.findAll(
                InspectionSpec.build(query, schoolId, scopePartnerId), query.toPageable());
        List<InspectionSessionResponse> items = page.getContent().stream().map(assembler::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public InspectionSessionResponse detail(String code, AuthUser actor) {
        return assembler.toResponse(loadInScope(code, actor));
    }

    // ═════════════════════════ Mở phiên (UC-052) ═════════════════════════

    @Override
    @Transactional
    public InspectionSessionResponse open(OpenSessionRequest request, AuthUser actor) {
        UUID schoolId = resolveSchoolId(actor);
        UUID partnerId = resolveCatererPartnerId(actor, schoolId);
        if (schoolId == null || partnerId == null) {
            throw InspExceptions.accessDenied();
        }
        LocalDate date = parseDate(request.date());
        if (date == null) {
            throw InspExceptions.validationFailed("date", "invalid date: " + request.date());
        }
        // Kiểm thực 3 bước ghi nhận TẠI THỜI ĐIỂM thực tế nhập/chế biến/ăn (QĐ 1246/QĐ-BYT
        // Điều 3-5) — không phải khai báo hồi tố. Chỉ cho mở phiên của ngày hiện tại; phiên
        // ngày khác chỉ xem lại (read-only) qua danh sách lịch sử, không mở/sửa được nữa.
        LocalDate today = LocalDate.now(clock);
        if (!date.equals(today)) {
            throw InspExceptions.validationFailed("date", "chỉ được mở phiên kiểm thực của ngày hiện tại (" + today + ")");
        }

        // BR-079 — một bữa (ngày × buổi × đơn vị) chỉ có 1 phiên gốc còn hiệu lực.
        boolean slotTaken = sessionRepository
                .findFirstBySchoolIdAndDateAndMealTypeAndPartnerIdAndOverallNot(
                        schoolId, date, request.mealType(), partnerId, StepStatus.FAILED)
                .isPresent();
        if (slotTaken) {
            throw InspExceptions.duplicateValue("date", date + " " + request.mealType());
        }

        InspectionSession session = new InspectionSession();
        session.setCode(generateSessionCode(schoolId, date, request.mealType()));
        session.setSchoolId(schoolId);
        session.setPartnerId(partnerId);
        session.setDate(date);
        session.setMealType(request.mealType());
        if (request.dishId() != null) {
            referenceResolver.dishIdByCode(request.dishId()).ifPresent(session::setDishId);
        }
        session.setInspectorUserId(actor.userId());
        session.setOverall(StepStatus.IN_PROGRESS);   // CF-23

        sessionRepository.saveAndFlush(session);
        return assembler.toResponse(session);
    }

    /** {@code INSP-<yyyyMMdd>-<L|S|B>} — sinh theo ngày + buổi (không qua {@code code_sequences}). */
    private String generateSessionCode(UUID schoolId, LocalDate date, MealType mealType) {
        String suffix = switch (mealType) {
            case BREAKFAST -> "B";
            case LUNCH -> "L";
            case SNACK -> "S";
        };
        String base = "INSP-" + CODE_DATE.format(date) + "-" + suffix;
        String candidate = base;
        int n = 2;
        while (sessionRepository.existsBySchoolIdAndCode(schoolId, candidate)) {
            candidate = base + "-" + n++;
        }
        return candidate;
    }

    // ═════════════════════════ Nộp bước (UC-052/UC-065) ═════════════════════════

    @Override
    @Transactional
    public InspectionSessionResponse submitStep(String code, int step, SubmitStepRequest request, AuthUser actor) {
        // KHOÁ GHI hàng phiên: chốt bước xoá sạch dòng mẫu biểu rồi chèn lại, hai request chạy
        // song song sẽ để lại HAI bộ dòng trùng nhau (xem Javadoc findForUpdateBySchoolIdAndCode).
        InspectionSession session = loadInScope(code, actor, true);
        if (actor.role() != RoleKey.SERVICE_CATERERS) {
            throw InspExceptions.accessDenied();
        }

        // BR-079 — khóa tuần tự: bước N cần bước (N-1) đã PASSED.
        if (step == 2 && session.getStep1() != StepStatus.PASSED) {
            throw InspExceptions.stepLocked("Bước 2");
        }
        if (step == 3 && session.getStep2() != StepStatus.PASSED) {
            throw InspExceptions.stepLocked("Bước 3");
        }

        boolean passed = "PASSED".equalsIgnoreCase(request.result());

        switch (step) {
            case 1 -> applyStep1(session, request, passed);
            case 2 -> applyStep2(session, request, passed);
            case 3 -> applyStep3(session, request, passed);
            default -> throw InspExceptions.validationFailed("step", "must be 1, 2 or 3");
        }

        recomputeOverall(session, step, passed, request, actor);
        sessionRepository.saveAndFlush(session);
        return assembler.toResponse(session);
    }

    private void applyStep1(InspectionSession session, SubmitStepRequest request, boolean passed) {
        session.getStep1Fresh().clear();
        int order = 0;
        for (Step1FreshRowDto dto : orEmpty(request.step1Fresh())) {
            checkEvidence(dto.sensoryPass(), dto.photoCount());
            UUID batchId = resolveBatchId(dto.batchId(), session.getSchoolId());
            checkBatchExpiry(batchId, dto.batchId());
            Step1FreshRow row = new Step1FreshRow();
            row.setSession(session);
            row.setRowOrder(order++);
            row.setName(dto.name());
            row.setBatchId(batchId);
            row.setReceivedAt(dto.receivedAt());
            row.setQuantity(dto.quantity());
            row.setSupplierName(dto.supplierName());
            row.setSupplierContact(dto.supplierContact());
            row.setDeliverer(dto.deliverer());
            row.setHasInvoice(dto.hasInvoice());
            row.setHasVetCert(dto.hasVetCert());
            row.setHasQuarantineCert(dto.hasQuarantineCert());
            row.setSensoryPass(dto.sensoryPass());
            row.setQuickTest(dto.quickTest() == null ? "NONE" : dto.quickTest());
            row.setAction(dto.action());
            row.setPhotoCount(dto.photoCount());
            session.getStep1Fresh().add(row);
        }

        session.getStep1Dry().clear();
        order = 0;
        for (Step1DryRowDto dto : orEmpty(request.step1Dry())) {
            checkEvidence(dto.sensoryPass(), dto.photoCount());
            UUID batchId = resolveBatchId(dto.batchId(), session.getSchoolId());
            checkBatchExpiry(batchId, dto.batchId());
            Step1DryRow row = new Step1DryRow();
            row.setSession(session);
            row.setRowOrder(order++);
            row.setName(dto.name());
            row.setBatchId(batchId);
            row.setProducer(dto.producer());
            row.setProducerAddress(dto.producerAddress());
            row.setReceivedAt(dto.receivedAt());
            row.setQuantity(dto.quantity());
            row.setSupplierName(dto.supplierName());
            row.setDeliverer(dto.deliverer());
            row.setSupplierContact(dto.supplierContact());
            row.setExpiry(parseDate(dto.expiry()));
            row.setStorage(dto.storage());
            row.setHasInvoice(dto.hasInvoice());
            row.setSensoryPass(dto.sensoryPass());
            row.setAction(dto.action());
            row.setPhotoCount(dto.photoCount());
            session.getStep1Dry().add(row);
        }

        session.setStep1(passed ? StepStatus.PASSED : StepStatus.FAILED);
    }

    private void applyStep2(InspectionSession session, SubmitStepRequest request, boolean passed) {
        session.getStep2Rows().clear();
        int order = 0;
        for (Step2RowDto dto : orEmpty(request.step2Rows())) {
            checkEvidence(dto.sensoryPass(), dto.photoCount());
            Step2Row row = new Step2Row();
            row.setSession(session);
            row.setRowOrder(order++);
            row.setShift(dto.shift());
            row.setDishId(resolveDishId(dto.dishId()));
            row.setDishName(dto.dishName());
            row.setMainIngredients(dto.mainIngredients());
            row.setServings(dto.servings());
            row.setPrepDoneAt(dto.prepDoneAt());
            row.setCookDoneAt(dto.cookDoneAt());
            row.setHygieneStaff(dto.hygieneStaff());
            row.setHygieneEquipment(dto.hygieneEquipment());
            row.setHygieneArea(dto.hygieneArea());
            row.setSensoryPass(dto.sensoryPass());
            row.setCoreTemp(dto.coreTemp());
            row.setAction(dto.action());
            row.setPhotoCount(dto.photoCount());
            session.getStep2Rows().add(row);
        }

        // BR-090 — chốt PASSED phải có nhiệt độ lõi >= 75, KHÔNG tin client.
        if (passed) {
            BigDecimal coreTemp = request.coreTemp();
            if (coreTemp == null || coreTemp.compareTo(new BigDecimal("75")) < 0) {
                throw InspExceptions.coreTempLow();
            }
            session.setCoreTemp(coreTemp);
        }
        session.setStep2(passed ? StepStatus.PASSED : StepStatus.FAILED);
    }

    private void applyStep3(InspectionSession session, SubmitStepRequest request, boolean passed) {
        session.getStep3Rows().clear();
        int order = 0;
        for (Step3RowDto dto : orEmpty(request.step3Rows())) {
            checkEvidence(dto.sensoryPass(), dto.photoCount());
            Step3Row row = new Step3Row();
            row.setSession(session);
            row.setRowOrder(order++);
            row.setShift(dto.shift());
            row.setDishId(resolveDishId(dto.dishId()));
            row.setDishName(dto.dishName());
            row.setServings(dto.servings());
            row.setDividedDoneAt(dto.dividedDoneAt());
            row.setEatStartAt(dto.eatStartAt());
            row.setUtensilsPass(dto.utensilsPass());
            row.setUtensils(dto.utensils());
            row.setHoldingTemp(dto.holdingTemp());
            row.setSensoryPass(dto.sensoryPass());
            row.setAction(dto.action());
            row.setPhotoCount(dto.photoCount());
            session.getStep3Rows().add(row);
        }

        // BR-091 — CF-24: FE chưa có ô nhập holdingTemp ⇒ chỉ kiểm khi CÓ gửi giá trị.
        if (passed && request.holdingTemp() != null) {
            if (request.holdingTemp().compareTo(new BigDecimal("60")) < 0) {
                throw InspExceptions.holdingTempLow();
            }
            session.setHoldingTemp(request.holdingTemp());
        }
        session.setStep3(passed ? StepStatus.PASSED : StepStatus.FAILED);
    }

    private void recomputeOverall(InspectionSession session, int step, boolean passed, SubmitStepRequest request,
                                   AuthUser actor) {
        if (!passed) {
            // CF-25 — chính thức hoá 2026-08-03. Ghi lại lý do/mức độ/nhân chứng — kể cả khi
            // khắc phục được ngay tại chỗ (needsNewIngredients=false), để giữ dấu vết.
            session.setFailStep(step);
            session.setFailReason(request.failReason());
            session.setFailSeverity(parseSeverity(request.failSeverity()));
            session.setFailWitness(request.failWitness());
            session.setDecidedAt(clock.instant());
            // BR-095 — CAPA tự động, CÙNG transaction ghi bước trượt (M11-D08, nối 04/08/2026).
            // Vẫn tạo dù khắc phục tại chỗ — CAPA là dấu vết corrective action, không phụ thuộc
            // việc sau đó có phải huỷ cả bữa hay không.
            String stepLabel = "Bước " + step;
            String capaCode = capaPort.openFromFailedStep(session.getId(), stepLabel, request.failReason(),
                    session.getSchoolId(), actor == null ? null : actor.userId());
            log.info("INSPECTION_STEP_FAILED sessionCode={} step={} — CAPA {} (BR-095).",
                    session.getCode(), step, capaCode);

            notificationPort.publish(new NotificationCommand(session.getSchoolId(), false,
                    List.of("school-admin", "service-caterers"),
                    com.sfms.common.enums.NotificationType.INSPECTION_ALERT,
                    stepLabel + " KHÔNG đạt — " + session.getCode(),
                    stepLabel + " của phiên " + session.getCode() + " không đạt: " + request.failReason()
                            + " — đã tự tạo CAPA " + capaCode + " (BR-095).",
                    "/service-caterers/inspections/" + session.getCode(), "Xem phiên kiểm thực"));

            /*
             * 13/08/2026 (theo Nam) — trước đây MỌI bước trượt đều khoá cả phiên (overall=
             * FAILED), bắt tái kiểm toàn bộ từ Bước 1 dù lỗi chỉ là thao tác/vệ sinh sửa được
             * ngay. Tách theo `needsNewIngredients`:
             *   - true  → như cũ: overall=FAILED (bất biến, ADR-M07-02), chặn checkGate, bắt
             *             buộc reinspect() — ĐÚNG vì nguyên liệu mới phải qua lại Bước 1
             *             (QĐ 1246 Điều 3: kiểm nguyên liệu MỚI đưa vào chế biến).
             *   - false → giữ phiên MỞ (như một bước đang làm dở), bếp gọi lại CHÍNH
             *             submitStep(step) này để sửa & nộp lại, không mở phiên mới. Chỉ áp
             *             dụng khi phiên CHƯA từng FAILED thật (bất biến vẫn giữ khi đã chốt).
             */
            if (Boolean.TRUE.equals(request.needsNewIngredients())) {
                session.setOverall(StepStatus.FAILED);
            } else if (session.getOverall() != StepStatus.FAILED) {
                session.setOverall(StepStatus.IN_PROGRESS);
            }
            return;
        }
        if (step == 3) {
            // BR-098 — overall PASSED chỉ khi cả 3 bước PASSED.
            session.setOverall(StepStatus.PASSED);
            session.setDecidedAt(clock.instant());

            notificationPort.publish(new NotificationCommand(session.getSchoolId(), false,
                    List.of("school-admin", "service-caterers"),
                    com.sfms.common.enums.NotificationType.INSPECTION_ALERT,
                    "Kiểm thực 3 bước ĐẠT — " + session.getCode(),
                    "Phiên kiểm thực " + session.getCode() + " đã Đạt cả 3 bước — đủ điều kiện phục vụ (BR-094).",
                    "/service-caterers/inspections/" + session.getCode(), "Xem phiên kiểm thực"));

        } else if (session.getOverall() != StepStatus.FAILED) {
            session.setOverall(StepStatus.IN_PROGRESS);
        }
        // BR-083 mở rộng (10/08/2026) — cùng khuôn BR-073/074 (mf06) và BR-109 (mf08): toạ độ
        // nộp bước phải nằm trong geofence trường, không chỉ lưu chuỗi hiển thị như trước.
        if (request.gpsLat() != null && request.gpsLng() != null) {
            var geofence = referenceResolver.schoolGeofence(session.getSchoolId()).orElse(null);
            if (!GeofenceValidator.isInside(geofence, request.gpsLat(), request.gpsLng())) {
                throw InspExceptions.outsideGeofence();
            }
            session.setGps(request.gpsLat() + "," + request.gpsLng());
        } else if (request.gps() != null) {
            session.setGps(request.gps());
        }
    }

    // ═════════════════════════ Đồng ký (BR-099/BR-098) ═════════════════════════

    @Override
    @Transactional
    public InspectionSessionResponse coSign(String code, CoSignRequest request, AuthUser actor) {
        InspectionSession session = loadInScope(code, actor);
        if (session.getStep1() != StepStatus.PASSED || session.getStep2() != StepStatus.PASSED
                || session.getStep3() != StepStatus.PASSED) {
            throw InspExceptions.coSignBlocked("chưa hoàn tất");
        }
        UUID coSignerId = referenceResolver.userIdByCode(request.coSignerUserId()).orElse(actor.userId());
        session.setCoSignerUserId(coSignerId);
        session.setCoSignedAt(clock.instant());
        sessionRepository.saveAndFlush(session);
        return assembler.toResponse(session);
    }

    // ═════════════════════════ Tái kiểm tra (BR-096) ═════════════════════════

    @Override
    @Transactional
    public InspectionSessionResponse reinspect(String failedCode, AuthUser actor) {
        InspectionSession failed = loadInScope(failedCode, actor);
        if (failed.getOverall() != StepStatus.FAILED) {
            throw InspExceptions.validationFailed("id", "only a FAILED session can be re-inspected");
        }

        InspectionSession fresh = new InspectionSession();
        fresh.setCode(generateSessionCode(failed.getSchoolId(), failed.getDate(), failed.getMealType()));
        fresh.setSchoolId(failed.getSchoolId());
        fresh.setPartnerId(failed.getPartnerId());
        fresh.setDate(failed.getDate());
        fresh.setMealType(failed.getMealType());
        fresh.setMenuId(failed.getMenuId());
        fresh.setDishId(failed.getDishId());
        fresh.setInspectorUserId(actor.userId());
        fresh.setOverall(StepStatus.IN_PROGRESS);
        // §4.2 bất biến — phiên cũ giữ nguyên, KHÔNG sửa.
        sessionRepository.saveAndFlush(fresh);
        return assembler.toResponse(fresh);
    }

    // ═════════════════════════ Đánh giá Y tế (BR-222) ═════════════════════════

    @Override
    @Transactional
    public InspectionSessionResponse medicalReview(String code, MedicalReviewRequest request, AuthUser actor) {
        InspectionSession session = loadInScope(code, actor);
        if (actor.role() != RoleKey.MEDICAL_STAFF) {
            throw InspExceptions.accessDenied();
        }
        StepStatus result = StepStatus.valueOf(request.medicalReviewResult());
        if (result == StepStatus.FAILED
                && (request.medicalReviewReason() == null || request.medicalReviewReason().isBlank())) {
            throw InspExceptions.validationFailed("medicalReviewReason", "required when result is FAILED");
        }
        // BR-222 — KHÔNG ghi đè overall, KHÔNG đổi kết quả BR-094/BR-098.
        session.setMedicalReviewResult(result);
        session.setMedicalReviewReason(request.medicalReviewReason());
        session.setMedicalReviewBy(actor.userId());
        session.setMedicalReviewAt(clock.instant());
        sessionRepository.saveAndFlush(session);
        return assembler.toResponse(session);
    }

    // ═════════════════════════ Cổng chặn phục vụ (BR-094) ═════════════════════════

    @Override
    @Transactional(readOnly = true)
    public ServingGateResponse checkGate(LocalDate date, String mealType, AuthUser actor) {
        UUID schoolId = resolveSchoolId(actor);
        MealType meal = MealType.valueOf(mealType);
        // BR-094 — phiên MỚI NHẤT của bữa (kể cả FAILED); Caterer thu hẹp theo đơn vị mình,
        // vai trò khác (Teacher/SchoolAdmin) hỏi chung không lọc partner.
        var sessionOpt = actor.role() == RoleKey.SERVICE_CATERERS
                ? sessionRepository.findFirstBySchoolIdAndDateAndMealTypeAndPartnerIdOrderByCreatedAtDesc(
                        schoolId, date, meal, resolveCatererPartnerId(actor, schoolId))
                : sessionRepository.findFirstBySchoolIdAndDateAndMealTypeOrderByCreatedAtDesc(schoolId, date, meal);
        if (sessionOpt.isEmpty()) {
            return new ServingGateResponse(false, "NO_SESSION",
                    "No inspection session found for this meal slot (BR-079).", null);
        }
        InspectionSession session = sessionOpt.get();
        if (session.getOverall() == StepStatus.PASSED) {
            return new ServingGateResponse(true, null, null, session.getCode());
        }
        if (session.getOverall() == StepStatus.FAILED) {
            return new ServingGateResponse(false, "INSPECTION_FAILED",
                    "Bữa ăn không được phép phục vụ vì bước kiểm tra thất bại (BR-094).", session.getCode());
        }
        return new ServingGateResponse(false, "IN_PROGRESS",
                "Inspection session is still in progress (BR-098).", session.getCode());
    }

    // ═════════════════════════ Hỗ trợ ═════════════════════════

    private InspectionSession loadInScope(String code, AuthUser actor) {
        return loadInScope(code, actor, false);
    }

    /** @param forUpdate khoá hàng phiên (chỉ dùng cho đường GHI — xem {@code submitStep}). */
    private InspectionSession loadInScope(String code, AuthUser actor, boolean forUpdate) {
        UUID schoolId = resolveSchoolId(actor);
        InspectionSession session = (forUpdate
                ? sessionRepository.findForUpdateBySchoolIdAndCode(schoolId, code)
                : sessionRepository.findBySchoolIdAndCode(schoolId, code))
                .orElseThrow(InspExceptions::notFound);
        // BR-099 — Caterer chỉ đọc/ghi phiên của đơn vị mình.
        if (actor.role() == RoleKey.SERVICE_CATERERS
                && !session.getPartnerId().equals(resolveCatererPartnerId(actor, schoolId))) {
            throw InspExceptions.notFound();
        }
        return session;
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

    /**
     * {@code partnerId} của ĐƠN VỊ SUẤT ĂN đang hợp tác với trường — actor vai trò
     * {@code SERVICE_CATERERS} KHÔNG có claim {@code partnerId} trong JWT (chỉ tài khoản
     * {@code SUPPLIER} có), nên phải tra qua {@code supplier_partnerships} theo {@code
     * schoolId}. Bug tìm thấy 06/08/2026: 4 chỗ trong file này từng đọc thẳng
     * {@code actor.partnerId()} (luôn {@code null}) — mọi tài khoản service-caterers bị
     * chặn ở {@code open()}/{@code checkGate()}/{@code loadInScope()} dù JWT/role đúng.
     */
    private UUID resolveCatererPartnerId(AuthUser actor, UUID schoolId) {
        return actor.partnerId() != null ? actor.partnerId() : referenceResolver.approvedCatererPartnerId(schoolId);
    }

    private static void checkEvidence(boolean sensoryPass, int photoCount) {
        // BR-083/BR-207 — DB CHECK cũng chặn, nhưng ném lỗi nghiệp vụ ở đây để trả đúng
        // INSP-083/MSG-148 thay vì lộ constraint-violation 500.
        if (!sensoryPass && photoCount < 1) {
            throw InspExceptions.evidenceRequired();
        }
    }

    /**
     * BR-092/093 (backlog A1, sửa 08/08/2026) — resolve MÃ LÔ về UUID rồi mới kiểm hạn.
     *
     * <p>Bản trước gọi {@code parseUuid()} — một hàm NUỐT {@code IllegalArgumentException} rồi
     * trả {@code null}. FE luôn gửi mã lô ({@code "BATCH-GF-4412"}) nên nhánh đó luôn ném, luôn
     * ra {@code null}, và {@code checkBatchExpiry} lập tức {@code return} ⇒ luật chặn nguyên
     * liệu hết hạn vào bếp CHƯA TỪNG CHẠY, nhận hàng hết hạn vẫn ghi "Đạt". Nay mã sai thì
     * BÁO LỖI RÕ thay vì im lặng bỏ qua.
     *
     * <p>Bỏ trống vẫn hợp lệ: không phải dòng Mẫu số 1 nào cũng truy được về một lô trong kho
     * (nguyên liệu mua lẻ trong ngày), và cột {@code batch_id} là NULLABLE.
     */
    private UUID resolveBatchId(String raw, UUID schoolId) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return referenceResolver.batchIdByCode(raw, schoolId)
                .orElseThrow(() -> InspExceptions.validationFailed("batchId", "unknown batch: " + raw));
    }

    private void checkBatchExpiry(UUID batchId, String rawLabel) {
        if (batchId == null) {
            return;
        }
        LocalDate expiry = referenceResolver.batchExpiryDate(batchId);
        if (expiry != null && expiry.isBefore(LocalDate.now(clock))) {
            throw InspExceptions.batchExpired(rawLabel);
        }
    }

    /**
     * D1 — {@code dishId} trên các dòng Mẫu 2/3 là MÃ MÓN ({@code dishes.code}), đúng như
     * {@code InspectionAssembler} trả ra ({@code referenceResolver.dishCode(...)}). Cùng lỗi
     * với {@code batchId}: {@code parseUuid()} cũ nuốt lỗi ⇒ {@code step2_rows.dish_id} NULL ⇒
     * màn "Đăng ký lưu mẫu" không có món nào để chọn (bước lưu mẫu tắc, BR-110/111).
     */
    private UUID resolveDishId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ignored) {
            // Không phải UUID ⇒ mã nghiệp vụ, tra thật.
        }
        return referenceResolver.dishIdByCode(raw)
                .orElseThrow(() -> InspExceptions.validationFailed("dishId", "unknown dish: " + raw));
    }

    private static LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw);
        } catch (Exception e) {
            try {
                return LocalDate.parse(raw, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception e2) {
                return null;
            }
        }
    }

    private static com.sfms.mf07.domain.Severity parseSeverity(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return com.sfms.mf07.domain.Severity.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }
}
