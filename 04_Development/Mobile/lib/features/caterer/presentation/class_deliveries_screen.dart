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

/// Giao suất tới lớp — phía BẾP của luồng `class-meal-deliveries` (SENDING
/// end); phía giáo viên (RECEIVING end) đối chiếu
/// `04_Development/Mobile/lib/features/teacher/presentation/class_deliveries_screen.dart`.
/// Đối chiếu web
/// `04_Development/Frontend/src/app/(app)/service-caterers/class-deliveries/page.tsx`.
class ClassDeliveriesScreen extends StatefulWidget {
  const ClassDeliveriesScreen({super.key});

  @override
  State<ClassDeliveriesScreen> createState() => _ClassDeliveriesScreenState();
}

class _ClassDeliveriesScreenState extends State<ClassDeliveriesScreen> {
  String _mealType = 'LUNCH';
  late String _date;
  bool _loading = true;
  String? _error;
  List<Classroom> _classrooms = [];
  Map<String, ClassMealDelivery> _deliveriesByClass = {};
  final Set<String> _delivering = {};

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
      final classrooms = await ClassroomRepository.instance.fetchAll();
      final deliveries = await ClassDeliveryRepository.instance.fetchAll(date: _date, mealType: _mealType);
      setState(() {
        _classrooms = classrooms;
        _deliveriesByClass = {for (final d in deliveries) if (d.classroomId != null) d.classroomId!: d};
      });
    } on ApiException catch (e) {
      setState(() => _error = e.message);
    } catch (_) {
      setState(() => _error = 'Không tải được dữ liệu. Kiểm tra mạng và thử lại.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _showDeliverDialog(Classroom c) async {
    final notesCtrl = TextEditingController();
    final plannedFuture = ClassDeliveryRepository.instance.fetchPlannedDishes(date: _date, mealType: _mealType, classroomId: c.id);
    await showDialog<void>(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Giao suất — Lớp ${c.name ?? c.id}'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('${classDeliveryMealTypeLabel[_mealType] ?? _mealType} · $_date', style: const TextStyle(fontSize: 12, color: Colors.white60)),
            const SizedBox(height: 10),
            FutureBuilder<PlannedDishes>(
              future: plannedFuture,
              builder: (context, snapshot) {
                if (snapshot.connectionState != ConnectionState.done) {
                  return const Padding(padding: EdgeInsets.symmetric(vertical: 8), child: SizedBox(height: 16, width: 16, child: CircularProgressIndicator(strokeWidth: 2)));
                }
                final byDish = snapshot.data?.byDish ?? const [];
                if (byDish.isEmpty) return const SizedBox.shrink();
                return Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text('Thực đơn dự kiến:', style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600)),
                    const SizedBox(height: 6),
                    for (final d in byDish)
                      Padding(
                        padding: const EdgeInsets.only(bottom: 3),
                        child: Text('• ${d.dishName ?? '—'}: ${d.count} suất${d.substituteCount > 0 ? ' (${d.substituteCount} thay thế)' : ''}',
                            style: const TextStyle(fontSize: 12)),
                      ),
                    const SizedBox(height: 10),
                  ],
                );
              },
            ),
            TextField(controller: notesCtrl, decoration: const InputDecoration(labelText: 'Ghi chú (tuỳ chọn)', border: OutlineInputBorder())),
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context), child: const Text('Hủy')),
          ElevatedButton(
            onPressed: () {
              Navigator.pop(context);
              _deliver(c, notesCtrl.text.trim());
            },
            child: const Text('Xác nhận đã giao'),
          ),
        ],
      ),
    );
  }

  Future<void> _deliver(Classroom c, String note) async {
    setState(() => _delivering.add(c.id));
    try {
      final delivery = await ClassDeliveryRepository.instance.deliver(date: _date, mealType: _mealType, classroomId: c.id, note: note.isEmpty ? null : note);
      setState(() => _deliveriesByClass[c.id] = delivery);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Đã giao suất tới lớp ${c.name ?? c.id}.'), backgroundColor: AppTheme.successEmerald),
        );
      }
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message), backgroundColor: AppTheme.dangerRose));
    } catch (_) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Không thể xác nhận giao suất.')));
    } finally {
      if (mounted) setState(() => _delivering.remove(c.id));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Giao suất tới lớp')),
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
                        Text('$_date', style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                        const SizedBox(height: 12),
                        Wrap(
                          spacing: 8,
                          children: [
                            for (final entry in classDeliveryMealTypeLabel.entries)
                              ChoiceChip(
                                label: Text(entry.value),
                                selected: _mealType == entry.key,
                                onSelected: (_) {
                                  setState(() => _mealType = entry.key);
                                  _load();
                                },
                              ),
                          ],
                        ),
                        const SizedBox(height: 16),
                        if (_classrooms.isEmpty)
                          const Padding(padding: EdgeInsets.only(top: 40), child: Center(child: Text('Chưa có lớp nào.', style: TextStyle(color: Colors.white54)))),
                        for (final c in _classrooms)
                          _ClassroomTile(
                            classroom: c,
                            delivery: _deliveriesByClass[c.id],
                            delivering: _delivering.contains(c.id),
                            onDeliver: () => _showDeliverDialog(c),
                          ),
                      ],
                    ),
                  ),
      ),
    );
  }
}

class _ClassroomTile extends StatelessWidget {
  final Classroom classroom;
  final ClassMealDelivery? delivery;
  final bool delivering;
  final VoidCallback onDeliver;
  const _ClassroomTile({required this.classroom, required this.delivery, required this.delivering, required this.onDeliver});

  @override
  Widget build(BuildContext context) {
    final status = delivery == null ? null : (delivery!.isReceived ? 'received' : 'delivered');
    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      child: ListTile(
        title: Text('Lớp ${classroom.name ?? classroom.id}', style: const TextStyle(fontWeight: FontWeight.w600)),
        subtitle: Text(
          status == 'received' ? 'Giáo viên đã nhận' : status == 'delivered' ? 'Đã giao — chờ giáo viên nhận' : 'Chưa giao',
          style: TextStyle(
            fontSize: 12,
            color: status == 'received' ? AppTheme.successEmerald : status == 'delivered' ? AppTheme.primarySky : AppTheme.warningAmber,
          ),
        ),
        trailing: status != null
            ? Icon(status == 'received' ? Icons.check_circle : Icons.local_shipping, color: status == 'received' ? AppTheme.successEmerald : AppTheme.primarySky)
            : SizedBox(
                width: 96,
                child: ElevatedButton(
                  onPressed: delivering ? null : onDeliver,
                  child: delivering
                      ? const SizedBox(width: 14, height: 14, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                      : const Text('Giao', style: TextStyle(fontSize: 12)),
                ),
              ),
      ),
    );
  }
}
