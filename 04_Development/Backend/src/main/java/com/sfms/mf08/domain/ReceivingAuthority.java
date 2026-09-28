package com.sfms.mf08.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * {@code receiving_authorities} — danh mục nơi tiếp nhận mẫu (MF08 sở hữu, MF11 dùng lại bằng
 * mã). Danh mục TOÀN HỆ THỐNG (không {@code school_id}) — {@code code}/{@code name} UNIQUE
 * toàn cục là ĐÚNG ở đây, khác các entity kia của module (M08-D03).
 */
@Entity
@Table(name = "receiving_authorities")
public class ReceivingAuthority extends BaseEntity {

    @JsonProperty("id")
    @Column(nullable = false, unique = true, length = 32)
    private String code;   // "LAB-CDC-HN"

    @Column(nullable = false, unique = true, length = 180)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AuthorityType type;

    @Column(length = 180)
    private String contact;

    @Column(length = 255)
    private String address;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(length = 400)
    private String note;

    // ⛔ D9 / M08-D02 — KHÔNG khai createdAt/updatedAt/createdBy/updatedBy ở đây (BaseEntity).

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AuthorityType getType() {
        return type;
    }

    public void setType(AuthorityType type) {
        this.type = type;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
