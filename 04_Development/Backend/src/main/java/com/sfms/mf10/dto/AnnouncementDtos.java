package com.sfms.mf10.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.Instant;
import java.util.List;

/** DTO của {@code announcements}/{@code announcement_revisions}. TDS-MF10_Backend.md §9.2. */
public final class AnnouncementDtos {

    private AnnouncementDtos() {
    }

    // ───────────────────────── Requests ─────────────────────────

    @Schema(description = "POST /announcements (UC-017, BR-138/144)")
    public record CreateAnnouncementRequest(
            @NotBlank String title,
            @NotBlank String content,
            @NotEmpty @Schema(description = "CF-M10-7 — mã chuẩn hoá (SCREAMING_SNAKE), VD PARENT_GRADE_3, TEACHER")
            List<String> audienceCodes,
            boolean urgent,
            @Schema(description = "DRAFT|PUBLISHED, mặc định DRAFT") String status) {}

    @Schema(description = "PATCH /announcements/{code} (UC-089, BR-144)")
    public record UpdateAnnouncementRequest(
            String title, String content, List<String> audienceCodes,
            Boolean urgent, String status,
            @Schema(description = "BR-144 — nội dung mục lịch sử sửa đổi mới") String revisionNote) {}

    // ───────────────────────── Response ─────────────────────────

    @Schema(name = "AnnouncementRevisionView")
    public record AnnouncementRevisionView(Instant at, String by, String note) {}

    @Schema(name = "AnnouncementDetailResponse")
    public record AnnouncementDetailResponse(
            String id, String title, String content,
            @Schema(description = "Dẫn xuất — nhãn hiển thị (M10-D14)") String audience,
            @Schema(description = "Field SỬA ĐƯỢC") List<String> audienceCodes,
            String status,
            @Schema(description = "NULL khi DRAFT — M10-D10") Instant publishedAt,
            @Schema(description = "Dẫn xuất") String author,
            boolean urgent,
            @Schema(description = "Chỉ trả cho School Administration — UC-105") List<AnnouncementRevisionView> revisions) {}

    /** School Administration — thấy cả DRAFT, có revisionCount. D11/M10-D13. */
    @Schema(name = "AnnouncementAdminListItem")
    public record AnnouncementAdminListItem(
            String id, String title, String audience, List<String> audienceCodes, String status,
            Instant publishedAt, String author, boolean urgent,
            @Schema(description = "Chỉ School Administration — L13") int revisionCount) {}

    /** Vai khác — chỉ PUBLISHED + audience khớp, KHÔNG revisionCount. D11/M10-D13. */
    @Schema(name = "AnnouncementAudienceListItem")
    public record AnnouncementAudienceListItem(
            String id, String title, String audience, List<String> audienceCodes, String status,
            Instant publishedAt, String author, boolean urgent) {}

    @Schema(name = "PagedResponseAnnouncementListItem", description = "oneOf AnnouncementAdminListItem / AnnouncementAudienceListItem")
    public record PagedResponseAnnouncementListItem(
            @ArraySchema(schema = @Schema(oneOf = {AnnouncementAdminListItem.class, AnnouncementAudienceListItem.class}))
            List<Object> items,
            long total, int page, int pageSize, int pageCount) {}
}
