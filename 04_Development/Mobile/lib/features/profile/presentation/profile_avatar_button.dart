import 'package:flutter/material.dart';

import 'profile_screen.dart';

/// Lối vào "Hồ sơ cá nhân" — đặt ở `AppBar.actions` của từng dashboard theo
/// role, thay vai trò dropdown Topbar bên bản web (`user-menu.tsx`).
class ProfileAvatarButton extends StatelessWidget {
  const ProfileAvatarButton({super.key});

  @override
  Widget build(BuildContext context) {
    return IconButton(
      icon: const Icon(Icons.account_circle_outlined),
      tooltip: 'Hồ sơ cá nhân',
      onPressed: () => Navigator.of(context).push(
        MaterialPageRoute(builder: (_) => const ProfileScreen()),
      ),
    );
  }
}
