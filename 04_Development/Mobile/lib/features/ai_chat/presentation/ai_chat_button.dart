import 'package:flutter/material.dart';

import 'ai_chat_screen.dart';

/// Lối vào "Trợ lý AI" — đặt cạnh [NotificationBellButton]/[ProfileAvatarButton]
/// ở AppBar mỗi dashboard, cùng khuôn. Dùng CHUNG cho mọi vai trò (không có bản
/// riêng theo role — `AiController.chat()` cho phép cả 7 role).
class AiChatButton extends StatelessWidget {
  const AiChatButton({super.key});

  @override
  Widget build(BuildContext context) {
    return IconButton(
      icon: const Icon(Icons.smart_toy_outlined),
      tooltip: 'Trợ lý AI',
      onPressed: () => Navigator.of(context).push(
        MaterialPageRoute(builder: (_) => const AiChatScreen()),
      ),
    );
  }
}
