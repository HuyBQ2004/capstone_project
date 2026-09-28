package com.sfms.mf12.service;

import com.sfms.mf12.domain.AiRequestStatus;

import java.util.UUID;

/**
 * ADR-M12-01 — cổng AI <b>duy nhất</b> nội bộ MF12; {@link GeminiAiAssistAdapter} (cho MF05)
 * và {@code AiChatServiceImpl} (UC-012) đều đi qua đây, đảm bảo BR-157 (archive bắt buộc,
 * cùng transaction) không bị lách bởi bất kỳ đường gọi AI nào khác.
 */
public interface AiGateway {

    /**
     * @param conversationId hội thoại đã tồn tại — caller chịu trách nhiệm find-or-create
     * @param module         {@code "MF05_MENU"} | {@code "MF12_CHAT"}
     * @throws com.sfms.common.error.ApiException AIS-073 (AI lỗi/timeout, đã archive
     *                                             {@code status=TIMEOUT} trước khi ném) hoặc
     *                                             AIS-157 (ghi archive thất bại, rollback)
     */
    AiOutcome generate(UUID conversationId, String module, String prompt, UUID actorUserId);

    record AiOutcome(String responseText, String model, AiRequestStatus status, int latencyMs, String requestCode) {}
}
