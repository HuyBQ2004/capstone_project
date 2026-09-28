package com.sfms.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Trước đây KHÔNG có bean {@code RestTemplate} nào — {@code GeminiClient} tự
 * {@code new RestTemplate()} qua constructor no-arg dự phòng. {@code InsightFaceServiceAdapter}
 * (kích hoạt khi {@code @Profile("prod")} + {@code sfms.frs.enabled=true}) chỉ có ĐÚNG MỘT
 * constructor nhận {@code RestTemplate} — không có bean này app sẽ KHÔNG boot được
 * (`NoSuchBeanDefinitionException`) ngay khi bật FRS thật.
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
