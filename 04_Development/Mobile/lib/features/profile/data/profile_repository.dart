import '../../../core/network/api_client.dart';
import '../../auth/data/auth_models.dart';

/// UC-097/UC-011 — đối chiếu `components/app/user-menu.tsx` (bản web) nhưng chỉ
/// vế profile + password: "Cài đặt thông báo" bên web KHÔNG gọi API thật (chỉ
/// `toast` giả), nên KHÔNG port sang mobile — theo đúng nguyên tắc "bám API
/// thật" đã áp dụng cho màn Quên mật khẩu.
class ProfileRepository {
  ProfileRepository._();
  static final ProfileRepository instance = ProfileRepository._();

  final _api = ApiClient.instance;

  Future<Profile> fetchMine() async {
    final json = await _api.get('/profile/me');
    return Profile.fromJson(json!);
  }

  /// BR-012 — chỉ `name`/`phone`. `role`/`schoolId`/`status` không nằm trong
  /// DTO backend nên không có đường tự nâng quyền qua màn này.
  Future<Profile> updateProfile({required String name, String? phone}) async {
    final json = await _api.patch('/profile/me', body: {
      'name': name,
      if (phone != null && phone.isNotEmpty) 'phone': phone,
    });
    return Profile.fromJson(json!);
  }

  /// BR-013 — backend kiểm định dạng theo magic bytes, tối đa 10MB.
  Future<Profile> uploadAvatar(String filePath) async {
    final json = await _api.uploadFile('/profile/me/photo', fieldName: 'file', filePath: filePath);
    return Profile.fromJson(json!);
  }

  /// UC-011 — chủ thể lấy từ JWT, không nhận `userId` (chặn đổi mật khẩu hộ
  /// người khác). BR-006: chính sách + không trùng 3 mật khẩu gần nhất, backend
  /// tự thi hành.
  Future<void> changePassword({required String currentPassword, required String newPassword}) =>
      _api.patch('/account/password', body: {
        'currentPassword': currentPassword,
        'newPassword': newPassword,
      });
}
