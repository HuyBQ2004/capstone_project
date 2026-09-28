import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../data/meal_record_models.dart';
import '../data/meal_record_repository.dart';

String _todayIso() {
  final d = DateTime.now();
  String p(int n) => n.toString().padLeft(2, '0');
  return '${d.year}-${p(d.month)}-${p(d.day)}';
}

/// Ghi nhận tiêu thụ bữa ăn — Fully/Partially/Not Consumed theo lô (UC-056/
/// UC-080). Đối chiếu
/// `04_Development/Frontend/src/app/(app)/teacher/meal-records/page.tsx`.
/// `PATCH /meal-records/{code}` chỉ nhận `consumption` — `recordedAt` server
/// tự set (BR-121).
class MealRecordsScreen extends StatefulWidget {
  const MealRecordsScreen({super.key});

  @override
  State<MealRecordsScreen> createState() => _MealRecordsScreenState();
}

class _MealRecordsScreenState extends State<MealRecordsScreen> {
  late String _date;
  List<MealRecord> _records = [];
  bool _loading = true;
  String? _error;
  bool _selectMode = false;
  final Map<String, String> _overrides = {};
  bool _saving = false;

  @override
  void initState() {
    super.initState();
    _date = _todayIso();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final records = await MealRecordRepository.instance.fetchForDate(_date);
      setState(() => _records = records);
    } on ApiException catch (e) {
      setState(() => _error = e.message);
    } catch (_) {
      setState(() => _error = 'Không tải được dữ liệu. Kiểm tra mạng và thử lại.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _pickDate() async {
    final picked = await showDatePicker(
      context: context,
      initialDate: DateTime.parse(_date),
      firstDate: DateTime(2024),
      lastDate: DateTime.now().add(const Duration(days: 1)),
    );
    if (picked == null) return;
    String p(int n) => n.toString().padLeft(2, '0');
    setState(() => _date = '${picked.year}-${p(picked.month)}-${p(picked.day)}');
    _load();
  }

  void _exitSelectMode() {
    setState(() {
      _selectMode = false;
      _overrides.clear();
    });
  }

  void _choose(MealRecord r, String value) {
    setState(() {
      if (value == r.consumption) {
        _overrides.remove(r.id);
      } else {
        _overrides[r.id] = value;
      }
    });
  }

  Future<void> _saveAll() async {
    if (_overrides.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Chưa có thay đổi nào để lưu.')));
      return;
    }
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: AppTheme.cardDark,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        title: Text('Lưu ghi nhận tiêu thụ cho ${_overrides.length} học sinh?'),
        content: const Text(
          'Mức tiêu thụ đã đánh dấu sẽ được lưu cùng lúc, đặc biệt lưu ý các em có suất riêng (BR-125).',
          style: TextStyle(color: Colors.white70),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Hủy')),
          ElevatedButton(onPressed: () => Navigator.pop(ctx, true), child: const Text('Lưu ghi nhận')),
        ],
      ),
    );
    if (confirmed != true) return;

