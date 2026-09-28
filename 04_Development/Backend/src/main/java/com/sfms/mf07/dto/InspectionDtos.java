package com.sfms.mf07.dto;

import com.sfms.common.enums.MealType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO của {@code inspections} (Kiểm thực ba bước — UC-052/UC-065/UC-112/UC-124).
 * TDS-MF07_Backend.md §9.3.1..§9.3.4. Giữ NGUYÊN tên field mock (D0 chống lệch FE).
 */
public final class InspectionDtos {

    private InspectionDtos() {
    }

    // ───────────────────────── Rows (Mẫu số 1/2/3) ─────────────────────────

    public record Step1FreshRowDto(
            String name, String batchId, @NotNull String receivedAt, @NotNull String quantity,
            String supplierName, String supplierContact, String deliverer,
            boolean hasInvoice, boolean hasVetCert, boolean hasQuarantineCert,
            boolean sensoryPass, String quickTest, String action, int photoCount) {}

    public record Step1DryRowDto(
            String name, String batchId, String producer, String producerAddress,
            @NotNull String receivedAt, @NotNull String quantity, String supplierName, String deliverer,
            String supplierContact, String expiry, String storage, boolean hasInvoice,
            boolean sensoryPass, String action, int photoCount) {}

    public record Step2RowDto(
            String shift, String dishId, String dishName, String mainIngredients, String servings,
            String prepDoneAt, String cookDoneAt, boolean hygieneStaff, boolean hygieneEquipment,
            boolean hygieneArea, boolean sensoryPass, BigDecimal coreTemp, String action, int photoCount) {}

    public record Step3RowDto(
            String shift, String dishId, String dishName, String servings, String dividedDoneAt,
            String eatStartAt,
            @Schema(description = "Mẫu 3 cột 7 — dụng cụ chia/chứa đựng/che đậy ĐẠT yêu cầu vệ sinh. "
                    + "Không đạt thì bắt buộc có ảnh bằng chứng (cùng luật với sensoryPass).")
            boolean utensilsPass,
            @Schema(description = "Ghi chú kèm cột 7 (tuỳ chọn) — loại dụng cụ, hoặc lý do khi Không đạt")
            String utensils,
            BigDecimal holdingTemp, boolean sensoryPass,
            String action, int photoCount) {}

    /**
     * Phần in trên đầu Mẫu 1/2/3 (QĐ 1246/QĐ-BYT) — CHỈ CÓ Ở RESPONSE, hoàn toàn dẫn xuất
     * từ bản ghi phiên (xem {@code InspectionAssembler}). Không có cột lưu trữ nào tương ứng.
     */
    public record FormHeader(String facility, String inspector, String checkedAt, String location) {}

    // ───────────────────────── Create (POST /inspections — UC-052) ─────────────────────────

    @Schema(description = "D4 — date chấp nhận ISO (\"2026-08-10\") lẫn dd/MM/yyyy (\"10/08/2026\"), "
            + "parse khoan dung ở service layer, KHÔNG khai LocalDate ở đây (Jackson JavaTimeModule chỉ nhận ISO)")
    public record OpenSessionRequest(
            @NotNull String date, @NotNull MealType mealType, String dishId) {}

    // ───────────────────────── Step submission (POST /inspections/{id}/steps/{step}) ─────────────────────────

    @Schema(description = "PUT/POST bước — 4 biến thể payload gộp làm một, field không dùng ở "
            + "bước hiện tại được bỏ qua")
    public record SubmitStepRequest(
            /*
             * BỎ `formHeader` (08/08/2026). Nó là field ma: FE gửi lên, DTO nhận, KHÔNG
             * service nào đọc, KHÔNG cột nào lưu — rồi lúc đọc lại backend dựng header
             * mới nên client thấy giá trị khác hẳn thứ mình vừa gửi. Header là dẫn xuất,
             * chỉ tồn tại ở response.
             */
            List<Step1FreshRowDto> step1Fresh, List<Step1DryRowDto> step1Dry,
            List<Step2RowDto> step2Rows, List<Step3RowDto> step3Rows,
            @NotNull String result,                 // "PASSED" | "FAILED"
            BigDecimal coreTemp, BigDecimal holdingTemp,
            String failReason, String failSeverity, String failWitness,
            /*
             * 13/08/2026 (theo Nam) — chỉ có nghĩa khi result=FAILED. true: phải dùng nguyên
             * liệu/chế biến MỚI để khắc phục ⇒ session FAILED hẳn, bắt buộc tái kiểm toàn bộ
             * (BR-096, làm lại từ Bước 1 — QĐ 1246 Điều 3 đòi kiểm nguyên liệu MỚI đưa vào chế
             * biến). false/null: khắc phục được ngay bằng đúng nguyên liệu/mẻ đã kiểm (lỗi vệ
             * sinh, thao tác...) ⇒ giữ phiên MỞ, bếp sửa & nộp lại ĐÚNG bước này trong cùng
             * phiên, không cần tái kiểm. Xem {@code recomputeOverall}.
             */
            Boolean needsNewIngredients,
            /*
             * `gps` (String tự do "lat,lng") giữ để KHÔNG vỡ payload cũ, nhưng chưa từng được
             * validate — thêm 10/08/2026 `gpsLat`/`gpsLng` (BigDecimal, cùng khuôn BR-073/074
             * của mf06 và BR-109 của mf08) để backend KIỂM ĐƯỢC geofence trường, thay vì chỉ
             * lưu chuỗi hiển thị. `gps` tự dựng lại từ 2 field này khi cả hai có mặt.
             */
            String gps, BigDecimal gpsLat, BigDecimal gpsLng) {}

    public record CoSignRequest(@NotNull String coSignerUserId) {}

    public record MedicalReviewRequest(
            @NotNull String medicalReviewResult, String medicalReviewReason) {}

    // ───────────────────────── Response ─────────────────────────

    @Schema(name = "InspectionSessionResponse")
    public record InspectionSessionResponse(
            String id, String date, String meal, String step1, String step2, String step3, String overall,
            BigDecimal coreTemp, BigDecimal holdingTemp, String inspector, String coSigner,
            String dish, String dishId, int evidencePhotos, String gps, String staffNotes,
            String medicalReviewResult, String medicalReviewReason, String medicalReviewBy, String medicalReviewAt,
            FormHeader formHeader,
            List<Step1FreshRowDto> step1Fresh, List<Step1DryRowDto> step1Dry,
            List<Step2RowDto> step2Rows, List<Step3RowDto> step3Rows) {}

    @Schema(name = "PagedResponseInspectionSummaryResponse", description = "PagedResponse<InspectionSessionResponse>")
    public record PagedResponseInspectionSummaryResponse(
            List<InspectionSessionResponse> items, long total, int page, int pageSize, int pageCount) {}

    @Schema(description = "GET /inspections/serving-gate — BR-094")
    public record ServingGateResponse(boolean allowed, String reasonCode, String message, String sessionId) {}
}
