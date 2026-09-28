package com.sfms.mf06.domain;

import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Dòng phiên nhận hàng (BR-072/078). */
@Entity
@Table(name = "receiving_lines")
public class ReceivingLine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "session_id", nullable = false)
    private ReceivingSession session;

    @Column(name = "supplier_batch_id", nullable = false)
    private UUID supplierBatchId;

    @Column(name = "ingredient_id", nullable = false)
    private UUID ingredientId;

    @Column(name = "ingredient_label", nullable = false, length = 180)
    private String ingredientLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_zone", nullable = false, length = 10)
    private StorageZone storageZone;

    @Column(nullable = false)
    private boolean rejected = false;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    /** BR-071 (10/08/2026) — lượng THỰC NHẬN ở dòng này; NULL khi {@code rejected}. */
    @Column(name = "received_quantity", precision = 12, scale = 3)
    private BigDecimal receivedQuantity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "reject_photo_keys", nullable = false, columnDefinition = "jsonb")
    private List<String> rejectPhotoKeys = new ArrayList<>();

    /** Mã lô kho sau khi nhập — chỉ có ở dòng KHÔNG bị từ chối (D12: response-only). */
    @jakarta.persistence.Transient
    private String batchCode;

    public ReceivingSession getSession() { return session; }
    public void setSession(ReceivingSession session) { this.session = session; }
    public UUID getSupplierBatchId() { return supplierBatchId; }
    public void setSupplierBatchId(UUID supplierBatchId) { this.supplierBatchId = supplierBatchId; }
    public UUID getIngredientId() { return ingredientId; }
    public void setIngredientId(UUID ingredientId) { this.ingredientId = ingredientId; }
    public String getIngredientLabel() { return ingredientLabel; }
    public void setIngredientLabel(String ingredientLabel) { this.ingredientLabel = ingredientLabel; }
    public StorageZone getStorageZone() { return storageZone; }
    public void setStorageZone(StorageZone storageZone) { this.storageZone = storageZone; }
    public boolean isRejected() { return rejected; }
    public void setRejected(boolean rejected) { this.rejected = rejected; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    public BigDecimal getReceivedQuantity() { return receivedQuantity; }
    public void setReceivedQuantity(BigDecimal receivedQuantity) { this.receivedQuantity = receivedQuantity; }
    public List<String> getRejectPhotoKeys() { return rejectPhotoKeys; }
    public void setRejectPhotoKeys(List<String> rejectPhotoKeys) {
        this.rejectPhotoKeys = rejectPhotoKeys == null ? new ArrayList<>() : rejectPhotoKeys;
    }
    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }
}
