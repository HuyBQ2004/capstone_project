package com.sfms.mf08.port;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Port sang MF11 (Incident &amp; Emergency Management) — BR-112/114/149. Khác
 * {@code GeofenceQueryPort}/{@code InspectionGatePort} mà TDS v1.0 từng khai (cả hai đã BỎ,
 * xem {@code M08-D06}/{@code M08-D07} — viết lại hạ tầng {@code ReferenceResolver} có sẵn),
 * cái này là port THẬT: BR-112/149 cần HÀNH VI của MF11 (trạng thái sự cố mở/đóng, cửa sổ
 * điều tra), không phải tra cứu thuần.
 *
 * <p>MF11 CHƯA merge (2026-08-03) ⇒ {@link com.sfms.mf08.port.IncidentStatusPortStub} là bean
 * duy nhất tồn tại, trả giá trị KHÔNG khoá nghiệp vụ (M08-D08) kèm {@code log.warn}. Thay bằng
 * bean thật khi MF11 lên (§2.5 mục 2 của TDS-MF08).
 */
public interface IncidentStatusPort {

    /**
     * D1 — resolve MÃ NGHIỆP VỤ (vd {@code "INC-2026-014"}, client luôn gửi mã, không phải
     * UUID nội bộ) sang {@link IncidentRef} thật. BẮT BUỘC dùng trước {@link #findOpen}/
     * {@link #hasOpenIncidentLinked}/{@link #windowOf} — các method đó nhận UUID THẬT, không
     * tự resolve mã.
     */
    Optional<IncidentRef> resolveByCode(String code, UUID schoolId);

    /** BR-112/114 — sự cố còn MỞ trong phạm vi trường, hoặc rỗng nếu không tồn tại/đã đóng. */
    Optional<IncidentRef> findOpen(UUID incidentId, UUID schoolId);

    /** BR-112 — {@code true} nếu sự cố (dù không truyền qua findOpen) đang khoá hủy mẫu. */
    boolean hasOpenIncidentLinked(UUID sampleIncidentId);

    /** BR-149 — cửa sổ điều tra (mốc ưu tiên {@code occurredAt} → {@code mealDate} → {@code reportedAt}). */
    InvestigationWindow windowOf(UUID incidentId);

    record IncidentRef(UUID id, String code, boolean open) {}

    record InvestigationWindow(Instant from, Instant to) {}
}
