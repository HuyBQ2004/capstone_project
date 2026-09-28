package com.sfms.mf07.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf07.domain.ChecklistItem;
import com.sfms.mf07.domain.ChecklistItemResult;
import com.sfms.mf07.domain.ConfirmStatus;
import com.sfms.mf07.domain.KitchenInspection;
import com.sfms.mf07.domain.StepStatus;
import com.sfms.mf07.dto.KitchenInspectionDtos.ItemFindingDto;
import com.sfms.mf07.dto.KitchenInspectionDtos.ItemResultDto;
import com.sfms.mf07.dto.KitchenInspectionDtos.ItemResultRequest;
import com.sfms.mf07.dto.KitchenInspectionDtos.KitchenInspectionResponse;
import com.sfms.common.storage.StorageService;
import com.sfms.mf07.domain.InspectionEvidence;
import com.sfms.mf07.dto.EvidenceDtos.EvidenceResponse;
import com.sfms.mf07.dto.EvidenceDtos.KitchenEvidenceSummary;
import com.sfms.mf07.service.EvidenceService.DownloadedFile;
import com.sfms.mf07.dto.KitchenInspectionDtos.SubmitKitchenInspectionRequest;
import com.sfms.mf07.dto.KitchenInspectionDtos.Warning;
import com.sfms.mf07.error.InspExceptions;
import com.sfms.mf07.error.InspMessages;
import com.sfms.mf07.repository.ChecklistItemRepository;
import com.sfms.mf07.repository.InspectionEvidenceRepository;
import com.sfms.mf07.repository.KitchenInspectionRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.Year;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class KitchenInspectionServiceImpl implements KitchenInspectionService {

    private static final DateTimeFormatter EVIDENCE_TIMESTAMP = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final KitchenInspectionRepository inspectionRepository;
    private final ChecklistItemRepository itemRepository;
    private final ReferenceResolver referenceResolver;
    private final CodeGenerator codeGenerator;
    private final EvidenceService evidenceService;
    private final InspectionEvidenceRepository evidenceRepository;
    private final StorageService storageService;
    private final Clock clock;

    public KitchenInspectionServiceImpl(KitchenInspectionRepository inspectionRepository,
                                         ChecklistItemRepository itemRepository, ReferenceResolver referenceResolver,
                                         CodeGenerator codeGenerator, EvidenceService evidenceService,
                                         InspectionEvidenceRepository evidenceRepository, StorageService storageService,
                                         Clock clock) {
        this.inspectionRepository = inspectionRepository;
        this.itemRepository = itemRepository;
        this.referenceResolver = referenceResolver;
        this.codeGenerator = codeGenerator;
        this.evidenceService = evidenceService;
        this.evidenceRepository = evidenceRepository;
        this.storageService = storageService;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<KitchenInspectionResponse> list(ListQuery query, AuthUser actor) {
        UUID schoolId = resolveSchoolId(actor);
        // System Admin (schoolId=null) thấy TOÀN BỘ trường — KitchenInspectionSpec.build()
        // tự bỏ qua predicate schoolId khi null (cùng khuôn InspectionSpec).
        Page<KitchenInspection> page = inspectionRepository.findAll(
                KitchenInspectionSpec.build(query, schoolId), query.toPageable());
        List<KitchenInspectionResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public KitchenInspectionResponse detail(String code, AuthUser actor) {
        return toResponse(load(code, actor));
    }

    @Override
    @Transactional
    public KitchenInspectionResponse submit(SubmitKitchenInspectionRequest request, AuthUser actor) {
        if (actor.role() != RoleKey.MEDICAL_STAFF) {
            throw InspExceptions.accessDenied();
        }
        UUID schoolId = resolveSchoolId(actor);

        KitchenInspection inspection = new KitchenInspection();
        inspection.setCode(codeGenerator.next("mf07_kitchen_inspection:" + Year.now(clock).getValue(),
                "KIN-" + Year.now(clock).getValue(), 3));
        inspection.setSchoolId(schoolId);
        inspection.setDate(java.time.LocalDate.now(clock));
        inspection.setInspectorUserId(actor.userId());
        inspection.setPestControlValid(request.pestControlValid());
        inspection.setStaffHealthValid(request.staffHealthValid());

        int total = 0;
        int passedCount = 0;
        for (ItemResultRequest ir : request.itemResults()) {
            // BR-207 — "Không đạt" bắt buộc ảnh + lý do.
            if (!ir.passed() && ((ir.photos() == null || ir.photos() < 1) || ir.reason() == null || ir.reason().isBlank())) {
                throw InspExceptions.checklistIncomplete();
            }
            ChecklistItem item = itemRepository.findBySchoolIdAndCode(schoolId, ir.item())
                    .orElseThrow(() -> InspExceptions.validationFailed("itemResults.item", "unknown checklist item: " + ir.item()));
            ChecklistItemResult result = new ChecklistItemResult();
            result.setResultId(UUID.randomUUID());   // V68 — owner_id cho ảnh bằng chứng riêng hạng mục
            result.setInspection(inspection);
            result.setItem(item);
            result.setPassed(ir.passed());
            result.setReason(ir.reason());
            result.setPhotoCount(ir.photos() == null ? 0 : ir.photos());
            inspection.getItemResults().add(result);
            total++;
            if (ir.passed()) {
                passedCount++;
            }
        }
        // BR-100/BR-104 — điểm tính lại server-side, KHÔNG tin số client (CF-26).
        int score = total == 0 ? 0 : Math.round(passedCount * 100f / total);
        inspection.setScore(score);
        inspection.setResult(score >= 60 ? StepStatus.PASSED : StepStatus.FAILED);
        inspection.setConfirmStatus(ConfirmStatus.AWAITING_CATERERS_CONFIRM);
        inspection.setRecordContent(autoRecordContent(inspection));

        inspectionRepository.saveAndFlush(inspection);
        return toResponse(inspection);
    }

    /** BR-221 mở rộng — nội dung khởi tạo của biên bản, dựng từ chính các hạng mục vừa
     *  chấm; Medical Staff sửa lại được qua {@link #updateRecord} trước khi bếp xác nhận. */
    private String autoRecordContent(KitchenInspection inspection) {
        List<String> failed = inspection.getItemResults().stream()
                .filter(r -> !r.isPassed())
                .map(r -> r.getItem().getName() + (r.getReason() == null || r.getReason().isBlank()
                        ? "" : " — " + r.getReason()))
                .toList();
        if (failed.isEmpty()) {
            return "Qua kiểm tra ngày " + inspection.getDate() + ", toàn bộ "
                    + inspection.getItemResults().size() + " hạng mục checklist đạt yêu cầu.";
        }
        return "Qua kiểm tra ngày " + inspection.getDate() + ", ghi nhận " + failed.size() + "/"
                + inspection.getItemResults().size() + " hạng mục không đạt:\n- "
                + String.join("\n- ", failed);
    }

    @Override
    @Transactional
    public KitchenInspectionResponse updateRecord(String code, String content, AuthUser actor) {
        if (actor.role() != RoleKey.MEDICAL_STAFF) {
            throw InspExceptions.accessDenied();
        }
        KitchenInspection inspection = load(code, actor);
        if (inspection.getConfirmStatus() == ConfirmStatus.CONFIRMED) {
            throw InspExceptions.stepLocked("Biên bản sự việc");
        }
        inspection.setRecordContent(content);
        inspectionRepository.saveAndFlush(inspection);
        return toResponse(inspection);
    }

    @Override
    @Transactional
    public KitchenInspectionResponse confirmByCaterers(String code, AuthUser actor) {
        if (actor.role() != RoleKey.SERVICE_CATERERS) {
            throw InspExceptions.accessDenied();
        }
        KitchenInspection inspection = load(code, actor);
        // BR-221 (mới 2026-08-03) — xác nhận hai chiều.
        inspection.setConfirmStatus(ConfirmStatus.CONFIRMED);
        inspection.setConfirmedBy(actor.userId());
        inspection.setConfirmedAt(clock.instant());
        inspectionRepository.saveAndFlush(inspection);
        return toResponse(inspection);
    }

    private KitchenInspection load(String code, AuthUser actor) {
        UUID schoolId = resolveSchoolId(actor);
        return inspectionRepository.findBySchoolIdAndCode(schoolId, code).orElseThrow(InspExceptions::notFound);
    }

    @Override
    @Transactional
    public EvidenceResponse uploadItemEvidence(String code, String itemCode, MultipartFile file, String gps,
                                                AuthUser actor) {
        if (actor.role() != RoleKey.MEDICAL_STAFF) {
            throw InspExceptions.accessDenied();
        }
        KitchenInspection inspection = load(code, actor);   // BR-099 — kiểm phạm vi trước khi ghi
        ChecklistItemResult result = inspection.getItemResults().stream()
                .filter(r -> r.getItem().getCode().equals(itemCode))
                .findFirst()
                .orElseThrow(() -> InspExceptions.validationFailed("itemCode", "unknown checklist item: " + itemCode));
        return evidenceService.upload("KITCHEN_ITEM", result.getResultId().toString(), file, gps, actor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KitchenEvidenceSummary> listEvidences(String code, AuthUser actor) {
        KitchenInspection inspection = load(code, actor);   // BR-099 — kiểm phạm vi trước khi đọc
        // 13/08/2026 (theo Nam) — `resultId -> itemCode` để trả kèm hạng mục thật, không còn bỏ phí
        // liên kết đã lưu sẵn lúc upload (`uploadItemEvidence` set ownerId = resultId).
        Map<UUID, String> itemCodeByResultId = inspection.getItemResults().stream()
                .collect(java.util.stream.Collectors.toMap(ChecklistItemResult::getResultId,
                        r -> r.getItem().getCode()));
        List<UUID> resultIds = List.copyOf(itemCodeByResultId.keySet());
        if (resultIds.isEmpty()) {
            return List.of();
        }
        return evidenceRepository.findByOwnerTypeAndOwnerIdInOrderByUploadedAtDesc("KITCHEN_ITEM", resultIds).stream()
                .map(e -> new KitchenEvidenceSummary(e.getId().toString(),
                        itemCodeByResultId.get(e.getOwnerId()), e.getFileName(),
                        EVIDENCE_TIMESTAMP.format(e.getUploadedAt().atZone(ZoneOffset.UTC))))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadedFile downloadEvidence(String code, String evidenceId, AuthUser actor) {
        KitchenInspection inspection = load(code, actor);   // BR-099 — kiểm phạm vi trước khi trả file
        List<UUID> resultIds = inspection.getItemResults().stream().map(ChecklistItemResult::getResultId).toList();
        UUID id;
        try {
            id = UUID.fromString(evidenceId);
        } catch (IllegalArgumentException e) {
            throw InspExceptions.validationFailed("id", "invalid UUID: " + evidenceId);
        }
        InspectionEvidence evidence = evidenceRepository.findById(id)
                .filter(e -> "KITCHEN_ITEM".equals(e.getOwnerType()) && resultIds.contains(e.getOwnerId()))
                .orElseThrow(InspExceptions::notFound);
        return new DownloadedFile(storageService.load(evidence.getStorageKey()), evidence.getFileName());
    }

    private KitchenInspectionResponse toResponse(KitchenInspection k) {
        String[] inspector = referenceResolver.userById(k.getInspectorUserId());
        List<String> failedItems = new ArrayList<>();
        List<ItemFindingDto> findings = new ArrayList<>();
        List<ItemResultDto> results = new ArrayList<>();
        int photosCount = 0;
        for (ChecklistItemResult r : k.getItemResults()) {
            results.add(new ItemResultDto(r.getItem().getCode(), r.isPassed()));
            photosCount += r.getPhotoCount();
            if (!r.isPassed()) {
                failedItems.add(r.getItem().getName());
                findings.add(new ItemFindingDto(r.getItem().getName(), r.getPhotoCount(), r.getReason()));
            }
        }
        List<Warning> warnings = k.getScore() < 60
                ? List.of(new Warning("MSG-093", InspMessages.MSG_093)) : List.of();

        String[] confirmer = k.getConfirmedBy() == null ? null : referenceResolver.userById(k.getConfirmedBy());
        return new KitchenInspectionResponse(k.getCode(), k.getDate().toString(), k.getScore(),
                inspector == null ? null : inspector[2], k.isPestControlValid(), k.isStaffHealthValid(),
                k.getResult().name(), failedItems, findings, photosCount, k.getConfirmStatus().name(),
                results, warnings,
                confirmer == null ? null : confirmer[2],
                k.getConfirmedAt() == null ? null : k.getConfirmedAt().toString(),
                k.getRecordContent());
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
