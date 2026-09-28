package com.sfms.iam.dto;

import com.sfms.common.enums.RoleKey;
import com.sfms.iam.domain.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;

/** DTO của {@code /users}, {@code /staff}, {@code /profile} — khớp JSON TDS §9.2. */
public final class UserDtos {

    private UserDtos() {
    }

    /**
     * BR-012 — SĐT Việt Nam: {@code 0xxxxxxxxx} (10 số) hoặc {@code +84xxxxxxxxx}.
     * MF01-TC-INT-009 bước 6 assert {@code "12345"} và {@code "+1650123456"} đều bị từ chối.
     */
    public static final String VN_PHONE_REGEX = "^(0\\d{9}|\\+84\\d{9})$";

    // ───────────────────────── POST /users (UC-026) ─────────────────────────

    /**
     * ⚠️ KHÔNG nhận {@code status} (review B1 — server quyết định
     * {@code PENDING_VERIFICATION}) và KHÔNG nhận {@code password} (invariant TDS §6.3:
     * chỉ UC-154 mới đặt mật khẩu).
     *
     * <p>{@code id} là <b>tùy chọn</b> theo D5: có thì kiểm trùng → 409, vắng thì server sinh.
     */
    @Schema(name = "CreateUserRequest")
    public record CreateUserRequest(
            @Schema(description = "D5 — mã client tự sinh; trùng ⇒ 409, vắng ⇒ server sinh",
                    example = "U-0011")
            String id,

            @NotBlank @Size(max = 150)
            @Schema(example = "Trần Thị B") String name,

            @NotBlank @Email @Size(max = 180)
            @Schema(example = "b.tran@dtd.edu.vn") String email,

            @Pattern(regexp = VN_PHONE_REGEX, message = "phone must match +84 or 0xx format (BR-012)")
            @Schema(example = "0912345678") String phone,

            @NotNull
            @Schema(description = "D6 — RoleKey kebab-case; nhãn PascalCase bị từ chối 422")
            RoleKey role,

            @Schema(description = "Mã nghiệp vụ của trường (D1)", example = "SCH-HN-0042")
            String schoolId,

            @Schema(description = "D4 — ngày sinh ISO-8601 (yyyy-MM-dd)", example = "2000-07-30")
            LocalDate birthDate
    ) {}

    /**
     * TDS §9.2 (sửa 2026-07-29 review B2): trả <b>cả</b> {@code schoolId} (khóa) và
     * {@code school} (nhãn). Chỉ trả nhãn buộc FE gửi ngược lại <b>tên trường</b> làm khóa —
     * đúng lỗi mà {@code db.ts findIsolatedNameFields} cảnh báo.
     */
    @Schema(name = "UserResponse")
    public record UserResponse(
            @Schema(description = "D1 — mã nghiệp vụ, KHÔNG phải UUID", example = "U-0011")
            String id,

            @Schema(example = "Trần Thị B") String name,
            @Schema(example = "b.tran@dtd.edu.vn") String email,
            @Schema(example = "0912345678") String phone,

            @Schema(description = "D6 — kebab-case") RoleKey role,

            @Schema(description = "Khóa để FE nối (review B2)", example = "SCH-HN-0042")
            String schoolId,

            @Schema(description = "Nhãn hiển thị (review B2)", example = "TH Đoàn Thị Điểm")
            String school,

            @Schema(example = "SUP-001") String partnerId,

            @Schema(description = "Server quyết định khi tạo = PENDING_VERIFICATION (review B1)")
            UserStatus status,

            @Schema(type = "string", format = "date-time", example = "2026-07-02T00:45:00Z")
            Instant lastLogin
    ) {}

    // ───────────────────────── POST /staff (UC-024) ─────────────────────────

    /**
     * ⚠️ KHÔNG có {@code schoolId}. Trường lấy <b>từ JWT</b> của School Admin (TDS §17 C2,
     * BR-013). MF01-TC-INT-010 bước 2 và MF01-TC-SEC-006 bước 5: gửi kèm
     * {@code "schoolId":"SCH-HN-0051"} không được tạo nhân viên ở trường khác.
     */
    @Schema(name = "CreateStaffRequest")
    public record CreateStaffRequest(
            @Schema(description = "D5 — mã client tự sinh", example = "STF-012")
            String id,

            @NotBlank @Size(max = 150) String name,
            @NotBlank @Email @Size(max = 180) String email,

            @Pattern(regexp = VN_PHONE_REGEX, message = "phone must match +84 or 0xx format (BR-012)")
            String phone,

            @NotNull
            @Schema(description = "UC-024 bước 4 — chỉ role cấp trường: teacher, medical-staff")
            RoleKey role
    ) {}

