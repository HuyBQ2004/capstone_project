package com.sfms.common.config;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.type.format.jackson.Jackson3JsonFormatMapper;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class JacksonConfig {

    /**
     * Sửa 10/08/2026 (Nam) — file này trước đây khai `com.fasterxml.jackson.databind.*`
     * (Jackson 2), nhưng Spring Boot 4.1/Spring 7 đi kèm <b>Jackson 3</b>
     * ({@code tools.jackson.databind.*} — nhóm groupId đổi từ {@code com.fasterxml.jackson}
     * sang {@code tools.jackson}). Bean {@code ObjectMapper} kiểu Jackson 2 KHÔNG khớp type
     * mà {@code AbstractJacksonHttpMessageConverter} (Spring MVC) cần, nên Spring âm thầm
     * dùng converter mặc định của nó thay vì bean cấu hình ở đây — chỗ sửa
     * {@code FAIL_ON_NULL_FOR_PRIMITIVES} tưởng có tác dụng nhưng thực ra là DEAD CODE (biên
     * dịch qua được vì có dependency transitive khác vẫn kéo theo lớp Jackson 2 cũ, không
     * phải vì Spring dùng lớp đó).
     *
     * <p>Phát hiện qua lỗi thật: {@code POST /inspections/{code}/steps/1} luôn trả 422
     * "malformed request body" khi request thiếu field {@code int} không bắt buộc (VD
     * {@code Step1FreshRowDto.photoCount}) — {@code tools.jackson.databind.exc
     * .MismatchedInputException: Cannot map `null` into type `int`}. Sửa đúng type thì tắt
     * {@code FAIL_ON_NULL_FOR_PRIMITIVES} mới thật sự có tác dụng.
     *
     * <p>Jackson 3 gộp sẵn hỗ trợ {@code java.time} vào {@code jackson-databind}
     * ({@code tools.jackson.databind.ext.javatime.*}) — KHÔNG cần {@code JavaTimeModule}
     * rời như Jackson 2 nữa, dòng {@code registerModule(new JavaTimeModule())} cũ đã bỏ.
     */
    @Bean
    public JsonMapper objectMapper() {
        return JsonMapper.builder()
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .build();
    }

    /**
     * Bean Jackson 2 riêng (10/08/2026) — 7 nơi khác trong hệ thống (OAuth2 handler,
     * import Excel học sinh, DishService/MenuService...) vẫn tiêm thẳng
     * {@code com.fasterxml.jackson.databind.ObjectMapper} qua constructor, không liên quan
     * gì tới việc đọc/ghi JSON của Spring MVC. Trước đây bean {@code objectMapper()} ở trên
     * VÔ TÌNH đúng type này nên phục vụ luôn cả hai việc; nay tách bean {@link #objectMapper()}
     * sang Jackson 3 thật (để Spring MVC dùng đúng), phải giữ lại một bean Jackson 2 riêng
     * cho 7 nơi đó — KHÔNG sửa 7 file này sang Jackson 3 trong đợt này, ngoài phạm vi lỗi
     * đang xử lý (INSP-422).
     */
    @Bean
    public com.fasterxml.jackson.databind.ObjectMapper legacyJackson2ObjectMapper() {
        return new com.fasterxml.jackson.databind.ObjectMapper();
    }

    /**
     * Phát hiện 05/08/2026 khi verify `sent-reports` (payload JSONB chứa LocalDate lồng trong
     * record con — VD `InspectionBrief.date`): Hibernate `@JdbcTypeCode(SqlTypes.JSON)` (13 entity
     * toàn hệ thống) mặc định dùng MỘT ObjectMapper NỘI BỘ riêng, KHÔNG phải bean {@link #objectMapper()}
     * ở trên — nên LocalDate ghi lúc response (qua Spring MVC, dùng bean có JavaTimeModule) ra ISO
     * string, nhưng ĐỌC LẠI từ DB (qua Hibernate, ObjectMapper nội bộ không có JavaTimeModule) lại
     * ra mảng `[năm,tháng,ngày]` — cùng 1 API, 2 hình dạng response khác nhau tuỳ mới tạo hay đọc
     * lại. Trỏ Hibernate dùng LẠI ĐÚNG bean Spring đang cấu hình để nhất quán toàn hệ thống.
     *
     * <p>10/08/2026 — {@code Jackson3JsonFormatMapper} (Hibernate 7, nhận thẳng
     * {@code JsonMapper}) thay cho {@code JacksonJsonFormatMapper} (Jackson 2) cũ, khớp
     * đúng loại {@code ObjectMapper} thật đang dùng.
     */
    @Bean
    public HibernatePropertiesCustomizer hibernateJsonFormatMapperCustomizer(JsonMapper objectMapper) {
        return props -> props.put(AvailableSettings.JSON_FORMAT_MAPPER, new Jackson3JsonFormatMapper(objectMapper));
    }
}
