package com.sfms.mf04.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf04.dto.DeliveryPersonDtos.CreateDeliveryPersonRequest;
import com.sfms.mf04.dto.DeliveryPersonDtos.DeliveryPersonResponse;
import org.springframework.web.multipart.MultipartFile;

/** UC-019 (Create) · UC-110 (View List) · UC-068 (Register Face) · UC-028 (Delete Face). */
public interface DeliveryPersonService {

    PagedResponse<DeliveryPersonResponse> list(ListQuery query, AuthUser actor);

    DeliveryPersonResponse create(CreateDeliveryPersonRequest req, AuthUser actor);

    DeliveryPersonResponse registerFace(String code, MultipartFile image, AuthUser actor);

    DeliveryPersonResponse deleteFace(String code, AuthUser actor);
}