    /**
     * {@code POST /staff/bulk} (11/08/2026) — nhập hàng loạt từ Excel/CSV. FE tự parse +
     * validate file, gửi cả lô hàng hợp lệ trong MỘT request thay vì N request tuần tự
     * (mỗi hàng 1 {@code POST /staff}) như trước.
     */
    @Schema(name = "BulkCreateStaffRequest")
    public record BulkCreateStaffRequest(
            @NotNull @jakarta.validation.Valid java.util.List<CreateStaffRequest> rows
    ) {}

    @Schema(name = "BulkRowError")
    public record BulkRowErrorResponse(int rowIndex, String name, String code, String message) {}

    @Schema(name = "BulkCreateStaffResult")
    public record BulkCreateStaffResponse(int created, int failed, java.util.List<BulkRowErrorResponse> errors) {}

    // ───────────────────────── PATCH /{users|staff}/{code}/status (UC-002/003) ─────────

    @Schema(name = "ToggleStatusRequest")
    public record ToggleStatusRequest(
            @NotNull
            @Schema(description = "true ⇒ ACTIVE, false ⇒ INACTIVE + revoke refresh tức thì (BR-004)")
            Boolean active
    ) {}

    // ───────────────────────── GET/PATCH /profile/me (UC-059/097/138) ────────────────

    /**
     * ⚠️ KHÔNG có {@code userId}/{@code role}/{@code schoolId}. Chủ thể là chính người
     * cầm JWT; nhận {@code userId} từ body cho phép sửa hồ sơ người khác
     * (MF01-TC-SEC-005 mục a).
     */
    @Schema(name = "UpdateProfileRequest")
    public record UpdateProfileRequest(
            @Size(max = 150) String name,

            @Pattern(regexp = VN_PHONE_REGEX, message = "phone must match +84 or 0xx format (BR-012)")
            String phone
    ) {}

    @Schema(name = "ProfileResponse")
    public record ProfileResponse(
            @Schema(example = "U-0001") String id,
            String name,
            String email,
            String phone,
            RoleKey role,
            @Schema(example = "SCH-HN-0042") String schoolId,
            String school,
            @Schema(example = "SUP-001") String partnerId,

            /*
             * Thêm 08/08/2026. `schoolId` nghĩa là "tài khoản THUỘC trường nào" và nó NULL với
             * mọi tài khoản đối tác (đăng ký qua hợp tác ⇒ chỉ có `partnerId`). Nhưng FE còn
             * cần một thứ khác: "cấu hình của trường NÀO áp cho tôi" — với Đơn vị suất ăn thì
             * đó là trường trong hợp tác đang hiệu lực.
             *
             * Vì sao phải có: `useSchoolConfig()` ở FE đọc `schoolId` ⇒ với tài khoản Đơn vị
             * suất ăn nó luôn `undefined` ⇒ `menuLeadTimeDays` không bao giờ về ⇒ màn Lịch
             * thực đơn khoá TOÀN BỘ ô (FE cố ý khoá khi chưa biết lead time, thay vì đoán 3)
             * ⇒ **đúng vai có nhiệm vụ lập thực đơn thì không lập được**. Cùng lớp lỗi với
             * `Mf06Scope`/`Mf11Scope` đã sửa ở backend cùng ngày.
             *
             * KHÔNG ghi đè `schoolId` để giữ nguyên nghĩa của nó (và để màn quản trị vẫn phân
             * biệt được tài khoản trường với tài khoản đối tác).
             */
            @Schema(example = "SCH-HN-0042",
                    description = "Trường mà cấu hình vận hành áp cho tài khoản này. Bằng "
                            + "`schoolId` với tài khoản của trường; với Đơn vị suất ăn là trường "
                            + "trong hợp tác đang hiệu lực (quan hệ 1-1, BR-EXCLUSIVE-CATERER). "
                            + "NULL với Nhà cung cấp — NCC phục vụ nhiều trường nên không có "
                            + "'trường của tôi' duy nhất.")
            String activeSchoolId,

            @Schema(description = "Nhãn hiển thị của `activeSchoolId` — đi cặp với nó, cùng lý do "
                    + "tồn tại: tài khoản đối tác không có `school` vì không có `schoolId`.")
            String activeSchool,

            UserStatus status,
            String avatarUrl,

            @Schema(type = "string", format = "date-time", example = "2026-07-02T00:45:00Z")
            Instant lastLogin,

            @Schema(type = "string", format = "date-time", example = "2026-01-10T02:00:00Z")
            Instant termsAcceptedAt
    ) {}
}
