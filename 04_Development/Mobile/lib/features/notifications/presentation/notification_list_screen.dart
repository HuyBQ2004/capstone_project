import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../data/notification_models.dart';
import '../data/notification_repository.dart';
import 'notification_detail_screen.dart';

const _pageSize = 20;

/// UC-058/UC-136 — danh sách thông báo, phân trang kiểu "Tải thêm" (đủ dùng cho
/// khối lượng của một trường; bản web hiện chưa có UI nhóm này để đối chiếu).
class NotificationListScreen extends StatefulWidget {
  const NotificationListScreen({super.key});

  @override
  State<NotificationListScreen> createState() => _NotificationListScreenState();
}

class _NotificationListScreenState extends State<NotificationListScreen> {
  final List<NotificationItem> _items = [];
  int _page = 1;
  int _pageCount = 1;
  bool _loading = true;
  bool _loadingMore = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final result = await NotificationRepository.instance.fetchPage(page: 1, pageSize: _pageSize);
      setState(() {
        _items
          ..clear()
          ..addAll(result.items);
        _page = result.page;
        _pageCount = result.pageCount;
      });
    } on ApiException catch (e) {
      setState(() => _error = e.message);
    } catch (_) {
      setState(() => _error = 'Không tải được thông báo. Kiểm tra mạng và thử lại.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _loadMore() async {
    if (_loadingMore || _page >= _pageCount) return;
    setState(() => _loadingMore = true);
    try {
      final result = await NotificationRepository.instance.fetchPage(page: _page + 1, pageSize: _pageSize);
      setState(() {
        _items.addAll(result.items);
        _page = result.page;
        _pageCount = result.pageCount;
      });
    } catch (_) {
      // Lỗi tải thêm không đáng làm mất danh sách đã có — im lặng, người dùng bấm lại.
    } finally {
      if (mounted) setState(() => _loadingMore = false);
    }
  }

  Future<void> _openDetail(int index) async {
    final updated = await Navigator.of(context).push<NotificationItem>(
      MaterialPageRoute(builder: (_) => NotificationDetailScreen(notification: _items[index])),
    );
    if (updated != null && mounted) {
      setState(() => _items[index] = updated);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Thông báo')),
      body: SafeArea(
        child: _loading
            ? const Center(child: CircularProgressIndicator())
            : _error != null && _items.isEmpty
                ? Center(
                    child: Padding(
                      padding: const EdgeInsets.all(24),
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Text(_error!, textAlign: TextAlign.center),
                          const SizedBox(height: 12),
                          ElevatedButton(onPressed: _load, child: const Text('Thử lại')),
                        ],
                      ),
                    ),
                  )
                : _items.isEmpty
                    ? Center(
                        child: Text(
                          'Chưa có thông báo nào.',
                          style: Theme.of(context).textTheme.bodyMedium?.copyWith(color: Colors.white54),
                        ),
                      )
                    : RefreshIndicator(
                        onRefresh: _load,
                        child: ListView.separated(
                          padding: const EdgeInsets.all(12),
                          itemCount: _items.length + 1,
                          separatorBuilder: (context, index) => const SizedBox(height: 8),
                          itemBuilder: (context, index) {
                            if (index == _items.length) {
                              return _buildFooter();
                            }
                            return _NotificationTile(
                              item: _items[index],
                              onTap: () => _openDetail(index),
                            );
                          },
                        ),
                      ),
      ),
    );
  }

  Widget _buildFooter() {
    if (_page >= _pageCount) return const SizedBox(height: 12);
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 16),
      child: Center(
        child: _loadingMore
            ? const CircularProgressIndicator()
            : OutlinedButton(onPressed: _loadMore, child: const Text('Tải thêm')),
      ),
    );
  }
}

class _NotificationTile extends StatelessWidget {
  final NotificationItem item;
  final VoidCallback onTap;
  const _NotificationTile({required this.item, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: EdgeInsets.zero,
      color: item.read ? AppTheme.cardDark : AppTheme.primarySky.withValues(alpha: 0.08),
      child: InkWell(
        borderRadius: BorderRadius.circular(16),
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.all(14),
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Icon(
                item.critical ? Icons.warning_amber_rounded : Icons.notifications_none,
                color: item.critical ? AppTheme.dangerRose : AppTheme.primarySky,
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: Text(
                            item.title,
                            style: TextStyle(
                              fontWeight: item.read ? FontWeight.normal : FontWeight.bold,
                            ),
                          ),
                        ),
                        if (!item.read)
                          Container(
                            width: 8,
                            height: 8,
                            margin: const EdgeInsets.only(left: 8, top: 4),
                            decoration: const BoxDecoration(color: AppTheme.primarySky, shape: BoxShape.circle),
                          ),
                      ],
                    ),
                    const SizedBox(height: 4),
                    Text(
                      item.content,
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(color: Colors.white70, fontSize: 13),
                    ),
                    const SizedBox(height: 6),
                    Text(
                      _relativeTime(item.sentAt),
                      style: const TextStyle(color: Colors.white38, fontSize: 11),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

String _relativeTime(DateTime dt) {
  final diff = DateTime.now().difference(dt.toLocal());
  if (diff.inMinutes < 1) return 'Vừa xong';
  if (diff.inMinutes < 60) return '${diff.inMinutes} phút trước';
  if (diff.inHours < 24) return '${diff.inHours} giờ trước';
  if (diff.inDays < 7) return '${diff.inDays} ngày trước';
  final local = dt.toLocal();
  return '${local.day}/${local.month}/${local.year}';
}
