package com.sfms.mf13.web;

import com.sfms.mf13.domain.ReportSlug;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC binds {@code @PathVariable} enums bằng {@code Enum.valueOf(name())} mặc định —
 * {@code ReportSlug} lưu kebab-case ({@code ADR-M13-5}), nên {@code /reports/hygiene/rows} sẽ
 * ném {@code MethodArgumentTypeMismatchException} nếu không có converter riêng. {@code @JsonCreator}
 * trên enum chỉ áp dụng cho body JSON, không áp dụng cho path variable.
 */
@Configuration
public class ReportSlugPathConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(@NonNull FormatterRegistry registry) {
        registry.addConverter(new StringToReportSlugConverter());
    }

    private static final class StringToReportSlugConverter implements Converter<String, ReportSlug> {
        @Override
        public ReportSlug convert(@NonNull String source) {
            return ReportSlug.from(source);
        }
    }
}
