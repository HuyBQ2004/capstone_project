package com.sfms.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * D8 — cấu hình OpenAPI/springdoc. <b>Nam khai một lần ở {@code common/}; 12 module còn lại
 * KHÔNG khai lại.</b>
 *
 * <p>Vì sao D8 tồn tại: {@code /v3/api-docs} vừa là <b>cổng nghiệm thu</b>
 * ({@code 01_MVP_Backend_Implementation_Plan.md} §Nghiệm thu — <i>"52 resource CRUD qua
 * Swagger"</i>) vừa là <b>nguồn sinh type FE</b> ({@code review_fix_plan.md} T3.4,
 * {@code openapi-typescript}). Spec sai ⇒ type FE sinh sai theo ⇒ lệch D1/D2/D4/D6 quay lại
 * qua file sinh tự động.
 *
 * <p>8 luật S1–S8 được enforce ở:
 * <ul>
 *   <li><b>S1</b> {@code @Tag(name="MFxx — <Tên module>")} — trên từng {@code @RestController}</li>
 *   <li><b>S2</b> {@code @Operation.description} chứa UC ID + BR ID — từng endpoint</li>
 *   <li><b>S3</b> {@code @ApiResponse} 4xx/5xx trỏ {@code ErrorResponse} — từng endpoint</li>
 *   <li><b>S4</b> field ngày {@code format="date"/"date-time"} + example ISO — từng DTO</li>
 *   <li><b>S5</b> enum khai bằng kiểu thật ở {@code common/enums/} — KHÔNG {@code allowableValues}</li>
 *   <li><b>S6</b> {@code example} của {@code id} là mã nghiệp vụ, không UUID</li>
 *   <li><b>S7</b> {@code PagedResponse<T>} generic — khai một lần ở {@code common/dto/}</li>
 *   <li><b>S8</b> một {@code @SecurityScheme} bearer JWT toàn cục — ngay dưới đây</li>
 * </ul>
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "SFMS API",
                version = "v1",
                description = """
                        School Food Management System — REST API (13 module).
 
                        Quy ước chung `07_implement/00_Common_Conventions.md`:
                        D1 `id` công khai = mã nghiệp vụ (UUID chỉ là PK nội bộ) ·
                        D2 envelope lỗi `{ error: { code, message, details } }` ·
                        D3 tiền tố `/api/v1` ·
                        D4 ngày/giờ luôn ISO-8601 ·
                        D5 `body.id` client gửi → kiểm trùng → 409 ·
                        D6 enum dùng chung SCREAMING_SNAKE (RoleKey kebab-case) ·
                        D7 message lỗi tiếng Anh nguyên văn `error_message.md`.
                        """,
                contact = @Contact(name = "Nguyễn Văn Nam", email = "namnvhe180863@fpt.edu.vn")
        ),
        servers = @Server(url = "/", description = "Mặc định"),
        tags = {
                @io.swagger.v3.oas.annotations.tags.Tag(name = "MF03 — Student & Parent Management", description = "Student & Parent Management"),
                @io.swagger.v3.oas.annotations.tags.Tag(name = "MF04 — Supplier & Partner Management", description = "Supplier & Partner Management")
        },
        // S8 — áp bearerAuth cho toàn bộ API; endpoint public ghi @SecurityRequirements rỗng để ghi đè.
        security = @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
)
@SecurityScheme(
        name = OpenApiConfig.BEARER_SCHEME,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = "Access token do MF01 phát hành (BR-004: TTL 30' đọc từ SecurityPolicy)"
)
public class OpenApiConfig {

    public static final String BEARER_SCHEME = "bearerAuth";

    @org.springframework.context.annotation.Bean
    public org.springdoc.core.customizers.OpenApiCustomizer customOpenApiCustomizer() {
        return openApi -> {
            if (openApi.getComponents() == null) {
                openApi.setComponents(new io.swagger.v3.oas.models.Components());
            }
            if (openApi.getComponents().getSchemas() == null) {
                openApi.getComponents().setSchemas(new java.util.LinkedHashMap<>());
            }
            var schemas = openApi.getComponents().getSchemas();
            if (!schemas.containsKey("AllergySeverity")) {
                var schema = new io.swagger.v3.oas.models.media.StringSchema();
                schema.addEnumItem("MILD");
                schema.addEnumItem("MODERATE");
                schema.addEnumItem("SEVERE");
                openApi.getComponents().addSchemas("AllergySeverity", schema);
            }
        };
    }
}
