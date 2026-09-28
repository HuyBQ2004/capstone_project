package com.sfms.mf07.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * DTO của {@code checklist-groups} (UC-186/BR-207/BR-210). TDS-MF07_Backend.md §9.3.7. M07-D10.
 *
 * <p>⚠️ Mọi record đặt {@code @Schema(name=...)} tiền tố {@code Checklist} — {@code ItemRequest}/
 * {@code ItemResponse} trùng tên hệt {@code mf06.dto.PurchaseRequestDtos.ItemRequest/ItemResponse}:
 * springdoc gộp theo SIMPLE NAME của record, không theo package, nên trước khi sửa,
 * {@code openapi-typescript} chỉ sinh MỘT shape (của MF06) cho CẢ HAI endpoint — lỗi không lộ ở
 * {@code tsc}, chỉ lộ khi gọi thật. Đúng lớp lỗi Huy đã tìm+sửa cho MF06 (4 {@code CreateRequest}
 * trùng tên), phát hiện lại ở đây khi làm Bước 3 (đối chiếu {@code schema.d.ts} thật với DTO Java).
 */
public final class ChecklistDtos {

    private ChecklistDtos() {
    }

    @Schema(name = "ChecklistItemRequest")
    public record ItemRequest(String id, @NotBlank String name, String legalNote) {}

    @Schema(name = "CreateChecklistGroupRequest")
    public record CreateGroupRequest(@NotBlank String name, Integer order, List<ItemRequest> items) {}

    @Schema(name = "UpdateChecklistGroupRequest")
    public record UpdateGroupRequest(String name, Integer order, List<ItemRequest> items) {}

    @Schema(name = "ChecklistItemResponse")
    public record ItemResponse(String id, String name, String legalNote) {}

    @Schema(name = "ChecklistGroupResponse")
    public record GroupResponse(String id, String name, int order, List<ItemResponse> items) {}

    @Schema(name = "PagedResponseChecklistGroupResponse")
    public record PagedResponseGroupResponse(List<GroupResponse> items, long total, int page, int pageSize, int pageCount) {}
}
