package com.sfms.mf12.service;

import com.sfms.iam.service.AuditService;
import com.sfms.mf12.domain.ExternalIntegration;
import com.sfms.mf12.dto.IntegrationDtos.ConnectionTestResult;
import com.sfms.mf12.dto.IntegrationDtos.IntegrationResponse;
import com.sfms.mf12.dto.IntegrationDtos.SaveIntegrationRequest;
import com.sfms.mf12.dto.IntegrationDtos.TestConnectionRequest;
import com.sfms.mf12.error.AisExceptions;
import com.sfms.mf12.repository.ExternalIntegrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * UC-040 — BR-163 (test connection bắt buộc trước khi lưu) + BR-164 (mã hoá envelope).
 * {@code M12-D05}: {@code connected}/{@code lastTestedAt}/{@code lastTestPassed} là field
 * DẪN XUẤT — {@link SaveIntegrationRequest} cố tình KHÔNG có chúng (D12).
 */
@Service
public class IntegrationServiceImpl implements IntegrationService {

    /** BR-163 — kết quả test chỉ còn hiệu lực trong 10 phút. */
    private static final Duration TEST_VALIDITY = Duration.ofMinutes(10);
    private static final String RESOURCE_INTEGRATIONS = "External Integrations";

    private final ExternalIntegrationRepository repository;
    private final CredentialCipher credentialCipher;
    private final Clock clock;
    private final AuditService auditService;

    public IntegrationServiceImpl(ExternalIntegrationRepository repository, CredentialCipher credentialCipher,
                                   Clock clock, AuditService auditService) {
        this.repository = repository;
        this.credentialCipher = credentialCipher;
        this.clock = clock;
        this.auditService = auditService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<IntegrationResponse> list() {
        return repository.findAllByOrderByLabelAsc().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public ConnectionTestResult testConnection(String code, TestConnectionRequest request, String actorEmail,
                                                String ip) {
        ExternalIntegration integration = repository.findByCode(code).orElseThrow(AisExceptions::notFound);

        Instant now = clock.instant();
        boolean passed = stubHandshake(request);
        integration.setLastTestedAt(now);
        integration.setLastTestPassed(passed);
        if (!passed) {
            // BR-163 — test fail: một lần test hỏng làm mất hiệu lực trạng thái "đã kết nối" cũ.
            integration.setConnected(false);
        }
        repository.save(integration);
        auditService.success(actorEmail, ip, "TEST_CONNECTION integration=" + code, RESOURCE_INTEGRATIONS);

        return new ConnectionTestResult(passed, passed ? "Connection test passed." : AisExceptions
                .integrationTestFailed().getMessage(), now);
    }

    /** ADR-000 stub — deterministic, không gọi mạng thật ở dev/test. */
    private static boolean stubHandshake(TestConnectionRequest request) {
        String apiKey = request == null ? null : request.apiKey();
        if (apiKey == null || apiKey.isBlank()) {
            return false;
        }
        return !apiKey.toLowerCase().contains("invalid");
    }

    @Override
    @Transactional
    public IntegrationResponse save(String code, SaveIntegrationRequest request, String actorEmail, String ip) {
        ExternalIntegration integration = repository.findByCode(code).orElseThrow(AisExceptions::notFound);

        integration.setEndpoint(request.endpoint());

        if (request.apiKey() != null && !request.apiKey().isBlank()) {
            boolean recentlyTested = integration.getLastTestedAt() != null
                    && integration.isLastTestPassed()
                    && Duration.between(integration.getLastTestedAt(), clock.instant()).compareTo(TEST_VALIDITY) <= 0;
            if (!recentlyTested) {
                throw AisExceptions.integrationNotTested();
            }
            CredentialCipher.Encrypted encrypted = credentialCipher.encrypt(request.apiKey());
            integration.setCredentialCipher(encrypted.ciphertext());
            integration.setCredentialIv(encrypted.iv());
            integration.setConnected(true);
        }

        IntegrationResponse response = toResponse(repository.save(integration));
        auditService.success(actorEmail, ip, "SAVE integration=" + code, RESOURCE_INTEGRATIONS);
        return response;
    }

    private IntegrationResponse toResponse(ExternalIntegration i) {
        return new IntegrationResponse(i.getCode(), i.getLabel(), i.getEndpoint(), i.getEndpointPlaceholder(),
                i.getPurpose(), i.isConnected(), i.getStatusNote(), i.hasCredential() ? "••••••••" : null,
                i.getLastTestedAt(), i.isLastTestPassed());
    }
}
