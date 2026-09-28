package com.sfms.mf03.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf03.dto.HealthFileDtos.HealthFileResponse;
import com.sfms.mf03.dto.HealthFileDtos.UpdateHealthFileRequest;
import com.sfms.mf03.dto.HealthRecordDtos.HealthRecordRequest;
import com.sfms.mf03.dto.HealthRecordDtos.HealthRecordResponse;
import com.sfms.mf03.error.StuExceptions;
import com.sfms.mf03.repository.StudentRepository;
import com.sfms.mf03.service.StudentHealthFileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * MF03 — hồ sơ lâm sàng (UC-051/094) + hồ sơ y tế cơ bản (UC-050/094, CF-M03-9).
 *
 * <p>C8/COND-009 — {@code PATCH .../health-file} chỉ nhận 4 field lâm sàng. Chặn field lạ
 * (allergies/specialDiet/healthStatus/allergySeverity/medicalDocuments…) bằng cách đọc
 * <b>raw JSON map</b> trước khi bind DTO — Jackson mặc định BỎ QUA field lạ khi bind trực tiếp
 * vào record, nên gate C8 phải nằm ở tầng đọc map thô (AP-AI-015 — "silent field drop").
 */
@Tag(name = "MF03 — Student & Parent Management")
@RestController
@RequestMapping("/api/v1/students/{code}")
public class StudentHealthFileController {

    private static final Set<String> ALLOWED_HEALTH_FILE_FIELDS = Set.of(
            "medicalConditions", "medications", "emergencyContact", "emergencyInstructions");

    private final StudentHealthFileService service;
    private final StudentRepository studentRepository;
    private final ObjectMapper objectMapper;

    public StudentHealthFileController(StudentHealthFileService service,
                                       StudentRepository studentRepository,
                                       ObjectMapper objectMapper) {
        this.service = service;
        this.studentRepository = studentRepository;
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "Xem hồ sơ lâm sàng", description = "UC-051 · BR-030")
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @GetMapping("/health-file")
    public HealthFileResponse getFile(@PathVariable String code, @CurrentUser AuthUser actor) {
        return service.getFile(resolveStudentId(code), actor);
    }

    @Operation(summary = "Cập nhật hồ sơ lâm sàng",
            description = "UC-094 · BR-030 · C8 — CHỈ 4 field lâm sàng; field khai báo -> 400 STU-094")
    @ApiResponse(responseCode = "400", description = "STU-094 — MSG-115",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PatchMapping("/health-file")
    public HealthFileResponse patchFile(@PathVariable String code, @RequestBody Map<String, Object> rawBody,
                                        @CurrentUser AuthUser actor) {
        for (String field : rawBody.keySet()) {
            if (!ALLOWED_HEALTH_FILE_FIELDS.contains(field)) {
                throw StuExceptions.healthFileFieldNotEditable(field);        // 400 STU-094
            }
        }
        UpdateHealthFileRequest request = objectMapper.convertValue(rawBody, UpdateHealthFileRequest.class);
        return service.appendClinical(resolveStudentId(code), request, actor);
    }

    /**
     * Chưa có bản ghi ⇒ {@code 204 No Content} (KHÔNG 404 — chốt 02/08/2026: học sinh TỒN TẠI,
     * chỉ là chưa nhập chỉ số, 404 sẽ khiến FE hiện "không tìm thấy học sinh").
     * <p>
     * Lịch sử 2 lần sửa — đọc trước khi đổi tiếp:
     * <ol>
     *   <li>Ban đầu {@code ResponseEntity.ok("null")} — trả CHUỖI JSON {@code "null"}, FE phải
     *       vá phòng thủ bằng {@code typeof === "object"}.</li>
     *   <li>08/2026 đổi sang {@code ResponseEntity.ok(res)} với ý định trả JSON {@code null}
     *       thật — NHƯNG Spring KHÔNG serialize body {@code null}, nó bỏ hẳn body: kết quả thực
     *       tế là {@code 200} + {@code Content-Length: 0} (đo bằng curl 07/08/2026), một hợp
     *       đồng MƠ HỒ mà mỗi HTTP client parse một kiểu. Test
     *       {@code HealthRecordIT#getHealthRecordReturns204WhenAbsent} khi đó vẫn khẳng định
     *       hành vi (1) nên FAIL — lần sửa đó không chạy test.</li>
     * </ol>
     * Nay chốt hẳn {@code 204}: "không có nội dung" nói đúng ý định, và mọi client đều hiểu
     * giống nhau.
     */
    @Operation(summary = "Xem hồ sơ y tế cơ bản",
            description = "UC-050 · BR-030 · CF-M03-9 · chưa có bản ghi -> 204 No Content "
                    + "(KHÔNG 404, KHÔNG chuỗi \"null\" — chốt 07/08/2026, xem HealthRecordDto ở FE)")
    @PreAuthorize("hasAnyRole('MEDICAL_STAFF','PARENT')")
    @GetMapping("/health-record")
    public org.springframework.http.ResponseEntity<HealthRecordResponse> getRecord(@PathVariable String code, @CurrentUser AuthUser actor) {
        HealthRecordResponse res = service.getHealthRecord(resolveStudentId(code), actor);
        return res == null
                ? org.springframework.http.ResponseEntity.noContent().build()
                : org.springframework.http.ResponseEntity.ok(res);
    }

    @Operation(summary = "Cập nhật hồ sơ y tế cơ bản", description = "UC-094 · BR-030 · CF-M03-9")
    @PreAuthorize("hasAnyRole('MEDICAL_STAFF','PARENT')")
    @PutMapping("/health-record")
    public HealthRecordResponse putRecord(@PathVariable String code,
                                          @jakarta.validation.Valid @RequestBody HealthRecordRequest request,
                                          @CurrentUser AuthUser actor) {
        return service.upsertHealthRecord(resolveStudentId(code), request, actor);
    }

    private UUID resolveStudentId(String code) {
        return studentRepository.findAll().stream()
                .filter(s -> s.getCode().equals(code)).findFirst()
                .orElseThrow(StuExceptions::notFound)
                .getId();
    }
}
