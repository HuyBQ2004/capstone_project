package com.sfms.mf08.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/** DTO dùng chung giữa {@code FoodSample} và {@code SampleLabRequest} (ADR-M08-4). */
public final class CommonDtos {

    private CommonDtos() {
    }

    @Schema(name = "SampleUploadedFile", description = "File bằng chứng — D10-BIS: id tài liệu, KHÔNG bao giờ storageKey")
    public record UploadedFileDto(String id, String name, Instant uploadedAt, long sizeBytes) {}
}
