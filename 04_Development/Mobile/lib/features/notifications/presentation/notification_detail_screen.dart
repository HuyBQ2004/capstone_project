import 'package:flutter/material.dart';

import '../../../core/theme/app_theme.dart';
import '../data/notification_models.dart';
import '../data/notification_repository.dart';

/// UC-136 — mở chi tiết tự đánh dấu đã đọc (idempotent phía backend). Trả lại
/// `NotificationItem` đã cập nhật `read: true` cho màn danh sách cập nhật state
/// tại chỗ, không phải gọi lại `fetchPage`.
class NotificationDetailScreen extends StatefulWidget {
  final NotificationItem notification;
  const NotificationDetailScreen({super.key, required this.notification});

  @override
  State<NotificationDetailScreen> createState() => _NotificationDetailScreenState();
}

class _NotificationDetailScreenState extends State<NotificationDetailScreen> {
  late NotificationItem _item;

  @override
  void initState() {
    super.initState();
    _item = widget.notification;
    if (!_item.read) {
      NotificationRepository.instance.markRead(_item.id).then((_) {
        if (mounted) setState(() => _item = _item.copyWith(read: true));
      }).catchError((_) {
        // Không chặn người dùng đọc nội dung chỉ vì đánh dấu đã đọc lỗi mạng.
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return PopScope(
      canPop: false,
      onPopInvokedWithResult: (didPop, result) {
        if (!didPop) Navigator.of(context).pop(_item);
      },
      child: Scaffold(
        appBar: AppBar(
          title: const Text('Thông báo'),
          leading: IconButton(
            icon: const Icon(Icons.arrow_back),
            onPressed: () => Navigator.of(context).pop(_item),
          ),
        ),
        body: SafeArea(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(24),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                if (_item.critical)
                  Container(
                    margin: const EdgeInsets.only(bottom: 12),
                    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
                    decoration: BoxDecoration(
                      color: AppTheme.dangerRose.withValues(alpha: 0.15),
                      borderRadius: BorderRadius.circular(999),
                    ),
                    child: const Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(Icons.warning_amber_rounded, size: 14, color: AppTheme.dangerRose),
                        SizedBox(width: 4),
                        Text('Cảnh báo khẩn cấp', style: TextStyle(color: AppTheme.dangerRose, fontSize: 12, fontWeight: FontWeight.w600)),
                      ],
                    ),
                  ),
                Text(
                  _item.title,
                  style: Theme.of(context).textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 6),
                Text(
                  _formatDateTime(_item.sentAt),
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(color: Colors.white54),
                ),
                const SizedBox(height: 20),
                Text(
                  _item.content,
                  style: Theme.of(context).textTheme.bodyMedium?.copyWith(height: 1.5),
                ),
                if (_item.link != null) ...[
                  const SizedBox(height: 24),
                  Container(
                    padding: const EdgeInsets.all(14),
                    decoration: BoxDecoration(
                      color: AppTheme.cardDark,
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(color: AppTheme.borderDark),
                    ),
                    child: Row(
                      children: [
                        const Icon(Icons.link, size: 18, color: AppTheme.primarySky),
                        const SizedBox(width: 8),
                        Expanded(
                          child: Text(
                            _item.linkLabel ?? _item.link!,
                            style: const TextStyle(color: AppTheme.primarySky),
                          ),
                        ),
                      ],
                    ),
                  ),
                  Padding(
                    padding: const EdgeInsets.only(top: 6),
                    child: Text(
                      'Mở màn liên quan trên app mobile — chưa hỗ trợ cho mọi loại thông báo, dùng bản web nếu cần.',
                      style: Theme.of(context).textTheme.bodySmall?.copyWith(color: Colors.white38),
                    ),
                  ),
                ],
              ],
            ),
          ),
        ),
      ),
    );
  }
}

String _formatDateTime(DateTime dt) {
  final local = dt.toLocal();
  String two(int n) => n.toString().padLeft(2, '0');
  return '${two(local.day)}/${two(local.month)}/${local.year} ${two(local.hour)}:${two(local.minute)}';
}
