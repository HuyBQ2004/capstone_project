package com.sfms.mf04.mapper;

import com.sfms.mf04.domain.SupplierPartnership;
import com.sfms.mf04.dto.PartnershipDtos.PartnershipResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct — {@link SupplierPartnership} → DTO. {@code docStatus} KHÔNG map từ entity —
 * dẫn xuất runtime từ {@code partner_documents} (ADR-M04-4). {@code startDate}/{@code endDate}
 * ánh xạ {@code since}/{@code until} (M04-D04).
 */
@Mapper(componentModel = "spring")
public interface PartnershipMapper {

    @Mapping(target = "id", source = "ptn.code")
    @Mapping(target = "partnerId", source = "partnerCode")
    @Mapping(target = "schoolId", source = "schoolCode")
    @Mapping(target = "school", source = "schoolName")
    @Mapping(target = "startDate", source = "ptn.since")
    @Mapping(target = "endDate", source = "ptn.until")
    PartnershipResponse toResponse(SupplierPartnership ptn, String partnerCode, String schoolCode,
                                    String schoolName, String docStatus);
}
