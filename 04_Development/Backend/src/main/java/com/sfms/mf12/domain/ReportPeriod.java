package com.sfms.mf12.domain;

/** CF-27 — giá trị lưu trữ là kebab/upper enum; FE gửi "Weekly"/"Daily" được parse case-insensitive. */
public enum ReportPeriod {
    DAILY, WEEKLY
}
