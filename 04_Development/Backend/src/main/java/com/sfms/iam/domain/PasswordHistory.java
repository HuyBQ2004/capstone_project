package com.sfms.iam.domain;

import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Lịch sử mật khẩu — BR-006 thân mục: <i>"Mật khẩu mới không được trùng với 3 mật khẩu
 * gần nhất (BR-015)"</i>.
 *
 * <p>⚠️ Oracle của MF01-TC-003 là <b>câu trong thân BR-006</b> + nội dung MSG-105, KHÔNG
 * phải một mục BR-015 riêng — {@code business_rules.md} không có mục định nghĩa BR-015
 * (TestSpec §2 L6, mã treo).
 *
 * <p>Cửa sổ là <b>đúng 3</b>: bản ghi thứ 4 trở đi không còn chặn (MF01-TC-003 nhánh (c)).
 */
@Entity
@Table(name = "password_history")
public class PasswordHistory extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "password_hash", nullable = false, length = 72)
    private String passwordHash;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
