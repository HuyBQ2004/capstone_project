package com.sfms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * SFMS — School Food Management System.
 *
 * <p>Điểm khởi động duy nhất cho cả 13 module. `common/` là nền dùng chung
 * (Nam giữ, Team Plan §4b); mỗi module là một package con `com.sfms.<bc>`.
 * MF01 (User Management &amp; Access Control) nằm ở {@code com.sfms.iam} —
 * Bounded Context IAM theo TDS-MF01 §1.
 *
 * <p>{@code @EnableScheduling} chuyển sang {@code common.config.SchedulingConfig} (12/08/2026,
 * gắn {@code @Profile("!test")}) — job nền (MF08 retention, MF13 report tick, MF12 metric
 * snapshot) chạy theo đồng hồ THẬT, không theo {@code Clock} giả của IT; để nguyên ở đây thì
 * job tình cờ chạy đúng lúc {@code AbstractIamIntegrationTest} đang clean+migrate schema giữa
 * 2 test sẽ văng lỗi "relation does not exist" — noise vô hại nhưng làm log verify dài vô ích.
 */
@SpringBootApplication
@EnableJpaAuditing(dateTimeProviderRef = "dateTimeProvider", auditorAwareRef = "auditorAware")
@EnableAsync
public class SfmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SfmsApplication.class, args);
    }
}
