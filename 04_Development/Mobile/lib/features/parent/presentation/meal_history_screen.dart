import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../data/child_models.dart';
import '../data/child_repository.dart';
import '../data/meal_history_models.dart';
import '../data/meal_history_repository.dart';
import '../data/menu_models.dart' show mealTypeLabel;
import 'widgets/child_selector.dart';

/// 12_MealHistory.md — UC-133. BR-121: lịch sử tiêu thụ. Đối chiếu
/// `04_Development/Frontend/src/app/(app)/parent/meal-history/page.tsx`. Bỏ
/// "Phản hồi" (nộp trong 24h, BR-139) — chưa nối MF10 `feedbacks` trên mobile;
/// bỏ cột "Nhà cung cấp suất ăn" khỏi card để gọn, vẫn hiện trong chi tiết món.
class MealHistoryScreen extends StatefulWidget {
  const MealHistoryScreen({super.key});

  @override
  State<MealHistoryScreen> createState() => _MealHistoryScreenState();
}

class _MealHistoryScreenState extends State<MealHistoryScreen> {
  List<ParentChild> _children = [];
  ParentChild? _active;
  List<MealHistoryEntry> _entries = [];
  String _consumptionFilter = 'ALL';
  bool _loading = true;
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
      final children = await ParentChildRepository.instance.fetchChildren();
      final active = ParentChildRepository.instance.activeChildOf(children);
      final entries =
          active == null ? <MealHistoryEntry>[] : await MealHistoryRepository.instance.fetchForStudent(active.id);
      setState(() {
        _children = children;
        _active = active;
        _entries = entries;
      });
    } on ApiException catch (e) {
      setState(() => _error = e.message);
    } catch (_) {
      setState(() => _error = 'Không tải được dữ liệu. Kiểm tra mạng và thử lại.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _selectChild(String studentId) async {
    ParentChildRepository.instance.setActiveChild(studentId);
    await _load();
  }

  List<MealHistoryEntry> get _filtered => _consumptionFilter == 'ALL'
      ? _entries
      : _entries.where((e) => e.consumption == _consumptionFilter).toList();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Lịch sử bữa ăn')),
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
                        padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            if (_children.isEmpty)
                              const Text('Tài khoản chưa liên kết học sinh nào.', style: TextStyle(color: Colors.white54))
                            else ...[
                              ChildSelector(children: _children, active: _active, onSelect: _selectChild),
                              const SizedBox(height: 10),
                              Wrap(
                                spacing: 8,
                                runSpacing: 8,
                                children: [
                                  ChoiceChip(
                                    label: const Text('Tất cả'),
                                    selected: _consumptionFilter == 'ALL',
                                    onSelected: (_) => setState(() => _consumptionFilter = 'ALL'),
                                  ),
                                  for (final c in kConsumptionOptions)
                                    ChoiceChip(
                                      label: Text(consumptionLabel(c)),
                                      selected: _consumptionFilter == c,
                                      onSelected: (_) => setState(() => _consumptionFilter = c),
                                    ),
                                ],
                              ),
                            ],
                          ],
                        ),
                      ),
                      Expanded(
                        child: _filtered.isEmpty
                            ? const Center(
                                child: Text('Không có bữa ăn nào khớp bộ lọc.', style: TextStyle(color: Colors.white54)),
                              )
                            : RefreshIndicator(
                                onRefresh: _load,
                                child: ListView.builder(
                                  padding: const EdgeInsets.fromLTRB(16, 0, 16, 24),
                                  itemCount: _filtered.length,
                                  itemBuilder: (context, index) => _EntryCard(entry: _filtered[index]),
                                ),
                              ),
                      ),
                    ],
                  ),
      ),
    );
  }
}

class _EntryCard extends StatelessWidget {
  final MealHistoryEntry entry;
  const _EntryCard({required this.entry});

  Color _color() {
    switch (entry.consumption) {
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

  @override
  Widget build(BuildContext context) {
    final dishNames = entry.dishes.map((d) => d.dishName ?? '—').join(', ');
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
                  child: Text(
                    entry.date ?? '—',
                    style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
                  ),
                ),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                  decoration: BoxDecoration(
                    color: _color().withValues(alpha: 0.15),
                    borderRadius: BorderRadius.circular(999),
                  ),
                  child: Text(
                    consumptionLabel(entry.consumption),
                    style: TextStyle(color: _color(), fontSize: 11, fontWeight: FontWeight.w600),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 4),
            Text(
              mealTypeLabel[entry.mealType] ?? entry.mealType ?? '—',
              style: const TextStyle(fontSize: 12, color: Colors.white60),
            ),
            if (dishNames.isNotEmpty) ...[
              const SizedBox(height: 6),
              Text(dishNames, style: const TextStyle(fontSize: 13)),
            ],
            if (entry.catererName != null) ...[
              const SizedBox(height: 6),
              Text('NCC suất ăn: ${entry.catererName}', style: const TextStyle(fontSize: 11, color: Colors.white38)),
            ],
          ],
        ),
      ),
    );
  }
}
