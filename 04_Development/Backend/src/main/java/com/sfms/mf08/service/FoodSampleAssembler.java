package com.sfms.mf08.service;

import com.sfms.common.reference.ReferenceResolver;
import com.sfms.mf08.domain.AttachmentKind;
import com.sfms.mf08.domain.AttachmentOwner;
import com.sfms.mf08.domain.FoodSample;
import com.sfms.mf08.dto.CommonDtos.UploadedFileDto;
import com.sfms.mf08.dto.FoodSampleDtos.FoodSampleResponse;
import com.sfms.mf08.repository.SampleAttachmentRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.util.List;

/**
 * Ánh xạ {@link FoodSample} ↔ {@link FoodSampleResponse}. {@code hoursRemaining} tính RUNTIME
 * (ADR-M08-3) — KHÔNG có cột, không cache. {@code weight}/{@code meal} là nhãn dẫn xuất giữ
 * tương thích FE (CF-M08-7/8, D12/M08-D14).
 */
@Component
public class FoodSampleAssembler {

    private final ReferenceResolver referenceResolver;
    private final SampleAttachmentRepository attachmentRepository;
    private final Clock clock;

    public FoodSampleAssembler(ReferenceResolver referenceResolver,
                               SampleAttachmentRepository attachmentRepository, Clock clock) {
        this.referenceResolver = referenceResolver;
        this.attachmentRepository = attachmentRepository;
        this.clock = clock;
    }

    public FoodSampleResponse toResponse(FoodSample s) {
        String dishCode = referenceResolver.dishCode(s.getDishId());
        String inspectionCode = referenceResolver.inspectionCode(s.getInspectionId());

        // ADR-M08-3: hoursRemaining = max(0, ceil(Duration.between(now, retentionDeadline))) — tính RUNTIME.
        long secondsLeft = Duration.between(clock.instant(), s.getRetentionDeadline()).getSeconds();
        long hoursRemaining = secondsLeft <= 0 ? 0 : (secondsLeft + 3599) / 3600;

        List<UploadedFileDto> evidencePhotos = attachmentRepository
                .findByOwnerTypeAndOwnerIdAndKindOrderByCreatedAtAsc(
                        AttachmentOwner.SAMPLE, s.getId(), AttachmentKind.EVIDENCE_PHOTO)
                .stream()
                .map(a -> new UploadedFileDto(a.getId().toString(), a.getFileName(), a.getCreatedAt(), a.getSizeBytes()))
                .toList();

        BigDecimal amount = s.getSampleAmount();
        String unit = s.getSampleUnit() == null ? null : s.getSampleUnit().name();
        String weight = amount == null || unit == null ? null
                : amount.stripTrailingZeros().toPlainString() + " " + (unit.equals("G") ? "g" : "ml");
        String meal = s.getMealType() == null ? null
                : mealLabel(s.getMealType().name()) + " " + dayMonth(s.getCollectedAt());

        return new FoodSampleResponse(
                s.getCode(), s.getDishName(), dishCode,
                s.getMealType() == null ? null : s.getMealType().name(), meal, s.getMealShift(), s.getServings(),
                amount, unit, weight, s.getContainer(), s.isSanitized(), s.isSealed(),
                s.getFridgeTemp(), s.isTempOutOfRange(), s.getCollectedAt(), s.getCollectedBy(),
                s.getStorageLocation(), s.getRetentionDeadline(), hoursRemaining,
                s.getStatus() == null ? null : s.getStatus().name(),
                s.getDisposedAt(), s.getDisposedBy(), s.getDisposalWitness(), s.getDisposalNote(),
                s.getLabRequestId() == null ? null : s.getLabRequestId().toString(),
                s.getRetrievedAt(), s.getSentToLabAt(),
                /*
                 * D1 — trả MÃ NGHIỆP VỤ ("INC-2026-005"), không phải UUID nội bộ (sửa
                 * 08/08/2026). Gap "§2.5 mục 2 TDS-MF08" ghi ở đây từ 05/08 nay bịt được vì
                 * `ReferenceResolver.incidentCode()` đã có sẵn (MF06 dùng cho `quarantine`).
                 *
                 * Hậu quả đo được của việc trả UUID: màn điều tra sự cố của Y tế lọc
                 * `s.incidentId === incident.id` với `incident.id` là mã ⇒ KHÔNG BAO GIỜ khớp,
                 * nên sự cố đang giữ 1 mẫu `HOLD` vẫn ghi "Chưa gửi mẫu nào (không bắt buộc)".
                 */
                referenceResolver.incidentCode(s.getIncidentId()),
                inspectionCode, evidencePhotos);
    }

    private static String mealLabel(String mealType) {
        return switch (mealType) {
            case "BREAKFAST" -> "Sáng";
            case "LUNCH" -> "Trưa";
            case "SNACK" -> "Phụ";   // D6 — "Bữa phụ", KHÔNG phải "Bữa chiều"
            default -> mealType;
        };
    }

    private static String dayMonth(java.time.Instant instant) {
        if (instant == null) {
            return "";
        }
        var d = instant.atZone(java.time.ZoneOffset.UTC);
        return String.format("%02d/%02d", d.getDayOfMonth(), d.getMonthValue());
    }
}
