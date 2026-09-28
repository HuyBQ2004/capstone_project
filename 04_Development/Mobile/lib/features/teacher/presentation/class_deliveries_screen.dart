import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../data/class_delivery_models.dart';
import '../data/class_delivery_repository.dart';
import '../data/classroom_models.dart';
import '../data/classroom_repository.dart';

String _todayIso() {
  final d = DateTime.now();
  String p(int n) => n.toString().padLeft(2, '0');
  return '${d.year}-${p(d.month)}-${p(d.day)}';
}

const _mealTypeLabels = {'BREAKFAST': 'Bữa sáng', 'LUNCH': 'Bữa trưa', 'SNACK': 'Bữa phụ'};

/// Đối chiếu
/// `04_Development/Frontend/src/app/(app)/teacher/class-deliveries/page.tsx`
/// — giáo viên xác nhận ĐÃ NHẬN đủ suất bếp giao cho lớp mình TRƯỚC khi sang
/// màn Phân phát bữa ăn chia từng món cho từng học sinh.
class ClassDeliveriesScreen extends StatefulWidget {
  const ClassDeliveriesScreen({super.key});

  @override
  State<ClassDeliveriesScreen> createState() => _ClassDeliveriesScreenState();
}

class _ClassDeliveriesScreenState extends State<ClassDeliveriesScreen> {
  String _mealType = 'LUNCH';
  late String _date;
  Classroom? _classroom;
  ClassMealDelivery? _delivery;
  PlannedDishes? _planned;
  bool _loading = true;
  bool _confirming = false;
  String? _error;

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
      final classroom = await ClassroomRepository.instance.resolveMyClassroom();
      _classroom = classroom;
      if (classroom == null) {
        setState(() {
          _error = 'Không tìm thấy lớp chủ nhiệm của tài khoản này.';
          _loading = false;
        });
        return;
      }
      final delivery = await ClassDeliveryRepository.instance.fetchDelivery(
        date: _date,
        mealType: _mealType,
        classroomId: classroom.id,
      );
      final planned = await ClassDeliveryRepository.instance.fetchPlannedDishes(
        date: _date,
        mealType: _mealType,
        classroomId: classroom.id,
      );
      setState(() {
        _delivery = delivery;
        _planned = planned;
      });
    } on ApiException catch (e) {
      setState(() => _error = e.message);
    } catch (_) {
      setState(() => _error = 'Không tải được dữ liệu. Kiểm tra mạng và thử lại.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _confirmReceive() async {
    final delivery = _delivery;
    if (delivery == null) return;
    setState(() => _confirming = true);
    try {
      final updated = await ClassDeliveryRepository.instance.confirmReceive(delivery.id);
      setState(() => _delivery = updated);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Đã xác nhận nhận đủ suất từ bếp.')),
        );
      }
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message)));
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Không thể xác nhận nhận suất.')),
        );
      }
    } finally {
      if (mounted) setState(() => _confirming = false);
    }
  }

  Map<String, ({int count, int substitute, int blocked})> _groupByDish() {
    final map = <String, ({int count, int substitute, int blocked})>{};
    for (final row in _planned?.perStudent ?? const <PlannedDishRow>[]) {
      final name = row.dishName ?? '—';
      final cur = map[name] ?? (count: 0, substitute: 0, blocked: 0);
      map[name] = (
        count: cur.count + 1,
        substitute: cur.substitute + (row.isSubstitute ? 1 : 0),
        blocked: cur.blocked + (row.blocked ? 1 : 0),
      );
    }
    return map;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Nhận suất ăn từ bếp')),
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
                : RefreshIndicator(
                    onRefresh: _load,
                    child: ListView(
                      padding: const EdgeInsets.all(16),
                      children: [
                        Text(
                          'Lớp ${_classroom?.id ?? '—'} — $_date',
                          style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                        ),
                        const SizedBox(height: 12),
                        _MealTypeSelector(
                          value: _mealType,
                          onChanged: (v) {
                            setState(() => _mealType = v);
                            _load();
                          },
                        ),
                        const SizedBox(height: 16),
                        if (_delivery == null)
                          const _WarningBanner(
                            title: 'Bếp chưa giao suất cho lớp/bữa này',
                            detail: 'Chưa có bản ghi giao suất — chờ nhà bếp xác nhận giao trước khi nhận.',
                          )
                        else
                          _DeliveryCard(
                            delivery: _delivery!,
                            byDish: _groupByDish(),
                            confirming: _confirming,
                            onConfirm: _confirmReceive,
                          ),
                        const SizedBox(height: 12),
                        const Text(
                          'Món "thay thế" là suất đã đổi do dị ứng (BR-065); suất "chặn" cần Y tế/nhà bếp xử lý '
                          'tay trước khi phát.',
                          style: TextStyle(fontSize: 12, color: Colors.white54),
                        ),
                      ],
                    ),
                  ),
      ),
    );
  }
}

