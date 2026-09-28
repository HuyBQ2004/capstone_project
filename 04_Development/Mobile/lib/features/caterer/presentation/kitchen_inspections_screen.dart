import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../data/kitchen_inspection_models.dart';
import '../data/kitchen_inspection_repository.dart';

/// UC-014/BR-221 — Danh sách kiểm tra vệ sinh bếp do Y tế chấm, Caterers xem
/// và xác nhận kết quả. Đối chiếu
/// `04_Development/Frontend/src/app/(app)/service-caterers/kitchen-inspections/page.tsx`.
class KitchenInspectionsScreen extends StatefulWidget {
  const KitchenInspectionsScreen({super.key});

  @override
  State<KitchenInspectionsScreen> createState() => _KitchenInspectionsScreenState();
}

class _KitchenInspectionsScreenState extends State<KitchenInspectionsScreen> {
  late Future<List<KitchenInspection>> _future;

  @override
  void initState() {
    super.initState();
    _future = KitchenInspectionRepository.instance.fetchHistory();
  }

  Future<void> _reload() async {
    final next = KitchenInspectionRepository.instance.fetchHistory();
    setState(() {
      _future = next;
    });
    await next;
  }

  Color _statusColor(KitchenInspection k) {
    if (k.confirmStatus == 'CONFIRMED') return AppTheme.successEmerald;
    if (k.confirmStatus == 'AWAITING_CATERERS_CONFIRM') return AppTheme.warningAmber;
    return Colors.white38;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Kiểm tra vệ sinh bếp')),
      body: SafeArea(
        child: FutureBuilder<List<KitchenInspection>>(
          future: _future,
          builder: (context, snapshot) {
            if (snapshot.connectionState != ConnectionState.done) {
              return const Center(child: CircularProgressIndicator());
            }
            if (snapshot.hasError) {
              final message =
                  snapshot.error is ApiException ? (snapshot.error as ApiException).message : 'Không tải được dữ liệu.';
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
                    Center(child: Text('Chưa có phiên kiểm tra bếp nào.', style: TextStyle(color: Colors.white54))),
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
                  return Card(
                    margin: const EdgeInsets.only(bottom: 10),
                    child: ListTile(
                      leading: CircleAvatar(
                        backgroundColor: _statusColor(k).withValues(alpha: 0.2),
                        child: Text('${k.score ?? 0}%', style: const TextStyle(fontSize: 11)),
                      ),
                      title: Text(k.id, style: const TextStyle(fontWeight: FontWeight.w600)),
                      subtitle: Text(
                        '${k.date ?? '—'} · ${k.inspector ?? '—'} · ${kitchenConfirmLabel(k)}',
                        style: const TextStyle(fontSize: 12),
                      ),
                      trailing: const Icon(Icons.chevron_right),
                      onTap: () async {
                        await Navigator.of(context)
                            .push(MaterialPageRoute(builder: (_) => KitchenInspectionDetailScreen(code: k.id)));
                        _reload();
                      },
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

class KitchenInspectionDetailScreen extends StatefulWidget {
  final String code;
  const KitchenInspectionDetailScreen({super.key, required this.code});

  @override
  State<KitchenInspectionDetailScreen> createState() => _KitchenInspectionDetailScreenState();
}

class _KitchenInspectionDetailScreenState extends State<KitchenInspectionDetailScreen> {
  late Future<KitchenInspection> _future;
  bool _confirming = false;

  @override
  void initState() {
    super.initState();
    _future = KitchenInspectionRepository.instance.fetchDetail(widget.code);
  }

  Future<void> _confirm() async {
    setState(() => _confirming = true);
    try {
      final updated = await KitchenInspectionRepository.instance.confirm(widget.code);
      if (!mounted) return;
      setState(() {
        _future = Future.value(updated);
      });
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Đã xác nhận kết quả kiểm tra.'), backgroundColor: AppTheme.successEmerald),
      );
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message), backgroundColor: AppTheme.dangerRose));
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Không xác nhận được kết quả kiểm tra.'), backgroundColor: AppTheme.dangerRose),
        );
      }
    } finally {
      if (mounted) setState(() => _confirming = false);
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
            final awaitingConfirm = k.confirmStatus == 'AWAITING_CATERERS_CONFIRM';
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
                        if (k.confirmedBy != null) ...[
                          const SizedBox(height: 4),
                          Text('Đã xác nhận bởi: ${k.confirmedBy} · ${k.confirmedAt ?? ''}',
                              style: const TextStyle(color: AppTheme.successEmerald, fontSize: 12)),
                        ],
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
                if ((k.recordContent ?? '').trim().isNotEmpty) ...[
                  const SizedBox(height: 16),
                  const Text('Biên bản sự việc', style: TextStyle(fontWeight: FontWeight.bold)),
                  const SizedBox(height: 8),
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(14),
                      child: Text(k.recordContent!, style: const TextStyle(fontSize: 13)),
                    ),
                  ),
                ],
                const SizedBox(height: 20),
                if (awaitingConfirm)
                  SizedBox(
                    width: double.infinity,
                    child: ElevatedButton(
                      onPressed: _confirming ? null : _confirm,
                      child: _confirming
                          ? const SizedBox(width: 16, height: 16, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                          : const Text('Xác nhận kết quả kiểm tra'),
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
