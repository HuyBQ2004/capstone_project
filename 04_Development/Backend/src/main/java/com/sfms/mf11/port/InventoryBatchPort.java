package com.sfms.mf11.port;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port sang MF06 (Ingredient &amp; Inventory Management) — cách ly lô, chuỗi truy xuất
 * (BR-149/BR-151/BR-217). TDS-MF11_Backend.md §8.3.
 *
 * <p>ADR-M11-7 — tham chiếu BẰNG ID, MF11 KHÔNG import entity của MF06.
 */
public interface InventoryBatchPort {

    record BatchBrief(UUID batchId, String batchCode, String ingredientName, String unit,
                       LocalDate receivedAt, UUID supplierId, String supplierName) {}

    Optional<BatchBrief> findByCode(String batchCode, UUID schoolId);

    /**
     * BR-149 — cách ly MỘT CHIỀU, ghi vào {@code inventory_batches.quarantine_*} (V10).
     *
     * @param cause {@code SUPPLIER_LOT_DEFECT} hoặc {@code SCHOOL_STORAGE} (V67, 11/08/2026) —
     *              quyết định MF06 có chặn NCC giao thêm đúng lô này hay không.
     */
    void markQuarantined(UUID batchId, String incidentCode, String reason, String cause, UUID actorId);

    /** {@code true} nếu lô ĐÃ cách ly trước đó (chặn cách ly lại — {@code IEM-149B}). */
    boolean isAlreadyQuarantined(UUID batchId);

    /**
     * C7/BR-217 — gate đóng sự cố: lô cách ly đã ở trạng thái CUỐI
     * ({@code quarantine_disposition} — cột MF11 tự thêm qua {@code ALTER TABLE} ở V20,
     * vì MF06/V10 không có khái niệm này).
     */
    boolean isAtTerminalState(UUID batchId);

    /** Trạng thái CUỐI hợp lệ của một lô cách ly — khớp CHECK ở {@code V20:327}. */
    enum Disposition { RETURNED_TO_SUPPLIER, DISPOSED }

    /**
     * C7/BR-217 (backlog B1) — ghi trạng thái CUỐI cho lô đã cách ly.
     *
     * <p>Cột {@code quarantine_disposition} có từ V20 và được {@link #isAtTerminalState} ĐỌC để
     * chặn đóng sự cố, nhưng tới 08/08/2026 KHÔNG NƠI NÀO GHI — không entity field, không
     * endpoint, không màn. Hệ quả đo được: mọi sự cố đã cách ly ≥1 lô là không thể đóng vĩnh
     * viễn, kể cả khi CAPA đã COMPLETED và học sinh hồi phục hết.
     *
     * <p>KHÔNG nhận {@code actorId}: V20 không chừa cột cho người XỬ LÝ (chỉ có
     * {@code quarantine_by_user_id} = người CÁCH LY). Danh tính người xử lý ghi ở
     * {@code incident_events} — đừng thêm tham số rồi bỏ rơi nó.
     *
     * @return {@code true} nếu có dòng được cập nhật
     */
    boolean applyDisposition(UUID batchId, Disposition disposition);

    record DispositionBrief(String disposition, java.time.Instant at) {}

    /** Trạng thái cuối hiện tại của lô cách ly — {@code empty} nghĩa là CHƯA xử lý. */
    Optional<DispositionBrief> dispositionOf(UUID batchId);

    record ReceivingBrief(String sessionCode, LocalDate deliveryDate, UUID supplierId, String supplierName) {}

    /** Cột cuối của bản đồ truy xuất (BR-151) — phiên nhận hàng gốc của lô. */
    Optional<ReceivingBrief> receivingSessionOf(UUID batchId);

    /**
     * Tên nguyên liệu của lô — cột "nguyên liệu" trên bản đồ truy xuất (BR-151).
     * Trước 08/08/2026 cột này LUÔN null vì `traceabilityMap()` truyền thẳng `null`.
     */
    Optional<String> ingredientNameOf(UUID batchId);

    /**
     * Các MÓN đã dùng lô này — nửa còn thiếu của bản đồ truy xuất (BR-151).
     *
     * <p>Bản đồ trước 08/08/2026 chỉ có chiều "lô ← phiên nhận ← NCC"; **không hề có món**.
     * Nhưng câu hỏi đầu tiên của người điều tra là "lô nghi vấn này đã đi vào món nào" — không
     * có nó thì bản đồ không nối được sang danh sách học sinh đã ăn.
     *
     * <p>Mắt xích thật nằm ở {@code inventory_transactions} (có cả {@code batch_id} lẫn
     * {@code dish_id}) — cùng nguồn mà MF06 {@code GET /traceability/dish-batches} dùng theo
     * chiều ngược lại. ⚠️ Chỉ có dữ liệu nếu bếp CÓ lập phiếu xuất kho: {@code preparation_records}
     * đã DROP ở V43 nên xuất kho là đường DUY NHẤT nối lô với món.
     */
    List<String> dishNamesUsingBatch(UUID batchId, UUID schoolId);

    /**
     * ADR-M11-10 — phạm vi đọc "Liên đới" của Service Caterers: toàn bộ lô do một NCC/đơn vị
     * cung cấp trong một trường, dùng dựng {@code IncidentSpec} (M11-D19).
     */
    List<UUID> batchIdsSuppliedBy(UUID partnerId, UUID schoolId);
}
