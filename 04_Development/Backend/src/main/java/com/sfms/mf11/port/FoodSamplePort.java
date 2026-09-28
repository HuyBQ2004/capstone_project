package com.sfms.mf11.port;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Port sang MF08 (Food Sample Retention) — mẫu lưu làm tang chứng (BR-113).
 * TDS-MF11_Backend.md §8.3.
 */
public interface FoodSamplePort {

    record SampleBrief(String sampleCode, String dishName, Instant collectedAt, String status,
                        String labRequestCode) {}

    List<SampleBrief> findByIncident(UUID incidentId);

    record LabRequestBrief(String code, String labDestination, String testCriteria, String status, Instant sentToLabAt) {}

    List<LabRequestBrief> labRequestsByIncident(UUID incidentId);

    /**
     * BR-113 — mở phiếu gửi lab/thu giữ cho các mẫu đã chọn, gắn {@code incidentId}. Ghi
     * trực tiếp vào {@code sample_lab_requests} (MF08, V14) — bảng đã chừa sẵn cột
     * {@code incident_id} cho MF11 (thấy khi rà Bước 0), cùng tinh thần với
     * {@link InventoryBatchPort#markQuarantined}.
     */
    void requestRetrieval(UUID incidentId, List<String> sampleCodes, String authorityName,
                           String testCriteria, UUID actorId, UUID schoolId);

    /**
     * BR-112 (backlog B3, kế hoạch §2.3) — GIỮ mẫu tại trường để phục vụ điều tra, KHÔNG mở
     * phiếu gửi lab: mẫu vẫn nằm trong tủ lưu nhưng chuyển sang {@code HOLD}, gắn
     * {@code incidentId} và được gia hạn {@code retention_deadline} thêm một chu kỳ BR-111.
     *
     * <p>Tách hẳn khỏi {@link #requestRetrieval} vì hai việc khác nhau: ở đây mẫu chưa đi đâu
     * cả. Trước 08/08/2026 y tế chỉ có đường {@code requestRetrieval}, nên muốn giữ mẫu là
     * buộc phải khai khống rằng đã bàn giao đi xét nghiệm.
     *
     * @return số mẫu thực sự chuyển sang {@code HOLD}
     */
    int holdForIncident(UUID incidentId, List<String> sampleCodes, UUID schoolId);
}
