import 'package:flutter/material.dart';

import 'notification_list_screen.dart';

/// Lối vào "Danh sách thông báo" — đặt cạnh [ProfileAvatarButton] ở AppBar mỗi
/// dashboard. Chưa hiện badge số chưa đọc (backend không có endpoint đếm
/// riêng, `GET /notifications` phải tải cả trang mới biết — để dành cho lúc
/// làm push notification tap-to-open, lúc đó tiện thể cộng dồn số đếm cục bộ).
class NotificationBellButton extends StatelessWidget {
  const NotificationBellButton({super.key});

  @override
  Widget build(BuildContext context) {
    return IconButton(
      icon: const Icon(Icons.notifications_none),
      tooltip: 'Thông báo',
      onPressed: () => Navigator.of(context).push(
        MaterialPageRoute(builder: (_) => const NotificationListScreen()),
      ),
    );
  }
}
