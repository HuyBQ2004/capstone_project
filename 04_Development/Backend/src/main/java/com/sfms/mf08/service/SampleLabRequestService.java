package com.sfms.mf08.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf08.dto.FoodSampleDtos.FoodSampleResponse;
import com.sfms.mf08.dto.SampleLabRequestDtos.HandOverSamplesRequest;
import com.sfms.mf08.dto.SampleLabRequestDtos.SampleLabRequestResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** @version 1.0 — TDS-MF08_Backend.md §8.1 (BR-113/212). */
public interface SampleLabRequestService {

    PagedResponse<SampleLabRequestResponse> list(ListQuery query, AuthUser actor);

    SampleLabRequestResponse getById(String code, AuthUser actor);

    /** UC-076 — 1 transaction: tạo phiếu + chuyển N mẫu sang SENT_TO_LAB (ADR-M08-1). */
    SampleLabRequestResponse handOver(HandOverSamplesRequest req, List<MultipartFile> sealPhotos,
                                      List<MultipartFile> handoverRecords, List<MultipartFile> labReceipts,
                                      AuthUser actor);

    /** Mẫu còn RETAINED trong cửa sổ điều tra của một sự cố (nguồn: IncidentStatusPort). */
    List<FoodSampleResponse> retrievableForIncident(String incidentId, boolean includeAllDishes, AuthUser actor);

    /** Tải 1 tệp (ảnh niêm phong/biên bản giao nhận/phiếu nhận lab, BR-212) — D10-BIS. */
    DownloadedFile downloadFile(String code, String fileId, AuthUser actor);
}
