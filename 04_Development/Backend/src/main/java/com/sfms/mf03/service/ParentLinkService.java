package com.sfms.mf03.service;

import com.sfms.iam.security.AuthUser;
import com.sfms.mf03.dto.StudentDtos.ParentInfoRequest;

import java.util.UUID;

/**
 * ADR-M03-1 — liên kết HS↔tài khoản phụ huynh bằng bảng nối {@code student_parents}, trỏ
 * {@code user_accounts.id} (KHÔNG bằng chuỗi tên — CF-M03-7). BR-030 (≥1 PH/HS), BR-201
 * (≤5 HS/PH).
 *
 * @version 1.0
 */
public interface ParentLinkService {

    /**
     * UC-025 bước 5 — tra tài khoản PH theo email; chưa có thì tạo qua
     * {@link com.sfms.iam.port.ParentAccountPort} (BR-010/012, MF01).
     *
     * @throws com.sfms.common.error.ApiException {@code STU-201} nếu PH đã đạt 5 liên kết
     */
    LinkResult linkOrCreateParent(UUID studentId, ParentInfoRequest info, AuthUser actor, String ip);

    /**
     * @throws com.sfms.common.error.ApiException {@code STU-030} nếu đây là dòng liên kết
     *         cuối cùng của học sinh (BR-030: ≥1 PH bắt buộc)
     */
    void unlink(UUID studentId, UUID parentUserId, AuthUser actor);

    long countLinkedStudents(UUID parentUserId);

    record LinkResult(UUID parentUserId, String parentCode, String parentName, boolean accountCreated) {}
}
