package com.sfms.iam.service;

import com.sfms.iam.domain.PasswordHistory;
import com.sfms.iam.error.IamExceptions;
import com.sfms.iam.repository.PasswordHistoryRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * BR-006 thân mục (dẫn BR-015) — <i>"Mật khẩu mới không được trùng với 3 mật khẩu gần nhất"</i>.
 *
 * <p>⚠️ Oracle là câu trong thân <b>BR-006</b> + nội dung <b>MSG-105</b>.
 * {@code business_rules.md} KHÔNG có mục định nghĩa BR-015 riêng (TestSpec §2 L6 — mã treo).
 *
 * <p>Vi phạm ⇒ {@code IAM-006reuse} (422, MSG-105), <b>không phải</b> {@code IAM-006policy}
 * và <b>không phải</b> MSG-063 (Warning, không chặn được — TestSpec §2 L1/L2).
 */
@Service
public class PasswordHistoryService {

    /** Cửa sổ là <b>đúng 3</b>. MF01-TC-003 nhánh (c): bản ghi thứ 4 không còn chặn. */
    public static final int WINDOW = 3;

    private final PasswordHistoryRepository repository;
    private final PasswordEncoder passwordEncoder;

    public PasswordHistoryService(PasswordHistoryRepository repository,
                                  PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * @param noReuseLast3 cờ đọc từ {@code SecurityPolicy} — tắt cờ thì bỏ qua kiểm tra
     *                     (MF01-TC-003 nhánh (d))
     * @throws com.sfms.common.error.ApiException {@code IAM-006reuse} (422, MSG-105)
     */
    @Transactional(readOnly = true)
    public void assertNotReused(UUID userId, String rawNewPassword, boolean noReuseLast3) {
        if (!noReuseLast3) {
            return;
        }
        boolean reused = repository.findTop3ByUserIdOrderByCreatedAtDesc(userId).stream()
                .anyMatch(h -> passwordEncoder.matches(rawNewPassword, h.getPasswordHash()));
        if (reused) {
            throw IamExceptions.passwordReuse();
        }
    }

    /** Thêm hash mới và cắt bớt để giữ tối đa {@value #WINDOW} bản ghi (TDS §5.1 "0..3"). */
    @Transactional
    public void record(UUID userId, String newPasswordHash) {
        PasswordHistory entry = new PasswordHistory();
        entry.setUserId(userId);
        entry.setPasswordHash(newPasswordHash);
        repository.saveAndFlush(entry);

        List<PasswordHistory> all = repository.findByUserIdOrderByCreatedAtDesc(userId);
        if (all.size() > WINDOW) {
            List<UUID> keep = all.subList(0, WINDOW).stream().map(PasswordHistory::getId).toList();
            repository.deleteOlderThan(userId, keep);
        }
    }
}
