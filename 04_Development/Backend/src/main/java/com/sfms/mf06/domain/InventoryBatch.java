package com.sfms.mf06.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Lô trong kho trường (BR-071/075/202/205). ADR-M06-1: {@code initialQuantity} BẤT BIẾN sau
 * khi nhập; tồn khả dụng tính runtime = initialQuantity + SUM(tx.delta), KHÔNG cột.
 */
@Entity
@Table(name = "inventory_batches",
        uniqueConstraints = @UniqueConstraint(name = "uq_batch_school_code", columnNames = {"school_id", "code"}),
        indexes = {
                @Index(name = "ix_batch_fefo", columnList = "school_id,ingredient_id,expiry_date"),
                @Index(name = "ix_batch_session", columnList = "receiving_session_id")
        })
public class InventoryBatch extends BaseEntity {

    /** Mã lô do NCC gán — KHÔNG phải PK (D1). Chảy xuyên đơn -> giao -> nhận -> kho -> xuất. */
    @Column(nullable = false, length = 32)
    private String code;

    @JsonProperty("id")
    public String getPublicId() { return code; }

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "ingredient_id", nullable = false)
    private UUID ingredientId;

    @Column(name = "ingredient_label", nullable = false, length = 180)
    private String ingredientLabel;

    @Column(name = "supplier_id", nullable = false)
    private UUID supplierId;

    /** ADR-M06-1 — lượng NHẬP BAN ĐẦU, bất biến. */
    @Column(name = "initial_quantity", nullable = false, precision = 12, scale = 3)
    private BigDecimal initialQuantity;

    @Column(nullable = false, length = 12)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_zone", nullable = false, length = 10)
    private StorageZone storageZone;

    @Column(name = "received_date", nullable = false)
    private LocalDate receivedDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "receiving_session_id")
    private UUID receivingSessionId;

    @Embedded
    private QuarantineInfo quarantine;

    /**
     * V20 (MF11, ALTER TABLE) — trạng thái CUỐI khi lô cách ly đã xử lý xong (trả NCC/huỷ bỏ).
     * MF06 CHỈ ĐỌC ({@code insertable/updatable = false}) — MF11 mới là chủ ghi, qua native SQL
     * ở {@code InventoryBatchPortImpl.applyDisposition()} (ADR-M11-7, tham chiếu bằng ID, không
     * import entity chéo module). Thêm 11/08/2026 để {@code PurchaseRequestService} biết một lô
     * cách ly do LỖI BẢO QUẢN TRƯỜNG (không phải lỗi lô) đã được xử lý xong hay chưa trước khi
     * cho nhận thêm hàng gộp vào cùng dòng batch.
     */
    @Column(name = "quarantine_disposition", length = 24, insertable = false, updatable = false)
    private String quarantineDisposition;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public UUID getSchoolId() { return schoolId; }
    public void setSchoolId(UUID schoolId) { this.schoolId = schoolId; }
    public UUID getIngredientId() { return ingredientId; }
    public void setIngredientId(UUID ingredientId) { this.ingredientId = ingredientId; }
    public String getIngredientLabel() { return ingredientLabel; }
    public void setIngredientLabel(String ingredientLabel) { this.ingredientLabel = ingredientLabel; }
    public UUID getSupplierId() { return supplierId; }
    public void setSupplierId(UUID supplierId) { this.supplierId = supplierId; }
    public BigDecimal getInitialQuantity() { return initialQuantity; }
    public void setInitialQuantity(BigDecimal initialQuantity) { this.initialQuantity = initialQuantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public StorageZone getStorageZone() { return storageZone; }
    public void setStorageZone(StorageZone storageZone) { this.storageZone = storageZone; }
    public LocalDate getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public UUID getReceivingSessionId() { return receivingSessionId; }
    public void setReceivingSessionId(UUID receivingSessionId) { this.receivingSessionId = receivingSessionId; }
    public QuarantineInfo getQuarantine() { return quarantine; }
    public void setQuarantine(QuarantineInfo quarantine) { this.quarantine = quarantine; }
    public String getQuarantineDisposition() { return quarantineDisposition; }

    /** BR-205 — DẪN XUẤT, không cột (ADR-M06-2). Ngưỡng EXPIRING_SOON <= 2 ngày. */
    public ShelfStatus shelfStatus(LocalDate today) {
        if (expiryDate.isBefore(today)) {
            return ShelfStatus.EXPIRED;
        }
        if (!expiryDate.isAfter(today.plusDays(2))) {
            return ShelfStatus.EXPIRING_SOON;
        }
        return ShelfStatus.IN_STOCK;
    }

    /** BR-205 + BR-149 — lô khoá bị LOẠI khỏi tập ứng viên FEFO (ADR-M06-4). */
    public boolean isLocked(LocalDate today) {
        return shelfStatus(today) == ShelfStatus.EXPIRED || quarantine != null;
    }

    /**
     * 11/08/2026 — có nên chặn NHẬN THÊM hàng cùng mã lô này (gộp vào đúng dòng batch, BR-071)
     * hay không:
     * <ul>
     *   <li>{@code cause == null} (chưa phân loại, dữ liệu cũ trước V67) hoặc
     *       {@code SUPPLIER_LOT_DEFECT} — chặn VĨNH VIỄN, lô mới cùng số coi như cùng vấn đề.</li>
     *   <li>{@code SCHOOL_STORAGE} — chỉ chặn CHỪNG NÀO phần tồn cách ly cũ CHƯA có
     *       {@code quarantineDisposition} (chưa trả NCC/huỷ xong); nhận thêm trước khi xử lý xong
     *       sẽ trộn lẫn số lượng tốt mới với số lượng hỏng cũ trong CÙNG một dòng, không tách lại
     *       được (ADR-M06-1: remaining chỉ là SUM(delta), không theo dõi riêng từng đợt).</li>
     * </ul>
     */
    public boolean blocksFurtherReceiving() {
        if (quarantine == null) {
            return false;
        }
        QuarantineCause cause = quarantine.getCause();
        if (cause == null || cause == QuarantineCause.SUPPLIER_LOT_DEFECT) {
            return true;
        }
        return quarantineDisposition == null;
    }
}
