package com.sfms.iam.port;

import com.sfms.iam.security.AuthUser;

import java.util.Optional;
import java.util.UUID;

/**
 * Port do <b>MF01 (iam)</b> sở hữu — mở "hành vi" (tạo tài khoản) cho module khác gọi mà
 * <b>không</b> import entity/repository nội bộ của {@code iam} (ADR-M02-5, §0-BIS "Cross-module:
 * đọc dữ liệu module khác thế nào" của {@code 00_Common_Conventions.md}).
 *
 * <p>MF03 (UC-025 bước 5, UC-010 bước 3) dùng port này để liên kết/tạo tài khoản phụ huynh khi
 * tạo hồ sơ học sinh — <b>không</b> tự ghi {@code user_accounts}, không tự gửi email. Logic
 * BR-010 (kích hoạt qua email)/BR-012 (định dạng email/SĐT) đã nằm sẵn trong
 * {@code UserService.create()}; adapter ở đây chỉ bọc mỏng, truyền {@code RoleKey.PARENT}.
 *
 * @version 1.0
 */
public interface ParentAccountPort {

    /**
     * Tra tài khoản Parent theo email (UC-025 bước 5 — "email đã có tài khoản").
     *
     * @return thông tin tối thiểu để MF03 dựng {@code student_parents}, hoặc rỗng nếu chưa có
     *         tài khoản nào dùng email này (bất kể role — email UNIQUE toàn hệ thống, BR-012).
     */
    Optional<ParentAccountRef> findParentByEmail(String email);

    /**
     * Tạo tài khoản Parent mới qua {@code UserService.create()} (BR-010/BR-011/BR-012 đã xử lý
     * sẵn ở MF01: {@code PENDING_VERIFICATION}, {@code emailVerified=false}, gửi email kích hoạt
     * 48h). MF03 gọi hàm này khi {@link #findParentByEmail(String)} rỗng.
     *
     * @param name      họ tên phụ huynh
     * @param email     email — kiểm trùng ở {@code UserService} (BR-012), 409 nếu trùng
     * @param phone     SĐT Việt Nam, có thể {@code null}
     * @param schoolCode mã nghiệp vụ của trường (D1) — gắn tài khoản PH vào đúng trường của HS
     * @param actor     người thực hiện thao tác nghiệp vụ gốc (School Admin tạo HS…) — dùng để
     *                  audit; {@code AuthUser} là identity dùng chung (§0-BIS), không phải
     *                  entity nội bộ của {@code iam}
     * @param ip        IP request gốc (audit)
     */
    ParentAccountRef createParentAccount(String name, String email, String phone,
                                         String schoolCode, AuthUser actor, String ip);

    /** Hồ sơ tối thiểu MF03 cần để dựng {@code student_parents(parent_user_id, ...)}. */
    record ParentAccountRef(UUID userId, String code, String name, String email) {
    }
}
