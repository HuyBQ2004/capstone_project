package com.sfms.mf06.dto;

import com.sfms.mf06.domain.ReasonCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** DTO của resource {@code inventory} (lô trong kho) — TDS-MF06_Backend.md §9.2. */
public final class InventoryDtos {

    private InventoryDtos() {
    }

    public record QuarantineView(Instant at, String by, String reason, String incidentId,
                                  @io.swagger.v3.oas.annotations.media.Schema(
                                          description = "SUPPLIER_LOT_DEFECT | SCHOOL_STORAGE — V67, "
                                                  + "null nếu bản ghi cách ly cũ chưa phân loại")
                                  String cause) {}

    /**
     * D12 — Response chỉ. {@code status}/{@code isLocked}/{@code remaining}/{@code ingredient}
     * /{@code supplier} là field DẪN XUẤT, KHÔNG có mặt ở request nào (M06-D07).
     */
    public record InventoryBatchResponse(
            String id, String ingredientId, String ingredient, String supplierId, String supplier,
            BigDecimal quantity, BigDecimal remaining, String unit, String storageZone,
            LocalDate receivedDate, Instant receivedAt, LocalDate expiryDate, String status, boolean isLocked,
            QuarantineView quarantine
    ) {}

    @io.swagger.v3.oas.annotations.media.Schema(name = "PagedResponseInventoryBatchResponse",
            description = "PagedResponse<InventoryBatchResponse>")
    public record PagedResponseInventoryBatchResponse(
            List<InventoryBatchResponse> items, long total, int page, int pageSize, int pageCount) {}

    /** {@code PATCH /inventory/{code}/storage-zone} — chỉ MỘT field ghi được (D12). */
    public record StorageZoneUpdateRequest(@NotBlank String storageZone) {}

    /** {@code PATCH /inventory/{code}/quarantine} — Medical Staff, BR-149. */
    public record QuarantineRequest(
            @NotBlank String reason, String incidentId,
            @io.swagger.v3.oas.annotations.media.Schema(
                    description = "SUPPLIER_LOT_DEFECT | SCHOOL_STORAGE — V67 (11/08/2026), bắt buộc để "
                            + "hệ thống biết có nên chặn NCC giao thêm đúng lô này hay không.",
                    allowableValues = {"SUPPLIER_LOT_DEFECT", "SCHOOL_STORAGE"})
            @NotBlank String cause) {}

    // ───────────────────────── issue-plan (FEFO, BR-075/077) ─────────────────────────

    public record AllocationLine(String batchId, BigDecimal take, LocalDate expiryDate) {}

    public record IssuePlanLine(
            String dishId, String dishName, String ingredientId, String ingredientName,
            BigDecimal perServing, String perServingUnit, BigDecimal needed, String unit,
            List<AllocationLine> allocation, BigDecimal shortfall, boolean unknownIngredient
    ) {}

    public record ShortageLine(String ingredientId, String ingredientName, BigDecimal needed,
                               BigDecimal available, BigDecimal shortfall, String unit) {}

    public record IssuePlanResponse(LocalDate serviceDate, String mealType, List<IssuePlanLine> lines,
                                     List<ShortageLine> shortages, List<String> unknownIngredients) {}

    // ───────────────────────── issue-slips (ADR-M06-6) ─────────────────────────

    public record IssueSlipCreateLine(@NotBlank String batchId, @NotBlank String dishId,
                                       @NotNull BigDecimal quantity, @NotBlank String unit) {}

    public record IssueSlipCreateRequest(
            @NotNull LocalDate serviceDate, @NotBlank String mealType,
            @NotNull List<String> menuIds, @NotNull List<IssueSlipCreateLine> lines, String note
    ) {}

    public record IssueSlipResponse(String id, LocalDate serviceDate, String mealType,
                                     List<String> menuIds, String issuedBy, Instant createdAt,
                                     String status, int lineCount, String note) {}

    @io.swagger.v3.oas.annotations.media.Schema(name = "PagedResponseIssueSlipResponse",
            description = "PagedResponse<IssueSlipResponse>")
    public record PagedResponseIssueSlipResponse(
            List<IssueSlipResponse> items, long total, int page, int pageSize, int pageCount) {}

    /**
     * {@code serviceDate}/{@code mealType} — TÙY CHỌN (13/08/2026, BR-204 mở rộng). Khi điều
     * chỉnh EXTRA_USAGE gắn với một bữa cụ thể (VD nấu thiếu, xuất bù), truyền 2 field này để
     * giao dịch được nhận diện bởi {@code step1RowsOfIssue()} (kiểm thực Bước 1 auto-fill) và
     * hiển thị "Phạm vi bữa ăn" giống hệt giao dịch ISSUE — thiếu chúng thì điều chỉnh vẫn ghi
     * sổ bình thường nhưng KHÔNG link được vào bữa/lô cho truy vết.
     */
    public record ManualAdjustmentRequest(
            @NotBlank String batchId, @NotNull BigDecimal delta, @NotBlank String unit,
            @NotNull ReasonCode reasonCode, @NotBlank String managerSignature,
            LocalDate serviceDate, String mealType
    ) {}

    public record InventoryTransactionResponse(
            String id, Instant occurredAt, String type, String batchId, String ingredientId,
            String ingredient, BigDecimal delta, String unit, String reasonCode, String performedBy,
            String managerSignature, String issueSlipId, String menuId, String dishId, String dish,
            String mealType, LocalDate serviceDate
    ) {}

    @io.swagger.v3.oas.annotations.media.Schema(name = "PagedResponseInventoryTransactionResponse",
            description = "PagedResponse<InventoryTransactionResponse>")
    public record PagedResponseInventoryTransactionResponse(
            List<InventoryTransactionResponse> items, long total, int page, int pageSize, int pageCount) {}
}
