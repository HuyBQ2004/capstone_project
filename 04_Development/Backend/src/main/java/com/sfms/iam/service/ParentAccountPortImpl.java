package com.sfms.iam.service;

import com.sfms.common.enums.RoleKey;
import com.sfms.iam.domain.UserAccount;
import com.sfms.iam.dto.UserDtos.CreateUserRequest;
import com.sfms.iam.port.ParentAccountPort;
import com.sfms.iam.repository.UserAccountRepository;
import com.sfms.iam.security.AuthUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter mỏng cho {@link ParentAccountPort} — bọc {@link UserService#create}, truyền
 * {@link RoleKey#PARENT}. <b>Không</b> logic mới: BR-010 (kích hoạt qua email)/BR-011 (token
 * 48h)/BR-012 (email/SĐT trùng, định dạng) đã nằm sẵn trong {@code UserService.create()}.
 *
 * <p>Việc tồn đọng #3 của {@code 00_Common_Conventions.md} §0-BIS: "Tạo `ParentAccountPort`
 * (BR-010/012) — Huy tự viết, báo Nam vì file nằm trong {@code iam/}".
 */
@Service
public class ParentAccountPortImpl implements ParentAccountPort {

    private final UserService userService;
    private final UserAccountRepository userAccountRepository;

    public ParentAccountPortImpl(UserService userService, UserAccountRepository userAccountRepository) {
        this.userService = userService;
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<ParentAccountRef> findParentByEmail(String email) {
        if (email == null || email.isBlank()) {
            return java.util.Optional.empty();
        }
        return userAccountRepository.findByEmail(email).map(ParentAccountPortImpl::toRef);
    }

    @Override
    @Transactional
    public ParentAccountRef createParentAccount(String name, String email, String phone,
                                                 String schoolCode, AuthUser actor, String ip) {
        CreateUserRequest request = new CreateUserRequest(
                null,                 // D5 — không nhận id từ MF03; server (UserService) tự sinh U-xxxx
                name,
                email,
                phone,
                RoleKey.PARENT,       // quyết định (a) đã chốt — luôn PARENT qua port này
                schoolCode,
                null                  // birthDate — UC-025 không thu thập ngày sinh phụ huynh
        );
        userService.create(request, actor, ip == null ? "0.0.0.0" : ip);
        // ⚠️ Tra lại bằng EMAIL (UNIQUE toàn hệ thống, V1 DDL), KHÔNG bằng `code` — `code` chỉ
        // UNIQUE(school_id, code) (D1), nên UserAccountRepository.findByCode() (tra cứu KHÔNG
        // lọc theo trường) có thể khớp NHIỀU dòng nếu 2 trường khác nhau từng cấp cùng một mã
        // (ví dụ bộ đếm code_sequences scope='users' chưa được các seed viết tay — như
        // db/testseed/V900 của MF01/MF02 — tiến bộ đếm lên trước) và ném
        // NonUniqueResultException — phát hiện thực nghiệm khi chạy StudentCreateIT/
        // StudentCapacityIT/PublicIdContractIT thật với Testcontainers.
        UserAccount saved = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException(
                        "ParentAccountPort: user vừa tạo không tìm lại được bằng email"));
        return toRef(saved);
    }

    private static ParentAccountRef toRef(UserAccount u) {
        return new ParentAccountRef(u.getId(), u.getCode(), u.getName(), u.getEmail());
    }
}
