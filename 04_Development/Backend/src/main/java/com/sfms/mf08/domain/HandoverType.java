package com.sfms.mf08.domain;

/**
 * BR-212 — {@code LAB} (gửi xét nghiệm, chờ kết quả, bắt buộc {@code testCriteria}) hoặc
 * {@code AUTHORITY} (cơ quan chức năng thu giữ làm vật chứng, bắt buộc {@code authorityId},
 * không có chỉ tiêu).
 */
public enum HandoverType {
    LAB, AUTHORITY
}
