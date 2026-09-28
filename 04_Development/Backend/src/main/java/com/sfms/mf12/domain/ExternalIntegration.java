package com.sfms.mf12.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * UC-040 — tích hợp dịch vụ ngoài (BR-163/BR-164). {@code credentialCipher}/{@code credentialIv}
 * KHÔNG BAO GIỜ lộ ra JSON ({@code @JsonIgnore}) — API chỉ trả {@code credentialMasked} tính
 * trong DTO. {@code M12-D05}: {@code updatedBy} người xem UC-040 chính là cột reserved của
 * {@link BaseEntity}, entity này KHÔNG khai lại.
 */
@Entity
@Table(name = "external_integrations")
public class ExternalIntegration extends BaseEntity {

    @Column(name = "code", length = 32, nullable = false)
    private String code;

    @Column(nullable = false, length = 120)
    private String label;

    @Column(nullable = false, length = 300)
    private String endpoint;

    @Column(name = "endpoint_placeholder", length = 300)
    private String endpointPlaceholder;

    @Column(nullable = false, length = 300)
    private String purpose;

    @Column(nullable = false)
    private boolean connected;

    @Column(name = "status_note", length = 300)
    private String statusNote;

    @JsonIgnore
    @Column(name = "credential_cipher")
    private byte[] credentialCipher;

    @JsonIgnore
    @Column(name = "credential_iv")
    private byte[] credentialIv;

    @Column(name = "key_version", nullable = false)
    private int keyVersion = 1;

    @Column(name = "last_tested_at")
    private Instant lastTestedAt;

    @Column(name = "last_test_passed", nullable = false)
    private boolean lastTestPassed;

    @JsonProperty("id")
    public String getPublicId() {
        return code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getEndpointPlaceholder() {
        return endpointPlaceholder;
    }

    public void setEndpointPlaceholder(String endpointPlaceholder) {
        this.endpointPlaceholder = endpointPlaceholder;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getStatusNote() {
        return statusNote;
    }

    public void setStatusNote(String statusNote) {
        this.statusNote = statusNote;
    }

    public byte[] getCredentialCipher() {
        return credentialCipher;
    }

    public void setCredentialCipher(byte[] credentialCipher) {
        this.credentialCipher = credentialCipher;
    }

    public byte[] getCredentialIv() {
        return credentialIv;
    }

    public void setCredentialIv(byte[] credentialIv) {
        this.credentialIv = credentialIv;
    }

    public int getKeyVersion() {
        return keyVersion;
    }

    public void setKeyVersion(int keyVersion) {
        this.keyVersion = keyVersion;
    }

    public Instant getLastTestedAt() {
        return lastTestedAt;
    }

    public void setLastTestedAt(Instant lastTestedAt) {
        this.lastTestedAt = lastTestedAt;
    }

    public boolean isLastTestPassed() {
        return lastTestPassed;
    }

    public void setLastTestPassed(boolean lastTestPassed) {
        this.lastTestPassed = lastTestPassed;
    }

    /** BR-164 — chỉ trả dạng che, KHÔNG BAO GIỜ trả credential thật ra API. */
    public boolean hasCredential() {
        return credentialCipher != null && credentialCipher.length > 0;
    }
}
