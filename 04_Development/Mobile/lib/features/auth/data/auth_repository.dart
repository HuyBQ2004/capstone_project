import 'dart:async';

import 'package:google_sign_in/google_sign_in.dart';

import '../../../core/constants/google_config.dart';
import '../../../core/network/api_client.dart';
import '../../../core/network/api_exception.dart';
import '../../../core/services/push_notification_service.dart';
import '../../../core/storage/token_storage.dart';
import 'auth_models.dart';

/// Đối chiếu `04_Development/Frontend/src/lib/stores/auth-store.ts` — cùng 2
/// bước "login → GET /profile/me" (LoginResponse không có tên/email hiển thị),
/// khác ở chỗ refresh token nằm trong Keystore/Keychain ([TokenStorage]) thay
/// vì cookie httpOnly.
class AuthRepository {
  AuthRepository._();
  static final AuthRepository instance = AuthRepository._();

  final _api = ApiClient.instance;
  final _googleSignIn = GoogleSignIn(
    scopes: const ['email'],
    serverClientId: kGoogleServerClientId,
  );

  Future<Profile> loginWithPassword(String email, String password) async {
    final json = await _api.post('/auth/login', body: {
      'email': email,
      'password': password,
    });
    final result = LoginResult.fromJson(json!);
    return _completeSession(result);
  }

  /// UC-191 — vế mobile: `google_sign_in` trả id_token trực tiếp từ Google SDK,
  /// gửi lên `POST /api/v1/auth/google` (backend tự verify chữ ký, xem
  /// `GoogleIdTokenVerifier`). Trả `null` khi người dùng tự hủy picker tài
  /// khoản — KHÔNG phải lỗi.
  Future<Profile?> loginWithGoogle() async {
    final account = await _googleSignIn.signIn();
    if (account == null) return null;

    final auth = await account.authentication;
    final idToken = auth.idToken;
    if (idToken == null) {
      throw ApiException(message: 'Không lấy được id_token từ Google.');
    }

    final json = await _api.post('/auth/google', body: {'idToken': idToken});
    final result = LoginResult.fromJson(json!);
    return _completeSession(result);
  }

  /// UC-032 — luôn trả 200 dù email có tồn tại hay không (TDS §8.1): server
  /// không phân biệt để tránh lộ email nào có tài khoản. Màn gọi hàm này chỉ
  /// nên hiện một thông báo trung tính, không tự suy luận thành/bại từ đây.
  Future<void> forgotPassword(String email) => _api.post('/auth/forgot-password', body: {'email': email});

  /// UC-032 — `token` lấy từ link trong email (BR-003: hiệu lực 1 giờ, dùng 1
  /// lần). Bản mobile chưa có deep link mở thẳng app từ email nên người dùng
  /// dán token thủ công — xem [ForgotPasswordScreen]/[ResetPasswordScreen].
  Future<void> resetPassword(String token, String newPassword) =>
      _api.post('/auth/reset-password', body: {'token': token, 'newPassword': newPassword});

  Future<PublicPasswordPolicy> fetchPublicPasswordPolicy() async {
    final json = await _api.get('/public/password-policy');
    return PublicPasswordPolicy.fromJson(json!);
  }

  Future<Profile> _completeSession(LoginResult result) async {
    _api.setAccessToken(result.accessToken);
    await TokenStorage.instance.saveRefreshToken(result.refreshToken);

    final json = await _api.get('/profile/me');
    final profile = Profile.fromJson(json!);
    // Chỉ có access token TỪ ĐÂY — đăng ký device token push ngay sau khi đăng nhập thành
    // công, không phải lúc app khởi động (`main()` gọi `PushNotificationService.init()`
    // trước khi biết có phiên hay không, gọi lúc đó luôn 401 vì chưa có access token).
    unawaited(PushNotificationService.instance.registerTokenIfAvailable());
    return profile;
  }

  /// Khôi phục phiên khi mở lại app — đổi refresh token đã lưu lấy access
  /// token mới rồi nạp lại profile. Trả `null` nếu chưa từng đăng nhập hoặc
  /// refresh token đã chết.
  Future<Profile?> restoreSession() async {
    final refreshToken = await TokenStorage.instance.readRefreshToken();
    if (refreshToken == null) return null;

    try {
      final json = await _api.post('/auth/refresh', body: {'refreshToken': refreshToken});
      final pair = json!;
      _api.setAccessToken(pair['accessToken'] as String);
      await TokenStorage.instance.saveRefreshToken(pair['refreshToken'] as String);

      final profileJson = await _api.get('/profile/me');
      final profile = Profile.fromJson(profileJson!);
      unawaited(PushNotificationService.instance.registerTokenIfAvailable());
      return profile;
    } on ApiException catch (e) {
      /*
       * 13/08/2026 (theo Nam) — trước đây MỌI lỗi ở đây (kể cả mất mạng thoáng qua lúc mở
       * app) đều xoá refresh token, đăng xuất người dùng dù token vẫn còn hạn. Phân biệt:
       * `ApiException.network()` có `status == null` (không có phản hồi HTTP nào, do lỗi
       * kết nối) — GIỮ token, chỉ coi lần khôi phục này chưa thành công; lỗi có `status`
       * thật (401 — refresh token hết hạn/bị revoke) mới thật sự xoá phiên.
       */
      if (e.status == null) return null;
      await TokenStorage.instance.clear();
      _api.setAccessToken(null);
      return null;
    } catch (_) {
      await TokenStorage.instance.clear();
      _api.setAccessToken(null);
      return null;
    }
  }

  Future<void> logout() async {
    try {
      await _api.post('/auth/logout');
    } catch (_) {
      // Token đã hết hạn thì server cũng đã revoke — vẫn dọn phía client.
    }
    try {
      await _googleSignIn.signOut();
    } catch (_) {
      // Không đăng nhập qua Google thì signOut() no-op/lỗi im lặng — bỏ qua.
    }
    await TokenStorage.instance.clear();
    _api.setAccessToken(null);
  }
}
