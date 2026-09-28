package com.sfms.mf10.domain;

import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * {@code illness_keywords} — BR-142 (ADR-M10-6). Bảng CẤU HÌNH, không lộ qua path ⇒ không
 * cần cột {@code code} (M10-D01).
 */
@Entity
@Table(name = "illness_keywords")
public class IllnessKeyword extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String keyword;

    @Column(nullable = false)
    private boolean active = true;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
