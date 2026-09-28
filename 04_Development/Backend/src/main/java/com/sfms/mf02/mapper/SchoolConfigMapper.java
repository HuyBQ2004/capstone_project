package com.sfms.mf02.mapper;

import com.sfms.mf02.domain.SchoolConfig;
import com.sfms.mf02.dto.SchoolConfigDtos.SchoolConfigResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface SchoolConfigMapper {

    /**
     * {@code menuLeadTimeDays}/{@code address}/{@code lat}/{@code lng} KHÔNG có trên entity
     * ({@code school_config}) — service truyền vào qua tham số (3 field toạ độ đọc từ
     * {@code School}, xem Javadoc ở {@code SchoolConfigResponse}).
     */
    @Mapping(target = "menuLeadTimeDays", source = "menuLeadTimeDays")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "lat", source = "lat")
    @Mapping(target = "lng", source = "lng")
    SchoolConfigResponse toResponse(SchoolConfig config, int menuLeadTimeDays, String address, BigDecimal lat,
                                     BigDecimal lng);
}
