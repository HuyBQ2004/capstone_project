package com.sfms.mf04.mapper;

import com.sfms.mf04.domain.PartnershipApplication;
import com.sfms.mf04.dto.ApplicationDtos.ApplicationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct — {@link PartnershipApplication} → DTO. {@code partnerId}/{@code schoolId} là
 * MÃ nghiệp vụ (D1), dịch từ UUID qua {@code common/reference/ReferenceResolver} ở service.
 * {@code school} (tên hiển thị) theo E7/L8.
 */
@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    @Mapping(target = "id", source = "app.code")
    @Mapping(target = "partnerId", source = "partnerCode")
    @Mapping(target = "schoolId", source = "schoolCode")
    @Mapping(target = "school", source = "schoolName")
    ApplicationResponse toResponse(PartnershipApplication app, String partnerCode, String schoolCode,
                                    String schoolName);
}
