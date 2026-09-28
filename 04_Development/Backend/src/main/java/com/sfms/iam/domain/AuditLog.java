package com.sfms.iam.domain;

import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.Instant;
import java.util.UUID;

/**
 * Nhật ký kiểm toán — UC-106; BR-162 (downstream MF12): <b>append-only</b>,
 * không bao giờ UPDATE/DELETE.
 *
 * <p>Ghi {@code actor} + {@code ip} + {@code action}, KHÔNG bao giờ ghi giá trị credential
 * (MF01-TC-SEC-012).
 *
 * <p><b>M12-D01/ADR-M12-02</b> (TDS-MF12 §5.2/§8.1) — 3 cột {@code actor_user_id}/
 * {@code prev_hash}/{@code entry_hash} do MF12 thêm ở {@code V23} (ALTER, không CREATE lại
 * bảng — bảng thuộc MF01). {@code @Immutable}: Hibernate không sinh câu {@code UPDATE} cho
 * entity này — nạp lại rồi đổi field + {@code flush()} không ghi đè được bằng chứng
 * (MF12-TC-INT-003 bước 6).
 */
@Entity
@Table(name = "audit_logs")
@Immutable
public class AuditLog extends BaseEntity {

    public static final String RESULT_SUCCESS = "SUCCESS";
    public static final String RESULT_BLOCKED = "BLOCKED";

    @Column(name = "code", length = 32)
    private String code;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    /** Email của người thao tác, hoặc {@code "anonymous"} với thao tác chưa xác thực. */
    @Column(name = "actor", nullable = false, length = 180)
    private String actor;

    /** UC-106 — cột IP bắt buộc; MF01-TC-004 assert {@code ip} không null cho login BLOCKED. */
    @Column(name = "ip", length = 45)
    private String ip;

    @Column(name = "action", nullable = false, length = 255)
    private String action;

    @Column(name = "resource", nullable = false, length = 120)
    private String resource;

    /** {@code SUCCESS} | {@code BLOCKED}. */
    @Column(name = "result", nullable = false, length = 16)
    private String result;

    /** M12-D01 — C2: FK tới {@code user_accounts}, resolve được từ {@code actor} (email). Nullable. */
    @Column(name = "actor_user_id")
    private UUID actorUserId;

    /** ADR-M12-02 — hash của dòng liền trước trong chuỗi; {@code null} với dòng đầu tiên. */
    @Column(name = "prev_hash", length = 64)
    private String prevHash;

    /** ADR-M12-02 — SHA-256({@code prevHash|timestamp|actor|ip|action|resource|result}), hex. */
    @Column(name = "entry_hash", length = 64)
    private String entryHash;

    public static AuditLog of(Instant when, String actor, String ip,
                              String action, String resource, String result) {
        AuditLog log = new AuditLog();
        log.timestamp = when;
        log.actor = actor;
        log.ip = ip;
        log.action = action;
        log.resource = resource;
        log.result = result;
        return log;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public UUID getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(UUID actorUserId) {
        this.actorUserId = actorUserId;
    }

    public String getPrevHash() {
        return prevHash;
    }

    public void setPrevHash(String prevHash) {
        this.prevHash = prevHash;
    }

    public String getEntryHash() {
        return entryHash;
    }

    public void setEntryHash(String entryHash) {
        this.entryHash = entryHash;
    }
}
