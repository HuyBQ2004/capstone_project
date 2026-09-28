package com.sfms.mf07.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf07.dto.EvidenceDtos.EvidenceResponse;
import com.sfms.mf07.dto.EvidenceDtos.KitchenEvidenceSummary;
import com.sfms.mf07.dto.KitchenInspectionDtos.KitchenInspectionResponse;
import com.sfms.mf07.dto.KitchenInspectionDtos.SubmitKitchenInspectionRequest;
import com.sfms.mf07.service.EvidenceService.DownloadedFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Kiểm tra an toàn & vệ sinh bếp (UC-014). ADR-M07-03: &lt;60 điểm ⇒ cảnh báo, KHÔNG khóa bếp. */
public interface KitchenInspectionService {

    PagedResponse<KitchenInspectionResponse> list(ListQuery query, AuthUser actor);

    KitchenInspectionResponse detail(String code, AuthUser actor);

    /** BR-100/BR-104 — điểm tính lại server-side từ {@code itemResults} (CF-26). */
    KitchenInspectionResponse submit(SubmitKitchenInspectionRequest request, AuthUser actor);

    /** BR-221 — Service Caterers xác nhận kết quả phiên. */
    KitchenInspectionResponse confirmByCaterers(String code, AuthUser actor);

    /**
     * BR-221 mở rộng — Medical Staff sửa nội dung biên bản sự việc (tự sinh lúc nộp
     * checklist). Khoá sau khi Service Caterers đã xác nhận — biên bản không đổi âm thầm
     * sau khi hai bên đã thống nhất.
     */
    KitchenInspectionResponse updateRecord(String code, String content, AuthUser actor);

    /**
     * V68 — ảnh bằng chứng gắn ĐÚNG hạng mục checklist (KHÔNG gộp chung cả phiên), khớp
     * BR-207: hạng mục "Không đạt" bắt buộc ≥1 ảnh gắn đúng hạng mục đó. Dùng chung
     * {@code EvidenceService}/bảng {@code inspection_evidences} (ownerType=KITCHEN_ITEM,
     * ownerId={@code ChecklistItemResult.resultId}) — cùng hạ tầng ảnh của kiểm thực 3 bước.
     */
    EvidenceResponse uploadItemEvidence(String code, String itemCode, MultipartFile file, String gps, AuthUser actor);

    /**
     * V68 — mọi ảnh bằng chứng gắn phiên (gộp mọi hạng mục, KHÔNG phân biệt hạng mục nào —
     * xem chú thích tương tự ở {@code EvidenceService.listForSession}). Dùng cho màn xem lại
     * phiên (`conduct-inspection/[id]`) — trước 11/08/2026 chỉ hiện SỐ đếm, không có ảnh thật.
     */
    List<KitchenEvidenceSummary> listEvidences(String code, AuthUser actor);

    DownloadedFile downloadEvidence(String code, String evidenceId, AuthUser actor);
}
