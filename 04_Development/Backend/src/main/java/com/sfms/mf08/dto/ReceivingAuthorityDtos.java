package com.sfms.mf08.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** DTO của {@code receiving_authorities} (danh mục MF08 sở hữu). */
public final class ReceivingAuthorityDtos {

    private ReceivingAuthorityDtos() {
    }

    @Schema(name = "ReceivingAuthorityResponse")
    public record ReceivingAuthorityResponse(
            String id, String name, String type, String contact, String address,
            boolean isActive, String note) {}

    @Schema(name = "PagedResponseReceivingAuthorityResponse",
            description = "PagedResponse<ReceivingAuthorityResponse>")
    public record PagedResponseReceivingAuthorityResponse(
            List<ReceivingAuthorityResponse> items, long total, int page, int pageSize, int pageCount) {}
}
