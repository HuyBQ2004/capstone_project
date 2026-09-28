package com.sfms.iam.repository;

import com.sfms.iam.domain.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/** TDS §8.2. */
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, UUID> {

    /**
     * BR-006 — cửa sổ <b>đúng 3</b> bản ghi gần nhất.
     * MF01-TC-003 nhánh (c) chứng minh không phải "toàn bộ lịch sử": thêm bản ghi thứ 4
     * thì mật khẩu cũ nhất phải dùng lại được.
     */
    List<PasswordHistory> findTop3ByUserIdOrderByCreatedAtDesc(UUID userId);

    List<PasswordHistory> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /** Giữ tối đa 3 bản ghi (TDS §5.1 {@code "0..3 PasswordHistory"}). */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from PasswordHistory p where p.userId = :uid and p.id not in :keepIds")
    int deleteOlderThan(@Param("uid") UUID userId, @Param("keepIds") List<UUID> keepIds);

    long countByUserId(UUID userId);
}
