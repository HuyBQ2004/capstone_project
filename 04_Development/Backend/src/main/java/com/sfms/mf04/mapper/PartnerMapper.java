package com.sfms.mf04.mapper;

import com.sfms.mf04.domain.Partner;
import com.sfms.mf04.dto.PartnerDtos.PartnerDetailResponse;
import com.sfms.mf04.dto.PartnerDtos.PartnerDocumentSummary;
import com.sfms.mf04.dto.PartnerDtos.PartnerResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;
import java.util.List;

/**
 * MapStruct — {@link Partner} → DTO. {@code certExpiry}/{@code certStatus}/{@code documents}
 * KHÔNG map từ entity — dẫn xuất runtime từ {@link com.sfms.mf04.domain.PartnerDocument}
 * (ADR-M04-4), service tính rồi truyền vào.
 */
@Mapper(componentModel = "spring")
public interface PartnerMapper {

    @Mapping(target = "id", source = "partner.code")
    PartnerResponse toResponse(Partner partner, LocalDate certExpiry, String certStatus,
                                boolean registrantEmailVerified);

    @Mapping(target = "id", source = "partner.code")
    @Mapping(target = "documents", source = "documents")
    PartnerDetailResponse toDetailResponse(Partner partner, LocalDate certExpiry, String certStatus,
                                            List<PartnerDocumentSummary> documents,
                                            boolean registrantEmailVerified);
}
