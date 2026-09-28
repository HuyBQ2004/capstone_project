import '../../../core/network/api_client.dart';
import 'notification_models.dart';

/// UC-058/UC-136 (MF10) — chuông thông báo. Bản web chưa dựng UI cho nhóm này
/// (chỉ có endpoint), nên không có trang để lấy khuôn — dựng thẳng theo DTO
/// backend (`NotificationDtos`).
class NotificationRepository {
  NotificationRepository._();
  static final NotificationRepository instance = NotificationRepository._();

  final _api = ApiClient.instance;

  Future<NotificationPage> fetchPage({int page = 1, int pageSize = 20}) async {
    final json = await _api.get('/notifications', query: {
      'page': '$page',
      'pageSize': '$pageSize',
    });
    return NotificationPage.fromJson(json!);
  }

  /// Idempotent phía backend — gọi lại nhiều lần không lỗi.
  Future<void> markRead(String id) => _api.patch('/notifications/$id/read');

  /// `POST /device-tokens` (V34, MF10) — đăng ký token FCM cho thiết bị hiện tại. Upsert
  /// theo token, gọi lại mỗi lần app khởi động hoặc token refresh không lỗi.
  Future<void> registerDeviceToken(String token, {required String platform}) =>
      _api.post('/device-tokens', body: {'token': token, 'platform': platform});

  /// `DELETE /device-tokens/{token}` — gỡ token lúc đăng xuất khỏi thiết bị này.
  Future<void> unregisterDeviceToken(String token) => _api.delete('/device-tokens/$token');
}
