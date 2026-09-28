package com.sfms.mf07.service;

import com.sfms.common.reference.ReferenceResolver;
import com.sfms.mf07.domain.InspectionSession;
import com.sfms.mf07.domain.Step1DryRow;
import com.sfms.mf07.domain.Step1FreshRow;
import com.sfms.mf07.domain.Step2Row;
import com.sfms.mf07.domain.Step3Row;
import com.sfms.mf07.dto.InspectionDtos.FormHeader;
import com.sfms.mf07.dto.InspectionDtos.InspectionSessionResponse;
import com.sfms.mf07.dto.InspectionDtos.Step1DryRowDto;
import com.sfms.mf07.dto.InspectionDtos.Step1FreshRowDto;
import com.sfms.mf07.dto.InspectionDtos.Step2RowDto;
import com.sfms.mf07.dto.InspectionDtos.Step3RowDto;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

/** Ánh xạ {@link InspectionSession} ↔ {@code InspectionSessionResponse} — giữ NGUYÊN tên field mock. */
@Component
public class InspectionAssembler {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Mọi mốc giờ hiển thị trên biểu mẫu là GIỜ ĐỊA PHƯƠNG của trường.
     *
     * Trước 08/08/2026 chỗ này format bằng {@code ZoneOffset.UTC}, nên phiên kiểm thực lúc
     * 13:34 in ra "06:34" trên cả ba Mẫu 1/2/3 — lệch đúng 7 tiếng, và là giờ ghi trên biểu
     * mẫu pháp lý (QĐ 1246/QĐ-BYT).
     */
    private static final java.time.ZoneId SCHOOL_ZONE = java.time.ZoneId.of("Asia/Ho_Chi_Minh");

    private final ReferenceResolver referenceResolver;

