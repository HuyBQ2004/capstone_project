package com.sfms.common.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** TDS §11.3 Chặng 4 — {@code curl https://[host]/api/v1/health} ⇒ {@code {"status":"ok"}}. */
@Tag(name = "Common — Infrastructure")
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @Operation(summary = "Kiểm tra sức khỏe dịch vụ", description = "TDS §11.3 Chặng 4")
    @SecurityRequirements
    @GetMapping
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
