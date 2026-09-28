import 'package:flutter_secure_storage/flutter_secure_storage.dart';

/// Giữ refresh token trong Keystore/Keychain (Android EncryptedSharedPreferences,
/// iOS Keychain) — tương đương vai trò cookie httpOnly bên bản web
/// (`04_Development/Frontend/src/lib/api/session-cookie.ts`): app khác trên máy
/// và JS không đọc được, chỉ mất khi người dùng gỡ app hoặc logout.
class TokenStorage {
  TokenStorage._();
  static final TokenStorage instance = TokenStorage._();

  static const _refreshTokenKey = 'sfms_refresh_token';
  final _storage = const FlutterSecureStorage();

  Future<void> saveRefreshToken(String token) =>
      _storage.write(key: _refreshTokenKey, value: token);

  Future<String?> readRefreshToken() => _storage.read(key: _refreshTokenKey);

  Future<void> clear() => _storage.delete(key: _refreshTokenKey);
}
