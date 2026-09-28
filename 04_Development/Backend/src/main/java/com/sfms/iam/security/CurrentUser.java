package com.sfms.iam.security;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Inject {@link AuthUser} vào tham số controller.
 *
 * <p>Đây là <b>đường duy nhất</b> để controller biết mình đang phục vụ ai. Không controller
 * nào của MF01 được nhận {@code userId} qua {@code @RequestBody}/{@code @RequestParam}
 * (TDS §17 C2, AP-AI-006).
 *
 * <p>{@code @Hidden} để springdoc không sinh tham số giả trong {@code /v3/api-docs} —
 * nếu không, {@code openapi-typescript} sẽ sinh type FE có field {@code authUser} không tồn tại.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
@Hidden
public @interface CurrentUser {
}
