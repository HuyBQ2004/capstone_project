package com.sfms.iam.repository;

import com.sfms.iam.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** TDS §8.2. */
public interface UserAccountRepository
        extends JpaRepository<UserAccount, UUID>, JpaSpecificationExecutor<UserAccount> {

    /** Login tra không phân biệt hoa/thường (index {@code ix_user_email_lower}). */
    @Query("select u from UserAccount u where lower(u.email) = lower(:email)")
    Optional<UserAccount> findByEmail(@Param("email") String email);

    /** BR-218 — tra theo định danh Google. */
    Optional<UserAccount> findByGoogleSub(String googleSub);

    /** D1 — path param của {@code /users/{code}}. */
    Optional<UserAccount> findByCode(String code);

    /** D1 — path param của {@code /staff/{code}} (không gian mã riêng, xem V1). */
    Optional<UserAccount> findByStaffCode(String staffCode);

    boolean existsByStaffCode(String staffCode);

    /** BR-012. */
    @Query("select count(u) > 0 from UserAccount u where lower(u.email) = lower(:email)")
    boolean existsByEmail(@Param("email") String email);

    boolean existsByCode(String code);

    /**
     * BR-008 — đếm <b>mọi</b> user gán role, <b>kể cả đã vô hiệu hóa</b>.
     * KHÔNG thêm {@code where status = 'ACTIVE'}: MF01-TC-012 nhánh (2) chứng minh
     * điều đó bằng cách deactivate cả 3 user rồi vẫn phải nhận 409 với {@code [Number] = 3}.
     */
    long countByRoleId(UUID roleId);

    /** MF04 cascade {@code PartnerSuspended} — TDS §7.2. */
    List<UserAccount> findAllByPartnerId(UUID partnerId);

    /** MF02 cascade {@code SchoolDeactivated} (BR-021) — TDS §7.2. */
    List<UserAccount> findAllBySchoolId(UUID schoolId);

    /**
     * M12-D09 (TDS-MF12 §8.1) — {@code GeminiAiAssistAdapter} cần một actor để archive
     * {@code ai_requests} khi gọi từ {@code com.sfms.mf05.port.AiAssistPort.suggest()}, vốn
     * KHÔNG mang tham số actor (interface do MF05 khai, §5 "không refactor"). Fallback: tài
     * khoản System Admin ACTIVE đầu tiên.
     */
    Optional<UserAccount> findFirstByRole_RoleKeyAndStatus(String roleKey, com.sfms.iam.domain.UserStatus status);
}
