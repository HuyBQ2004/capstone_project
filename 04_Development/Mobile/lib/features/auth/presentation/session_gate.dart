import 'package:flutter/material.dart';

import '../../../core/models/user_model.dart';
import '../../../core/theme/app_theme.dart';
import '../../caterer/presentation/caterer_dashboard.dart';
import '../../medical_staff/presentation/medical_staff_dashboard.dart';
import '../../parent/presentation/parent_dashboard.dart';
import '../../teacher/presentation/teacher_dashboard.dart';
import '../data/auth_models.dart';
import '../data/auth_repository.dart';
import 'login_screen.dart';

/// Màn khởi động: thử khôi phục phiên từ refresh token đã lưu (Keystore/Keychain)
/// trước khi quyết định vào thẳng dashboard hay bắt đăng nhập lại — tương đương
/// `Providers` gọi `restoreSession()` lúc mount bên bản web.
class SessionGate extends StatefulWidget {
  const SessionGate({super.key});

  @override
  State<SessionGate> createState() => _SessionGateState();
}

class _SessionGateState extends State<SessionGate> {
  late final Future<Profile?> _future;

  @override
  void initState() {
    super.initState();
    _future = AuthRepository.instance.restoreSession();
  }

  Widget? _dashboardFor(UserRole role) {
    switch (role) {
      case UserRole.teacher:
        return const TeacherDashboard();
      case UserRole.parent:
        return const ParentDashboard();
      case UserRole.medicalStaff:
        return const MedicalStaffDashboard();
      case UserRole.serviceCaterers:
        return const CatererDashboard();
      case UserRole.rawFoodSupplier:
      case UserRole.schoolAdmin:
      case UserRole.systemAdmin:
        return null;
    }
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<Profile?>(
      future: _future,
      builder: (context, snapshot) {
        if (snapshot.connectionState != ConnectionState.done) {
          return const Scaffold(
            backgroundColor: AppTheme.bgDark,
            body: Center(child: CircularProgressIndicator()),
          );
        }

        final profile = snapshot.data;
        if (profile == null) {
          return const LoginScreen();
        }
        final dashboard = _dashboardFor(profile.role);
        if (dashboard != null) {
          return dashboard;
        }
        /*
         * 13/08/2026 — trước đây rơi thẳng về `LoginScreen()` trống, không giải
         * thích gì: phiên khôi phục thành công (đăng nhập đúng) nhưng vai trò
         * ngoài phạm vi mobile (school-admin/system-admin/supplier) làm người
         * dùng tưởng mình bị đăng xuất/đăng nhập sai. Cùng thông báo với
         * `login_screen.dart._goToDashboard()` khi đăng nhập lần đầu.
         */
        return LoginScreen(
          initialError:
              'Vai trò "${profile.role.displayName}" chưa hỗ trợ trên app mobile — dùng bản web.',
        );
      },
    );
  }
}