class _MealTypeSelector extends StatelessWidget {
  final String value;
  final ValueChanged<String> onChanged;
  const _MealTypeSelector({required this.value, required this.onChanged});

  @override
  Widget build(BuildContext context) {
    return Wrap(
      spacing: 8,
      children: [
        for (final entry in _mealTypeLabels.entries)
          ChoiceChip(
            label: Text(entry.value),
            selected: value == entry.key,
            onSelected: (_) => onChanged(entry.key),
          ),
      ],
    );
  }
}

class _WarningBanner extends StatelessWidget {
  final String title;
  final String detail;
  const _WarningBanner({required this.title, required this.detail});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: AppTheme.warningAmber.withValues(alpha: 0.1),
        border: Border.all(color: AppTheme.warningAmber.withValues(alpha: 0.4)),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Icon(Icons.warning_amber_rounded, color: AppTheme.warningAmber),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title, style: const TextStyle(fontWeight: FontWeight.bold)),
                const SizedBox(height: 4),
                Text(detail, style: const TextStyle(color: Colors.white70, fontSize: 13)),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _DeliveryCard extends StatelessWidget {
  final ClassMealDelivery delivery;
  final Map<String, ({int count, int substitute, int blocked})> byDish;
  final bool confirming;
  final VoidCallback onConfirm;

  const _DeliveryCard({
    required this.delivery,
    required this.byDish,
    required this.confirming,
    required this.onConfirm,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text('Suất bếp đã giao', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
                if (delivery.isReceived)
                  const _Badge(label: 'Đã nhận', color: AppTheme.successEmerald)
                else
                  const _Badge(label: 'Chờ xác nhận nhận', color: AppTheme.primarySky),
              ],
            ),
            const SizedBox(height: 12),
            if (byDish.isEmpty)
              const Text('Chưa có thực đơn bữa này.', style: TextStyle(color: Colors.white54))
            else
              ...byDish.entries.map(
                (e) => Padding(
                  padding: const EdgeInsets.only(bottom: 8),
                  child: Row(
                    children: [
                      Expanded(child: Text(e.key)),
                      Text('${e.value.count} suất', style: const TextStyle(fontWeight: FontWeight.w600)),
                      if (e.value.substitute > 0) ...[
                        const SizedBox(width: 6),
                        _Badge(label: 'thay thế ×${e.value.substitute}', color: AppTheme.successEmerald),
                      ],
                      if (e.value.blocked > 0) ...[
                        const SizedBox(width: 6),
                        _Badge(label: 'chặn ×${e.value.blocked}', color: AppTheme.dangerRose),
                      ],
                    ],
                  ),
                ),
              ),
            const SizedBox(height: 8),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: delivery.isReceived || confirming ? null : onConfirm,
                icon: confirming
                    ? const SizedBox(
                        width: 16,
                        height: 16,
                        child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                      )
                    : Icon(delivery.isReceived ? Icons.check_circle : Icons.check),
                label: Text(delivery.isReceived ? 'Đã nhận đủ suất' : 'Xác nhận đã nhận'),
              ),
            ),
            if (delivery.note != null && delivery.note!.isNotEmpty) ...[
              const SizedBox(height: 8),
              Text('Ghi chú: ${delivery.note}', style: const TextStyle(color: Colors.white60, fontSize: 12)),
            ],
          ],
        ),
      ),
    );
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
