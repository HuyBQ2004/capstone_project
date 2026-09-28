import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../data/inspection_models.dart';
import '../data/inspection_repository.dart';

/// Đối chiếu
/// `04_Development/Frontend/src/app/(app)/medical-staff/conduct-inspection/history/page.tsx`.
class InspectionHistoryScreen extends StatefulWidget {
  const InspectionHistoryScreen({super.key});

  @override
  State<InspectionHistoryScreen> createState() => _InspectionHistoryScreenState();
}

class _InspectionHistoryScreenState extends State<InspectionHistoryScreen> {
  late Future<List<KitchenInspection>> _future;

  @override
  void initState() {
    super.initState();
    _future = InspectionRepository.instance.fetchHistory();
  }

  Future<void> _reload() async {
    final next = InspectionRepository.instance.fetchHistory();
    setState(() {
      _future = next;
    });
    await next;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Lịch sử kiểm tra bếp')),
      body: SafeArea(
        child: FutureBuilder<List<KitchenInspection>>(
          future: _future,
          builder: (context, snapshot) {
            if (snapshot.connectionState != ConnectionState.done) {
              return const Center(child: CircularProgressIndicator());
            }
            if (snapshot.hasError) {
              final message = snapshot.error is ApiException
                  ? (snapshot.error as ApiException).message
                  : 'Không tải được dữ liệu. Kiểm tra mạng và thử lại.';
              return Center(
                child: Padding(
                  padding: const EdgeInsets.all(24),
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(message, textAlign: TextAlign.center),
                      const SizedBox(height: 12),
                      ElevatedButton(onPressed: _reload, child: const Text('Thử lại')),
                    ],
                  ),
                ),
              );
            }
            final items = snapshot.data ?? const [];
            if (items.isEmpty) {
              return RefreshIndicator(
                onRefresh: _reload,
                child: ListView(
                  padding: const EdgeInsets.all(24),
                  children: const [
                    SizedBox(height: 80),
                    Center(
                      child: Text('Chưa có phiên kiểm tra nào.', style: TextStyle(color: Colors.white54)),
                    ),
                  ],
                ),
              );
            }
            return RefreshIndicator(
              onRefresh: _reload,
              child: ListView.builder(
                padding: const EdgeInsets.all(16),
                itemCount: items.length,
                itemBuilder: (context, index) {
                  final k = items[index];
                  final ok = (k.score ?? 0) >= 60;
                  return Card(
                    margin: const EdgeInsets.only(bottom: 10),
                    child: ListTile(
                      leading: CircleAvatar(
                        backgroundColor: (ok ? AppTheme.successEmerald : AppTheme.dangerRose).withValues(alpha: 0.2),
                        child: Text('${k.score ?? 0}%', style: const TextStyle(fontSize: 11)),
                      ),
                      title: Text(k.id, style: const TextStyle(fontWeight: FontWeight.w600)),
                      subtitle: Text(
                        '${k.date ?? '—'} · ${k.inspector ?? '—'} · ${kitchenConfirmLabel(k)}',
                        style: const TextStyle(fontSize: 12),
                      ),
                      trailing: const Icon(Icons.chevron_right),
                      onTap: () => Navigator.of(context)
                          .push(MaterialPageRoute(builder: (_) => InspectionDetailScreen(code: k.id))),
                    ),
                  );
                },
              ),
            );
          },
        ),
      ),
    );
  }
}

class InspectionDetailScreen extends StatefulWidget {
  final String code;
  const InspectionDetailScreen({super.key, required this.code});

  @override
  State<InspectionDetailScreen> createState() => _InspectionDetailScreenState();
}

class _InspectionDetailScreenState extends State<InspectionDetailScreen> {
  late Future<KitchenInspection> _future;
  final _recordCtrl = TextEditingController();
  bool _recordInitialized = false;
  bool _savingRecord = false;

  @override
  void initState() {
    super.initState();
    _future = InspectionRepository.instance.fetchDetail(widget.code);
  }

  @override
  void dispose() {
    _recordCtrl.dispose();
    super.dispose();
  }

