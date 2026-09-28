package com.sfms.mf02.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** DTO của {@code /api/v1/grades} — hợp đồng API cố định (FE code song song dựa trên đây). */
public final class GradeDtos {

    private GradeDtos() {
    }

    @Schema(name = "CreateGradeRequest")
    public record CreateGradeRequest(
            @NotBlank @Schema(example = "Khối 6") String name,
            @NotNull @Schema(example = "13") Integer sortOrder
    ) {}

    @Schema(name = "UpdateGradeRequest", description = "Partial update — field không gửi giữ nguyên")
    public record UpdateGradeRequest(
            String name,
            Integer sortOrder,
            Boolean active
    ) {}

    @Schema(name = "GradeResponse")
    public record GradeResponse(
            java.util.UUID id,
            String name,
            int sortOrder,
            boolean active
    ) {}
}
