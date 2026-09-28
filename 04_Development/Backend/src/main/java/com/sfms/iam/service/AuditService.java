package com.sfms.iam.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.iam.domain.AuditLog;
import com.sfms.iam.repository.AuditLogRepository;
import com.sfms.iam.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Ghi audit log — UC-106, BR-002 (<i>"Sự kiện khóa tài khoản phải được ghi vào audit log"</i>),
 * BR-170/171 (downstream MF12).
 *
 * <p>{@code REQUIRES_NEW}: dấu vết kiểm toán phải tồn tại <b>kể cả khi nghiệp vụ rollback</b>.
 * Một lần login bị chặn là sự kiện an ninh; nó không được biến mất chỉ vì transaction chính
 * ném exception.
 *
 * <p>Ghi {@code actor} + {@code ip} + {@code action}, <b>không bao giờ</b> ghi giá trị
 * credential (MF01-TC-SEC-012).
 *
 * <p><b>M12-D01/M12-D08 (TDS-MF12 §8.1)</b> — MF12 mở rộng lớp này (chỉ THÊM, không đổi chữ
 * ký {@link #success} / {@link #blocked}) để: (1) nối chuỗi {@code prev_hash → entry_hash}
 * (ADR-M12-02, BR-172), (2) điền {@code actor_user_id} khi resolve được từ email, (3) cấp
 * {@code code} (D1). MF12 dùng lại {@link #verifyChain()} cho {@code GET /audit-logs/integrity},
 * không mở một service hash riêng.
 */
@Service
public class AuditService {

    // Nhóm action dùng chung — dùng hằng để test grep được, không rải chuỗi rời rạc.
    public static final String LOGIN = "LOGIN";
    public static final String LOGIN_GOOGLE = "LOGIN_GOOGLE";
    public static final String LOGOUT = "LOGOUT";
    public static final String GOOGLE_LINK = "GOOGLE_LINK";
    public static final String CREATE_USER = "CREATE_USER";
    public static final String CREATE_STAFF = "CREATE_STAFF";
    public static final String TOGGLE_STATUS = "TOGGLE_STATUS";
    public static final String CHANGE_PASSWORD = "CHANGE_PASSWORD";
    public static final String RESET_PASSWORD = "RESET_PASSWORD";
    public static final String ACTIVATE_ACCOUNT = "ACTIVATE_ACCOUNT";
    public static final String CREATE_ROLE = "CREATE_ROLE";
    public static final String UPDATE_ROLE = "UPDATE_ROLE";
    public static final String DELETE_ROLE = "DELETE_ROLE";
    public static final String CASCADE_SUSPEND = "CASCADE_SUSPEND";

    public static final String RESOURCE_AUTH = "Authentication";
    public static final String RESOURCE_USERS = "users";
    public static final String RESOURCE_STAFF = "staff";
    public static final String RESOURCE_ROLES = "roles";

    private final AuditLogRepository repository;
    private final UserAccountRepository userAccountRepository;
    private final CodeGenerator codeGenerator;
    private final Clock clock;

    public AuditService(AuditLogRepository repository, UserAccountRepository userAccountRepository,
                         CodeGenerator codeGenerator, Clock clock) {
        this.repository = repository;
        this.userAccountRepository = userAccountRepository;
        this.codeGenerator = codeGenerator;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void success(String actor, String ip, String action, String resource) {
        write(actor, ip, action, resource, AuditLog.RESULT_SUCCESS);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void blocked(String actor, String ip, String action, String resource) {
        write(actor, ip, action, resource, AuditLog.RESULT_BLOCKED);
    }

    private void write(String actor, String ip, String action, String resource, String result) {
        Instant when = clock.instant();
        String safeActor = safeActor(actor);

        AuditLog log = AuditLog.of(when, safeActor, ip, action, resource, result);
        log.setCode(codeGenerator.next("audit_logs", "AUD", 5));
        userAccountRepository.findByEmail(safeActor).ifPresent(u -> log.setActorUserId(u.getId()));

        String prevHash = repository.findFirstByOrderByTimestampDescCodeDesc()
                .map(AuditLog::getEntryHash)
                .orElse(null);
        log.setPrevHash(prevHash);
        log.setEntryHash(chainHash(prevHash, when, safeActor, ip, action, resource, result));

        repository.save(log);
    }

    /**
     * ADR-M12-02 (bản sửa 2026-08-04) — verify toàn bộ chuỗi từ gốc. BR-172 đòi phát hiện
     * <b>cả</b> sửa nội dung thẳng bằng SQL (self-consistency: {@code entry_hash} không khớp
     * nội dung hiện tại) <b>lẫn</b> xoá một dòng giữa chuỗi (linkage: {@code prev_hash} của
     * dòng kế không khớp {@code entry_hash} thật của dòng liền trước trong chuỗi còn lại).
     *
     * <p>Không lọc theo khoảng ngày: một chuỗi hash chỉ verify được toàn vẹn từ gốc, cắt theo
     * range sẽ cho báo động giả ở biên (dòng đầu của range luôn "thiếu" tiền nhiệm).
     */
    @Transactional(readOnly = true)
    public IntegrityCheckResult verifyChain() {
        List<AuditLog> chain = repository.findAllByOrderByTimestampAscCodeAsc();
        String expectedPrevLink = null;
        for (AuditLog log : chain) {
            // Dòng sinh trước khi có ADR-M12-02 (V1/V2, entry_hash NULL — cột nullable đúng vì
            // lý do này) không tham gia chuỗi hash; chỉ reset mốc, không kiểm nội dung.
            if (log.getEntryHash() == null) {
                expectedPrevLink = null;
                continue;
            }
            String selfExpected = chainHash(log.getPrevHash(), log.getTimestamp(), log.getActor(),
                    log.getIp(), log.getAction(), log.getResource(), log.getResult());
            if (!selfExpected.equals(log.getEntryHash())) {
                return new IntegrityCheckResult(false, log.getCode());
            }
            if (!Objects.equals(log.getPrevHash(), expectedPrevLink)) {
                return new IntegrityCheckResult(false, log.getCode());
            }
            expectedPrevLink = log.getEntryHash();
        }
        return new IntegrityCheckResult(true, null);
    }

    private static String chainHash(String prevHash, Instant timestamp, String actor, String ip,
                                    String action, String resource, String result) {
        String input = (prevHash == null ? "" : prevHash) + "|" + timestamp + "|" + actor + "|"
                + (ip == null ? "" : ip) + "|" + action + "|" + resource + "|" + result;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] raw = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(raw.length * 2);
            for (byte b : raw) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Thao tác chưa xác thực (login sai email lạ) vẫn phải có dòng audit. */
    private String safeActor(String actor) {
        return (actor == null || actor.isBlank()) ? "anonymous" : actor;
    }

    /** @param brokenAt {@code code} của dòng đầu tiên gãy chuỗi, {@code null} nếu {@code valid}. */
    public record IntegrityCheckResult(boolean valid, String brokenAt) {}
}
