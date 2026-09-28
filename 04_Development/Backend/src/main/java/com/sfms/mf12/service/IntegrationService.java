package com.sfms.mf12.service;

import com.sfms.mf12.dto.IntegrationDtos.ConnectionTestResult;
import com.sfms.mf12.dto.IntegrationDtos.IntegrationResponse;
import com.sfms.mf12.dto.IntegrationDtos.SaveIntegrationRequest;
import com.sfms.mf12.dto.IntegrationDtos.TestConnectionRequest;

import java.util.List;

/** UC-040/UC-149 — tích hợp dịch vụ ngoài. */
public interface IntegrationService {

    /** UC-040 — danh sách 4 dịch vụ; credential trả về dạng masked (BR-164). */
    List<IntegrationResponse> list();

    /** BR-163 — handshake timeout 5s (ADR-000: stub); kết quả lưu {@code lastTestPassed}. */
    ConnectionTestResult testConnection(String code, TestConnectionRequest request, String actorEmail, String ip);

    /** BR-163: chỉ lưu khi lần test gần nhất PASS và chưa quá 10 phút. BR-164: mã hoá credential. */
    IntegrationResponse save(String code, SaveIntegrationRequest request, String actorEmail, String ip);
}
