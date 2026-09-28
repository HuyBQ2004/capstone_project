import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../data/sample_models.dart';
import '../data/sample_repository.dart';
import 'sample_handover_screen.dart';

/// 13_FoodSampleRecords.md (UC-118) + 14_RetrieveFoodSampleRecordsDialog.md
/// (UC-076). Đối chiếu
/// `04_Development/Frontend/src/app/(app)/medical-staff/samples/page.tsx`.
/// BR-111: hạn lưu tối thiểu 24 giờ · BR-110: tủ 2–8°C.
class SamplesScreen extends StatefulWidget {
  const SamplesScreen({super.key});

  @override
  State<SamplesScreen> createState() => _SamplesScreenState();
}

class _SamplesScreenState extends State<SamplesScreen> {
  String? _status;
  late Future<List<FoodSample>> _future;

  @override
  void initState() {
    super.initState();
    _future = SampleRepository.instance.fetchSamples();
  }

  Future<void> _reload() async {
    final next = SampleRepository.instance.fetchSamples(status: _status);
    setState(() {
      _future = next;
    });
    await next;
  }

  Future<void> _linkDisposed(FoodSample sample) async {
    final incidents = await SampleRepository.instance.fetchIncidents();
    if (!mounted) return;
    final open = incidents.where((i) => i.status != 'CLOSED').toList();
    String? chosen;
    await showDialog<void>(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setDialogState) => AlertDialog(
          title: Text('Liên kết mẫu ${sample.id} đã huỷ với sự cố'),
          content: DropdownButtonFormField<String>(
            initialValue: chosen,
            decoration: const InputDecoration(labelText: 'Sự cố đang mở (BR-114)'),
            items: [
              for (final i in open) DropdownMenuItem(value: i.id, child: Text('${i.id} — ${i.title ?? ''}')),
            ],
            onChanged: (v) => setDialogState(() => chosen = v),
          ),
          actions: [
            TextButton(onPressed: () => Navigator.pop(context), child: const Text('Hủy')),
            ElevatedButton(
              onPressed: chosen == null
                  ? null
                  : () async {
                      Navigator.pop(context);
                      try {
                        await SampleRepository.instance.linkDisposedToIncident(sample.id, chosen!);
                        if (mounted) {
                          ScaffoldMessenger.of(context).showSnackBar(
                            SnackBar(content: Text('Đã liên kết mẫu ${sample.id} với $chosen.')),
                          );
                          await _reload();
                        }
                      } on ApiException catch (e) {
                        if (mounted) {
                          ScaffoldMessenger.of(context)
                              .showSnackBar(SnackBar(content: Text(e.message)));
                        }
                      }
                    },
              child: const Text('Liên kết'),
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Sổ lưu mẫu thực phẩm'),
        actions: [
          IconButton(
            icon: const Icon(Icons.science_outlined),
            tooltip: 'Thu hồi & gửi xét nghiệm',
            onPressed: () async {
              await Navigator.of(context).push(MaterialPageRoute(builder: (_) => const SampleHandoverScreen()));
              _reload();
            },
          ),
        ],
      ),
      body: SafeArea(
        child: Column(
          children: [
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              child: SizedBox(
                height: 36,
                child: ListView(
                  scrollDirection: Axis.horizontal,
                  children: [
                    for (final entry in const {
                      null: 'Tất cả',
                      'RETAINED': 'Đang lưu',
                      'SENT_TO_LAB': 'Đã gửi',
                      'DISPOSED': 'Đã hủy',
                    }.entries)
                      Padding(
                        padding: const EdgeInsets.only(right: 8),
                        child: ChoiceChip(
                          label: Text(entry.value),
                          selected: _status == entry.key,
                          onSelected: (_) {
                            setState(() => _status = entry.key);
                            _reload();
                          },
                        ),
                      ),
                  ],
                ),
              ),
            ),
            Expanded(
              child: FutureBuilder<List<FoodSample>>(
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
                          Center(child: Text('Không có mẫu lưu nào.', style: TextStyle(color: Colors.white54))),
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
                        final hours = s.hoursRemaining ?? 0;
                        final tempOk = (s.fridgeTemp ?? 5) >= 2 && (s.fridgeTemp ?? 5) <= 8;
                        return Card(
                          margin: const EdgeInsets.only(bottom: 10),
                          child: Padding(
                            padding: const EdgeInsets.all(14),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Row(
                                  children: [
                                    Expanded(
                                      child: Text('${s.id} · ${s.dish ?? '—'}',
                                          style: const TextStyle(fontWeight: FontWeight.w600)),
                                    ),
                                    Container(
                                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                                      decoration: BoxDecoration(
                                        color: AppTheme.primarySky.withValues(alpha: 0.15),
                                        borderRadius: BorderRadius.circular(999),
                                      ),
                                      child: Text(sampleStatusLabel(s.status),
                                          style: const TextStyle(color: AppTheme.primarySky, fontSize: 11)),
                                    ),
                                  ],
                                ),
                                const SizedBox(height: 6),
                                Text(
                                  '${s.mealShift ?? mealTypeLabel[s.mealType] ?? s.meal ?? '—'} · ${s.weight ?? '—'}',
                                  style: const TextStyle(fontSize: 12, color: Colors.white70),
                                ),
                                const SizedBox(height: 4),
                                Row(
                                  children: [
                                    Text(
                                      hours > 0 ? 'Còn $hours h trong hạn lưu 24h' : 'Hết hạn lưu tối thiểu',
                                      style: TextStyle(
                                        fontSize: 11,
                                        color: hours <= 8 && hours > 0 ? AppTheme.warningAmber : Colors.white54,
                                      ),
                                    ),
                                    const SizedBox(width: 10),
                                    Text(
                                      'Tủ lưu ${s.fridgeTemp ?? '—'}°C',
                                      style: TextStyle(
                                        fontSize: 11,
                                        color: tempOk ? Colors.white54 : AppTheme.dangerRose,
                                      ),
                                    ),
                                  ],
                                ),
                                Text('Người lấy mẫu: ${s.collectedBy ?? '—'}',
                                    style: const TextStyle(fontSize: 11, color: Colors.white38)),
                                if (s.status == 'DISPOSED')
                                  Align(
                                    alignment: Alignment.centerRight,
                                    child: TextButton(
                                      onPressed: () => _linkDisposed(s),
                                      child: const Text('Liên kết sự cố (AF1)'),
                                    ),
                                  ),
                              ],
                            ),
                          ),
                        );
                      },
                    ),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}
