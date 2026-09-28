import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../data/inspection_models.dart';
import '../data/inspection_repository.dart';
import 'inspection_detail_screen.dart';

String todayIso() {
  final d = DateTime.now();
  String p(int n) => n.toString().padLeft(2, '0');
  return '${d.year}-${p(d.month)}-${p(d.day)}';
}

Color _overallColor(String? overall) {
  switch (overall) {
    case 'PASSED':
      return AppTheme.successEmerald;
    case 'FAILED':
      return AppTheme.dangerRose;
    default:
      return AppTheme.warningAmber;
  }
}

String _overallLabel(String? overall) {
  switch (overall) {
    case 'PASSED':
      return 'Đạt';
    case 'FAILED':
      return 'Không đạt';
    default:
      return 'Đang kiểm';
  }
}

/// UC-052/UC-124 — Kiểm thực ba bước (Mẫu 1/2/3, QĐ 1246/QĐ-BYT). Đối chiếu
/// `04_Development/Frontend/src/app/(app)/service-caterers/inspections/page.tsx`.
/// BR-079: đúng 3 bước tuần tự · BR-090/098: ngưỡng nhiệt · BR-094: chặn phục
/// vụ nếu bước thất bại · BR-099: Caterer chỉ thấy phiên của mình (server tự
/// lọc theo partnerId).
class InspectionsScreen extends StatefulWidget {
  const InspectionsScreen({super.key});

  @override
  State<InspectionsScreen> createState() => _InspectionsScreenState();
}

class _InspectionsScreenState extends State<InspectionsScreen> {
  late Future<List<InspectionSession>> _future;
  bool _opening = false;

  @override
  void initState() {
    super.initState();
    _future = InspectionRepository.instance.fetchList();
  }

  Future<void> _reload() async {
    final next = InspectionRepository.instance.fetchList();
    setState(() {
      _future = next;
    });
    await next;
  }

  Future<void> _openNewSessionDialog() async {
    String mealType = 'LUNCH';
    final sessions = await _future.catchError((_) => <InspectionSession>[]);
    final existing = sessions
        .where((s) => s.date == todayIso() && s.meal == mealType)
        .toList();

    if (!mounted) return;
    await showDialog<void>(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setDialogState) {
          final clash = sessions.where((s) => s.date == todayIso() && s.meal == mealType).toList();
          return AlertDialog(
            title: const Text('Mở phiên kiểm thực mới'),
            content: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('Chỉ mở được cho hôm nay ($todayIsoDisplay) — mỗi bữa đúng 1 phiên (BR-079).',
                    style: const TextStyle(fontSize: 12, color: Colors.white60)),
                const SizedBox(height: 12),
                DropdownButtonFormField<String>(
                  initialValue: mealType,
                  decoration: const InputDecoration(labelText: 'Bữa', border: OutlineInputBorder()),
                  dropdownColor: AppTheme.cardDark,
                  items: mealTypeLabel.entries
                      .map((e) => DropdownMenuItem(value: e.key, child: Text(e.value)))
                      .toList(),
                  onChanged: (v) {
                    if (v != null) setDialogState(() => mealType = v);
                  },
                ),
                if (clash.isNotEmpty) ...[
                  const SizedBox(height: 12),
                  Text(
                    'Bữa này đã có phiên ${clash.first.id} (${_overallLabel(clash.first.overall)}).',
                    style: const TextStyle(color: AppTheme.warningAmber, fontSize: 12),
                  ),
                ],
              ],
            ),
            actions: [
              TextButton(onPressed: () => Navigator.pop(context), child: const Text('Hủy')),
              if (clash.isNotEmpty)
                ElevatedButton(
                  onPressed: () {
                    Navigator.pop(context);
                    Navigator.of(context)
                        .push(MaterialPageRoute(builder: (_) => InspectionDetailScreen(code: clash.first.id)))
                        .then((_) => _reload());
                  },
                  child: Text('Mở phiên ${clash.first.id}'),
                )
              else
                ElevatedButton(
                  onPressed: _opening
                      ? null
                      : () async {
                          setState(() => _opening = true);
                          try {
                            final s = await InspectionRepository.instance.open(date: todayIso(), mealType: mealType);
                            if (mounted) {
                              Navigator.pop(context);
                              await Navigator.of(context)
                                  .push(MaterialPageRoute(builder: (_) => InspectionDetailScreen(code: s.id)));
                              _reload();
                            }
                          } on ApiException catch (e) {
                            if (mounted) {
                              ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message)));
                            }
                          } finally {
                            if (mounted) setState(() => _opening = false);
                          }
                        },
                  child: const Text('Tạo phiên'),
                ),
            ],
          );
        },
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Kiểm thực ba bước'),
        actions: [
          IconButton(
            icon: const Icon(Icons.add_circle_outline),
            tooltip: 'Phiên kiểm thực mới',
            onPressed: _openNewSessionDialog,
          ),
        ],
      ),
      body: SafeArea(
        child: FutureBuilder<List<InspectionSession>>(
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
                    Center(child: Text('Chưa có phiên kiểm thực nào.', style: TextStyle(color: Colors.white54))),
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
                  final s = items[index];
                  return Card(
                    margin: const EdgeInsets.only(bottom: 10),
                    child: ListTile(
                      onTap: () async {
                        await Navigator.of(context)
                            .push(MaterialPageRoute(builder: (_) => InspectionDetailScreen(code: s.id)));
                        _reload();
                      },
                      title: Text('${s.id} · ${mealTypeLabel[s.meal] ?? s.meal ?? ''}',
                          style: const TextStyle(fontWeight: FontWeight.w600)),
                      subtitle: Padding(
                        padding: const EdgeInsets.only(top: 6),
                        child: Wrap(
                          spacing: 6,
                          runSpacing: 4,
                          children: [
                            Text(s.date ?? '', style: const TextStyle(fontSize: 12, color: Colors.white60)),
                            _StepChip(label: 'B1', status: s.step1),
                            _StepChip(label: 'B2', status: s.step2),
                            _StepChip(label: 'B3', status: s.step3),
                          ],
                        ),
                      ),
                      trailing: Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                        decoration: BoxDecoration(
                          color: _overallColor(s.overall).withValues(alpha: 0.15),
                          borderRadius: BorderRadius.circular(999),
                        ),
                        child: Text(_overallLabel(s.overall),
                            style: TextStyle(color: _overallColor(s.overall), fontSize: 11, fontWeight: FontWeight.w600)),
                      ),
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

String get todayIsoDisplay {
  final parts = todayIso().split('-');
  return '${parts[2]}/${parts[1]}/${parts[0]}';
}

class _StepChip extends StatelessWidget {
  final String label;
  final String? status;
  const _StepChip({required this.label, required this.status});

  @override
  Widget build(BuildContext context) {
    final color = _overallColor(status);
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
      decoration: BoxDecoration(
        border: Border.all(color: color.withValues(alpha: 0.6)),
        borderRadius: BorderRadius.circular(6),
      ),
      child: Text('$label: ${stepStatusLabel(status)}', style: TextStyle(color: color, fontSize: 10)),
    );
  }
}
