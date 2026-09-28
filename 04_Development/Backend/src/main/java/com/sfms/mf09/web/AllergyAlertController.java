package com.sfms.mf09.web;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf09.dto.AllergyAlertDtos.AllergyAlertResponse;
import com.sfms.mf09.service.AllergyAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** MF09 — {@code allergy-alerts} (UC-103). TDS-MF09_Backend.md §9.2/§16. */
@Tag(name = "MF09 — Meal Serving & Distribution")
@RestController
@RequestMapping("/api/v1/allergy-alerts")
public class AllergyAlertController {

    private final AllergyAlertService service;

    public AllergyAlertController(AllergyAlertService service) {
        this.service = service;
    }

    @Operation(summary = "Cảnh báo dị ứng theo lớp/bữa/ngày", description = "UC-103 — L20: chỉ cờ boolean, không mã MSG-051")
    @PreAuthorize("hasAnyRole('SERVICE_CATERERS','TEACHER','MEDICAL_STAFF')")
    @GetMapping
    public PagedResponse<AllergyAlertResponse> list(@RequestParam Map<String, String> allParams,
                                                     @CurrentUser AuthUser actor) {
        return service.list(ListQuery.from(allParams), actor);
    }
}
