package com.sfms.mf03.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf03.dto.HealthHistoryDtos.HealthHistoryEventResponse;
import com.sfms.mf03.error.StuExceptions;
import com.sfms.mf03.service.HealthHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * MF03 — timeline sức khỏe (UC-146). BR-172/ADR-M03-4: APPEND-ONLY. {@code PUT}/{@code PATCH}/
 * {@code DELETE} khai <b>tường minh</b> ở đây để trả đúng envelope D2 + mã {@code STU-172}
 * thay vì để Spring MVC trả 405 mặc định không có body (C5, AP-AI-013).
 *
 * <p><b>Có chủ đích KHÔNG có {@code POST} công khai ở đây.</b> {@code HealthHistoryService.append}
 * javadoc + ADR-M03-4 (TDS §3, Approved 2026-08-02 bởi Nam) đều chốt: append() là <b>service nội
 * bộ</b>, gọi trong CÙNG transaction với hành động nghiệp vụ đã xác thực actor/phạm vi (verify
 * dị ứng — {@code HealthReviewServiceImpl}, khai báo — {@code AllergyDeclarationServiceImpl}/
 * {@code ChronicConditionServiceImpl}, ghi chú lâm sàng — {@code StudentHealthFileServiceImpl},
 * duyệt/từ chối chế độ ăn — {@code SpecialMealRequestServiceImpl}). Một {@code POST} công khai ở
 * đây sẽ cho phép GHI TỰ DO vào nhật ký sức khỏe với {@code type}/{@code event} tuỳ ý — đúng thứ
 * BR-172 ("ngăn chặn MỌI hình thức chỉnh sửa trái phép") muốn chặn, vì "chỉnh sửa trái phép" ở
 * mức nhật ký y tế không chỉ là UPDATE/DELETE mà còn là chèn dòng giả không gắn với hành động
 * nghiệp vụ đã qua gate xác thực. {@link HealthHistoryAppendOnlyIT} (đã có, đang PASS) khoá hành
 * vi 405 của 3 mapping dưới; không thêm endpoint ghi nào phá vỡ giả định đó mà không sửa cả test
 * lẫn ADR đã duyệt. Nếu một luồng nghiệp vụ MỚI cần append mà chưa có service tương ứng, hãy
 * thêm lời gọi {@code healthHistoryService.append(...)} vào service đó — không mở endpoint mới.</p>
 */
@Tag(name = "MF03 — Student & Parent Management")
@RestController
@RequestMapping("/api/v1/health-history")
public class HealthHistoryController {

    private final HealthHistoryService healthHistoryService;

    public HealthHistoryController(HealthHistoryService healthHistoryService) {
        this.healthHistoryService = healthHistoryService;
    }

    @Operation(summary = "Lịch sử sức khỏe (timeline read-only)", description = "UC-146 · BR-172. "
            + "studentId nhận MÃ NGHIỆP VỤ (D1, VD \"HS-3A-002\"), không phải UUID nội bộ.")
    @PreAuthorize("hasAnyRole('MEDICAL_STAFF','PARENT')")
    @GetMapping
    public PagedResponse<HealthHistoryEventResponse> list(@RequestParam Map<String, String> allParams,
                                                           @RequestParam String studentId,
                                                           @CurrentUser AuthUser actor) {
        return healthHistoryService.timeline(studentId, ListQuery.from(allParams), actor);
    }

    @Operation(summary = "KHÔNG hỗ trợ — append-only", description = "UC-146 · BR-172 · luôn 405 STU-172")
    @ApiResponse(responseCode = "405", description = "STU-172 — MSG-116",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping("/{id}")
    public HealthHistoryEventResponse put(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        throw StuExceptions.healthHistoryAppendOnly();
    }

    @Operation(summary = "KHÔNG hỗ trợ — append-only", description = "UC-146 · BR-172 · luôn 405 STU-172")
    @ApiResponse(responseCode = "405", description = "STU-172 — MSG-116",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping("/{id}")
    public HealthHistoryEventResponse patch(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        throw StuExceptions.healthHistoryAppendOnly();
    }

    @Operation(summary = "KHÔNG hỗ trợ — append-only", description = "UC-146 · BR-172 · luôn 405 STU-172")
    @ApiResponse(responseCode = "405", description = "STU-172 — MSG-116",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        throw StuExceptions.healthHistoryAppendOnly();
    }
}
