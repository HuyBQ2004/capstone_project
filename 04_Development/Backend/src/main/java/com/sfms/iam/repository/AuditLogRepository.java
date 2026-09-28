package com.sfms.iam.repository;

import com.sfms.iam.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * BR-162 (downstream MF12) — <b>append-only</b>. Chỉ dùng {@code save()} và các truy vấn đọc;
 * không bao giờ gọi {@code delete*()} hay update bản ghi đã ghi.
 */
public interface AuditLogRepository
        extends JpaRepository<AuditLog, UUID>, JpaSpecificationExecutor<AuditLog> {

    Optional<AuditLog> findByCode(String code);                          // M12-D01 — D1

    /**
     * ADR-M12-02 — nối chuỗi hash: dòng ghi gần nhất theo thời gian, {@code code} là tie-breaker
     * (không dùng {@code id} — {@code GenerationType.UUID} sinh UUID NGẪU NHIÊN, không đơn điệu
     * theo thời gian, nên không tie-break đúng khi 2 dòng trùng {@code timestamp} — VD test dùng
     * đồng hồ giả cố định. {@code code} do {@link com.sfms.common.code.CodeGenerator} cấp tăng
     * dần thật sự theo thứ tự ghi).
     */
    Optional<AuditLog> findFirstByOrderByTimestampDescCodeDesc();

    /** ADR-M12-02 — toàn bộ chuỗi theo thứ tự ghi (M12-D01, verifyChain). */
    List<AuditLog> findAllByOrderByTimestampAscCodeAsc();

    /** BR-174 — lọc theo khoảng ngày cho UC-106. */
    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(Instant from, Instant to);
}
