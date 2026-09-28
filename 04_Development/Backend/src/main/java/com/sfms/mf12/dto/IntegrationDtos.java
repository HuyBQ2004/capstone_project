package com.sfms.mf12.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.List;

/** UC-040 — tích hợp dịch vụ ngoài (BR-163/BR-164). TDS-MF12 §9.3 nhóm (B). */
public final class IntegrationDtos {

    private IntegrationDtos() {
    }

    /** BR-164 — {@code credentialMasked} LUÔN che, KHÔNG BAO GIỜ trả credential thật. */
    public record IntegrationResponse(
            String id,
            String label,
            String endpoint,
            String endpointPlaceholder,
            String purpose,
            boolean connected,
            String statusNote,
            String credentialMasked,
            Instant lastTestedAt,
            boolean lastTestPassed
    ) {}

    /**
     * D12 (a) — CHỈ 2 field ghi được: {@code endpoint}, {@code apiKey}. {@code connected}/
     * {@code lastTestedAt}/{@code lastTestPassed} là field DẪN XUẤT, cố tình KHÔNG có ở đây
     * (MF12-TC-CON-008 a-1/a-2) — client gửi lên bị bỏ qua, server tự suy từ kết quả test.
     */
    public record SaveIntegrationRequest(
            @NotBlank String endpoint,
            String apiKey
    ) {}

    public record TestConnectionRequest(
            String endpoint,
            String apiKey
    ) {}

    public record ConnectionTestResult(
            boolean passed,
            String message,
            Instant testedAt
    ) {}

    @Schema(name = "PagedResponseIntegrationResponse", description = "PagedResponse<IntegrationResponse>")
    public record PagedResponseIntegrationResponse(
            List<IntegrationResponse> items, long total, int page, int pageSize, int pageCount) {}
}
