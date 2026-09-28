import '../../../core/models/user_model.dart';

/// `POST /api/v1/auth/login` và `POST /api/v1/auth/google` cùng trả một shape
/// (`AuthDtos.LoginResponse`, backend) — đúng 6 khóa.
class LoginResult {
  final String accessToken;
  final String refreshToken;
  final UserRole role;
  final String? schoolId;
  final String? partnerId;
  final bool needTermsAccept;

  LoginResult({
    required this.accessToken,
    required this.refreshToken,
    required this.role,
    required this.schoolId,
    required this.partnerId,
    required this.needTermsAccept,
  });

  factory LoginResult.fromJson(Map<String, dynamic> json) => LoginResult(
        accessToken: json['accessToken'] as String,
        refreshToken: json['refreshToken'] as String,
        role: roleFromKey(json['role'] as String),
        schoolId: json['schoolId'] as String?,
        partnerId: json['partnerId'] as String?,
        needTermsAccept: json['needTermsAccept'] as bool? ?? false,
      );
}

/// `GET /api/v1/profile/me` (`UserDtos.ProfileResponse`, backend).
class Profile {
  final String id;
  final String name;
  final String email;
  final String? phone;
  final UserRole role;
  final String? schoolId;
  final String? school;
  final String? partnerId;
  final String status;
  final String? avatarUrl;
  final String? termsAcceptedAt;

  Profile({
    required this.id,
    required this.name,
    required this.email,
    required this.phone,
    required this.role,
    required this.schoolId,
    required this.school,
    required this.partnerId,
    required this.status,
    required this.avatarUrl,
    required this.termsAcceptedAt,
  });

  factory Profile.fromJson(Map<String, dynamic> json) => Profile(
        id: json['id'] as String,
        name: json['name'] as String,
        email: json['email'] as String,
        phone: json['phone'] as String?,
        role: roleFromKey(json['role'] as String),
        schoolId: json['schoolId'] as String?,
        school: json['school'] as String?,
        partnerId: json['partnerId'] as String?,
        status: json['status'] as String,
        avatarUrl: json['avatarUrl'] as String?,
        termsAcceptedAt: json['termsAcceptedAt'] as String?,
      );

  User toUser() => User(id: id, fullName: name, email: email, role: role);
}

/// `GET /api/v1/public/password-policy` (`SecurityPolicyDtos.PublicPasswordPolicyResponse`,
/// backend, MF12) — endpoint public dành riêng cho 2 màn (auth) đọc TRƯỚC khi có
/// JWT, KHÔNG lộ ngưỡng khóa tài khoản (không có `maxLoginAttempts`/`lockoutMinutes`).
class PublicPasswordPolicy {
  final int passwordMinLength;
  final bool requireSpecialChar;
  final bool requireNumber;
  final bool requireMixedCase;
  final bool noReuseLast3;

  const PublicPasswordPolicy({
    required this.passwordMinLength,
    required this.requireSpecialChar,
    required this.requireNumber,
    required this.requireMixedCase,
    required this.noReuseLast3,
  });

  factory PublicPasswordPolicy.fromJson(Map<String, dynamic> json) => PublicPasswordPolicy(
        passwordMinLength: json['passwordMinLength'] as int,
        requireSpecialChar: json['requireSpecialChar'] as bool,
        requireNumber: json['requireNumber'] as bool,
        requireMixedCase: json['requireMixedCase'] as bool,
        noReuseLast3: json['noReuseLast3'] as bool,
      );

  /// Fallback dùng khi `/public/password-policy` chưa gọi được (mất mạng lúc mở
  /// màn) — cùng giá trị mặc định mà `SecurityPolicy` seed phía backend dùng,
  /// chỉ để form không trống rule trong lúc chờ; server vẫn là nguồn thật kiểm
  /// tra ở `POST /auth/reset-password`.
  static const fallback = PublicPasswordPolicy(
    passwordMinLength: 8,
    requireSpecialChar: true,
    requireNumber: true,
    requireMixedCase: true,
    noReuseLast3: true,
  );
}

/// Đối chiếu `com.sfms.common.enums.RoleKey` (backend, D6 kebab-case) và
/// `04_Development/Frontend/src/lib/roles.ts` (`RoleKey`). `school-admin` /
/// `system-admin` / `supplier` KHÔNG có màn hình trên mobile (xem
/// `TASKS_MOBILE_SCOPE.md`) — đăng nhập vẫn cho qua, chỉ chặn ở màn hình sau
/// khi không tìm được dashboard tương ứng.
UserRole roleFromKey(String key) {
  switch (key) {
    case 'teacher':
      return UserRole.teacher;
    case 'parent':
      return UserRole.parent;
    case 'medical-staff':
      return UserRole.medicalStaff;
    case 'service-caterers':
      return UserRole.serviceCaterers;
    case 'supplier':
      return UserRole.rawFoodSupplier;
    // 13/08/2026 — school-admin/system-admin CŨNG ngoài phạm vi mobile (cùng
    // supplier), nhưng phải parse được thay vì throw: trước đây throw
    // ArgumentError ở đây bị `login_screen.dart` bắt nhầm vào nhánh catch
    // chung ("Không kết nối được máy chủ"), khiến 2 vai trò này KHÔNG bao giờ
    // thấy được thông báo "chưa hỗ trợ trên mobile" tử tế mà `rawFoodSupplier`
    // đã có — tưởng lỗi mạng dù mật khẩu đúng.
    case 'school-admin':
      return UserRole.schoolAdmin;
    case 'system-admin':
      return UserRole.systemAdmin;
    default:
      throw ArgumentError('Vai trò "$key" chưa được mobile hỗ trợ');
  }
}
