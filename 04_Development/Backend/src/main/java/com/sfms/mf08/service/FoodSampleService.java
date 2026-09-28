package com.sfms.mf08.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf08.dto.FoodSampleDtos.DisposeSampleRequest;
import com.sfms.mf08.dto.FoodSampleDtos.FoodSampleResponse;
import com.sfms.mf08.dto.FoodSampleDtos.IncidentLinkRequest;
import com.sfms.mf08.dto.FoodSampleDtos.Mau4LabelResponse;
import com.sfms.mf08.dto.FoodSampleDtos.RegisterSampleRequest;
import com.sfms.mf08.dto.FoodSampleDtos.UpdateStorageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/** @version 1.0 — TDS-MF08_Backend.md §8.1. */
public interface FoodSampleService {

    PagedResponse<FoodSampleResponse> list(ListQuery query, AuthUser actor);   // UC-118/042

    FoodSampleResponse getById(String code, AuthUser actor);                  // UC-118

    /** UC-069 (BR-105..109,114,203) — cổng theo thứ tự C4 (§17.1 TDS). */
    FoodSampleResponse register(RegisterSampleRequest req, List<MultipartFile> evidencePhotos,
                                String deviceInfo, AuthUser actor);

    FoodSampleResponse updateStorage(String code, UpdateStorageRequest req, AuthUser actor);   // UC-042 AF2 (BR-110)

    FoodSampleResponse dispose(String code, DisposeSampleRequest req, AuthUser actor);          // UC-030 (BR-111/112)

    FoodSampleResponse linkDisposedToIncident(String code, IncidentLinkRequest req, AuthUser actor); // UC-076 AF1 (BR-114)

    Mau4LabelResponse buildLabel(String code, AuthUser actor);                 // UC-069 bước 4 (BR-106)

    /** Tải 1 ảnh bằng chứng (BR-107) — D10-BIS, tài liệu riêng tư nên đi qua endpoint có @PreAuthorize. */
    DownloadedFile downloadEvidencePhoto(String code, String fileId, AuthUser actor);

    /** Dùng nội bộ bởi {@code SampleLabRequestService} — mẫu RETAINED trong cửa sổ điều tra. */
    List<FoodSampleResponse> retrievableForIncident(UUID schoolId, UUID incidentId, boolean includeAllDishes);
}
