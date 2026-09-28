package com.sfms.mf08.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf08.domain.AttachmentKind;
import com.sfms.mf08.domain.AttachmentOwner;
import com.sfms.mf08.domain.FoodSample;
import com.sfms.mf08.domain.FoodSampleStatus;
import com.sfms.mf08.domain.RetentionPolicy;
import com.sfms.mf08.domain.SampleLabelReservationStatus;
import com.sfms.mf08.domain.SampleUnit;
import com.sfms.mf08.dto.FoodSampleDtos.DisposeSampleRequest;
import com.sfms.mf08.dto.FoodSampleDtos.FoodSampleResponse;
import com.sfms.mf08.dto.FoodSampleDtos.IncidentLinkRequest;
import com.sfms.mf08.dto.FoodSampleDtos.Mau4LabelResponse;
import com.sfms.mf08.dto.FoodSampleDtos.RegisterSampleRequest;
import com.sfms.mf08.dto.FoodSampleDtos.UpdateStorageRequest;
import com.sfms.mf08.error.SmpExceptions;
import com.sfms.mf08.port.IncidentStatusPort;
import com.sfms.mf08.repository.FoodSampleRepository;
import com.sfms.mf08.repository.SampleLabelReservationRepository;
import com.sfms.mf10.dto.NotificationDtos.NotificationCommand;
import com.sfms.mf10.port.NotificationPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Hiện thực {@link FoodSampleService}.
 *
 * <p>⚠️ Simplification biết trước (ghi nợ tài liệu, không giấu — theo tinh thần MF07):
 * <ul>
 *   <li>{@link #retrievableForIncident} chưa lọc theo {@code suspectedDishIds} — MF11 chưa
 *   tồn tại nên không có danh sách món nghi ngờ để so khớp; tham số {@code includeAllDishes}
 *   được chấp nhận nhưng hiện luôn hành xử như {@code true}. Nối khi MF11 lên bean.</li>
 *   <li>{@code incidentId} nhận từ client (mã dạng "INC-2026-014") — VÁ 05/08/2026: nay resolve
 *   THẬT qua {@link IncidentStatusPort#resolveByCode} (MF11 đã merge); response
 *   {@code incidentId} vẫn trả UUID nội bộ (chưa đổi shape response, §2.5 mục 2 TDS-MF08 —
 *   FE tự tra ngược qua danh sách incidents nếu cần hiển thị mã).</li>
 * </ul>
 */
@Service
public class FoodSampleServiceImpl implements FoodSampleService {

    private static final DateTimeFormatter CODE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final UUID NO_MATCH = new UUID(0L, 0L);

    private final FoodSampleRepository foodSampleRepository;
    private final SampleLabelReservationRepository reservationRepository;
    private final FoodSampleAssembler assembler;
    private final ReferenceResolver referenceResolver;
    private final CodeGenerator codeGenerator;
    private final AttachmentSupport attachmentSupport;
    private final IncidentStatusPort incidentStatusPort;
    private final NotificationPort notificationPort;
    private final Clock clock;

    public FoodSampleServiceImpl(FoodSampleRepository foodSampleRepository,
                                  SampleLabelReservationRepository reservationRepository,
                                  FoodSampleAssembler assembler,
                                  ReferenceResolver referenceResolver, CodeGenerator codeGenerator,
                                  AttachmentSupport attachmentSupport, IncidentStatusPort incidentStatusPort,
                                  NotificationPort notificationPort, Clock clock) {
        this.foodSampleRepository = foodSampleRepository;
        this.reservationRepository = reservationRepository;
        this.assembler = assembler;
        this.referenceResolver = referenceResolver;
        this.codeGenerator = codeGenerator;
        this.attachmentSupport = attachmentSupport;
        this.incidentStatusPort = incidentStatusPort;
        this.notificationPort = notificationPort;
        this.clock = clock;
    }

    // ═════════════════════════ Đọc (D11 — không lệch shape theo role, chỉ lệch TẬP DÒNG, M08-D13) ═════════════════════════

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<FoodSampleResponse> list(ListQuery query, AuthUser actor) {
        UUID schoolId = resolveSchoolId(actor);
        UUID dishFilter = resolveDishFilter(query);
        UUID incidentFilter = resolveIncidentFilter(query, schoolId);
        Page<FoodSample> page = foodSampleRepository.findAll(
                FoodSampleSpec.build(query, schoolId, dishFilter, incidentFilter),
                query.toPageable(Sort.by(Sort.Direction.DESC, "collectedAt")));
        List<FoodSampleResponse> items = page.getContent().stream().map(assembler::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public FoodSampleResponse getById(String code, AuthUser actor) {
        return assembler.toResponse(loadInScope(code, actor));
    }

    // ═════════════════════════ Đăng ký mẫu (UC-069) ═════════════════════════

    @Override
    @Transactional
    public FoodSampleResponse register(RegisterSampleRequest req, List<MultipartFile> evidencePhotos,
                                        String deviceInfo, AuthUser actor) {
        if (actor.role() != RoleKey.SERVICE_CATERERS) {
            throw SmpExceptions.accessDenied();
        }
        UUID schoolId = resolveSchoolId(actor);
        if (schoolId == null) {
            throw SmpExceptions.accessDenied();
        }

        // C4 — thứ tự cổng bắt buộc: BR-105 -> BR-109 -> BR-203 -> BR-107 -> BR-110.

        // BR-105 — phiên kiểm thực PASSED 3/3 (M08-D07: ReferenceResolver, KHÔNG InspectionGatePort).
        UUID inspectionId = referenceResolver.inspectionIdByCode(req.inspectionId(), schoolId)
                .orElseThrow(SmpExceptions::inspectionNotPassed);
        if (!referenceResolver.inspectionPassedAllSteps(inspectionId, schoolId)) {
            throw SmpExceptions.inspectionNotPassed();
        }

        UUID dishId = referenceResolver.dishIdByCode(req.dishId())
                .orElseThrow(() -> SmpExceptions.validationFailed("dishId", "unknown dish: " + req.dishId()));

        // Điều 7.1 — mỗi món của một phiên kiểm thực chỉ lưu 1 mẫu.
        if (foodSampleRepository.existsByInspectionIdAndDishId(inspectionId, dishId)) {
            throw SmpExceptions.duplicateValue("dishId", req.dishId());
        }

        // BR-109 — geofence (M08-D06: ReferenceResolver.schoolGeofence, KHÔNG GeofenceQueryPort).
        var geofence = referenceResolver.schoolGeofence(schoolId).orElse(null);
        if (!GeofenceValidator.isInside(geofence, req.gpsLat(), req.gpsLng())) {
            throw SmpExceptions.outsideGeofence();
        }

        // BR-203 — khối lượng theo loại + dụng cụ tiệt trùng/niêm phong.
        SampleUnit unit = parseUnit(req.unit());
        BigDecimal threshold = unit == SampleUnit.G ? new BigDecimal("100") : new BigDecimal("150");
        if (req.amount() == null || req.amount().compareTo(threshold) < 0) {
            throw SmpExceptions.amountTooLow();
        }
        if (!req.sanitized() || !req.sealed()) {
            throw SmpExceptions.containerStateInvalid();
        }

        // BR-107 — ảnh bằng chứng bắt buộc.
        if (evidencePhotos == null || evidencePhotos.stream().allMatch(f -> f == null || f.isEmpty())) {
            throw SmpExceptions.evidenceRequired();
        }

        Instant collectedAt = req.collectedAt() != null ? req.collectedAt() : clock.instant();
        String actorName = resolveActorName(actor);

        FoodSample sample = new FoodSample();
        sample.setCode(resolveCreateCode(req.id(), schoolId, collectedAt));
        sample.setSchoolId(schoolId);
        sample.setInspectionId(inspectionId);
        sample.setDishId(dishId);
        sample.setDishName(referenceResolver.dishName(dishId));
        sample.setMealType(req.mealType());
        sample.setMealShift(req.mealShift());
        sample.setServings(req.servings());
        sample.setSampleAmount(req.amount());
        sample.setSampleUnit(unit);
        sample.setContainer(req.container());
        sample.setSanitized(req.sanitized());
        sample.setSealed(req.sealed());

        // BR-110 — cảnh báo mềm (MSG-020), KHÔNG chặn đăng ký, KHÔNG job cảnh báo 30 phút (CF-M08-3).
        BigDecimal temp = req.fridgeTemp();
        sample.setFridgeTemp(temp);
        sample.setTempOutOfRange(temp.compareTo(new BigDecimal("2")) < 0 || temp.compareTo(new BigDecimal("8")) > 0);

        sample.setCollectedAt(collectedAt);
        // BR-114/BR-108 — danh tính + GPS/thiết bị từ JWT/server, KHÔNG tin body cho collectedBy.
        sample.setCollectedBy(actorName);
        sample.setStorageLocation(req.storageLocation());
        sample.setRetentionDeadline(collectedAt.plus(RetentionPolicy.MIN_RETENTION_HOURS, ChronoUnit.HOURS));
        sample.setStatus(FoodSampleStatus.RETAINED);
        sample.setGpsLat(req.gpsLat());
        sample.setGpsLng(req.gpsLng());
        sample.setDeviceInfo(deviceInfo);

        foodSampleRepository.saveAndFlush(sample);
        attachmentSupport.store(AttachmentOwner.SAMPLE, sample.getId(), AttachmentKind.EVIDENCE_PHOTO, evidencePhotos);

        // In tem TRƯỚC (10/08/2026) — nếu `id` gửi lên trùng một mã đã đặt trước tem
        // (`sample-label-reservations`, cùng scope bộ đếm — xem `resolveCreateCode`), coi như
        // "đã dán tem" và đóng vòng đời chỗ đặt trước, KHÔNG bắt FE gọi thêm request nào khác.
        reservationRepository.findBySchoolIdAndCode(schoolId, sample.getCode())
                .filter(r -> r.getStatus() == SampleLabelReservationStatus.PENDING)
                .ifPresent(r -> {
                    r.setStatus(SampleLabelReservationStatus.COLLECTED);
                    reservationRepository.saveAndFlush(r);
                });

        // Báo cáo tiến độ lưu mẫu (10/08/2026, theo yêu cầu Nam) — Y tế/trường theo dõi
        // TỪNG lần lưu mẫu, không phải chỉ khi có sự cố; không urgent nên KHÔNG kèm push riêng.
        notificationPort.publish(new NotificationCommand(schoolId, false, List.of("medical-staff", "school-admin"),
                com.sfms.common.enums.NotificationType.SAMPLE_ALERT,
                "Đã lưu mẫu — " + sample.getCode(),
                "Đã lưu mẫu \"" + sample.getDishName() + "\" (" + amountLabel(sample) + "), hạn lưu tối thiểu 24h kể từ "
                        + "thời điểm lấy mẫu (BR-111).",
                "/medical-staff/samples", "Xem mẫu lưu"));

        return assembler.toResponse(sample);
    }

    private static String amountLabel(FoodSample s) {
        return (s.getSampleAmount() == null ? "" : s.getSampleAmount().stripTrailingZeros().toPlainString())
                + " " + (s.getSampleUnit() == null ? "" : s.getSampleUnit().name().equals("G") ? "g" : "ml");
    }

    // ═════════════════════════ Cập nhật lưu trữ (UC-042 AF2) ═════════════════════════

    @Override
    @Transactional
    public FoodSampleResponse updateStorage(String code, UpdateStorageRequest req, AuthUser actor) {
        if (actor.role() != RoleKey.SERVICE_CATERERS) {
            throw SmpExceptions.accessDenied();
        }
        FoodSample sample = loadInScope(code, actor);
        boolean wasOutOfRange = sample.isTempOutOfRange();
        sample.setStorageLocation(req.storageLocation());
        if (req.fridgeTemp() != null) {
            sample.setFridgeTemp(req.fridgeTemp());
            sample.setTempOutOfRange(req.fridgeTemp().compareTo(new BigDecimal("2")) < 0
                    || req.fridgeTemp().compareTo(new BigDecimal("8")) > 0);
        }
        foodSampleRepository.saveAndFlush(sample);

        // BR-110 vốn chỉ là cảnh báo MỀM (đánh dấu cờ, không job/không alert nào trước đây) —
        // báo ngay lúc chuyển từ trong dải sang NGOÀI dải (10/08/2026), không báo lặp lại mỗi
        // lần cập nhật nếu đã ngoài dải từ trước.
        if (!wasOutOfRange && sample.isTempOutOfRange()) {
            notificationPort.publish(new NotificationCommand(sample.getSchoolId(), false,
                    List.of("medical-staff", "school-admin"),
                    com.sfms.common.enums.NotificationType.SAMPLE_ALERT,
                    "Nhiệt độ bảo quản mẫu lưu ngoài dải — " + sample.getCode(),
                    "Mẫu \"" + sample.getDishName() + "\" (" + sample.getCode() + ") đang ở " + sample.getFridgeTemp()
                            + "°C, ngoài dải 2–8°C bắt buộc (Điều 8.1/BR-110).",
                    "/medical-staff/samples", "Xem mẫu lưu"));
        }

        return assembler.toResponse(sample);
    }

    // ═════════════════════════ Hủy mẫu (UC-030) ═════════════════════════

    @Override
    @Transactional
    public FoodSampleResponse dispose(String code, DisposeSampleRequest req, AuthUser actor) {
        if (actor.role() != RoleKey.SERVICE_CATERERS) {
            throw SmpExceptions.accessDenied();
        }
        FoodSample sample = loadInScope(code, actor);

        // BR-112 (backlog B3) — mẫu đang bị GIỮ để điều tra, hoặc đã bàn giao đi xét nghiệm,
        // thì KHÔNG còn là thứ bếp được quyền hủy: HOLD nghĩa là mẫu vẫn nằm trong tủ nhưng đã
        // thuộc về hồ sơ điều tra, SENT_TO_LAB nghĩa là mẫu không còn ở trường. Kiểm TRƯỚC cả
        // mốc BR-111 vì đây là lý do từ chối đúng nghĩa hơn "chưa đủ 24h".
        if (sample.getStatus() == FoodSampleStatus.HOLD || sample.getStatus() == FoodSampleStatus.SENT_TO_LAB) {
            throw SmpExceptions.disposalLocked(
                    sample.getIncidentId() == null ? sample.getStatus().name() : sample.getIncidentId().toString());
        }

        Instant disposedAt = req.disposedAt() != null ? req.disposedAt() : clock.instant();
        // BR-111 — chưa hết hạn lưu tối thiểu 24h.
        if (disposedAt.isBefore(sample.getRetentionDeadline())) {
            long hoursLeft = (Duration.between(clock.instant(), sample.getRetentionDeadline()).getSeconds() + 3599) / 3600;
            throw SmpExceptions.retentionNotElapsed(hoursLeft);
        }
        // BR-112 — ngoại lệ bắt buộc: khoá hủy khi có sự cố mở liên kết.
        if (sample.getIncidentId() != null && incidentStatusPort.hasOpenIncidentLinked(sample.getIncidentId())) {
            throw SmpExceptions.disposalLocked(sample.getIncidentId().toString());
        }
        // Mẫu số 5 cột 9/10/12 — hủy phải ghi đủ.
        if (isBlank(req.witness()) || isBlank(req.note())) {
            throw SmpExceptions.disposalIncomplete();
        }

        // ⚠️ resolveActorName() chạy native query qua ReferenceResolver — PHẢI gọi TRƯỚC khi
        // đụng bất kỳ setter nào của `sample`. Hibernate auto-flush trước mỗi query sẽ đẩy
        // entity đang DỞ DANG (status=DISPOSED nhưng witness/note chưa set) xuống DB, vỡ
        // ck_smp_dispose_complete. Đã đo được lỗi này thật khi viết IT test cho Bước 2.
        String actorName = resolveActorName(actor);
        sample.setStatus(FoodSampleStatus.DISPOSED);
        sample.setDisposedAt(disposedAt);
        sample.setDisposedBy(actorName);
        sample.setDisposalWitness(req.witness());
        sample.setDisposalNote(req.note());
        foodSampleRepository.saveAndFlush(sample);
        return assembler.toResponse(sample);
    }

    // ═════════════════════════ Liên kết mẫu đã hủy với sự cố (UC-076 AF1) ═════════════════════════

    @Override
    @Transactional
    public FoodSampleResponse linkDisposedToIncident(String code, IncidentLinkRequest req, AuthUser actor) {
        if (actor.role() != RoleKey.MEDICAL_STAFF) {
            throw SmpExceptions.accessDenied();
        }
        FoodSample sample = loadInScope(code, actor);
        if (sample.getStatus() != FoodSampleStatus.DISPOSED) {
            throw SmpExceptions.validationFailed("id", "only a DISPOSED sample can be linked (UC-076 AF1)");
        }
        UUID incidentId = resolveIncidentId(req.incidentId(), sample.getSchoolId());
        incidentStatusPort.findOpen(incidentId, sample.getSchoolId()).orElseThrow(SmpExceptions::incidentInvalid);

        sample.setIncidentId(incidentId);
        foodSampleRepository.saveAndFlush(sample);
        return assembler.toResponse(sample);
    }

    // ═════════════════════════ Nhãn Mẫu số 4 (UC-069 bước 4) ═════════════════════════

    @Override
    @Transactional(readOnly = true)
    public Mau4LabelResponse buildLabel(String code, AuthUser actor) {
        FoodSample sample = loadInScope(code, actor);
        String facility = referenceResolver.schoolName(sample.getSchoolId());
        return new Mau4LabelResponse(sample.getCode(), facility, sample.getDishName(),
                sample.getMealType() == null ? null : sample.getMealType().name(), sample.getMealShift(),
                sample.getCollectedAt(), sample.getCollectedBy());
    }

    // ═════════════════════════ Mẫu thu hồi được cho một sự cố (dùng bởi SampleLabRequestService) ═════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<FoodSampleResponse> retrievableForIncident(UUID schoolId, UUID incidentId, boolean includeAllDishes) {
        var window = incidentStatusPort.windowOf(incidentId);
        List<FoodSample> inWindow = foodSampleRepository.findBySchoolIdAndStatusAndCollectedAtBetween(
                schoolId, FoodSampleStatus.RETAINED, window.from(), window.to());
        // Gồm cả HOLD: mẫu y tế đã bấm giữ cho chính sự cố này vẫn phải nằm trong danh sách,
        // nếu không nó biến mất khỏi màn ngay sau thao tác giữ (backlog B3).
        List<FoodSample> alreadyLinked = foodSampleRepository.findBySchoolIdAndIncidentIdAndStatusIn(
                schoolId, incidentId, List.of(FoodSampleStatus.RETAINED, FoodSampleStatus.HOLD));

        java.util.LinkedHashMap<UUID, FoodSample> merged = new java.util.LinkedHashMap<>();
        inWindow.forEach(s -> merged.put(s.getId(), s));
        alreadyLinked.forEach(s -> merged.put(s.getId(), s));
        // includeAllDishes: chấp nhận tham số nhưng CHƯA lọc theo suspectedDishIds — MF11 chưa
        // có danh sách món nghi ngờ để so khớp (xem Javadoc lớp này).
        return merged.values().stream().map(assembler::toResponse).toList();
    }

    // ═════════════════════════ Hỗ trợ ═════════════════════════

    /**
     * D1 — {@code raw} là MÃ NGHIỆP VỤ (vd {@code "INC-2026-014"}), resolve qua
     * {@link IncidentStatusPort#resolveByCode} (repository thật, MF11 đã merge — M11-D08).
     *
     * <p>⚠️ Bản trước (tới 05/08/2026) KHÔNG tra cứu thật: parse UUID trực tiếp, thất bại thì
     * BĂM {@code raw} thành một UUID xác định qua {@code UUID.nameUUIDFromBytes()} — một UUID
     * KHÔNG liên quan gì tới sự cố thật trong bảng {@code incidents}. Hệ quả:
     * {@code linkDisposedToIncident()}/{@code handOver()} LUÔN 409 SMP-023 cho MỌI mã nghiệp vụ
     * hợp lệ (không tồn tại UUID nào khớp) — toàn bộ 2 luồng đó chưa từng chạy được.
     */
    UUID resolveIncidentId(String raw, UUID schoolId) {
        return incidentStatusPort.resolveByCode(raw, schoolId)
                .map(com.sfms.mf08.port.IncidentStatusPort.IncidentRef::id)
                .orElseThrow(SmpExceptions::incidentInvalid);
    }

    private FoodSample loadInScope(String code, AuthUser actor) {
        UUID schoolId = resolveSchoolId(actor);
        return foodSampleRepository.findBySchoolIdAndCode(schoolId, code).orElseThrow(SmpExceptions::notFound);
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadedFile downloadEvidencePhoto(String code, String fileId, AuthUser actor) {
        FoodSample sample = loadInScope(code, actor);
        return attachmentSupport.download(AttachmentOwner.SAMPLE, sample.getId(), UUID.fromString(fileId));
    }

    /**
     * {@code ?incidentId=INC-2026-005} → UUID nội bộ (D1, sửa 08/08/2026).
     *
     * <p>Trước đây filter này so chuỗi mã nghiệp vụ THẲNG với cột {@code incident_id} kiểu UUID
     * nên KHÔNG BAO GIỜ khớp — mọi lời gọi {@code GET /samples?incidentId=INC-…} trả rỗng, im
     * lặng. Mã không tồn tại ⇒ {@link #NO_MATCH} để kết quả rỗng thật sự, không phải "bỏ qua
     * filter rồi trả hết mẫu của trường" (cùng cách xử lý với {@code resolveDishFilter}).
     */
    UUID resolveIncidentFilter(ListQuery query, UUID schoolId) {
        String raw = query.filters().get("incidentId");
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return incidentStatusPort.resolveByCode(raw, schoolId)
                .map(com.sfms.mf08.port.IncidentStatusPort.IncidentRef::id)
                .orElse(NO_MATCH);
    }

    private UUID resolveDishFilter(ListQuery query) {
        String dishParam = query.filters().get("dish");
        if (dishParam == null || dishParam.isBlank()) {
            return null;
        }
        return referenceResolver.dishIdByCode(dishParam).orElse(NO_MATCH);
    }

    private String resolveCreateCode(String clientId, UUID schoolId, Instant collectedAt) {
        if (clientId != null && !clientId.isBlank()) {
            if (foodSampleRepository.existsBySchoolIdAndCode(schoolId, clientId)) {
                throw SmpExceptions.duplicateValue("id", clientId);
            }
            return clientId;
        }
        // M08-D09 (C1) — scope nhúng ngày, KHÔNG đổi bảng code_sequences dùng chung.
        String dateStr = CODE_DATE.format(collectedAt.atZone(ZoneOffset.UTC));
        return codeGenerator.next("mf08_food_sample:" + dateStr, "SMP-" + dateStr, 2);
    }

    private String resolveActorName(AuthUser actor) {
        String[] user = referenceResolver.userById(actor.userId());
        return user != null && user[2] != null && !user[2].isBlank() ? user[2] : actor.email();
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

    private static SampleUnit parseUnit(String raw) {
        try {
            return SampleUnit.valueOf(raw);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw SmpExceptions.validationFailed("unit", "must be G or ML");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
