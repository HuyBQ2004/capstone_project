package com.sfms.mf10.domain;

/** {@code ALL} — mọi người dùng của trường (hoặc toàn nền tảng khi {@code schoolId=null});
 *  {@code ROLES} — chỉ các vai liệt kê ở {@code recipientRoles} (M10-D08, kebab-case). */
public enum NotificationAudience {
    ALL,
    ROLES
}
