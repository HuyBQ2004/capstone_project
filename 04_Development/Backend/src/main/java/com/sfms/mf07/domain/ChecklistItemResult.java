package com.sfms.mf07.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Kết quả từng hạng mục checklist trong một {@link KitchenInspection} (BR-207).
 * "Không đạt" bắt buộc ≥1 ảnh gắn đúng hạng mục + lý do (CHECK {@code ck_cir_evidence}, DB).
 */
@Entity
@Table(name = "checklist_item_results")
@IdClass(ChecklistItemResultId.class)
public class ChecklistItemResult {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_id", nullable = false)
    private KitchenInspection inspection;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private ChecklistItem item;

    @Column(nullable = false)
    private boolean passed;

    @Column(length = 1000)
    private String reason;

    @Column(name = "photo_count", nullable = false)
    private int photoCount;

    /**
     * V68 — surrogate riêng cho DÒNG kết quả này (PK thật vẫn là ghép inspection_id+item_id,
     * cột này KHÔNG thay đổi gì ở đó). Dùng làm {@code inspection_evidences.owner_id}
     * (ownerType=KITCHEN_ITEM) — ảnh bằng chứng phải gắn đúng TỪNG hạng mục, không phải
     * gộp chung cả phiên (11/08/2026, theo Nam). Gán tay {@code UUID.randomUUID()} lúc tạo
     * mới (xem {@code KitchenInspectionServiceImpl.submit()}), không dựa vào DB default để
     * khỏi phải refresh sau insert.
     */
    @Column(name = "id", nullable = false, updatable = false)
    private UUID resultId;

    public KitchenInspection getInspection() {
        return inspection;
    }

    public void setInspection(KitchenInspection inspection) {
        this.inspection = inspection;
    }

    public ChecklistItem getItem() {
        return item;
    }

    public void setItem(ChecklistItem item) {
        this.item = item;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getPhotoCount() {
        return photoCount;
    }

    public void setPhotoCount(int photoCount) {
        this.photoCount = photoCount;
    }

    public UUID getResultId() {
        return resultId;
    }

    public void setResultId(UUID resultId) {
        this.resultId = resultId;
    }
}