  Future<void> _saveRecord() async {
    setState(() => _savingRecord = true);
    try {
      final updated = await InspectionRepository.instance.updateRecord(widget.code, _recordCtrl.text.trim());
      if (!mounted) return;
      setState(() {
        _future = Future.value(updated);
      });
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Đã lưu biên bản sự việc.'), backgroundColor: AppTheme.successEmerald),
      );
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message), backgroundColor: AppTheme.dangerRose));
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Không lưu được biên bản.'), backgroundColor: AppTheme.dangerRose),
        );
      }
    } finally {
      if (mounted) setState(() => _savingRecord = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Phiên ${widget.code}')),
      body: SafeArea(
        child: FutureBuilder<KitchenInspection>(
          future: _future,
          builder: (context, snapshot) {
            if (snapshot.connectionState != ConnectionState.done) {
              return const Center(child: CircularProgressIndicator());
            }
            if (snapshot.hasError || !snapshot.hasData) {
              final message = snapshot.error is ApiException
                  ? (snapshot.error as ApiException).message
                  : 'Không tải được chi tiết phiên kiểm tra.';
              return Center(child: Text(message, textAlign: TextAlign.center));
            }
            final k = snapshot.data!;
            final ok = (k.score ?? 0) >= 60;
            if (!_recordInitialized) {
              _recordCtrl.text = k.recordContent ?? '';
              _recordInitialized = true;
            }
            final locked = k.confirmStatus == 'CONFIRMED';
            return ListView(
              padding: const EdgeInsets.all(16),
              children: [
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(14),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(kitchenConfirmLabel(k), style: const TextStyle(fontWeight: FontWeight.w600)),
                        const SizedBox(height: 8),
                        Row(
                          children: [
                            Text(
                              'Điểm ${k.score ?? 0}%',
                              style: TextStyle(
                                fontWeight: FontWeight.bold,
                                fontSize: 18,
                                color: ok ? AppTheme.successEmerald : AppTheme.dangerRose,
                              ),
                            ),
                            const SizedBox(width: 10),
                            Text(k.result ?? '—'),
                          ],
                        ),
                        const SizedBox(height: 4),
                        Text('Người kiểm tra: ${k.inspector ?? '—'} · Ngày: ${k.date ?? '—'}'),
                        const SizedBox(height: 4),
                        Text(
                          'Khám sức khỏe NV bếp: ${k.staffHealthValid == true ? 'Hợp lệ' : 'Không hợp lệ'}',
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 16),
                Text('Hạng mục đã đánh giá (${k.itemResults.length})',
                    style: const TextStyle(fontWeight: FontWeight.bold)),
                const SizedBox(height: 8),
                for (final r in k.itemResults)
                  Card(
                    margin: const EdgeInsets.only(bottom: 6),
                    child: ListTile(
                      dense: true,
                      title: Text(r.item, style: const TextStyle(fontSize: 13)),
                      trailing: Text(
                        r.passed ? 'Đạt' : 'Không đạt',
                        style: TextStyle(
                          color: r.passed ? AppTheme.successEmerald : AppTheme.dangerRose,
                          fontWeight: FontWeight.w600,
                          fontSize: 12,
                        ),
                      ),
                    ),
                  ),
                if (k.itemResults.isEmpty)
                  const Text('Phiên cũ — không có snapshot đầy đủ.', style: TextStyle(color: Colors.white54)),
                const SizedBox(height: 16),
                Text('Biên bản sự việc', style: const TextStyle(fontWeight: FontWeight.bold)),
                const SizedBox(height: 4),
                Text(
                  locked
                      ? 'Bếp đã xác nhận — biên bản đã khoá, không sửa được nữa.'
                      : 'Tự sinh từ hạng mục Không đạt lúc nộp checklist — có thể chỉnh sửa cho tới khi bếp xác nhận (BR-221).',
                  style: const TextStyle(fontSize: 12, color: Colors.white54),
                ),
                const SizedBox(height: 8),
                TextField(
                  controller: _recordCtrl,
                  enabled: !locked,
                  maxLines: 6,
                  decoration: const InputDecoration(border: OutlineInputBorder(), hintText: 'Nội dung biên bản…'),
                ),
                const SizedBox(height: 8),
                if (!locked)
                  SizedBox(
                    width: double.infinity,
                    child: ElevatedButton(
                      onPressed: _savingRecord ? null : _saveRecord,
                      child: _savingRecord
                          ? const SizedBox(width: 16, height: 16, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                          : const Text('Lưu biên bản'),
                    ),
                  ),
              ],
            );
          },
        ),
      ),
    );
  }
}
