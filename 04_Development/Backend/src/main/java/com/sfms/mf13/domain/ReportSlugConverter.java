package com.sfms.mf13.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * ADR-M13-5: {@code report_slug} lưu kebab-case ở DB (khớp {@code ck_rpx_slug}/{@code ck_rsc_slug}
 * và 7 view {@code v_rpt_*}), KHÔNG phải {@code name()} SCREAMING_SNAKE mặc định của
 * {@code @Enumerated(STRING)} — cùng lý do ngoại lệ D6 mà {@code RoleKey} đã áp dụng.
 */
@Converter(autoApply = false)
public class ReportSlugConverter implements AttributeConverter<ReportSlug, String> {

    @Override
    public String convertToDatabaseColumn(ReportSlug attribute) {
        return attribute == null ? null : attribute.slug();
    }

    @Override
    public ReportSlug convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ReportSlug.from(dbData);
    }
}
