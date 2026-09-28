package com.sfms.mf03.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

/** DTO của {@code /students/import/*} — UC-010, BR-032, ADR-M03-6 (2 pha: validate -> commit). */
public final class StudentImportDtos {

    private StudentImportDtos() {
    }

    /** Một hàng đã parse từ file, kèm kết quả kiểm tra. Lưu nguyên trong {@code rows_json}. */
    public record ParsedRow(
            int rowNo,
            String name,
            String dob,
            String classroomId,
            String parentFullName,
            String parentEmail,
            String parentPhone,
            String relationship,
            boolean valid,
            boolean duplicate,
            String code,
            String message
    ) {}

    @Schema(name = "ImportRowResult")
    public record ImportRowResult(
            int rowNo,
            @Schema(description = "D1 — tên HS ở hàng này (chưa có mã tới khi commit)") String studentCode,
            boolean valid,
            String code,
            String message
    ) {}

    @Schema(name = "ImportPreviewResponse")
    public record ImportPreviewResponse(
            @Schema(description = "D1 — mã đợt import, dùng ở bước commit") String importId,
            int total,
            int validCount,
            int invalidCount,
            int duplicateCount,
            List<ImportRowResult> rows
    ) {}

    @Schema(name = "ImportResultResponse")
    public record ImportResultResponse(
            int imported,
            int skipped,
            int newParentAccounts,
            String message
    ) {}

    /** Nội bộ — hàng hợp lệ đã parse xong ngày sinh, dùng khi commit tạo {@code Student}. */
    public record ValidRow(int rowNo, String name, LocalDate dob, String classroomId,
                           String parentFullName, String parentEmail, String parentPhone,
                           String relationship) {}
}
