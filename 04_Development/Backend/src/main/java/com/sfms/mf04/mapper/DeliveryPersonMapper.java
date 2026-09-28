package com.sfms.mf04.mapper;

import com.sfms.mf04.domain.DeliveryPerson;
import com.sfms.mf04.dto.DeliveryPersonDtos.DeliveryPersonResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct — {@link DeliveryPerson} → DTO. {@code nationalId} trả DẠNG CHE, KHÔNG map trực
 * tiếp {@code nationalIdEnc}/{@code nationalIdHash} — service tự tính chuỗi che rồi truyền vào
 * (PDPD, ADR-M04-5).
 */
@Mapper(componentModel = "spring")
public interface DeliveryPersonMapper {

    @Mapping(target = "id", source = "dp.code")
    @Mapping(target = "partnerId", source = "partnerCode")
    @Mapping(target = "nationalId", source = "maskedNationalId")
    DeliveryPersonResponse toResponse(DeliveryPerson dp, String partnerCode, String maskedNationalId);
}
