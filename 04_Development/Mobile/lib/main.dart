import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/material.dart';
import 'core/services/push_notification_service.dart';
import 'core/theme/app_theme.dart';
import 'features/auth/presentation/session_gate.dart';
import 'firebase_options.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp(options: DefaultFirebaseOptions.currentPlatform);
  await PushNotificationService.instance.init();
  runApp(const SfmsMobileApp());
}

class SfmsMobileApp extends StatelessWidget {
  const SfmsMobileApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'SFMS Mobile App',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.darkTheme,
      // Trước đây `home` là bottom-nav chuyển tay giữa cả 5 dashboard demo —
      // không phản ánh đúng: một tài khoản chỉ có đúng 1 role. Nay vào qua
      // đăng nhập thật (UC-034/UC-191), `SessionGate` tự chọn đúng dashboard
      // theo `profile.role`.
      home: const SessionGate(),
    );
  }
}