    setState(() => _saving = true);
    final changes = Map<String, String>.from(_overrides);
    var failed = 0;
    for (final entry in changes.entries) {
      try {
        await MealRecordRepository.instance.updateConsumption(entry.key, entry.value);
      } catch (_) {
        failed++;
      }
    }
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            failed == 0
                ? 'Đã lưu ghi nhận tiêu thụ cho ${changes.length} học sinh.'
                : 'Đã lưu ${changes.length - failed}/${changes.length} học sinh, $failed lỗi.',
          ),
        ),
      );
    }
    setState(() => _saving = false);
    _exitSelectMode();
    _load();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Ghi nhận bữa ăn'),
        actions: [
          if (_selectMode)
            TextButton(onPressed: _exitSelectMode, child: const Text('Hủy', style: TextStyle(color: Colors.white70)))
          else
            IconButton(
              onPressed: () => setState(() => _selectMode = true),
              icon: const Icon(Icons.checklist),
              tooltip: 'Bắt đầu ghi nhận',
            ),
        ],
      ),
      floatingActionButton: _selectMode && _overrides.isNotEmpty
          ? FloatingActionButton.extended(
              onPressed: _saving ? null : _saveAll,
              icon: _saving
                  ? const SizedBox(width: 16, height: 16, child: CircularProgressIndicator(strokeWidth: 2))
                  : const Icon(Icons.save_outlined),
              label: Text('Lưu ghi nhận (${_overrides.length})'),
            )
          : null,
      body: SafeArea(
        child: _loading
            ? const Center(child: CircularProgressIndicator())
            : _error != null
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
                : Column(
                    children: [
                      Padding(
                        padding: const EdgeInsets.all(16),
                        child: Row(
                          children: [
                            const Icon(Icons.calendar_today, size: 16, color: Colors.white60),
                            const SizedBox(width: 8),
                            Text(_date, style: const TextStyle(fontWeight: FontWeight.w600)),
                            const Spacer(),
                            OutlinedButton(onPressed: _pickDate, child: const Text('Đổi ngày')),
                          ],
                        ),
                      ),
                      Expanded(
                        child: _records.isEmpty
                            ? const Center(
                                child: Text(
                                  'Chưa có dữ liệu bữa ăn trong ngày đã chọn.',
                                  style: TextStyle(color: Colors.white54),
                                ),
                              )
                            : RefreshIndicator(
                                onRefresh: _load,
                                child: ListView.builder(
                                  padding: const EdgeInsets.fromLTRB(16, 0, 16, 96),
                                  itemCount: _records.length,
                                  itemBuilder: (context, index) {
                                    final r = _records[index];
                                    return _MealRecordCard(
                                      record: r,
                                      selectMode: _selectMode,
                                      current: _overrides[r.id] ?? r.consumption,
                                      onChoose: (v) => _choose(r, v),
                                    );
                                  },
                                ),
                              ),
                      ),
                    ],
                  ),
      ),
    );
  }
}

class _MealRecordCard extends StatelessWidget {
  final MealRecord record;
  final bool selectMode;
  final String? current;
  final ValueChanged<String> onChoose;

  const _MealRecordCard({
    required this.record,
    required this.selectMode,
    required this.current,
    required this.onChoose,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(record.student ?? record.studentId ?? '—', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
            const SizedBox(height: 2),
            Text('Mã HS: ${record.studentId ?? '—'}', style: const TextStyle(color: Colors.white60, fontSize: 12)),
            if (record.dishes != null && record.dishes!.isNotEmpty) ...[
              const SizedBox(height: 6),
              Text(record.dishes!, style: const TextStyle(fontSize: 13)),
            ],
            if (record.specialDiet != null && record.specialDiet!.isNotEmpty) ...[
              const SizedBox(height: 6),
              _Badge(label: record.specialDiet!, color: AppTheme.primarySky),
            ],
            const SizedBox(height: 10),
            if (selectMode)
              Wrap(
                spacing: 8,
                runSpacing: 8,
                children: [
                  for (final option in kConsumptionOptions)
                    ChoiceChip(
                      label: Text(consumptionLabel(option)),
                      selected: current == option,
                      onSelected: (_) => onChoose(option),
                    ),
                ],
              )
            else
              Row(
                children: [
                  _Badge(label: consumptionLabel(record.consumption), color: _consumptionColor(record.consumption)),
                  if (record.recordedAt != null) ...[
                    const SizedBox(width: 8),
                    Text(record.recordedAt!, style: const TextStyle(color: Colors.white38, fontSize: 11)),
                  ],
                ],
              ),
          ],
        ),
      ),
    );
  }

  Color _consumptionColor(String? value) {
    switch (value) {
      case 'FULLY':
        return AppTheme.successEmerald;
      case 'PARTIALLY':
        return AppTheme.warningAmber;
      case 'NOT_CONSUMED':
        return AppTheme.dangerRose;
      default:
        return Colors.white38;
    }
  }
}

class _Badge extends StatelessWidget {
  final String label;
  final Color color;
  const _Badge({required this.label, required this.color});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.15),
        borderRadius: BorderRadius.circular(999),
      ),
      child: Text(label, style: TextStyle(color: color, fontSize: 11, fontWeight: FontWeight.w600)),
    );
  }
}