    public InspectionAssembler(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    public InspectionSessionResponse toResponse(InspectionSession s) {
        int evidencePhotos = s.getStep1Fresh().stream().mapToInt(Step1FreshRow::getPhotoCount).sum()
                + s.getStep1Dry().stream().mapToInt(Step1DryRow::getPhotoCount).sum()
                + s.getStep2Rows().stream().mapToInt(Step2Row::getPhotoCount).sum()
                + s.getStep3Rows().stream().mapToInt(Step3Row::getPhotoCount).sum();

        String[] inspector = referenceResolver.userById(s.getInspectorUserId());
        String[] coSigner = s.getCoSignerUserId() == null ? null : referenceResolver.userById(s.getCoSignerUserId());
        String[] medicalReviewer = s.getMedicalReviewBy() == null ? null : referenceResolver.userById(s.getMedicalReviewBy());
        String schoolName = referenceResolver.schoolName(s.getSchoolId());
        String dishName = referenceResolver.dishName(s.getDishId());
        String dishCode = referenceResolver.dishCode(s.getDishId());

        /*
         * `formHeader` là DẪN XUẤT, không phải dữ liệu người dùng nhập (xem chú thích ở
         * `InspectionDtos.SubmitStepRequest`). Cả 4 field đều suy từ bản ghi phiên:
         * cơ sở = tên trường, người kiểm = người mở phiên, thời điểm = lúc chốt kết luận,
         * địa điểm = địa chỉ trường. Trường chưa khai địa chỉ thì trả `null` để FE hiện
         * "—" — thà để trống còn hơn in một địa điểm sai lên biểu mẫu pháp lý.
         */
        FormHeader formHeader = new FormHeader(schoolName, inspector == null ? null : inspector[2],
                s.getDecidedAt() == null ? null : TIMESTAMP.format(s.getDecidedAt().atZone(SCHOOL_ZONE)),
                referenceResolver.schoolAddress(s.getSchoolId()));

        return new InspectionSessionResponse(
                s.getCode(), s.getDate().toString(), s.getMealType().name(),
                s.getStep1().name(), s.getStep2().name(), s.getStep3().name(), s.getOverall().name(),
                s.getCoreTemp(), s.getHoldingTemp(),
                inspector == null ? null : inspector[2], coSigner == null ? null : coSigner[2],
                dishName, dishCode, evidencePhotos, s.getGps(), s.getStaffNotes(),
                s.getMedicalReviewResult() == null ? null : s.getMedicalReviewResult().name(),
                s.getMedicalReviewReason(), medicalReviewer == null ? null : medicalReviewer[2],
                s.getMedicalReviewAt() == null ? null : TIMESTAMP.format(s.getMedicalReviewAt().atZone(SCHOOL_ZONE)),
                formHeader,
                toFreshDtos(s.getStep1Fresh()), toDryDtos(s.getStep1Dry()),
                toStep2Dtos(s.getStep2Rows()), toStep3Dtos(s.getStep3Rows()));
    }

    private List<Step1FreshRowDto> toFreshDtos(List<Step1FreshRow> rows) {
        return rows.stream().map(r -> new Step1FreshRowDto(r.getName(),
                referenceResolver.batchCode(r.getBatchId()), r.getReceivedAt(), r.getQuantity(),
                r.getSupplierName(), r.getSupplierContact(), r.getDeliverer(), r.isHasInvoice(), r.isHasVetCert(),
                r.isHasQuarantineCert(), r.isSensoryPass(), r.getQuickTest(), r.getAction(), r.getPhotoCount()))
                .toList();
    }

    private List<Step1DryRowDto> toDryDtos(List<Step1DryRow> rows) {
        return rows.stream().map(r -> new Step1DryRowDto(r.getName(),
                referenceResolver.batchCode(r.getBatchId()), r.getProducer(), r.getProducerAddress(),
                r.getReceivedAt(), r.getQuantity(), r.getSupplierName(), r.getDeliverer(), r.getSupplierContact(),
                r.getExpiry() == null ? null : r.getExpiry().toString(), r.getStorage(), r.isHasInvoice(),
                r.isSensoryPass(), r.getAction(), r.getPhotoCount()))
                .toList();
    }

    /**
     * VÁ 05/08/2026 — trước đây trả THẲNG {@code r.getDishId().toString()} (UUID nội bộ),
     * vi phạm D1: {@code FoodSampleServiceImpl.register()} nhận {@code RegisterSampleRequest.dishId}
     * làm MÃ NGHIỆP VỤ rồi tra bằng {@code ReferenceResolver.dishIdByCode()} (so cột {@code code},
     * không phải {@code id}) — gửi UUID thô sẽ luôn 422 "unknown dish". Trường {@code dishId} Ở CẤP
     * SESSION (không phải step rows) đã đúng D1 từ đầu (dùng {@code dishCode} biến cục bộ ở trên);
     * chỉ hai hàm dưới đây (per-row, nhiều món/phiên) bị bỏ sót.
     *
     * <p><b>08/08/2026 — cùng lỗi đó ở Mẫu số 1.</b> {@code batchId} của step1 fresh/dry vẫn trả
     * {@code UUID.toString()}; đo bằng API thật thấy {@code "680bc2a7-…"} thay cho
     * {@code "SB-THIT-HEO"} mà client vừa gửi. Nay dịch qua
     * {@code ReferenceResolver.batchCode()} cho khớp D1 và khớp chiều ghi
     * ({@code resolveBatchId()} nhận mã nghiệp vụ).
     */
    private List<Step2RowDto> toStep2Dtos(List<Step2Row> rows) {
        return rows.stream().map(r -> new Step2RowDto(r.getShift(),
                referenceResolver.dishCode(r.getDishId()), r.getDishName(), r.getMainIngredients(),
                r.getServings(), r.getPrepDoneAt(), r.getCookDoneAt(), r.isHygieneStaff(), r.isHygieneEquipment(),
                r.isHygieneArea(), r.isSensoryPass(), r.getCoreTemp(), r.getAction(), r.getPhotoCount()))
                .toList();
    }

    private List<Step3RowDto> toStep3Dtos(List<Step3Row> rows) {
        return rows.stream().map(r -> new Step3RowDto(r.getShift(),
                referenceResolver.dishCode(r.getDishId()), r.getDishName(), r.getServings(),
                r.getDividedDoneAt(), r.getEatStartAt(), r.isUtensilsPass(), r.getUtensils(), r.getHoldingTemp(), r.isSensoryPass(),
                r.getAction(), r.getPhotoCount()))
                .toList();
    }
}
