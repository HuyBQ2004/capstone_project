package com.sfms.mf04.mapper;

import com.sfms.mf04.domain.PartnerDocument;
import com.sfms.mf04.dto.DocumentDtos.DocumentResponse;
import com.sfms.mf04.dto.DocumentDtos.WarningInfo;
import com.sfms.mf04.dto.PartnerDtos.PartnerDocumentSummary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct — {@link PartnerDocument} → DTO. {@code status}/{@code warning} KHÔNG map từ
 * entity — dẫn xuất runtime (ADR-M04-4), service tính rồi truyền vào.
 */
@Mapper(componentModel = "spring")
public interface DocumentMapper {

    @Mapping(target = "id", source = "document.code")
    @Mapping(target = "partnerId", source = "partnerCode")
    @Mapping(target = "deliveryPersonId", source = "deliveryPersonCode")
    DocumentResponse toResponse(PartnerDocument document, String partnerCode, String status, WarningInfo warning,
                               String deliveryPersonCode);

    @Mapping(target = "id", source = "document.code")
    @Mapping(target = "expiry", source = "document.expiryDate")
    PartnerDocumentSummary toSummary(PartnerDocument document, String status);
}
