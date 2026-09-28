import 'dart:io' show Platform;

import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';

import '../../features/notifications/data/notification_repository.dart';

/// Handler cho push đến khi app ở background/đã kill — PHẢI là top-level function
/// (không phải method trong class), Firebase gọi nó trong 1 isolate riêng.
@pragma('vm:entry-point')
Future<void> firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  debugPrint('Push (background) nhận: ${message.messageId}');
}

/// 13/08/2026 — TODO cũ ("chưa có endpoint backend") đã lỗi thời: backend đã có sẵn
/// `POST /device-tokens` (MF10, `DeviceTokenController`) từ trước, chỉ là Mobile chưa
/// từng quay lại nối. Token lấy được nay đăng ký thật lên server ngay dưới đây.
class PushNotificationService {
  PushNotificationService._();
  static final PushNotificationService instance = PushNotificationService._();

  final FirebaseMessaging _messaging = FirebaseMessaging.instance;
  final FlutterLocalNotificationsPlugin _localNotifications =
      FlutterLocalNotificationsPlugin();

  Future<void> init() async {
    FirebaseMessaging.onBackgroundMessage(firebaseMessagingBackgroundHandler);

    await _localNotifications.initialize(
      const InitializationSettings(
        android: AndroidInitializationSettings('@mipmap/ic_launcher'),
        iOS: DarwinInitializationSettings(),
      ),
    );

    final settings = await _messaging.requestPermission(
      alert: true,
      badge: true,
      sound: true,
    );
    if (settings.authorizationStatus != AuthorizationStatus.authorized &&
        settings.authorizationStatus != AuthorizationStatus.provisional) {
      debugPrint('Push notification: user chưa cho phép quyền.');
      return;
    }

    final token = await _messaging.getToken();
    debugPrint('FCM token: $token');
    if (token != null) await _registerToken(token);

    // Token đổi khi app reinstall/xoá data — Firebase tự phát lại token mới.
    _messaging.onTokenRefresh.listen((newToken) {
      debugPrint('FCM token refreshed: $newToken');
      _registerToken(newToken);
    });

    // App đang mở (foreground) — FCM không tự hiện notification trên Android, phải tự show.
    FirebaseMessaging.onMessage.listen((message) {
      final notification = message.notification;
      if (notification == null) return;
      _localNotifications.show(
        message.hashCode,
        notification.title,
        notification.body,
        const NotificationDetails(
          android: AndroidNotificationDetails(
            'sfms_default',
            'Thông báo SFMS',
            importance: Importance.high,
            priority: Priority.high,
          ),
        ),
      );
    });
  }

  /// Gọi từ `AuthRepository` NGAY SAU khi đăng nhập/khôi phục phiên thành công — lúc
  /// `init()` chạy trong `main()` thì chưa có access token nên `_registerToken()` ở đó
  /// luôn 401 (đã bắt lỗi im lặng), phải đăng ký lại đúng lúc đã có phiên hợp lệ.
  Future<void> registerTokenIfAvailable() async {
    final token = await _messaging.getToken();
    if (token != null) await _registerToken(token);
  }

  /// Upsert theo token phía backend — gọi lỗi (mất mạng, 401 lúc chưa đăng nhập xong) không
  /// nên chặn luồng khởi động app, chỉ log lại để biết push có thể chưa nhận được.
  Future<void> _registerToken(String token) async {
    try {
      await NotificationRepository.instance.registerDeviceToken(
        token,
        platform: Platform.isIOS ? 'IOS' : 'ANDROID',
      );
    } catch (e) {
      debugPrint('Không đăng ký được device token FCM: $e');
    }
  }
}
