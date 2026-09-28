package com.sfms.mf05.service;

import com.sfms.iam.security.AuthUser;
import com.sfms.mf05.dto.MenuDtos.ApproveMenuRequest;
import com.sfms.mf05.dto.MenuDtos.MenuResponse;
import com.sfms.mf05.dto.MenuDtos.RejectMenuRequest;

/**
 * BR-068 — 4 action endpoint "chuẩn" (M05-D13), thay cho việc đổi trạng thái tự do qua
 * {@code PUT} (giai đoạn 2). TDS-MF05_Backend.md §8.1/§9.3.9.
 */
public interface MenuWorkflowService {

    /** UC-087 — BR-061 + BR-063 + BR-069 + BR-065; MSG-039. */
    MenuResponse submit(String code, AuthUser actor);

    /**
     * UC-006 — BR-068 (chỉ School Administration); MSG-040. CF-8: nhảy thẳng
     * {@code PENDING_APPROVAL → PUBLISHED} được xử lý atomic (approve+publish), ghi log
     * {@code MENU_STATE_JUMP}.
     */
    MenuResponse approve(String code, ApproveMenuRequest request, AuthUser actor);

    /** UC-006 AF1 — bắt buộc {@code rejectReason}; MSG-074. */
    MenuResponse reject(String code, RejectMenuRequest request, AuthUser actor);

    /** Tách {@code APPROVED → PUBLISHED} đúng BR-068 (đường "chuẩn", CF-8). */
    MenuResponse publish(String code, AuthUser actor);
}
