package com.sfms.iam.service;

import com.sfms.iam.domain.SecurityPolicy;
import com.sfms.iam.repository.SecurityPolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cổng duy nhất đọc {@link SecurityPolicy}.
 *
 * <p>TDS §17 <b>C3</b>: mọi ngưỡng BR-002/004/006 đọc từ DB, KHÔNG hardcode. Gom về một chỗ
 * để không service nào lỡ viết {@code if (count >= 5)} hay {@code Duration.ofMinutes(30)}.
 *
 * <p>Nếu DB không có bản ghi {@code active} (chỉ xảy ra khi seed V2 chưa chạy), ném lỗi
 * tường minh thay vì âm thầm dùng mặc định — mặc định ngầm chính là cách "hardcode"
 * lẻn vào hệ thống.
 */
@Service
public class SecurityPolicyProvider {

    private final SecurityPolicyRepository repository;

    public SecurityPolicyProvider(SecurityPolicyRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public SecurityPolicy current() {
        return repository.findFirstByActiveIsTrue()
                .orElseThrow(() -> new IllegalStateException(
                        "Không có SecurityPolicy nào đang active. "
                        + "V2__seed_roles_permissions_policy.sql phải chạy trước (TDS §11.1)."));
    }
}
